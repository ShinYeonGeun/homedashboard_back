package com.lotus.homeDashboard.cmn.usr.dao;

import java.util.List;

import com.lotus.homeDashboard.common.component.DataMap;
import com.querydsl.core.Tuple;

public interface UserGroupDslRepository {
	
	public List<Tuple> findUserGroupJoinGrpInfo(String uid, String delYn);

}
