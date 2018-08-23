package com.ods.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.Holder;
import javax.xml.ws.ResponseWrapper;
//import org.apache.log4j.Logger;

import com.ods.common.NameSpace;
//import com.ods.log.OdsLog;
import com.ods.manager.Handler;
import com.ods.message.AppHeadIn;
import com.ods.message.AppHeadOut;
import com.ods.message.EsbMessageOut;
import com.ods.message.LocalHead;
import com.ods.message.SysHeadIn;
import com.ods.message.SysHeadOut;

@WebService(targetNamespace=NameSpace.ODS_WSDL )
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
@XmlRootElement(namespace = NameSpace.ODS_WSDL)
public class ESBWaiter extends Thread {

	//private static Logger logger = OdsLog.getTxnLogger("ESBWaiter");
	
	/** 
	 * CK2384 
	 *   
	 */
	
	@WebMethod(operationName="ReqCK2384")
	@WebResult(name = "RspCK2384",  targetNamespace = NameSpace.ODS_WSDL )
	@ResponseWrapper(localName="RspCK2384", targetNamespace = NameSpace.ODS_WSDL )
	public void CK2384( 
			@WebParam(name= "ReqSysHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) SysHeadIn sysHeadIn,
			@WebParam(name= "ReqAppHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) AppHeadIn appHeadIn,
			@WebParam(name= "ReqLocalHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) LocalHead localHead,
			@WebParam(name= "Body", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) com.ods.transaction.CK2384.Ck2384ReqBody reqBody ,
			@WebParam(name= "RspSysHead", mode=Mode.OUT,  targetNamespace=NameSpace.ODS_URL) Holder<SysHeadOut> sysHeadOut,
			@WebParam(name= "RspAppHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<AppHeadOut> appHeadOut,
			@WebParam(name= "RspLocalHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<LocalHead> localHeadOut ,
			@WebParam(name= "Body", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<com.ods.transaction.CK2384.Ck2384RspBody> rspBody

		)  {
		 
		EsbMessageOut esbMessage =  Handler.QueryOdsData(sysHeadIn, appHeadIn, localHead, reqBody );
		sysHeadOut.value   = esbMessage.getSysHead();
		appHeadOut.value   = esbMessage.getAppHead();
		localHeadOut.value = esbMessage.getLocalHead();
		rspBody.value      = ( com.ods.transaction.CK2384.Ck2384RspBody ) esbMessage.getBody();
	}
	
	
	/**
	 * CK2384File
	 */
	@WebMethod(operationName="ReqCK2384File")
	@WebResult(name = "RspCK2384File",  targetNamespace = NameSpace.ODS_WSDL )
	@ResponseWrapper(localName="RspCK2384File", targetNamespace = NameSpace.ODS_WSDL )
	public void CK2384File( 
			@WebParam(name= "ReqSysHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) SysHeadIn sysHeadIn,
			@WebParam(name= "ReqAppHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) AppHeadIn appHeadIn,
			@WebParam(name= "ReqLocalHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) LocalHead localHead,
			@WebParam(name= "Body", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) com.ods.transaction.CK2384File.Ck2384FileReqBody reqBody ,
			@WebParam(name= "RspSysHead", mode=Mode.OUT,  targetNamespace=NameSpace.ODS_URL) Holder<SysHeadOut> sysHeadOut,
			@WebParam(name= "RspAppHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<AppHeadOut> appHeadOut,
			@WebParam(name= "RspLocalHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<LocalHead> localHeadOut ,
			@WebParam(name= "Body", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<com.ods.transaction.CK2384File.Ck2384FileRspBody> rspBody

		)  {
		 
		EsbMessageOut esbMessage =  Handler.QueryOdsData(sysHeadIn, appHeadIn, localHead, reqBody );
		sysHeadOut.value   = esbMessage.getSysHead();
		appHeadOut.value   = esbMessage.getAppHead();
		localHeadOut.value = esbMessage.getLocalHead();
		rspBody.value      = (com.ods.transaction.CK2384File.Ck2384FileRspBody) esbMessage.getBody();
	}
	
	
	@WebMethod(operationName="ReqS30013001504")
	@WebResult(name = "ReqS30013001504",  targetNamespace = NameSpace.ODS_WSDL )
	@ResponseWrapper(localName="RspS30013001504", targetNamespace = NameSpace.ODS_WSDL )
	public void ReqS30013001504( 
			@WebParam(name= "ReqSysHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) SysHeadIn sysHeadIn,
			@WebParam(name= "ReqAppHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) AppHeadIn appHeadIn,
			@WebParam(name= "ReqLocalHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) LocalHead localHead,
			@WebParam(name= "Body", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) com.ods.transaction.DepositTrans.Body.ReqBody reqBody ,
			@WebParam(name= "RspSysHead", mode=Mode.OUT,  targetNamespace=NameSpace.ODS_URL) Holder<SysHeadOut> sysHeadOut,
			@WebParam(name= "RspAppHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<AppHeadOut> appHeadOut,
			@WebParam(name= "RspLocalHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<LocalHead> localHeadOut  ,
			@WebParam(name= "Body", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<com.ods.transaction.DepositTrans.Body.RspBody> rspBody

		)  {
		
		EsbMessageOut esbMessage =  Handler.QueryOdsData(sysHeadIn, appHeadIn, localHead, reqBody );
		sysHeadOut.value   = esbMessage.getSysHead();
		appHeadOut.value   = esbMessage.getAppHead();
		localHeadOut.value = esbMessage.getLocalHead();
		rspBody.value      = (com.ods.transaction.DepositTrans.Body.RspBody) esbMessage.getBody();
	} 
	
	@WebMethod(operationName="ReqS30013001505")
	@WebResult(name = "ReqS30013001505",  targetNamespace = NameSpace.ODS_WSDL )
	@ResponseWrapper(localName="RspS30013001505", targetNamespace = NameSpace.ODS_WSDL )
	public void ReqS30013001505 ( 
			@WebParam(name= "ReqSysHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) SysHeadIn sysHeadIn,
			@WebParam(name= "ReqAppHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) AppHeadIn appHeadIn,
			@WebParam(name= "ReqLocalHead", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) LocalHead localHead,
			@WebParam(name= "Body", mode=Mode.IN, targetNamespace=NameSpace.ODS_URL) com.ods.transaction.FemTransDetailFile.ReqBody reqBody ,
			@WebParam(name= "RspSysHead", mode=Mode.OUT,  targetNamespace=NameSpace.ODS_URL) Holder<SysHeadOut> sysHeadOut,
			@WebParam(name= "RspAppHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<AppHeadOut> appHeadOut,
			@WebParam(name= "RspLocalHead", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<LocalHead> localHeadOut ,
			@WebParam(name= "Body", mode=Mode.OUT, targetNamespace=NameSpace.ODS_URL) Holder<com.ods.transaction.FemTransDetailFile.RspBody> rspBody

		)  {
		 
		EsbMessageOut esbMessage =  Handler.QueryOdsData(sysHeadIn, appHeadIn, localHead, reqBody );
		sysHeadOut.value   = esbMessage.getSysHead();
		appHeadOut.value   = esbMessage.getAppHead();
		localHeadOut.value = esbMessage.getLocalHead();
		rspBody.value      = (com.ods.transaction.FemTransDetailFile.RspBody) esbMessage.getBody();
	}
	
	
}
