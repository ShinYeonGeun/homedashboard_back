package com.lotus.homeDashboard.cmn.usr.dao;

public interface UserLogDslRepository {

	public Integer findMaxByTranDtAndUid(String tranDt, String uid);
}
