package com.ods.EsbFileTransmit;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.dcfs.esb.ftp.client.FtpDir;
import com.dcfs.esb.ftp.client.FtpGet;
import com.dcfs.esb.ftp.client.FtpPut;
import com.dcfs.esb.ftp.client.config.FtpClientConfigSet;
import com.dcfs.esb.ftp.client.util.FtpZipUtil;
import com.dcfs.esb.ftp.server.error.FtpException;

/**
 * 完成最基本的文件操作工作
 * @author ding_kaiye 
 * version 2018-07-17
 */
public class FilePutWorker {

	private static final Logger logger = LogManager.getLogger("TxnLoger");

	/**
	 * 获取文件传输平台文件,完成最基本的文件工作 调用文件传输平台接口,获取文件传输平台中的单个文件
	 */
	
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
	public static void putSignalFile(String localPath, String remotePath, String fileName ) throws FtpException {
		putSignalFile(localPath, fileName, remotePath, fileName );
	}
	public static void putSignalFile(String localPath, String localFileName, String remotePath, String remoteFileName ) throws FtpException {
		
		// 去除 结尾 的 "/"
		while (remotePath.endsWith("\\") || remotePath.endsWith("/")) {
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		while (localPath.endsWith("\\") || localPath.endsWith("/")) {
			localPath = localPath.substring(0, localPath.length() - 1);
		}
		String remoteFile = remotePath + "/" + remoteFileName;
		String localFile  = localPath + "/" + localFileName ;
		
		FtpPut ftp = null;
		// 读取配置文件
		FtpClientConfigSet configSet = new FtpClientConfigSet();

		// 上传单个文件 
		try {
			ftp = new FtpPut(localFile, remoteFile, configSet);
			logger.info("开始上传, 本地文件:" + localFile + " 远程文件:" + remoteFile );
			String fileResultStr = ftp.doPutFile();
			logger.info("文件上传完成: " + fileResultStr);
			
		} catch (FtpException e) {
			logger.error("文件[" + localFile + "]上传失败！" + e);
			throw e ;
		}
		
		logger.info("文件上传完成, 本地文件:" + localFile + " 远程文件:" + remoteFile );
	}


 	/**
 	 * 文件夹上传不通知
 	 * @param localPath
 	 * 		文件夹本地路径
 	 * @param remotePath
 	 * 		文件夹在ESB文件服务器上的路径
 	 * @throws FtpException 
 	 */
 	public static void putDir(String localPath, String remotePath, String dateNowStr) throws FtpException {
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
 
 		FtpClientConfigSet configSet = null;
 		// 调用批量上传方法
 		try {
 			configSet = new FtpClientConfigSet();
 			String str = ftpDir.FtpDirPut(localPath, remotePath, dateNowStr, configSet);
 			logger.info("上传至文件夹[" + str.substring(0, str.lastIndexOf("/")) + "]成功");
 		} catch (FtpException e) {
 			logger.error("文件[" + localPath + "]上传失败！" + e);
 			throw e;
 		}
 	}
	
	
}
