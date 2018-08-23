package com.ods.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import com.ods.log.OdsLog;

public class ArtifactInInterceptor extends AbstractPhaseInterceptor<Message> {
	
	
	private static Logger logger = OdsLog.getTxnLogger("ArtifactInInterceptor");
	
	public ArtifactInInterceptor() {   
        //这儿使用pre_stream，意思为在流关闭之前   
        super(Phase.PRE_STREAM);   
    }   
	
	@Override
	public void handleMessage(Message message) { 
		InputStream is = message.getContent(InputStream.class);
		if (is != null) {
            try {
                String soapXmlStr = IOUtils.toString(is);
                logger.info("收到请求报文 : \n[" + soapXmlStr + "]");
                
                Document document = null;
			    boolean domflg = true ; 
			    if("".equals(soapXmlStr)) {
			    	 domflg = false;
			    }
				if (domflg) {
					try {
						document = DocumentHelper.parseText(soapXmlStr);
					} catch (DocumentException e) {
						logger.error("请求生成到 Document 出错, 非XML报文, 确认正确请忽略", e);
						domflg = false;
					}
				}
				if (domflg) {
					Element rootElement = document.getRootElement();
					Element sysHead = (Element) rootElement.selectSingleNode(
							"/*[local-name()='Envelope']/*[local-name()='Body']//*[local-name()='ReqSysHead']");

					String nameSpace = sysHead.getNamespacePrefix();
					String nameSpaceUrl = sysHead.getNamespaceURI();

					List<Node> soapStrNodes = sysHead.selectNodes("./*[local-name()='OdsAddNodeReqSoapStr']");
					for (Node curNode : soapStrNodes) {
						logger.warn("移除意外收到的 OdsAddNodeReqSoapStr 节点" + curNode.getText());
						curNode.getParent().remove(curNode);
					}

					
					/* 此处可以替换Qname, 如需用配置文件适配各环境 
					 * .............
					 */
					
					Element SoapXml = sysHead.addElement(nameSpace + ":OdsAddNodeReqSoapStr");
					SoapXml.addNamespace(nameSpace, nameSpaceUrl);
					SoapXml.addCDATA(soapXmlStr);

					// InputStream ism = new ByteArrayInputStream(soapXmlStr.getBytes());
					String withMacResultXmlStr = rootElement.asXML();
					InputStream ism = new ByteArrayInputStream(withMacResultXmlStr.getBytes("UTF-8"));
					logger.info("添加信息后的报文 : \n" + withMacResultXmlStr);
					message.setContent(InputStream.class, ism);
				}
            } catch (IOException e) {
                logger.debug("" , e);
            }
		}
	}
//	
//	
//	private boolean checkMac(String soapXmlStr) throws TxnException {
//		// 校验 MAC
//		logger.error("开始校验MAC");
//		Properties EsbXmlConfig = null;
//
//			try {
//				EsbXmlConfig = TxnConfigManager.getTxnConfig("EsbXmlConfig");
//			} catch (Exception e) {
//				logger.error("获取 EsbXmlConfig 配置出错", e);
//				throw new TxnException("获取 EsbXmlConfig 配置出错");
//			}
//			if (EsbXmlConfig == null) {
//				logger.error("EsbXmlConfig 配置不存在, 不能解析报文!!!");
//				throw new TxnException("EsbXmlConfig 配置不存在, 不能解析报文!!!");
//			}
//
//			Document document = null;
//			try {
//				document = DocumentHelper.parseText(soapXmlStr);
//			} catch (DocumentException e1) {
//				logger.error("报文 转换到 Document 出错, 请检查报文格式", e1);
//			}
//			Element rootElement = document.getRootElement();
//
//			String xpathCnsmrSysId = EsbXmlConfig.getProperty("cnsmrSysId"); // 服务调用方系统编号
//			String xpathGlobalSeqNo = EsbXmlConfig.getProperty("globalSeqNo"); // 全局流水号
//			String xpathTranSeqNo = EsbXmlConfig.getProperty("CnsmrSeqNo"); // 交易流水号
//			String xpathTranCode  = EsbXmlConfig.getProperty("tranCode"); // 交易码
//			String xpathMac       = EsbXmlConfig.getProperty("MAC"); // 计算出来的mac
//
//			Node nodeCnsmrSysId = rootElement.selectSingleNode(xpathCnsmrSysId); // 服务调用方系统编号
//			Node nodeGlobalSeqNo = rootElement.selectSingleNode(xpathGlobalSeqNo); // 全局流水号
//			Node nodeTranSeqNo = rootElement.selectSingleNode(xpathTranSeqNo); // 交易流水号
//			Node nodeTranCode = rootElement.selectSingleNode(xpathTranCode); // 交易码
//			Node nodeMac = rootElement.selectSingleNode(xpathMac); // 计算出来的mac
//
//			String sysId = nodeCnsmrSysId.getText(); // 服务调用方系统编号
//			String globalSeqNo = nodeGlobalSeqNo.getText(); // 全局流水号
//			String tranSeqNo = nodeTranSeqNo.getText(); // 交易流水号
//			String tranCode = nodeTranCode.getText(); // 交易码
//			String mac = nodeMac.getText(); // 计算出来的mac
//			String macNode = EsbXmlConfig.getProperty("DesMacBrhId"); // 密钥节点
//			String macTyp = EsbXmlConfig.getProperty("macTyp"); // mac类型
//			String initMac = EsbXmlConfig.getProperty("InitMac");
//			
//			nodeMac.setText(initMac);
//			
//			byte[] macData;
//			try {
//				macData = rootElement.asXML().getBytes("UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				throw new TxnException("转换报文到byte[]时出错");
//			} // 待计算mac的数据
//			
//			if (logger.isDebugEnabled()) {
//				logger.debug("MAC: " + mac);
//				logger.debug("全局流水号: " + globalSeqNo);
//				logger.debug("交易流水号: " + tranSeqNo);
//				logger.debug("交易码: " + tranCode);
//			}
//
//			// 校验Mac
////			HsmRespMessage hsmRespMessage = HsmFactory.hsm_verify_mac(sysId, macNode, macTyp, macData, mac, globalSeqNo,
////					tranSeqNo, tranCode);
//			logger.error("完成校验MAC");
//			return true;
//
//	}
}
