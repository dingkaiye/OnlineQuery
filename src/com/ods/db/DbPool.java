package com.ods.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.ods.common.Config;
import com.ods.log.OdsLog;

/**
 * 数据库连接池
 * @author ding_kaiye
 *
 */
public class DbPool {
	private static DruidDataSource dataSource = null;
	private static Logger logger = OdsLog.getTxnLogger("DbPool");
	
	private DbPool() {
		
	}
	
	public static void init() {
		try {
			dataSource = getDataSource();
			dataSource.init();
		} catch (Exception e) {
			logger.error("数据库连接池建立失败,稍后连接时将重试", e);
		}
		
		logger.info("测试获取数据库连接 开始");
		DruidPooledConnection connnection = null;
		try {
			connnection = dataSource.getConnection();
			if( connnection != null ) {
				logger.info("测试获取数据库连接 成功");
			}
		} catch (SQLException e) {
			logger.error("测试获取数据库连接 失败, 新查询时将重试", e);
		} finally {
			closeConnection(connnection);
		}
	}
	
	/**
	 * @author ding_kaiye
	 * @return DruidPooledConnection
	 * @throws Exception
	 * 获取数据库连接, 成功则返回数据库连接, 失败返回 null
	 */
	public static DruidPooledConnection  getConnection() {
		if ( dataSource == null ) {
			logger.info("数据库连接池不存在, 重新建立数据库连接池");
			try {
				dataSource = getDataSource();
			} catch (Exception e) {
				logger.error("数据库连接池建立失败,稍后连接时将重试", e);
				return null;
			}
			logger.info("数据库连接池重新建立完成");
		}
		if(logger.isDebugEnabled()) {
			logger.debug("获取数据库连接池连接开始");
		}
		DruidPooledConnection connnection = null;
		try {
			connnection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("获取数据库连接失败", e);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("获取数据库连接池连接完成");
		}
		return connnection;
	}

	
	//获取配置数据
	private static DruidDataSource getDataSource() throws Exception {
		DruidDataSource druidDataSource = null;
		Properties properties = Config.loadConfigPropertiesFile("druid.properties");
		if (properties != null) {
			String decrypt = properties.getProperty("decrypt");
			if (decrypt == null || !"false".equals(decrypt)) {
				String decryptPassword = null;
				String passWord = properties.getProperty("password");
				try {
					Properties sysProperties = Config.loadConfigPropertiesFile("SysConfig.properties");
					String publicKey = sysProperties.getProperty("publicKey");
					decryptPassword = com.ods.tools.RsaTools.decrypt(publicKey, passWord);
					logger.info("解密 数据库密码 [" + passWord + "] 完成 ");
				} catch (Exception e) {
					logger.error("解密 数据库密码 [" + passWord + "] 失败");
					throw e;
				}
				properties.put("password", decryptPassword);
			}
		}
		druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
		logger.info("数据库连接池建立完毕");
		return druidDataSource; 
	}
	
	/**
	 * 关闭数据库连接
	 * @param resultSet
	 * @param preparedStatement
	 * @param conn
	 */
	protected static void closeConnection (Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("closeConnection ERROR: " + e.getMessage());
			}
		}
		logger.info("Close Connection Complate" );
	}
	protected static void closeConnection(ResultSet resultSet, PreparedStatement preparedStatement, Connection conn) {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		// 关闭PreparedStatement对象
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		// 关闭Connection 对象
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("closeConnection ERROR: " + e.getMessage());
			}
		}

	}
	
	
	protected static void closeConnection(PreparedStatement preparedStatement, Connection conn) {
//		if (resultSet != null) {
//			try {
//				resultSet.close();
//			} catch (SQLException e) {
//				logger.error(e.getMessage());
//			}
//		}
		// 关闭PreparedStatement对象
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
		}
		// 关闭Connection 对象
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("closeConnection ERROR: " + e.getMessage());
			}
		}

	}
	
}