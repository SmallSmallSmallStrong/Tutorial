package com.yijia.pool;

import java.util.concurrent.ExecutionException;

import org.elasticsearch.client.Client;

public interface ConnectionPool {
	public Client getClient() throws InterruptedException, ExecutionException;

	public void realse(Client client);

	public void destory();
}
