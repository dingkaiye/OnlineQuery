package com.ods.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ods.common.Config;
import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.manager.QueueManager;
import com.ods.manager.TxnConfigManager;
import com.ods.message.EsbMessageIn;
import com.ods.message.SysHeadIn;
import com.ods.message.TxnMessager;

/**
 * CheckMacService 
 * 校验MAC服务
 * @author ding_kaiye
 */
public class CheckMacService extends AbstractService {

	private static Logger logger = OdsLog.getTxnLogger("CheckMacService");

	/** 冲突时睡眠时间 **/
	private int sleeptime = 50;

	/** 输入队列 */
	private String inQueue = null;

	/** 处理成功后, 转入的队列名称 */
	private String nextQueue = null; // 默认

	/** 处理失败后, 转入的队列名称 */
	private String failQueue = null;
	
//	private Properties SysConfig = null;
	private Properties EsbXmlConfig = null;
	/**
	 * @param inQueueName
	 *            输入队列名称
	 * @param nextQueueName
	 *            输出队列名称
	 * @param failQueueName
	 *            失败队列
	 * @throws IOException
	 */
	@Override
	public void ServiceInit(String inQueueName, String nextQueueName, String failQueueName) {
		// TODO Auto-generated method stub
		this.inQueue = inQueueName;
		if (nextQueueName != null && !"".equals(nextQueueName)) {
			this.nextQueue = nextQueueName;
		}
		if (failQueueName != null && !"".equals(failQueueName)) {
			this.failQueue = failQueueName;
		}
		
		logger.info("服务初始化完成, 输入队列:[" + this.inQueue + "] 输出队列:[" + this.nextQueue + "]");
	}
		

