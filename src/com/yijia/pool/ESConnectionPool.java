package com.yijia.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.yijia.elasticsearch.bean.ESBean;
import com.yijia.util.Tool;
/**
 * 使用该连接池最终调用销毁方法否则出现线程泄露
 * @author Administrator
 *
 */
public class ESConnectionPool implements ConnectionPool {
	private ESBean esBean;
	volatile private int currentPoolSize = 0;// volatile 每次都去重新读取这个值
	private List<Client> usedPool;// 已使用的连接集合
	private List<ComClient> workablePool;// 可使用的连接集合
	private ExecutorService exc = Executors.newCachedThreadPool();// 获取线程池
	private static ESConnectionPool connectionPool;
	
	
	private ScheduledExecutorService set = Executors.newScheduledThreadPool(1);

	/**
	 * 创建存放可使用的连接集合和已使用的连接集合，初始化连接并放入可使用的连接集合
	 * 
	 * @param esBean
	 */
	private ESConnectionPool(ESBean esBean) {
		this.esBean = esBean;
		workablePool = new ArrayList<ComClient>();
		usedPool = new ArrayList<Client>();
		for (int i = 0; i < esBean.getInitialPoolSize(); i++) {
			Client client = createClient();
			workablePool.add(new ComClient(client, 0));
		}
		checkOut();
	}
	
	/**
	 * 检出不可使用的链接
	 */
	synchronized private void checkOut() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (int i = workablePool.size() - 1; i >= 0; i--) {
					ComClient com = workablePool.get(i);
					if (workablePool.get(i).getCount() > esBean.getMaxIdleTime()
							&& currentPoolSize > esBean.getMinPoolSize()) {
						com.getClient().close();
						workablePool.remove(i);
						com = null;
						currentPoolSize--;
					} else {
						com.setCount(com.getCount() + 1);
					}
				}
			}
		};
		set.scheduleAtFixedRate(r, 1, 10, TimeUnit.MINUTES);// 1分钟检查一次
	}

	synchronized public static ESConnectionPool getConnectionPool(ESBean esBean) {
		if (connectionPool == null) {
			connectionPool = new ESConnectionPool(esBean);
		}
		return connectionPool;
	}

	@SuppressWarnings("resource")
	private Client createClient() {
		Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true)
				.put("client.transport.ping_timeout", "300s").put("client.transport.ping_retries", "1000")
				.put("client.transport.ping_interval", "60s").put("cluster.name", esBean.getClusterName()).build();
		Client client = new TransportClient(settings).addTransportAddress(
				new InetSocketTransportAddress(esBean.getHost(), Tool.sToI(esBean.getPort(), 9300)));
		return client;
	}
	/**
	 * 获取一个可用连接
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	synchronized public Client getConect() throws InterruptedException, ExecutionException {
		Client client;
		if (workablePool.size() == 0) {
			Callable<Client> c = new Callable<Client>() {
				@Override
				public Client call() throws Exception {
					return createClient();
				}
			};
			for (int i = 0; i < 3; i++) {
				Future<Client> future = exc.submit(c);
				client = future.get();
				if (client != null) {
					usedPool.add(client);
					currentPoolSize++;
					return client;
				}
			}
			throw new RuntimeException("尝试三次未能成功获取连接");
		} else {
			client = workablePool.get(workablePool.size() - 1).getClient();
			workablePool.remove(workablePool.size() - 1);
			usedPool.add(client);
			currentPoolSize++;
			return client;
		}
	}

	@Override
	/**
	 * 手动释放连接
	 * 
	 * @param conn
	 */
	synchronized public void realse(Client client) {
		workablePool.add(new ComClient(client, 0));
		usedPool.remove(client);
		notify();
	}

	@Override
	/**
	 * 销毁连接池
	 */
	synchronized public void destory() {
		for (int i = workablePool.size(); i >= 0; i--) {
			workablePool.get(i).getClient().close();
			workablePool.remove(i);
		}
		for (int i = usedPool.size(); i >= 0; i--) {
			usedPool.get(i).close();
			usedPool.remove(i);
		}
		set.shutdown();
	}

	private class ComClient implements Comparable<ComClient> {
		Client client;
		int count;

		public ComClient(Client client, int count) {
			this.client = client;
			this.count = count;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public Client getClient() {
			return client;
		}

		// 从大到小排列，排序的目的是为了能够在释放长时间空闲连接时先释放？？
		@Override
		public int compareTo(ComClient o) {
			if (o.getCount() > count)
				return -1;
			else
				return 1;
		}

	}

	/**
	 * 如果当前连接数小于设定的最大连接数唤醒等待获取连接的线程
	 */
	synchronized private void checkEqual() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while (currentPoolSize >= esBean.getMaxPoolSize()) {
				}
				notify();
			}
		};
		exc.execute(r);
	}

	@Override
	/**
	 * 如果当前连接数大于等于连接池最大连接数并且可用连接集合大小为0，开启一个线程去检查是否当前连接数小于连接池最大连接数，
	 * 如果小于了就通知等待获取连接的线程
	 */
	synchronized public Client getClient() throws InterruptedException, ExecutionException {
		Client client = null;
		if (currentPoolSize >= esBean.getMaxPoolSize() && workablePool.size() == 0) {
			checkEqual();
			wait();
			client = getConect();
		} else {
			client = getConect();
		}
		return client;
	}
}
