package com.lotus.homeDashboard.common.constants;

/**
 * 서비스 결과 상태코드
 */
public enum ResultCode {
	SUCCESS("S")
	, ERROR("E")
	;
	
	private String code;

	private ResultCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
