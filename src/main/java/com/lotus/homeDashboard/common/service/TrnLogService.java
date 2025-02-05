package com.lotus.homeDashboard.common.service;

import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;

public interface TrnLogService {
	
	public TrnLogEntity saveTrnLogWithEntity(TrnLogEntity entity);
	
	public TrnLogEntity saveTrnLog(Request request);
}
