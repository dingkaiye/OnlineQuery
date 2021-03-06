package com.ods.transaction.FemTransDetailFile;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ods.common.NameSpace;
import com.ods.transaction.TxnBody;

@XmlRootElement(name = "Body", namespace = NameSpace.ODS_URL)
@XmlType(name="FemTransDetailFileRspBody", namespace=NameSpace.ODS_URL, propOrder = { "TotlNm", "RtrnNm", "Acct","AcctOfNm","DocTp", "CrtfctNo", "StrDt", "EndDt", "QuerySessionID"}) 
public class RspBody implements TxnBody {
	
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String TotlNm   = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String RtrnNm  = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String Acct      = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String AcctOfNm  = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String DocTp     = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String CrtfctNo  = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String StrDt     = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String EndDt     = null ; //
	@XmlElement(namespace = NameSpace.ODS_URL )
	public String QuerySessionID = null ; //

	@Override
	public void init (Map<String, Object> Map, List<Map<String, Object>> List ) {

		this.TotlNm    = (String) Map.get("TotlNm")  ; //
		this.RtrnNm    = (String) Map.get("RtrnNm")  ; //
		this.Acct      = (String) Map.get("Acct")  ; //
		this.AcctOfNm  = (String) Map.get("AcctOfNm")  ; //
		this.DocTp     = (String) Map.get("DocTp")  ; //
		this.CrtfctNo  = (String) Map.get("CrtfctNo")  ; //
		this.StrDt     = (String) Map.get("StrDt")  ; //
		this.EndDt     = (String) Map.get("EndDt")  ; //
		this.QuerySessionID = (String) Map.get("QuerySessionID");
		
	}
	
}
