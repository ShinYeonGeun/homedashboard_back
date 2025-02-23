package com.lotus.homeDashboard.cmn.usr.service;

import java.util.List;

import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;

public interface UserService {
	
	public DataMap<String, Object> doLogin(Request request);
	
	public DataMap<String, Object> extensionJWTPeriod(Request request);
	
	public DataMap<String, Object> inqUserList(Request request);
	
	public DataMap<String, Object> inqUserInfo(Request request);
	
	public List<DataMap<String, Object>> inqUserGrpList(Request params);
	
	public DataMap<String, Object> inqCntUser(Request request);
	
	public DataMap<String, Object> inqCntUserByUid(Request request);
	
	public DataMap<String, Object> createUser(Request request);
	
	public DataMap<String, Object> handleInvalidPassword(Request request);
	
	public DataMap<String, Object> updateUser(Request request);
	
	public DataMap<String, Object> deleteUser(Request request);
	
	public DataMap<String, Object> deleteManyUser(Request request);
}
