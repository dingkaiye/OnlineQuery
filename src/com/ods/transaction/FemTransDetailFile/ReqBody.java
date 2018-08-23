package com.ods.transaction.FemTransDetailFile;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ods.common.NameSpace;
import com.ods.transaction.TxnBody;

@XmlRootElement(namespace=NameSpace.ODS_URL)
@XmlType(name="FemTransDetailFileReqBody", namespace=NameSpace.ODS_URL)
//@XmlType(propOrder = { "AcctAcc", "StrDt", "EndDt","PgBgn","PgShwNum"}) 
public class ReqBody implements TxnBody {
	private String AcctAcc   = new String();   // 账号
	private String StrDt     = new String();   // 开始日期
	private String EndDt     = new String();   // 结束日期
	private String PgBgn     = new String();   // 起点
	private String PgShwNum  = new String();   // 请求数量
	private String QuerySessionID = new String(); // 查询ID标识 
	
	public String getAcctAcc() {
		return AcctAcc;
	}
	public String getStrDt() {
		return StrDt;
	}
	public String getEndDt() {
		return EndDt;
	}
	public String getPgBgn() {
		return PgBgn;
	}
	public String getPgShwNum() {
		return PgShwNum;
	}
		public String getQuerySessionID() {
		return QuerySessionID;
	}
	
	@XmlElement(name="AcctAcc", namespace = NameSpace.ODS_URL )
	public void setAcctAcc(String acctAcc) {
		AcctAcc = acctAcc;
	}
	@XmlElement(name="StrDt", namespace = NameSpace.ODS_URL )
	public void setStrDt(String strDt) {
		StrDt = strDt;
	}
	@XmlElement(name="EndDt", namespace = NameSpace.ODS_URL )
	public void setEndDt(String endDt) {
		EndDt = endDt;
	}
	@XmlElement(name="PgBgn", namespace = NameSpace.ODS_URL )
	public void setPgBgn(String pgBgn) {
		PgBgn = pgBgn;
	}
	@XmlElement(name="QuerySessionID", namespace = NameSpace.ODS_URL )
	public void setQuerySessionID(String querySessionID) {
		QuerySessionID = querySessionID;
	}
	
	@XmlElement(name="PgShwNum", namespace = NameSpace.ODS_URL )
	public void setPgShwNum(String pgShwNum) {
		PgShwNum = pgShwNum;
	}
	
	@Override
	public void init(Map<String, Object> Map, List<Map<String, Object>> List ) {
		
	}
	
	
	
}
