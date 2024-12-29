package com.lotus.homeDashboard.common.constants;

public enum Users {
	
	LOGIN("LGIUSR")
	;
	
	private String uid;

	private Users(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}
	
}
