package com.lotus.homeDashboard.common.service;

import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;

import jakarta.servlet.http.HttpServletRequest;

public interface TrnCdService {
	
	public ResultSet executeService(HttpServletRequest request);
	
	public DataMap<String, Object> inqTrnCdList(Request request);
	
	public DataMap<String, Object> inqCntTrnCd(Request request);
	
	public DataMap<String, Object> createTrnCd(Request request);
	
	public DataMap<String, Object> updateTrnCd(Request request);
	
	public DataMap<String, Object> deleteTrnCd(Request request);
	
	public DataMap<String, Object> deleteManyTrnCd(Request request);
	
	public DataMap<String, Object> inqTranByGroupPermissions(Request request);
	
	public DataMap<String, Object> saveTranByGroupPermissions(Request request);
}
