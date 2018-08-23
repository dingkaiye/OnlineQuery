package com.ods.EsbFileTransmit;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.dcfs.esb.ftp.client.FtpClientConfigSet;
import com.dcfs.esb.ftp.client.FtpDir;
import com.dcfs.esb.ftp.client.FtpPut;
import com.dcfs.esb.ftp.server.error.FtpException;

/**
 * 完成最基本的文件操作工作
 * @author ding_kaiye 
 * version 2018-07-17
 */
public class FilePutWorker {

	private final Logger logger = LogManager.getLogger("TxnLoger");

	/**
	 * 获取文件传输平台文件,完成最基本的文件工作 调用文件传输平台接口,获取文件传输平台中的单个文件
	 */
	private FtpClientConfigSet configSet = null;
	
	/**
	 * 制定配置文件位置的构造方法 
	 * @param configFile
	 */
	public 	FilePutWorker(String configFile){
    	logger.info("读取  ftp 配置 开始" + configFile);
		configSet = new FtpClientConfigSet(configFile);
		logger.info("读取  ftp 配置 成功" + configFile);
    }
	
	public 	FilePutWorker(){
    	logger.info("读取  ftp 配置 开始");
		configSet = new FtpClientConfigSet();
		logger.info("读取  ftp 配置 成功");
    }
    
    /**
	 * 单个文件上传不通知
	 * @param localPath
	 * 		文件夹本地路径
	 * @param remotePath
	 * 		文件夹在ESB文件服务器上的路径
	 * @throws FtpException 
	 * @Param fileName
	 *      文件名称
	 */
	// 本地文件与远程文件同名使用 
	public void putSignalFile(String localPath, String remotePath, String fileName ) throws FtpException {
		putSignalFile(localPath, fileName, remotePath, fileName );
	}
	public void putSignalFile(String localPath, String localFileName, String remotePath, String remoteFileName ) throws FtpException {
		
		// 去除 结尾 的 "/"
		while (remotePath.endsWith("\\") || remotePath.endsWith("/")) {
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		while (localPath.endsWith("\\") || localPath.endsWith("/")) {
			localPath = localPath.substring(0, localPath.length() - 1);
		}
		String remoteFile = remotePath + "/" + remoteFileName;
		String localFile  = localPath + "/" + localFileName ;
		
		// 上传单个文件 
		try {
			logger.info("开始上传, 本地文件:" + localFile + " 远程文件:" + remoteFile );
			FtpPut ftp = new FtpPut(localFile, remoteFile, configSet);
			String fileResultStr = ftp.doPutFile();
			logger.info("文件上传完成: " + fileResultStr);
			
		} catch (FtpException e) {
			logger.error("文件 [ " + localFile + " ] 上传失败！" + e);
			throw e ;
		}
		
		logger.info("文件上传完成, 本地文件:" + localFile + " 远程文件:" + remoteFile );
	}


 	/**
 	 * 文件夹上传, 不发通知
 	 * @param localPath
 	 * 		文件夹本地路径
 	 * @param remotePath
 	 * 		文件夹在ESB文件服务器上的路径
 	 * @throws FtpException 
 	 */
 	public void putDir(String localPath, String remotePath, String dateNowStr) throws FtpException {
 		FtpDir ftpDir = new FtpDir();
 
 		if(dateNowStr == null || "".equals(dateNowStr)){
 			Date date = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 			dateNowStr = sdf.format(date);
 		}
 		
 		// 如果 日期为空 , 生成被日 日期字符串 
 		if(dateNowStr == null || "".equals(dateNowStr)) {
 			Date date = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
 			dateNowStr = sdf.format(date);
 		}
 
 		// 调用批量上传方法
 		try {
 			logger.info("开始上传, 本地文件夹:" + localPath + " 远程文件:" + remotePath );
 			String fileResultStr = ftpDir.FtpDirPut(localPath, remotePath, configSet);
 			logger.info("文件上传完成: " + fileResultStr);
 		} catch (FtpException e) {
 			logger.error("文件[" + localPath + "]上传失败！" + e);
 			throw e;
 		}
 	}
	
	
}
