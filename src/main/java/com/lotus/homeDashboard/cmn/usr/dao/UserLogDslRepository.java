package com.lotus.homeDashboard.cmn.usr.dao;

public interface UserLogDslRepository {

	public Integer findMaxSeqByTrnDtAndUid(String trnDt, String uid);
}
