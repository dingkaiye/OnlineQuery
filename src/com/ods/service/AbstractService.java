package com.ods.service;

public abstract class AbstractService extends Thread {

	public abstract void  ServiceInit ( String inQueueName, String nextQueueName, String failQueueName ) ;

}
