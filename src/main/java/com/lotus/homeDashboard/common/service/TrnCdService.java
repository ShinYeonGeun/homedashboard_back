package com.lotus.homeDashboard.common.service;

import com.lotus.homeDashboard.common.component.ResultSet;

import jakarta.servlet.http.HttpServletRequest;

public interface TrnCdService {
	public ResultSet executeService(HttpServletRequest request);
}
