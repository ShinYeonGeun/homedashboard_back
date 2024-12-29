package com.lotus.homeDashboard.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "로그인 필요")
public class LoginException extends RuntimeException {

	private static final long serialVersionUID = 877328947123343551L;

	public LoginException() {
		super();
	}

	public LoginException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginException(String message) {
		super(message);
	}

	public LoginException(Throwable cause) {
		super(cause);
	}
	
	
	
}
