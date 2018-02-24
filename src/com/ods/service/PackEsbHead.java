package com.ods.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ods.common.Config;
import com.ods.common.Constant;
import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.message.AppHeadIn;
import com.ods.message.AppHeadOut;
import com.ods.message.EsbMessageOut;
import com.ods.message.LocalHead;
import com.ods.message.SysHeadIn;
import com.ods.message.SysHeadOut;

/**
 * @author ding_kaiye
 *
 */
public class PackEsbHead {

//	private static Properties properties = null;
	
	private static Logger logger = OdsLog.getTxnLogger("PackEsbHead");
//	private final static String confile = Constant.ESBPACK_CONFIG_FILE ;  //请求报文结构配置文件
//	private final static String reqSysHead = "ReqSysHead"; // SysHead在配置文件中的位置
//	private static Properties sysConfig = null;
//	private static String wsUrl = "";
	
	/**
	 * 
	 * @param inMessage
	 * @param serialNo
	 * @param txnSt
	 * @param retCd
	 * @param retMsg
	 * @return
	 * 2017-11-29
	 */
	
	public static EsbMessageOut packEsbFailMsg(SysHeadIn SysHead, String serialNo ,
			String txnSt, String retCd, String retMsg )  {
		SysHeadOut sysHeadOut = packEsbSysHead(SysHead, serialNo, txnSt, retCd, retMsg );
		EsbMessageOut rspMessage = new EsbMessageOut();
		rspMessage.setSysHead(sysHeadOut); 
		return rspMessage;
	}
	
	public static SysHeadOut packEsbSysHead(SysHeadIn sysHeadIn, String serialNo ,
			String txnSt, String retCd, String retMsg )  {

		SysHeadOut SysHead = new SysHeadOut();
		
		SysHead.setSvcId(sysHeadIn.getSvcId()); 
		SysHead.setSvcScn(sysHeadIn.getSvcScn());
		SysHead.setSvcSplrTxnCd(sysHeadIn.getSvcSplrTxnCd());
		SysHead.setSvcCstTxnCd(sysHeadIn.getSvcCstTxnCd());
		SysHead.setMAC(sysHeadIn.getMAC());
		SysHead.setCnsmrSysId(sysHeadIn.getCnsmrSysId());
		SysHead.setVrsn(Constant.Vrsn);
		SysHead.setCnsmrSeqNo(sysHeadIn.getCnsmrSeqNo());
		SysHead.setSvcSplrSysId(Constant.SysId);  //服务提供方系统编号
		SysHead.setSvcSplrSeqNo(serialNo);        //服务提供方流水号
		SysHead.setTxnDt(sysHeadIn.getTxnDt());
		SysHead.setTxnTm(sysHeadIn.getTxnTm());
		SysHead.setAcgDt(sysHeadIn.getAcgDt());
//		SysHead.setSvcSplrSvrId(sysHeadIn.getSvcSplrSvrId());
		SysHead.setTxnChnlTp(sysHeadIn.getTxnChnlTp());
		SysHead.setChnlNo(sysHeadIn.getChnlNo());
		SysHead.setTxnTmlId(sysHeadIn.getTxnTmlId());
		SysHead.setCnsmrSvrId(sysHeadIn.getCnsmrSvrId());
		SysHead.setOrigCnsmrId(sysHeadIn.getOrigCnsmrId());
		SysHead.setOrigCnsmrSeqNo(sysHeadIn.getOrigCnsmrSeqNo());
		SysHead.setOrigTmlId(sysHeadIn.getOrigTmlId());
		SysHead.setOrigCnsmrSvrId(sysHeadIn.getOrigCnsmrSvrId());
		SysHead.setUsrLng(sysHeadIn.getUsrLng());
		SysHead.setFileFlg(sysHeadIn.getFileFlg());
		SysHead.setTxnSt(txnSt);
		SysHead.setRetCd(retCd);
		SysHead.setRetMsg(retMsg);

		StringWriter xmlStringWriter = null;
		JAXBContext jc = null ;
		Marshaller marshaller = null;
		
		xmlStringWriter = new StringWriter();
		boolean flg = true;
		try {
			jc = JAXBContext.newInstance(SysHeadOut.class);
			marshaller = jc.createMarshaller();
			marshaller.marshal(SysHead, xmlStringWriter);
		} catch (JAXBException e) {
			logger.warn("转换 java 到 xml出现异常 ", e);
			flg = false;
		}
		if (true == flg) {
			String xml = xmlStringWriter.toString();
			logger.info(serialNo + ":返回报文为" + xml);
			xmlStringWriter = null;
		}
		
		return SysHead;
	}
	
	/**
	 * 组返回报文中的 AppHead
	 * @param appHeadIn
	 * @return
	 * 2017-11-29
	 */
	
	public static AppHeadOut packEsbAppHead(AppHeadIn appHeadIn)  {
		
		AppHeadOut appHeadOut = new AppHeadOut();
		appHeadOut.TxnTlrId    = appHeadIn.TxnTlrId   ;
		appHeadOut.OrgId       = appHeadIn.OrgId      ;
		appHeadOut.TlrPwsd     = appHeadIn.TlrPwsd    ;
		appHeadOut.AprvFlg     = appHeadIn.AprvFlg    ;
		appHeadOut.AprvTlrInf  = appHeadIn.AprvTlrInf ;
		appHeadOut.AhrTlrInf   = appHeadIn.AhrTlrInf  ;
		appHeadOut.ScndFlg     = appHeadIn.ScndFlg    ;
		
		return appHeadOut;
	}
	
	/**
	 * 组返回报文的 LocalHead
	 * @param localHead
	 * @return
	 * 2017-11-29
	 */
	public static LocalHead packEsbLocalHead(LocalHead localHead)  {
		return localHead;
	}
	
	
	
}
