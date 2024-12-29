package com.lotus.homeDashboard.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lotus.homeDashboard.common.dao.TrnLogRepository;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.service.TrnLogService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrnLogServiceImpl implements TrnLogService {
	
	@Autowired
	private TrnLogRepository trnLogRepository;

	@Override
	public TrnLogEntity saveTrnLog(TrnLogEntity entity) {
		return trnLogRepository.saveAndFlush(entity);
	}

}
