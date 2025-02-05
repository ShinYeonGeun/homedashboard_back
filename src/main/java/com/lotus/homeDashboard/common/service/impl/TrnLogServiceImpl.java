package com.lotus.homeDashboard.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.constants.REQ_RES_DSTCD;
import com.lotus.homeDashboard.common.dao.TrnLogRepository;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.service.TrnLogService;
import com.lotus.homeDashboard.common.utils.CommonUtil;

import lombok.extern.slf4j.Slf4j;

@Service("TrnLogService")
@Slf4j
public class TrnLogServiceImpl implements TrnLogService {
	
	@Autowired
	private TrnLogRepository trnLogRepository;

	@Override
	public TrnLogEntity saveTrnLogWithEntity(TrnLogEntity entity) {
		return trnLogRepository.saveAndFlush(entity);
	}

	@Override
	public TrnLogEntity saveTrnLog(Request request) {
		TrnLogEntity logEntity = null;
		CommonHeader header = null;
		DataMap<String, Object> params = null;
		
		header = request.getHeader();
		params = request.getParameter();
		
		logEntity = new TrnLogEntity();
		logEntity.setUuid(header.getUuid());
		logEntity.setReqResDstcd(params.getString("reqResDstcd"));
		logEntity.setTrnCd(header.getTrnCd());
		logEntity.setUid(header.getTrnUserId());
		logEntity.setUri(header.getRequestUri());
		logEntity.setIp(header.getRequestIp());
		logEntity.setContent(params.getString("content"));
		logEntity.setResultCode(params.getString("resultCode"));
		return trnLogRepository.saveAndFlush(logEntity);
	}

}
