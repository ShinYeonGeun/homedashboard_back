package com.lotus.homeDashboard.cmn.usr.dao;


public interface PasswordErrorHistoryDslRepository {

	public Integer findMaxSeqByTrnDtAndUid(String trnDt, String uid);
	
}
