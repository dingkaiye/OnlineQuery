package com.ods.transaction.DepositTrans.Body;


import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ods.common.NameSpace;

@XmlRootElement(name = "TransResultLine", namespace = NameSpace.ODS_URL)
public class TransResultLine {
	
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String EvntSrlNo  = new String() ;
//		@XmlElement(namespace = NameSpace.ODS_URL )
//		public String CrdNum     = new String() ;
//		@XmlElement(namespace = NameSpace.ODS_URL )
//		public String PsbkAcct   = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String CustAcctNO  = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String AcctSeqNo  = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String VoucherType  = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String CBSTransID   = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TransID      = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String ChannelID      = new String() ;
		
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TransDt      = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TransTime      = new String() ;
		
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TxnAmt     = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String Balce      = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String DoNotAcct  = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String DbAndCr    = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String Ccy      = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String CorrSign   = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String AbstRsm    = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String AcctNo     = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String OprNum     = new String() ;
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TdgNtw     = new String() ;
		
		// 交易IP地址 
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TransIP     = new String() ;
		
		@XmlElement(namespace = NameSpace.ODS_URL )
		public String TelleSrlNo = new String() ;

		public TransResultLine (Map<String, Object> result){
			this.EvntSrlNo  = (String) result.get("EvntSrlNo")   ;
//			this.CrdNum     = (String) result.get("CrdNum")   ;
//			this.PsbkAcct   = (String) result.get("PsbkAcct")   ;
//			this.TxnDt      = (String) result.get("TxnDt")   ;

			this.VoucherType = (String) result.get("VoucherType")   ;
			this.CBSTransID  = (String) result.get("CBSTransID")   ;
			this.TransID     = (String) result.get("TransID")   ;
			this.ChannelID   = (String) result.get("ChannelID")   ;
			
			this.TransDt     = (String) result.get("TransDt")   ;
			this.TransTime   = (String) result.get("TransTime")   ;
			
			this.TxnAmt     = (String) result.get("TxnAmt")   ;
			this.Balce      = (String) result.get("Balce")   ;
			this.DoNotAcct  = (String) result.get("DoNotAcct")   ;
			this.DbAndCr    = (String) result.get("DbAndCr")   ;
			this.Ccy        = (String) result.get("Ccy_A")   ;
			this.CorrSign   = (String) result.get("CorrSign")   ;
			this.AbstRsm    = (String) result.get("AbstRsm")   ;
			this.AcctNo     = (String) result.get("AcctNo")   ;
			this.OprNum     = (String) result.get("OprNum")   ;
			this.TdgNtw     = (String) result.get("TdgNtw")   ;
			this.TelleSrlNo = (String) result.get("TelleSrlNo")  ;
			
			this.TransIP = (String) result.get("TransIP")  ;
		}
		
		public TransResultLine (){
			
		}

}
