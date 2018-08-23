package com.ods.transaction.FemTransDetailFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.dcfs.esb.ftp.server.error.FtpException;
import com.ods.EsbFileTransmit.FilePutWorker;
import com.ods.db.DbDataLine;
import com.ods.db.DbQuery;
import com.ods.exception.TxnException;
import com.ods.log.OdsLog;
import com.ods.manager.TxnConfigManager;
import com.ods.message.QueryMessager;
import com.ods.message.QueryResult;
import com.ods.transaction.ITransaction;
import com.ods.transaction.TxnBody;

public class FemTransDetailFile  implements ITransaction {

	Logger logger = OdsLog.getTxnLogger("FemTransDetailFile");
	private final String txnName = "FemTransDetailFile" ; 
	@Override
	public QueryMessager transaction(TxnBody txnBody, String SerialNo) throws TxnException, SQLException {
		
		logger.info("开始执行 " + SerialNo + "的查询");
		ReqBody reqbody = null;
		// 判断是否是所需body 
		if( (txnBody instanceof ReqBody) != true){
			throw  new TxnException(" 请求 Body 类型错误");
		}
		
		reqbody = (ReqBody) txnBody;
		
		String AcctId  =  reqbody.getAcctAcc() ;  //账号
		String StartDt =  reqbody.getStrDt() ;   // 开始日期
		String EndDt   =  reqbody.getEndDt();      // 结束日期
		String pgBgn   =  reqbody.getPgBgn();      //起始位置
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
		
		
		// 读取 交易配置  
		
		Properties txnProperties = null;
		try {
			txnProperties = TxnConfigManager.getTxnConfig(txnName) ;
		} catch (Exception e) {
			logger.error(txnName + "流水号" + SerialNo + "获取交易配置出错" , e); 
			throw new TxnException (txnName + "流水号" + SerialNo + "获取交易配置出错");
		}
		if (txnProperties == null) {
			logger.error("流水号" + SerialNo + "对应的交易" + txnName +  "不存在" ); 
			throw new TxnException ("流水号" + SerialNo + "对应的交易" + txnName +  "不存在");
		}
		
		
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
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
		
		QueryResult queryResult = null;
		queryResult = DbQuery.excuteQuery(sql.toString(), params, PageBeginPos, PageBeginPos+PageShoeNum);
		
		
		// 导出路径   
		String localPath  = txnProperties.getProperty("localPath");
		String remotePath = txnProperties.getProperty("remotePath");	
		// 导出文件 
		String fileName = AcctId + StartDt + EndDt + pgBgn + pgShwNum + SerialNo ;

		String fileFullName = localPath + "/" + fileName ;
		// 数据导出到文件  
		// 创建文件夹
		File file = new File(fileFullName) ;
		File filePath = file.getParentFile();
		if( ! filePath.exists() ) {
			filePath.mkdirs();
		}
		// 文件存在, 删除 
		if(file.exists()) {
			file.delete();
		}
				
	    FileWriter writer = null;
	    ArrayList<DbDataLine> resultList = queryResult.getResultList();
		try {
			//writer = new FileWriter(fileFullName);
			writer = new FileWriter(file);
			StringBuffer sbfHead = new StringBuffer();  // 标题 
			StringBuffer sbfBody = new StringBuffer();  // 正文
			String splitStr = "|+|" ;
			// 写出文件头数据
			writer.write("起始时间|+|截止时间|+|币种|+|借贷标志\n");
			DbDataLine dbDataLine = null ;
			
			sbfHead.append(StartDt).append(splitStr);
			sbfHead.append(EndDt).append(splitStr);
			sbfHead.append(StartDt).append(splitStr);

			if (resultList != null && resultList.size() != 0) {
				dbDataLine = resultList.get(1);
				sbfHead.append(dbDataLine.get("Ccy")).append(splitStr);
				sbfHead.append(dbDataLine.get("DbAndCr")).append(splitStr);
			}else {
				sbfHead.append("").append(splitStr);
				sbfHead.append("").append(splitStr);
			}
			writer.write(sbfHead.toString());
			// 写正文数据  
			
			String value = null;
			writer.write("柜员流水|+|交易日期|+|交易时间|+|交易描述|+|交易渠道|+|凭证种类|+|凭证号码|+|币种|+|贷方金额|+|借方金额|+|账户余额|+|对方账号|+|对方行名|+|IP地址\n ");
			for (DbDataLine dbDataLineCur : resultList) {
				// 遍历本行数据 // 交易数据未定, 暂全部遍历
				int totColNum = dbDataLineCur.getColumnCount();
				for(int i=1; i<=totColNum; i++) {
					Object ob = dbDataLineCur.get(i);
					if (ob != null) {
						value = ob.toString();
					} else {
						value = "";
					}
					
					if (value.contains(splitStr)) {
						sbfBody.append("\"").append(value).append("\"").append(splitStr);
					}else {
						sbfBody.append(value).append(splitStr);
					}
				}
				sbfBody.append("\n");
			}
			writer.write(sbfBody.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("流水号" + SerialNo +"生成文件时出错" + fileFullName , e);
			throw new TxnException ("流水号" + SerialNo +"生成文件时出错" + e.getMessage()) ;
		}
		logger.info("流水号" + SerialNo + "生成文件完成" + fileFullName);
		// 发送文件到 文件交换平台 
		
		try {
			FilePutWorker filePetWorker = new FilePutWorker();
			filePetWorker.putSignalFile(localPath, remotePath, fileName);
		} catch (FtpException e) {
			logger.error("流水号" + SerialNo +"传送文件到ESB出错" + fileFullName , e);
			throw new TxnException ("流水号" + SerialNo +"传送文件到ESB出错" + e.getMessage()) ;
		}
		logger.info("流水号" + SerialNo +"上传文件完成" + fileFullName);
		// 组织返回报文中使用的信息 
		resultList = queryResult.getResultList();
		DbDataLine head = new DbDataLine();
		if( resultList.size() != 0 ){
			head = resultList.get(0);
		}
		QueryMessager Result = new QueryMessager(head, resultList);
		Result.resultHeadAdd("FileName", remotePath + "/" + fileName) ;
		Result.resultHeadAdd("Acct", AcctId) ;
		Result.resultHeadAdd("TotlNm", queryResult.getTotalRows()+""); 
		Result.resultHeadAdd("RtrnNm", resultList.size()+"");
		Result.resultHeadAdd("STRDT", StartDt+"");
		Result.resultHeadAdd("ENDDT", EndDt+"");
		logger.info("完成查询 " + SerialNo + "的查询");
		
		return Result;
	}
}
