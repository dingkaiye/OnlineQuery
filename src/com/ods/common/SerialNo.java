package com.ods.common;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SerialNo {
	
	private static long serialNo = 0;
	
	//private static Logger thisLogger = LogManager.getLogger("SysLog");  
	private static SimpleDateFormat dfTime = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
	//private static SimpleDateFormat dfTime = new SimpleDateFormat("yyyyMMdd"); //设置日期格式
	private static String currentDate = dfTime.format(new Date()).substring(0, 8); 
	
	private static DecimalFormat decimalFormat = new DecimalFormat("0000"); 
	private static String currentSerialNo = dfTime.format(new Date()) + "0000"; //初始流水

	private static final String odsSysCode = "0094";  //ODS系统代号
	private static final int maxSerialNo = 9999 ;
	
	//  交易总量计数 
	private static DecimalFormat totTxDecimalFormat = new DecimalFormat("0000000000"); 
	private static long totSerialNo = 0 ;
	//private static String strTotSerialNo = dfTime.format(new Date()) + "0000000000"; 
	
	/**
	 * 获取系统流水号
	 * 行方要求: 系统流水号22位,  系统编号(4位) + 交易日期(8位) + 编号。
	 * 数字每日归零
	 * @author ding_kaiye
	 * @return nextSerialNo
	 */
	public static synchronized String getNextSerialNo() {
		String nextSerialNo = null;
		final int dateLength = 8 ;
		String nowDate = dfTime.format(new Date());
		if (! (nowDate.substring(0, dateLength)).equals(currentDate)   // 换日 // 断言
				|| serialNo >= maxSerialNo  //  达到最大 
				|| Long.MAX_VALUE == serialNo ) {  
			currentDate = nowDate.substring(0, dateLength);
			serialNo = 1;
		} else {
			serialNo++;
		}
		
		if (! (nowDate.substring(0, dateLength)).equals(currentDate)   // 换日 // 断言
				|| Long.MAX_VALUE == totSerialNo ) {  
			totSerialNo = 1;
		} else {
			totSerialNo ++ ;
		}

		nextSerialNo = odsSysCode + nowDate + decimalFormat.format(serialNo);
		//strTotSerialNo = nowDate + totTxDecimalFormat.format(serialNo);
		//记录本流水号为当前流水号
		currentSerialNo = nextSerialNo; 
		
		return nextSerialNo;
	}

	public static String getCurrentDate() {
		return currentDate;
	}

	/**
	 * 获取当前系统流水号
	 * 流水号格式为 8位日期 ＋10位数字
	 * 数字每日归零
	 * @author ding_kaiye
	 * @return 系统当前流水号
	 */
	public static String getCurrentSerialNo() {
		return currentSerialNo;
	}


}
