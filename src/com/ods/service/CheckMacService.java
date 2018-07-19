package com.ods.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.manager.QueueManager;
import com.ods.manager.TxnConfigManager;
import com.ods.message.QueryMessager;
import com.ods.message.TxnMessager;
import com.ods.transaction.ITransaction;
import com.ods.ws.TxnBody;

public class CheckMacService extends AbstractService {

	// ArrayBlockingQueue
		private static Logger logger = OdsLog.getTxnLogger("CheckMacService");
		
		/** 交易处理类名称  **/
		private int sleeptime = 50;
		
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
			
			String sysId = null ;  // 系统代号
			String brhId = null ;  // 密钥节点
			String macTyp = null ; // mac类型
			String macData = null; // 待计算mac的数据
			String mac = null;  // 计算出来的mac
			String globalSeqNo = null; // 全局流水号
			String tranSeqNo = null; // 交易流水号
			String tranCode = null ; // 交易码
			
			//String 
					
			
			String txnName = null;
			
			String SerialNo = null;
			
			while (true) {
				// 查询对应交易
				TxnMessager txnMessager = null;
				try {
					txnMessager = QueueManager.SysQueuePoll(inQueue);
				} catch (Exception e) {
					logger.error("此次轮询" + inQueue + "出现异常, 稍后再次获取:" + e.getMessage());
					try {
						Thread.sleep(sleeptime);
					} catch (InterruptedException e1) {
						logger.warn("Thread.sleep Interrupted");
					}
				}
				try {
					if (null != txnMessager) {
						txnName = txnMessager.getTxnId(); // 获取交易代号
						SerialNo = txnMessager.getSerialNo(); // 获取 流水号
						logger.info(txnName + "流水号" + SerialNo + "处理开始");
						
						if (txnMessager.getMsgStatus() != true) { // 获取交易状态
							logger.info("交易:" + txnName + " 流水号" + SerialNo + " 状态为" + false + ", 交易不再处理");
							continue;
						}
						
						//1.  检查交易代号是否存在
						if (txnName == null ||  "".equals(txnName) ) {
							logger.error("交易代号不能为空,请检查:流水号:" + SerialNo); 
							txnMessager.setMsg("交易代号不能为空"); 
							QueueManager.moveToFailQueue(txnMessager, failQueue);
							continue;
						}
						
						QueueManager.SysQueueAdd(nextQueue, txnMessager); // 处理完成, 转入下一队列
						logger.info(txnName + "流水号" + SerialNo + "处理完成");
						
						// 继续处理下一交易
						continue;
						
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
						logger.error("交易处理出现异常[" + SerialNo + "]" + e.getMessage());
						QueueManager.moveToFailQueue(txnMessager, failQueue, "系统错误, 请稍候重试" + e.getMessage());
					} catch (Exception newE) {
						newE.printStackTrace();
					}
					continue;
				}
			}
		}
}
