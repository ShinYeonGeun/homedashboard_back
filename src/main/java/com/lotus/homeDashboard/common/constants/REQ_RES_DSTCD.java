package com.lotus.homeDashboard.common.constants;

public enum REQ_RES_DSTCD {
	REQ("Q")
	, RES("S")
	;
	
	private String code;

	private REQ_RES_DSTCD(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
