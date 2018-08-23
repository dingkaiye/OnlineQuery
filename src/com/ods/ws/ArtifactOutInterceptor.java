package com.ods.ws;


import java.io.ByteArrayInputStream;
import java.io.InputStream; 
import java.io.OutputStream;
import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import com.ods.common.NameSpace;
import com.ods.log.OdsLog;

import org.apache.cxf.phase.AbstractPhaseInterceptor; 

	public class ArtifactOutInterceptor extends AbstractPhaseInterceptor<SoapMessage>{   
	 
	private static Logger logger = OdsLog.getTxnLogger("ArtifactOutInterceptor");
	
	public ArtifactOutInterceptor() {   
        //在流关闭之前   
        super(Phase.PRE_STREAM);   
    }   
   
 
    @Override
    public void handleMessage(SoapMessage message) { 
    	logger.debug("" + message.getClass() );	
    	try {   
    		logger.debug("message"  + message.getClass() + message);
    		String xmlstring = null ;  
    		
            OutputStream os = message.getContent(OutputStream.class); 
    		logger.debug(":" + os.getClass());
            CachedOutputStream cs = new CachedOutputStream(1073741824);  // 2^30 
            message.setContent(OutputStream.class, cs); 
            message.getInterceptorChain().doIntercept(message);
            
            cs = (CachedOutputStream) message.getContent(OutputStream.class);   
            InputStream in = cs.getInputStream(); 
            
            xmlstring = IOUtils.toString(in);
            logger.info("输出报文为:" + xmlstring);

			Document document = null;
			boolean flg = true ; 
			try {
				document = DocumentHelper.parseText(xmlstring);
			} catch (DocumentException e) {
				logger.error("报文 转换到 Document 出错, 请检查报文格式", e);
				flg = false;
			}

			logger.debug(" \n" + xmlstring);
			if (flg) {
				Element root = document.getRootElement();
				QName qname = null;
				final String rooturl = "http://schemas.xmlsoap.org/soap/envelope/" ;
				// 
				String xpath = "/*[local-name()='Envelope']/*[local-name()='Body']";
				Element txnRspNode = (Element) root.selectSingleNode(xpath);
				txnRspNode.remove(txnRspNode.getNamespaceForPrefix(rooturl));
				
				qname = null;
				String nodeName = txnRspNode.getName();
				qname = new QName(nodeName , new Namespace("soapenv", rooturl));
				txnRspNode.setQName(qname);
				
				// 取  Body 下 交易节点 
				xpath = "/*[local-name()='Envelope']/*[local-name()='Body']/*";
				txnRspNode = (Element) root.selectSingleNode(xpath);
				
				txnRspNode.remove(txnRspNode.getNamespaceForURI(NameSpace.ODS_WSDL));
				txnRspNode.remove(txnRspNode.getNamespaceForURI(NameSpace.ODS_URL));
			
				nodeName = txnRspNode.getName();
				qname = new QName(nodeName , new Namespace("tns", NameSpace.ODS_WSDL));
				txnRspNode.setQName(qname);
				txnRspNode.add(DocumentHelper.createNamespace("tns", NameSpace.ODS_WSDL));
				txnRspNode.add(DocumentHelper.createNamespace("s", NameSpace.ODS_URL));
				
				// 遍历 head , body 
				xpath = "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='" + nodeName + "']//*" ;
				List<Node> ListNodeS = root.selectNodes(xpath);
				for (Node node : ListNodeS ) {
					Element el = (Element) node; 
					el.remove(txnRspNode.getNamespaceForPrefix(rooturl));
					nodeName = el.getName();
					qname = new QName(nodeName , new Namespace("s", NameSpace.ODS_URL));
					el.remove(txnRspNode.getNamespaceForURI(NameSpace.ODS_WSDL));
					el.remove(txnRspNode.getNamespaceForURI(NameSpace.ODS_URL));
					el.setQName(qname);
					el.add(DocumentHelper.createNamespace("tns", NameSpace.ODS_WSDL));
					el.add(DocumentHelper.createNamespace("s", NameSpace.ODS_URL));
				}
				
				
				String rootName  = root.getName();
				txnRspNode.remove(txnRspNode.getNamespaceForPrefix(rooturl));
				qname = new QName(rootName , new Namespace("soapenv", rooturl));
				root.setQName(qname);
				
				root.add(DocumentHelper.createNamespace("tns", NameSpace.ODS_WSDL));
				root.add(DocumentHelper.createNamespace("s", NameSpace.ODS_URL));

				
				xmlstring = root.asXML();

				// 处理完后同理，写回流中
				IOUtils.copy(new ByteArrayInputStream(xmlstring.getBytes("UTF-8")), os);
				
			}
			in.close();
			os.flush();
			message.setContent(OutputStream.class, os);
        } catch (Exception e) { 
            logger.error("Error when split original inputStream. CausedBy : " + "\n" , e); 
        } 
    } 
 
}
