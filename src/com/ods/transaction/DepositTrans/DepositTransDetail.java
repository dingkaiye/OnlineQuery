package com.ods.transaction.DepositTrans;

import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import com.ods.db.DbDataLine;
import com.ods.db.DbQuery;
import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.message.QueryMessager;
import com.ods.message.QueryResult;
import com.ods.transaction.ITransaction;
import com.ods.transaction.TxnBody;
import com.ods.transaction.DepositTrans.Body.ReqBody;

public class DepositTransDetail implements ITransaction{
	Logger logger = OdsLog.getTxnLogger("DepositTransDetail");

	@Override
	public QueryMessager transaction(TxnBody txnBody, String SerialNo) throws TxnException, SQLException {
		
		logger.info("开始执行 " + SerialNo + "的查询");
		QueryResult queryResult = null;
		ReqBody reqbody = null;
		// 判断是否是所需body 
		if( (txnBody instanceof ReqBody) != true){
			throw  new TxnException(" 请求 Body 类型错误, 不是 DepositTransDetailIn 类型");
		}
		
		reqbody = (ReqBody) txnBody;
		
		// AcctId,StartDt,EndDt,PageBeginPos,PageShoeNum
		String AcctId   =  reqbody.getAcctAcc() ;  //账号
		String StartDt  =  reqbody.getStrDt() ;   // 开始日期
		String EndDt    =  reqbody.getEndDt();   // 结束日期
		String pgBgn    =  reqbody.getPgBgn();   //起始位置
		String pgShwNum =  reqbody.getPgShwNum() ;  //显示条数
		StringBuffer errorMsg = new StringBuffer();
		
		Integer PageBeginPos = 0;
		Integer PageShoeNum = 0;
		// 检查输入项  
		if( AcctId == null || "".equals(AcctId)) {
			errorMsg.append("账号不能为空,"); //
		}
		if( StartDt == null || "".equals(StartDt)) {
			errorMsg.append("起始日期不能为空,"); //
		}
		if( EndDt == null || "".equals(EndDt)) {
			errorMsg.append("结束日期不能为空,"); //
		}
		

		if (pgBgn != null && !"".equals(pgBgn)) {
			try {
				PageBeginPos = new Integer(pgBgn);
			} catch (Exception e) {
				errorMsg.append("起始日期输入错误,");
			}
		} else {
			logger.warn(SerialNo + " 起始位置为空,使用默认值 0 ");
			PageBeginPos = 0;
		}
		
		if (pgShwNum != null && !"".equals(pgShwNum)) {
			try {
				PageShoeNum = new Integer(pgShwNum);
			} catch (Exception e) {
				errorMsg.append("结束日期输入错误,");
			}

		} else {
			logger.warn(SerialNo + " 起始位置为空,使用默认值  10 ");
			PageShoeNum = 10;
		}
		
		
		if(errorMsg.length() != 0){
			logger.error(SerialNo + " 输入参数有误: " + errorMsg);
			throw new TxnException (errorMsg.toString());
		}
		
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		//sql.append("  ?        Acct            , ");
		sql.append("  a.CUST_NAME          AcctOfNm        , ");
		sql.append("  a.CERT_TYPE          DocTp           , ");
		sql.append("  a.CERT_ID            CrtfctNo        , ");
		sql.append("  b.SEQID              EvntSrlNo       , ");
		sql.append("  b.ACCT_ID_TRANS      CustAcctNO      , ");
		sql.append("  b.ACCT_SEQ_TRANS     AcctSeqNo       , ");
		sql.append("  b.ACCT_TYPE_TRANS    VoucherType     , ");
		sql.append("  b.TRANS_CBS_CD       CBSTransID      , ");
		sql.append("  b.TRANS_CD           TransID         , ");
		sql.append("  b.TRANS_CHL          ChannelID       , ");
		sql.append("  b.TRANS_DT           TransDt         , ");
		sql.append("  b.TRANS_TM           TransTime       , ");
		sql.append("  b.TRANS_AMT          TxnAmt          , ");
		sql.append("  b.BAL                Balce           , ");
		sql.append("  b.CT_FLG             DoNotAcct       , ");
		sql.append("  b.DC_FLG             DbAndCr         , ");
		sql.append("  b.CURR_ID            Ccy             , ");
		sql.append("  b.RE_FLG             CorrSign        , ");
		sql.append("  b.REMARKS            AbstRsm         , ");
		sql.append("  b.OPPOACCT           AcctNo          , ");
		sql.append("  b.TELLER             OprNum          , ");
		sql.append("  b.TRANS_ORG          TdgNtw          , ");
		sql.append("  b.TLR_SEQNO          TelleSrlNo      , ");
		sql.append("  b.REQIP              TransIP           ");
		sql.append("from  H_DEP_ACCT  A  ");
		sql.append(" left join  H_DEP_DETAIL B    ");
		sql.append("  on   a.SUB_ACCT_ID = b.SUB_ACCT_ID  ");
		sql.append(" where a.SUB_ACCT_ID = ? and b.SUB_ACCT_ID = ? and b.TRANS_DT >= ? and b.TRANS_DT <= ? ");
		sql.append(" order by b.TRANS_DT , b.TRANS_TM, EvntSrlNo ");
		String params[] = {AcctId, AcctId, StartDt, EndDt};
		queryResult = DbQuery.excuteQuery(sql.toString(), params, PageBeginPos, PageBeginPos+PageShoeNum);
		
		// 导出文件 
		
		ArrayList<DbDataLine> resultList = queryResult.getResultList();
		DbDataLine head = new DbDataLine();
		if( resultList.size() != 0 ){
			head = resultList.get(0);
		}
		QueryMessager Result = new QueryMessager(head, resultList);
		Result.resultHeadAdd("Acct", AcctId) ;
		Result.resultHeadAdd("TotlNm", queryResult.getTotalRows()+""); 
		Result.resultHeadAdd("RtrnNm", resultList.size()+"");
		Result.resultHeadAdd("STRDT", StartDt+"");
		Result.resultHeadAdd("ENDDT", EndDt+"");
		logger.info("完成查询 " + SerialNo + "的查询");
		
		return Result;
	}

}
