package com.lotus.homeDashboard.common.exception;

import com.lotus.homeDashboard.common.utils.MessageUtil;

public class BizException extends RuntimeException {

	private static final long serialVersionUID = 4127710243634511833L;

	public BizException() {
		super();
	}
	
	public BizException(String message, String defaultMsg, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(MessageUtil.getMessage(message, defaultMsg), cause, enableSuppression, writableStackTrace);
	}
	
	public BizException(String message, String[] replaceStrs, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(MessageUtil.getMessage(message, replaceStrs), cause, enableSuppression, writableStackTrace);
	}

	public BizException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(MessageUtil.getMessage(message), cause, enableSuppression, writableStackTrace);
	}

	public BizException(String message, Throwable cause) {
		super(MessageUtil.getMessage(message), cause);
	}
	
	public BizException(String message, String[] replaceStrs, Throwable cause) {
		super(MessageUtil.getMessage(message, replaceStrs), cause);
	}
	
	public BizException(String message, String defaultMsg, Throwable cause) {
		super(MessageUtil.getMessage(message, defaultMsg), cause);
	}

	public BizException(String message, String[] replaceStrs) {
		super(MessageUtil.getMessage(message, replaceStrs));
		System.out.println("@@@ " + this.toString());
		System.out.println("@@@ " + this.getMessage());
	}
	
	public BizException(String message, String defaultMsg) {
		super(MessageUtil.getMessage(message, defaultMsg));
	}
	
	public BizException(String message) {
		super(MessageUtil.getMessage(message));
	}

	public BizException(Throwable cause) {
		super(cause);
	}
	
}
