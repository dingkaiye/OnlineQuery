package com.ods.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ods.log.OdsLog;
import com.ods.message.QueryResult;

/**
 * @author ding_kaiye
 * 提供操作数据库的接口
 */
public class DbQuery {
	private static Logger logger = OdsLog.getTxnLogger("DbQuery");
		
	/**
	 * 匿名参数查询 , 仅使用SQL查询需要总记录数时使用
	 * @param sql
	 * @param params
	 * @return ArrayList
	 * @throws SQLException 
	 * 使用匿名参数查询数据库,若没有参数, params 赋值 null 
	 */
	public static QueryResult excuteQuery(String sql) throws SQLException {
		return  excuteQuery(sql, null);
	}
	
	/**
	 * 使用匿名参数查询数据库,若没有参数, params 赋值  null 
	 * @param sql        SQL语句
	 * @param params     参数列表
	 * @param TotleRows  用于记录本次查询数据的总记录数
	 * @return ArrayList 本次查询数据的记录
	 * @throws SQLException
	 */
	public static QueryResult excuteQuery(String sql, Object[] params) throws SQLException  {
		
		ResultSet queryResult = null; 
		PreparedStatement queryStatement = null;
		Connection conn = null;
		int totleRows = 0;
		
		if (logger.isInfoEnabled()) {
			logger.info("查询开始,SQL[" + sql + "] 入参[" + params + "] " );
			for(Object ob : params) {
				logger.info(" 入参[" + ob.toString() + "]");
			}
		}
		
		conn = DbPool.getConnection();		//获取数据库连接
		queryStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				queryStatement.setObject(i + 1, params[i]);
			}
		}
		
		ArrayList<DbDataLine> resultList = new ArrayList<DbDataLine>();
		try {
			queryResult = queryStatement.executeQuery();
			queryResult.last();
			totleRows = queryResult.getRow();
			logger.debug(totleRows);
			queryResult.beforeFirst();
			
			while (queryResult.next()) {
				DbDataLine dbDataLine = new DbDataLine(queryResult);
				resultList.add(dbDataLine);
			}
		} catch (Exception e) {
			logger.error("excuteQuery Error:" + e.getMessage());
			throw e;
		} finally {
			// 释放资源连接池
			DbPool.closeConnection(queryResult, queryStatement, conn);
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("查询完成,SQL[" + sql + "] 入参[" + params + "] " );
		}
		return new QueryResult(resultList, totleRows);
	}

	/**
	 * 根据开始行和结束行数实现查询
	 * @param sql
	 * @param params
	 * @param startKey (含)
	 * @param endKey   (含)
	 * @return
	 * @throws Exception
	 */
	public static QueryResult excuteQuery(String sql, Object[] params, int startKey, int pageSize) throws SQLException {
//		public static ArrayList<DbDataLine> excuteQuery(String sql, Object[] params, long startKey, long endKey ) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("查询开始,SQL[" + sql + "] startKey[" + startKey + "] pageSize[" + pageSize + "]" );
			for(Object ob : params) {
				logger.info(" 入参[" + ob.toString() + "]");
			}
		}
		ResultSet queryResult = null; 
		PreparedStatement queryStatement = null;
		Connection conn = null;
		int totleRows = 0;
		int endKey = startKey + pageSize ;
		 
		conn = DbPool.getConnection();		//获取数据库连接
		queryStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				queryStatement.setObject(i + 1, params[i]);
			}
		}
		
		ArrayList<DbDataLine> resultList = new ArrayList<DbDataLine>();
		try {
			queryResult = queryStatement.executeQuery();
			
			queryResult.last();
			totleRows = queryResult.getRow();
			queryResult.beforeFirst();
			
			startKey = (startKey  <= 0 ? 1 : startKey );
			endKey = (endKey <= totleRows ?  endKey : totleRows);
			
			// 移动到 startKey 
			if(startKey > 1){
				queryResult.absolute(startKey -1 );
			}
			
			while (queryResult.next() && (queryResult.getRow() <= endKey) ) {
				DbDataLine dbDataLine = new DbDataLine(queryResult);
				resultList.add(dbDataLine);
			}
		} catch (SQLException e) {
			logger.error("excuteQuery Error:" + e.getMessage());
			throw e;
		} finally {
			// 释放资源连接池
			DbPool.closeConnection(queryResult, queryStatement, conn);
		}
		
		if (logger.isInfoEnabled()) {
			logger.info("查询完成,SQL[" + sql + "] startKey[" + startKey + "] endKey[" + endKey + "] pageSize[" + pageSize + "]");
		}
		return new QueryResult(resultList, totleRows);
	}
	
	
	/** 
	 * 数据库数据读入后 释放数据库连接, 
	 * 使用 ResultSet不能显式的关闭数据库链接 , 连接的关闭不受控制  
	 * 不启用 返回值类型为 ResultSet 的方法, 但保留此处代码
	 * 20180717  
	 **/ 
	
