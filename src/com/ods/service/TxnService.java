package com.ods.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.ods.common.Constant;
import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.manager.QueueManager;
import com.ods.manager.TxnConfigManager;
import com.ods.message.QueryMessager;
import com.ods.message.TxnMessager;
import com.ods.transaction.ITransaction;
import com.ods.ws.TxnBody;

/**
 * 交易管理分配类, 系统级交易相关处理
 * @author ding_kaiye
 * @date 2017-09-23
 */
public class TxnService extends AbstractService {
	
	
	// ArrayBlockingQueue
	private static Logger logger = OdsLog.getTxnLogger("TxnService");
	
	/** 交易处理类名称  **/
	private final String txnClassPropertie = "ClassName" ;
//	/** 请求报文中 body 实现类 配置 */
//	private final String repClassPropertie = "ReqBody" ; 
//	/** 返回报文中 body 实现类 配置 */
//	private final String rspClassPropertie = "RspBody" ;   
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
		
		String txnName = null;
		
		String txnClassName = null;
//		String reqBodyClassName = null;
//		String rspBodyClassName = null;
		
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

					//2.  读取配置文件, 获取入参
					Properties txnProperties = null;
					try {
						txnProperties = TxnConfigManager.getTxnConfig(txnName) ;
					} catch (Exception e) {
						logger.error(txnName + "流水号" + SerialNo + "获取交易配置出错" , e); 
						QueueManager.moveToFailQueue(txnMessager, "获取交易配置出错" ); //转入失败交易队列
						continue;
					}
					if (txnProperties == null) {
						logger.error("流水号" + SerialNo + "对应的交易" + txnName +  "不存在" ); 
						QueueManager.moveToFailQueue(txnMessager, "流水号:" + SerialNo + "对应的交易:" + txnName +  "不存在" ); //转入失败交易队列
						continue;
					}

					//4. 根据 properties 配置, 实例化 交易对应的处理类  (此处可以优化,可进一步拆分和扩展)
					// className = (String) properties.getProperty(txnName);  // 20171120 使用配置放入具体交易中
					txnClassName = (String) txnProperties.getProperty(txnClassPropertie);
					
					// 检查是否配置 
					if (txnClassName == null || "".equals(txnClassName.trim()) ) {
						logger.error("获取交易处理类出错, 流水号[" + SerialNo + "]交易中指定的交易代号:" + txnName + ", 配置不存在");
						txnMessager.setMsg("获取交易处理类出错, 流水号[" + SerialNo + "]交易中指定的交易代号:" + txnName + ", 配置不存在"); 
						QueueManager.moveToFailQueue(txnMessager, failQueue);
						continue;
					}
					logger.debug("获取的交易名称是:" + txnName + " 交易处理类名为:[" + txnClassName + "]");

					ITransaction instance = null;
					try {
						instance = (ITransaction) Class.forName(txnClassName).newInstance();
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						logger.error(txnClassName + "获取交易处理对象时出现异常", e);
						QueueManager.moveToFailQueue(txnMessager, failQueue); // 转入失败交易队列
					}
					logger.info(txnName + "流水号" + SerialNo + "实例化 交易对应的处理类完成, 交易处理类是:" + instance);
					
					//5. 启动数据库操作交易处理
					QueryMessager resultMessager = null;
					
					TxnBody txnBody = txnMessager.getMessageIn().getBody();
					try {
						resultMessager = instance.transaction(txnBody, SerialNo);
					} catch (TxnException e) { 
						logger.error("交易号:" + txnName + "流水号" + SerialNo + "TxnException 异常:" + e.getLocalizedMessage());
						//获取错误代码
						txnMessager.setReturnCode(e.getErrorCode());
						//获取错误信息
						txnMessager.setMsg(e.getMessage());
						//转入失败消息队列
						QueueManager.moveToFailQueue(txnMessager, failQueue);  //转入失败交易队列
						continue;
					} catch (SQLException e) {
						logger.error(txnName + "流水号" + SerialNo + "SQLException 异常:" + e.getLocalizedMessage());
						//获取错误信息
						txnMessager.setMsg(e.getMessage());
						//转入失败消息队列
						QueueManager.moveToFailQueue(txnMessager, failQueue);  //转入失败交易队列
						continue;
					}
					
					logger.info(txnName + "流水号" + SerialNo + "数据库操作交易处理完成");
					
					// 判断交易是否成功 	
					if(resultMessager.getResult() == false) {
						// 交易失败, 开始交易失败的处理
						txnMessager.setReturnCode(resultMessager.getReturnCode());
						//获取错误信息
						txnMessager.setMsg(resultMessager.getMsg());
						//转入失败消息队列
						QueueManager.moveToFailQueue(txnMessager, failQueue);  //转入失败交易队列
						continue;
					}
					
//					String keyDefine = (String) txnProperties.getProperty(Constant.keyDefine);

					txnMessager.setResultHead(resultMessager.getResultHead());
					txnMessager.setResultList(resultMessager.getResultList());
					
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
