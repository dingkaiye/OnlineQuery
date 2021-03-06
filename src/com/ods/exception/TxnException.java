package com.ods.exception;

public class TxnException extends Exception {

	/**
	 * 表明此异常为交易级的错误
	 */
	private static final long serialVersionUID = 1L;

	/** 错误代码 */
	private String errorCode = "";

	public TxnException() {
		super();
	}

	public TxnException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public TxnException(String message) {
		super(message);
	}

	public TxnException(String message, Throwable cause) {
		super(message, cause);
	}

	public TxnException(Throwable cause) {
		super(cause);
	}

	protected TxnException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	

}
