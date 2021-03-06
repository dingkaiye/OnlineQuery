package com.ods.service;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.ods.log.OdsLog;
import com.ods.manager.QueueManager;
import com.ods.message.EsbMessageIn;
import com.ods.message.EsbMessageOut;
import com.ods.message.EsbMessage;
import com.ods.message.TxnMessager;

public class SendFailMsgService extends AbstractService {
	
	/**
	 * 处理失败的交易, 组织失败报文并返回
	 * @author ding_kaiye
	 * @ 2017-09-27
	 */
		
	private static Logger logger = OdsLog.getTxnLogger("SendService");

	private int sleeptime = 50;

	Properties properties = null;
	
	/** 输入队列  */
	private String inQueue = null;
	
	/** 处理成功后, 转入的队列名称  */
	private String nextQueue = null ;  // 默认 
	
	/** 处理失败后, 转入的队列名称  */
	private String failQueue = null ;
	
 /**
  * @param inQueueName    输入队列名称
  * @param nextQueueName  输出队列名称
  * @param failQueueName  失败队列
  * @throws IOException
  */
	@Override
	public	void ServiceInit(String inQueueName, String nextQueueName, String failQueueName) {
		// TODO Auto-generated method stub
		this.inQueue = inQueueName;
		if(nextQueueName != null && ! "".equals(nextQueueName)){
			this.nextQueue = nextQueueName ; 
		}
		if(failQueueName != null && ! "".equals(failQueueName)){
			this.failQueue = failQueueName ; 
		}
		logger.info("服务初始化完成, 输入队列:[" + this.inQueue + "] 输出队列:[" + this.nextQueue + "]");	
	}

	@Override
	public void run() {
		
		final Thread service = Thread.currentThread() ; // 当前线程
		final String serviceName = service.getName();   // 当前线程名
		String SerialNo = null;
		String txnName = null ;
		TxnMessager txnMessager = null;
	
		while (true) {
			try {
				service.setName( serviceName );
			} catch (Exception e) {
				logger.error("Thread Set Name Catch Excrption" + e, e);
			}
			try { //服务不退出
				try { // 从队列中获取 txnMessager
					txnMessager = QueueManager.SysQueuePoll(inQueue);
				} catch (Exception e) {
					logger.error("此次轮询" + inQueue + "出现异常, 稍后再次获取:" + e.getMessage());
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e1) {
						logger.warn("Thread.sleep InterruptedException");
					}
					continue ;
				}

				if (null != txnMessager) {
					txnName = txnMessager.getTxnId(); // 获取交易代号
					SerialNo = txnMessager.getSerialNo(); // 获取 流水号
					if (txnMessager.getMsgStatus() != true) { // 获取交易状态
						logger.info("交易:" + txnName + " 流水号" + SerialNo + " 状态为" + false + ", 交易不再处理");
						continue;
					}
					try {
						service.setName( serviceName + ":" + SerialNo);
						// 组失败报文 
						EsbMessageIn esbMessage = null;
						EsbMessage reqMessage = txnMessager.getMessageIn(); 
						if (reqMessage instanceof EsbMessageIn ){
							esbMessage = (EsbMessageIn) reqMessage;
						
						SerialNo = txnMessager.getSerialNo();
						String txnSt = "F";  // F－系统处理失败
						String RetCd = txnMessager.getReturnCode() ;
						String retMsg = txnMessager.getMsg();
						EsbMessageOut rspMessage = PackEsbHead.packEsbFailMsg(esbMessage.getSysHead(), SerialNo, txnSt, RetCd, retMsg);
						logger.debug(SerialNo + " 组失败包完成");
						txnMessager.setMessageOut(rspMessage);
						
						// 同步方式, 取得TxnMessage中记录的线程, 中断线程 sleep
						// 先实现同步方式 相应, 后期开发异步相应方式
						Thread headler = (Thread) txnMessager.getHeadlerThread();
						headler.interrupt(); // 中断 headler的等待
						}
						// 异步方式, 放入 对应的 Gate 队列

						continue;

					} catch (Exception e) {
						logger.error("交易处理出现异常[" + SerialNo + "]" + e.getMessage());
						//QueueManager.moveToFailQueue(txnMessager);
					}
				} else {
					try {
						logger.trace("此次轮询" + inQueue + "未获得待处理交易,稍后再次获取");
						Thread.sleep(sleeptime);
					} catch (InterruptedException e) {
						logger.debug("sleep has be Interrupted", e);
					}
				}

			} catch (Exception e) {
				try {
					logger.error("交易处理出现异常[" + SerialNo + "]" + e.getMessage(), e);
					QueueManager.moveToFailQueue(txnMessager, failQueue, "系统错误, 请稍候重试" + e.getMessage());
				} catch (Exception newE) {
					logger.error("New Exception" + newE, newE);
				}
				continue ;
			}

		}
	}

}