//	/**
//	 * 返回  ResultSet 的查询结果 
//	 * @param sql
//	 * @param params
//	 * @return
//	 * @throws SQLException
//	 */
//		public static ResultSet excuteQueryDb(String sql, Object[] params) throws SQLException {
//			
//			ResultSet queryResult = null; 
//			PreparedStatement queryStatement = null;
//			Connection conn = null;
//			
//			if (logger.isInfoEnabled()) {
//				logger.info("查询开始,SQL[" + sql + "] 入参[" + params + "] " );
//				for(Object ob : params) {
//					logger.info(" 入参[" + ob.toString() + "]");
//				}
//			}
//			
//			conn = DbPool.getConnection();		//获取数据库连接
//			queryStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//			
//			if (params != null) {
//				for (int i = 0; i < params.length; i++) {
//					queryStatement.setObject(i + 1, params[i]);
//				}
//			}
//			
//			try {
//				queryResult = queryStatement.executeQuery();
//			} catch (Exception e) {
//				logger.error("excuteQuery Error:" + e.getMessage());
//				throw e;
//			} finally {
//				// 释放资源连接池
//				DbPool.closeConnection(queryResult, queryStatement, conn);
//			}
//			
//			if (logger.isInfoEnabled()) {
//				logger.info("查询完成,SQL[" + sql + "] " );
//			}
//			
//			return queryResult ; 
//		}
//		
//		/**
//		 * 未测试, 暂不使用 20180717 
//		 * @param sql
//		 * @param paramsMap
//		 * @return
//		 * @throws SQLException
//		 */
//	public static ResultSet excuteQueryDb(String sql, Map<String, String> paramsMap) throws SQLException {
//
//		if (logger.isInfoEnabled()) {
//			logger.info("查询 SQL : [" + sql + "]");
//		}
//		// 遍历MAP
//		for (Entry<?, ?> entry : paramsMap.entrySet()) {
//			String key = (String) entry.getKey();
//			String value = (String) entry.getValue();
//
//			// 替换 参数 数值
//			sql = sql.replaceAll(":" + key + ":", value);
//			if (logger.isInfoEnabled()) {
//				logger.info(key + ":" + value + "替换完成");
//			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("SQL NOW IS :" + sql);
//			}
//		}
//		if (logger.isInfoEnabled()) {
//			logger.info("查询开始,SQL : [" + sql + "]");
//		}
//		ResultSet queryResult = null;
//		PreparedStatement queryStatement = null;
//		Connection conn = null;
//			
//		conn = DbPool.getConnection(); // 获取数据库连接
//		queryStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//
//		try {
//			queryResult = queryStatement.executeQuery();
//		} catch (Exception e) {
//			logger.error("excuteQuery Error:" + e.getMessage());
//			throw e;
//		} finally {
//			// 释放资源连接池
//			DbPool.closeConnection(queryResult, queryStatement, conn);
//		}
//		if (logger.isInfoEnabled()) {
//			logger.info("查询完成,SQL : [" + sql + "]");
//		}
//
//		return queryResult;
//	}
		
		
}
