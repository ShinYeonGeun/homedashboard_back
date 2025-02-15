package com.lotus.homeDashboard.cmn.usr.dao;


public interface PasswordErrorHistoryDslRepository {

	public Integer findMaxByTrnDtAndUid(String trnDt, String uid);
	
}
