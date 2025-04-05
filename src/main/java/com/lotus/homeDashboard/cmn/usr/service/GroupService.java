package com.lotus.homeDashboard.cmn.usr.service;

import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;

public interface GroupService {

	public DataMap<String, Object> inqGroupList(Request request);
	
	public DataMap<String, Object> createGroup(Request request);
	
	public DataMap<String, Object> updateGroup(Request request);
	
	public DataMap<String, Object> deleteGroup(Request request);
	
	public DataMap<String, Object> deleteManyGroup(Request request);
}
