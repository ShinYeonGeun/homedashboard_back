package com.lotus.homeDashboard.common.service;

import java.util.List;

import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;

public interface TrnLogService {
	
	public TrnLogEntity saveTrnLogWithEntity(TrnLogEntity entity);
	
	public TrnLogEntity saveTrnLog(Request request);
	
	public DataMap<String, Object> inqTrnLogList(Request request);
}
