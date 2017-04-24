package com.yijia.jdbc;

import org.apache.log4j.Logger;

import com.yijia.jdbc.bean.JDBCBeanConfig;
import com.yijia.jdbc.exception.ConnectServerInitException;
import com.yijia.jdbc.impl.ConnectServer_DB2;
import com.yijia.jdbc.impl.ConnectServer_mysql;
import com.yijia.jdbc.util.JDBCUtil_DB2;
import com.yijia.jdbc.util.JDBCUtil_mysql;

public class ConnectServerConfig {

	private static Logger logger = Logger.getLogger(ConnectServerConfig.class);
	private static ConnectServer connectServer;

	private ConnectServerConfig() {
	}

	/**
	 * 初始化ConnectServer，保证单例
	 * 
	 * @return ConnectServer对象
	 */
	private static ConnectServer init() {
		if (connectServer == null) {
			synchronized (ConnectServerConfig.class) {
				if (connectServer == null) {
					String databaseType = JDBCBeanConfig.getJDBCBean().getDatabaseType();
					if ("db2".equals(databaseType)) {
						connectServer = new ConnectServer_DB2(new JDBCUtil_DB2());
					} else if ("mysql".equals(databaseType)) {
						connectServer = new ConnectServer_mysql(new JDBCUtil_mysql());
					} else {
						throw new ConnectServerInitException("初始化ConnectServer失败，暂不支持【" + databaseType + "】类型数据库。");
					}
				}
			}
		}
		return connectServer;

	}

	/**
	 * 开始同步数据，同步完毕自动关闭数据库和ES连接
	 * 
	 * @param synchTableName
	 *            指定同步表名(null表示根据配置文件， *代表全部，多个表用前后逗号区分，例如：,z_user,z_role,)
	 */
	public synchronized static void synch() {
		init();
		if (connectServer != null) {
			try {
				connectServer.synch();
			} catch (Exception e) {
				logger.error("数据同步异常", e);
			} finally {
				close();
			}
		}
	}

	/**
	 * 现成执行完毕自动调用ConnectServer的close方法，关闭数据库和ES连接
	 */
	private static void close() {
		if (connectServer != null) {
			connectServer.close();
			connectServer = null;
		}
	}

}
