package com.ods.manager;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.xml.ws.Endpoint;

import org.apache.cxf.jaxws.EndpointImpl;
//import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.log4j.Logger;

import com.ods.common.Config;
import com.ods.common.Constant;
import com.ods.db.DbPool;
import com.ods.exception.TxnException;
import com.ods.log.LogDateCheckService;
import com.ods.log.OdsLog;
import com.ods.service.AbstractService;
import com.ods.ws.ArtifactInInterceptor;
import com.ods.ws.ArtifactOutInterceptor;
import com.ods.ws.ESBWaiter;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;


public class InitSystem {
	
	private static Logger logger = OdsLog.getLogger("SysLog"); 
	
	public static void main (String[] args)  {
		try {
			
			// 初始化 队列 
			QueueManager.QueueInit(); 

			// 初始化数据库连接池
			DbPool.init();

			// 加载 TxnConfig
			TxnConfigManager.Init();

			// 启动日志日期监控
			Thread logDateCheckService = new LogDateCheckService();
			logDateCheckService.start();

			// 读取配置文件
			Properties SysConfig = null;
			try {
				SysConfig = Config.getPropertiesConfig(Constant.SysConfig);
			} catch (IOException e) {
				logger.error("启动失败, 读取队列配置文件" + Constant.SysConfig + "失败");
				throw e;
			}
			
			// 初始化系统交易量控制
			TxnCntContrl.init();
			
			// 启动并初始化 各交易服务 
			String servicesList = SysConfig.getProperty("ServicesList");
			if ("".equals(servicesList) || servicesList == null) {
				logger.error("ServicesList 配置为空或未配置, 系统初始化失败");
				throw new TxnException("ServicesList 配置为空或未配置, 系统初始化失败");
			}
			
			String className = null ; 
			int threadCnt = 0;
			String threadCntStr = null ;
			String inQueueName = null ;
			String nextQueueName = null ; 
			String failQueueName = null ; 
			AbstractService service = null ;
			String thredName = null; // 线程名
			
			String[] serviceNames = servicesList.split(","); // 按照 "," 分割参数
			for(String serviceName: serviceNames) {
				
				String serviceConfig = SysConfig.getProperty(serviceName + "Config");
				if ("".equals(servicesList) || servicesList == null) {
					logger.error("服务 " + serviceName + " 配置为空或未配置, 系统初始化失败");
					throw new TxnException("服务 " + serviceName + " 配置为空或未配置, 系统初始化失败");
				}
				logger.info(serviceName + "当前配置为:[" +  serviceConfig + "]");
				String config[] = serviceConfig.split(",");
				
				// 格式: 服务名 + Config = 实现类;线程数;输入队列;执行成功输出队列;执行失败输出队列;
				try {
					className = config[0];
					threadCntStr = config[1];
					inQueueName = (config.length>2 ? config[2]:null);
					nextQueueName = (config.length>3 ? config[3]:null);
					failQueueName = (config.length>4 ? config[4]:null);
				} catch (Exception e) {
					logger.error("解析" + Constant.SysConfig + "中 [" + serviceName + "Config] 配置出错, 请检查该项配置: [" + serviceConfig + "]"  );
					throw e ;
				}
				
				// 检查 服务实现类 设定 
				
				// 检查线程数 设定 
				if ("".equals(config[0]) || config == null) {
					logger.error(serviceName + "并发数 配置为空或未配置, 系统初始化失败");
					throw new TxnException("TxnService 配置为空或未配置, 系统初始化失败");
				}
				try {
					threadCnt = new Integer(threadCntStr);
				}catch (NumberFormatException e) {
					logger.error(serviceName + "并发数配置 格式错误, 系统初始化失败,当前格式为["+ threadCntStr +"]", e);
					throw new TxnException(serviceName + "并发数配置 格式错误, 系统初始化失败,当前格式为["+ threadCntStr +"]");
				}
				
				//  按照线程数 实例化服务 
				service = null;
				thredName = "";
				for(int i=0; i<threadCnt; i++) {
					try {
						service = (AbstractService) Class.forName(className).newInstance();
					} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
						logger.error("实例化服务" + serviceName + "出现异常", e);
						throw new TxnException ( "实例化服务" + serviceName + "出现异常" );
					}
					
					service.setName( serviceName + "-" + i);
					thredName = thredName + " " + service.getName();
					
					try {
						service.ServiceInit(inQueueName, nextQueueName, failQueueName) ;  //  初始化服务 
						service.start(); // 启动 线程 
					} catch (Exception e) {
						logger.error("初始化服务" + serviceName +":" + service.getName() + "出现异常", e);
						throw new TxnException ("初始化服务" + serviceName +":" + service.getName() + "出现异常" );
					}
				}
				logger.info(serviceName + " 启动成功, 启动线程数:" + threadCnt + " 线程名: " + thredName);
			}
			
			
/*	20180607 delete 增强配置文件的作用, 降低系统耦合程度 		
			// 读取服务线程数配置
//			String UnPackService = SysConfig.getProperty("UnPackService");
			String TxnService = SysConfig.getProperty("TxnService");
			String EsbPackService = SysConfig.getProperty("EsbPackService");
			String SendSuccMsgService = SysConfig.getProperty("SendSuccMsgService");
			String SendFailMsgService = SysConfig.getProperty("SendFailMsgService");

			// 检查服务线程数配置
//			if ("".equals(UnPackService) || UnPackService == null) {
//				logger.error("UnPackService 配置为空或未配置, 系统初始化失败");
//				throw new TxnException("UnPackService 配置为空或未配置, 系统初始化失败");
//			}
			if ("".equals(TxnService) || TxnService == null) {
				logger.error("TxnService 配置为空或未配置, 系统初始化失败");
				throw new TxnException("TxnService 配置为空或未配置, 系统初始化失败");
			}
			if ("".equals(EsbPackService) || EsbPackService == null) {
				logger.error("EsbPackService 配置为空或未配置, 系统初始化失败");
				throw new TxnException("EsbPackService 配置为空或未配置, 系统初始化失败");
			}
			if ("".equals(SendSuccMsgService) || SendSuccMsgService == null) {
				logger.error("SendSuccMsgService 配置为空或未配置, 系统初始化失败");
				throw new TxnException("SendSuccMsgService 配置为空或未配置, 系统初始化失败");
			}
			if ("".equals(SendFailMsgService) || SendFailMsgService == null) {
				logger.error("SendFailMsgService 配置为空或未配置, 系统初始化失败");
				throw new TxnException("SendFailMsgService 配置为空或未配置, 系统初始化失败");
			}

			// 实例化服务线程

			int threadcnt = 0;
			String thredName = null;

			threadcnt = new Integer(TxnService);
			thredName = "";
			for (int i = 0; i < threadcnt; i++) {
				TxnService service = new TxnService(Constant.TxnQueue, Constant.PackQueue);
				service.setName("TxnService-" + i);
				service.start();
				thredName = thredName + " " + service.getName();
			}
			logger.info("TxnService 启动成功, 启动线程数:" + threadcnt + " 线程名 " + thredName);

			threadcnt = new Integer(EsbPackService);
			thredName = "";
			for (int i = 0; i < threadcnt; i++) {
				EsbPackService service = new EsbPackService(Constant.PackQueue, Constant.SuccessQueue);
				service.setName("EsbPackService-" + i);
				service.start();
				thredName = thredName + " " + service.getName();
			}
			logger.info("EsbPackService 启动成功, 启动线程数:" + threadcnt + " 线程名 " + thredName);

			threadcnt = new Integer(SendSuccMsgService);
			thredName = "";
			for (int i = 0; i < threadcnt; i++) {
				SendSuccMsgService service = new SendSuccMsgService(Constant.SuccessQueue, null);
				service.setName("SendSuccMsgService-" + i);
				service.start();
				thredName = thredName + " " + service.getName();
			}
			logger.info("SendSuccMsgService 启动成功, 启动线程数:" + threadcnt + " 线程名: " + thredName);

			threadcnt = new Integer(SendFailMsgService);
			thredName = "";
			for (int i = 0; i < threadcnt; i++) {
				SendFailMsgService service = new SendFailMsgService(Constant.FailQueue, null);
				service.setName("SendFailMsgService-" + i);
				service.start();
				thredName = thredName + " " + service.getName();
			}
			logger.info("SendFailMsgService 启动成功, 启动线程数:" + threadcnt + " 线程名 " + thredName);
 */

			String wsUrl = SysConfig.getProperty("WebServiceUrl");
			if ("".equals(wsUrl) || wsUrl == null) {
				logger.error("WebServiceUrl 配置为空或未配置, 系统初始化失败");
				throw new TxnException("WebServiceUrl 配置为空或未配置, 系统初始化失败");
			}
			
			
			// 发布webservice
			try {
				ArtifactInInterceptor artifactInInterceptor = new ArtifactInInterceptor();
				ArtifactOutInterceptor artifactOutInterceptor = new ArtifactOutInterceptor();
				ESBWaiter waiter = new ESBWaiter();
				
				EndpointImpl endpointImpl = (EndpointImpl) Endpoint.publish(wsUrl, waiter); 
				//添加拦截器 
				endpointImpl.getInInterceptors().add(artifactInInterceptor);
				
				List<Interceptor<? extends Message>> inInterceptors =  endpointImpl.getOutInterceptors();
				inInterceptors.add(artifactOutInterceptor) ;
				//System.out.println(endpointImpl);
				
				logger.info("WebService发布完成, 地址:" + wsUrl);
			} catch (Exception e) {
				logger.error("WebService发布失败, 地址:" + wsUrl, e);
				throw new TxnException("WebService发布失败, 地址:" + wsUrl);
			}
			
			logger.info("系统启动成功");
		} catch (Exception e) {
			logger.error("系统启动失败, 进程退出", e);
			System.exit(-1);
		}

	}

}