	@Override
	public void run() {
			
		final Thread service = Thread.currentThread() ; // 当前线程
		final String serviceName = service.getName();   // 当前线程名
		
		String initMac  = null;
		// Mac在报文中的路径
		String xpathMac = null; 
		String macTyp   = null; // mac类型
		
//		HsmRespMessage hsmRespMessage = null;  //Mac校验使用

		String txnName = null;
		String SerialNo = null;
		String odsSysId = null ;
		String macNode = null;
		TxnMessager txnMessager = null;
		String macLabelStart = null;
		String macLabelEnd = null;
		String checkMacFlag = null;
		// 从系统配置中获取MAC校验开关状态
		try {
			Properties sysProperties = Config.loadConfigPropertiesFile("SysConfig.properties");
			checkMacFlag = sysProperties.getProperty("CheckMac");
		} catch (Exception e) {
			checkMacFlag = null;
		}
		
		
		while (true) {
			try {
				service.setName( serviceName );
			} catch (Exception e) {
				logger.error("Thread Set Name Catch Excrption" + e, e);
			}
			// 查询对应交易
			try {
				txnMessager = QueueManager.SysQueuePoll(inQueue);
			} catch (Exception e) {
				logger.error("此次轮询" + inQueue + "出现异常, 稍后再次获取:" + e.getMessage());
				try {
					Thread.sleep(sleeptime);
				} catch (InterruptedException e1) {
					logger.warn("Thread.sleep Interrupted");
				}
				continue ;
			}
			
			try {
				if (EsbXmlConfig == null || txnName == null || SerialNo == null || odsSysId == null || macNode == null ) {
					try {
						EsbXmlConfig =  Config.loadConfigPropertiesFile("EsbXmlConfig.properties");
					} catch (Exception e) {
						logger.error("获取 EsbXmlConfig 配置出错", e);
						throw new TxnException("获取 EsbXmlConfig 配置出错");
					}
					if (EsbXmlConfig == null) {
						logger.error("EsbXmlConfig 配置不存在, 不能解析报文!!!");
						throw new TxnException("EsbXmlConfig 配置不存在, 不能解析报文!!!");
					}
					initMac = EsbXmlConfig.getProperty("InitMac");
					odsSysId = EsbXmlConfig.getProperty("OdsSysId");
					// Mac在报文中的路径
					xpathMac = EsbXmlConfig.getProperty("MAC"); 
					macTyp  = EsbXmlConfig.getProperty("macTyp"); // mac类型
					macNode = EsbXmlConfig.getProperty("macNode"); // 密钥节点
					macLabelStart = EsbXmlConfig.getProperty("macLabelStart") ;
					macLabelEnd = EsbXmlConfig.getProperty("macLabelEnd") ;
				}
				
				if (null != txnMessager) {
					txnName = txnMessager.getTxnId(); // 获取交易代号
					SerialNo = txnMessager.getSerialNo(); // 获取 流水号
					if (txnMessager.getMsgStatus() != true) { // 获取交易状态
						logger.info("交易:" + txnName + "[" + SerialNo + "]状态为" + false + ", 交易不再处理");
						continue;
					}
					if (checkMacFlag != null && "off".equals(checkMacFlag)) {
						logger.warn("校验MAC配置状态为关闭," + txnName + "[" + SerialNo + "]直接转入下一队列");
						QueueManager.SysQueueAdd(nextQueue, txnMessager); // 处理完成, 转入下一队列
						continue;
					} else {
						service.setName( serviceName + ":" + SerialNo);
						logger.info(txnName + "流水号" + SerialNo + "处理开始");
						// 获取 校验 Mac 需要的信息
						EsbMessageIn messageIn = txnMessager.getMessageIn();
						SysHeadIn sysHeadIn = messageIn.getSysHead();
						
						String globalSeqNo = sysHeadIn.getOrigCnsmrSeqNo(); // 全局流水号
						String tranSeqNo   = sysHeadIn.getCnsmrSeqNo(); // 交易流水号
						String tranCode    = sysHeadIn.getSvcId();      // 交易码
						String mac         = sysHeadIn.getMAC(); 
						
						String soapXmlStr = sysHeadIn.getOdsAddNodeReqSoapStr(); // 获取原报文数据
						
						String strMacData = soapXmlStr.replace(macLabelStart + mac + macLabelEnd, macLabelStart + initMac + macLabelEnd);
						byte[] macData = null;
						if (strMacData != null) {
							macData = strMacData.getBytes("UTF-8");
						}
						
						logger.info("校验Mac[" + mac + "]开始, 全局流水[" + globalSeqNo + "] 本地流水[" + SerialNo + "]");
						String requestSoapMac = null ; 
						String retcod = null ; 
						String retmsg = null ; 
						try {
	//						HsmRespMessage hsmRespMessage = null;
						/** 重新计算 MAC, 进行 校验 
						*	sysId	         系统代号	    String	N	无	当系统代号为NULL的时候，默认从配置文件中获取
						*	brhId	         密钥节点	    String	Y	无	不做填充
						*	macTyp	    mac类型	    String	Y	无	1：银联 2：asni  3：pos 4:qrcb 行内大报文
						*	macData	         待计算mac数据	byte[]	Y	无	
						*	GlobalSeqNo 全局流水号	varchar（25）	N	无	
						*	TranSeqNo	交易流水号	varchar（22）	Y	无	
						*	TranCode	交易码	    varchar（10）	N	无	
						**/
							if (logger.isDebugEnabled()) {
								logger.debug("\n macData[" + strMacData + "]");
								logger.debug("\n 系统代号[" + odsSysId + "]");
								logger.debug("\n 密钥节点[" + macNode + "]");
								logger.debug("\n Mac类型[" + macTyp + "]");
								logger.debug("\n 全局流水号[" + globalSeqNo + "]");
								logger.debug("\n 交易流水号[" + tranSeqNo + "]");
								logger.debug("\n 交易码[" + tranCode + "]");
							}
	
//							hsmRespMessage = HsmFactory.hsm_generate_mac(odsSysId, macNode,
//													macTyp, macData, globalSeqNo, tranSeqNo, tranCode);
//							logger.debug("调用 HsmFactory.hsm_generate_mac 完成");
//							if() {
//								logger.error("校验MAC失败, 安全平台无返回");
//								throw new TxnException("校验MAC失败, 安全平台无返回");
//							}else {
//								logger.debug("安全平台返回的校验结果为:" + hsmRespMessage.isSuccess());
//							}
//							if (hsmRespMessage.isSuccess()) {
//								requestSoapMac = hsmRespMessage.getBodyItemString(0);
//								logger.debug("获取 返回 MAC 完成");
//							} else {
//								retcod = hsmRespMessage.getRetCode();
//								retmsg = hsmRespMessage.getRetMsg();
//								logger.debug("校验MAC失败, 安全平台返回:" + retcod + " " + retmsg);
//								throw new TxnException("校验MAC失败, 安全平台返回:" + retcod + " " + retmsg);
//							}
//	
//							// 校验失败 放入失败队列
//							if (requestSoapMac == null || !requestSoapMac.equals(mac)) {
//								logger.error("校验MAC失败,当前MAC [" + mac + "] 正确MAC [" + requestSoapMac + "]");
//								throw new TxnException("校验MAC失败:" + mac + "不正确");
//							}
							
						} catch (Exception e) {
							logger.error("校验Mac异常, 全局流水[" + globalSeqNo + "] 本地流水[" + SerialNo + "]", e);
							txnMessager.setMsg(e.getMessage()); 
							QueueManager.moveToFailQueue(txnMessager, failQueue);
							continue ;
						}
						logger.info("校验Mac通过, 全局流水[" + globalSeqNo + "] 本地流水[" + SerialNo + "]");
						QueueManager.SysQueueAdd(nextQueue, txnMessager); // 处理完成, 转入下一队列
					}
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
					logger.error("New Exception", newE);
				}
				continue;
			}
		}
	}
}
