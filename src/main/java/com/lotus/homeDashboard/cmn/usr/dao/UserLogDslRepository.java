package com.lotus.homeDashboard.cmn.usr.dao;

public interface UserLogDslRepository {

	public Integer findMaxByTrnDtAndUid(String trnDt, String uid);
}
