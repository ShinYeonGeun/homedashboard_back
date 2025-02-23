package com.lotus.homeDashboard.common.constants;

public enum Keys {
	  COM_HEADER("commonHeader")
	, TRN_CD("trnCd")
	, AUTHORIZATION("Authorization")
	, ACCESS_TOKEN("Access-Token")
	;
	
	private String key;

	public String getKey() {
		return key;
	}

	private Keys(String key) {
		this.key = key;
	}
	
}
