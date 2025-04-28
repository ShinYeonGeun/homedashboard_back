package com.lotus.homeDashboard.common.dao;

import java.util.List;

import com.querydsl.core.Tuple;

public interface TrnCdDslRepository {
	
	public List<Tuple> findAllSvcMthByGrpPermissions(String grpCd, String delYn);
	
	public long findCountTrnGroupAuth(String trnCd, String uid);
	
	public int findMaxTrnAuthLogSeq(String trnCd, String grpCd);
}
