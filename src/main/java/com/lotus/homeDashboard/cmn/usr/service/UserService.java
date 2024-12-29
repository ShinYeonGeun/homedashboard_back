package com.lotus.homeDashboard.cmn.usr.service;

import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;

public interface UserService {
	public DataMap<String, Object> doLogin(Request params);
	
	public DataMap<String, Object> extensionJWTPeriod(Request params);
	
	public DataMap<String, Object> inqUserList(Request params);
}
