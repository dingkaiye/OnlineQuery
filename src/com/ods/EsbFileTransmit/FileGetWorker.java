package com.ods.EsbFileTransmit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.dcfs.esb.ftp.client.FtpClientConfigSet;
import com.dcfs.esb.ftp.client.FtpDir;
import com.dcfs.esb.ftp.client.FtpGet;
import com.dcfs.esb.ftp.server.error.FtpException;

/**
 * 完成最基本的文件操作工作
 * @author ding_kaiye
 * version 2017-11-18
 */
public class FileGetWorker {
	
	private final Logger logger = LogManager.getLogger("FileGetWorker");
	
	private FtpClientConfigSet configSet = null;
	
	/**
	 * 制定配置文件位置的构造方法 
	 * @param configFile
	 */
	public 	FileGetWorker(String configFile){
    	logger.info("读取  ftp 配置 开始" + configFile);
		configSet = new FtpClientConfigSet(configFile);
		logger.info("读取  ftp 配置 成功" + configFile);
    }
	
	public 	FileGetWorker(){
    	logger.info("读取  ftp 配置 开始");
		configSet = new FtpClientConfigSet();
		logger.info("读取  ftp 配置 成功");
    }
    
    
	/**
	 * 获取文件传输平台文件,完成最基本的文件获取工作
	 * 调用文件传输平台接口,获取文件传输平台中的单个文件
	 * @author ding_kaiye
	 * @param remotePath  远程文件所在目录
	 * @param localPath   本地保存目录
	 * @param dateStr    日期 YYYYMMDD
	 * @throws FtpException 
	 */
	
	// 本地文件与远程文件同名使用  
	public void getSignalFile(String localPath, String remotePath, String fileName) throws FtpException{
		getSignalFile(localPath, fileName, remotePath, fileName);
	}
	public void getSignalFile(String localPath, String localFileName, String remotePath, String remoteFileName) throws FtpException{
		
		FtpGet ftpGet = null;
		
		// 去除 结尾 的 "/"
		while( remotePath.endsWith("\\") || remotePath.endsWith("/")  ){
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		while( localPath.endsWith("\\") || localPath.endsWith("/")  ){
			localPath = localPath.substring(0, localPath.length() - 1);
		}
		
		String remoteFile = remotePath + "/" + remoteFileName;
		String localFile  = localPath  + "/" + localFileName ;
		
		boolean result = false;
		try {
			ftpGet = new FtpGet(remoteFile, localFile, configSet);
			logger.info("开始下载, 远程文件: " + remoteFile + " 本地文件" + localFile);
			result = ftpGet.doGetFile();
		} catch (FtpException e) {
			logger.error("文件 " + remoteFile + " 下载失败", e);
			throw e;
		}
		if (result == false) {
			throw new FtpException("文件 " + remoteFile + " 下载失败, 下载结果:" + result) ;
		}
		logger.info("文件下载成功,远程文件: " + remoteFile + " , 本地文件:" + localFile) ;
	}
	
	/**
	 * 调用文件传输平台接口, 从文件传输平台获取文件夹
	 * @author ding_kaiye
	 * @param localPath
	 * @param remotePath
	 * @param dateStr
	 * @return
	 * @throws FtpException
	 * @throws IOException
	 *  20180717 
	 */
	public void getDir(String localPath, String remotePath, String dateStr) throws FtpException, IOException {
		
		FtpDir ftpDir = null;
		
		while( remotePath.endsWith("\\") || remotePath.endsWith("/")  ){
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		while( localPath.endsWith("\\") || localPath.endsWith("/")  ){
			localPath = localPath.substring(0, localPath.length() - 1);
		}
		
		boolean result = false;
		try {
			ftpDir = new FtpDir();
			result = ftpDir.FtpDirGet(remotePath, localPath, configSet);
			logger.info("开始批量下载文件:" + remotePath + dateStr);
		} catch (FtpException e) {
			logger.error("远程文件夹 [ " + remotePath + " ] 日期 [" + dateStr + "] 下载失败, FtpException:", e);
			throw e;
		} catch (IOException e) {
			logger.error("远程文件夹 [ " + remotePath + " ] 日期 [" + dateStr + "] 下载失败, IOException:", e);
			throw e ;
		}
		if (result == false) {
			throw new FtpException("远程文件夹 [ " + remotePath + " ] 日期 [ " + dateStr + " ] 下载失败, 下载结果:" + result) ;
		}
		logger.info("文件夹下载成功,远程文件夹 [ " + remotePath + " ] 本地文件夹[" + localPath + "] 日期[" + dateStr + "]");
	}
	
	
	/**
     * 复制文件
     * @param fromFile
     * @param toFile
     * @throws IOException 
     */
    public void copyFile(File fromFile, File toFile) throws IOException {
        FileInputStream ins = new FileInputStream(fromFile);
        FileOutputStream out = new FileOutputStream(toFile);
        byte[] b = new byte[1024];
        int n = 0;
        while((n=ins.read(b))!=-1){
            out.write(b, 0, n);
        }
        ins.close();
        out.close();
    }
	
}
