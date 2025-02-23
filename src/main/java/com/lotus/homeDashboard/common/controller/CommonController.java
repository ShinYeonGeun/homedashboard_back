package com.lotus.homeDashboard.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.ResultCode;
import com.lotus.homeDashboard.common.service.TrnCdService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CommonController {
	
	
	@Autowired
	private TrnCdService trnCdService;
	
	@PostMapping("/callService")
	public ResultSet callService(HttpServletRequest request) throws Exception {
		log.debug("====================================================================================================");
		log.debug("callService 시작");
		log.debug("====================================================================================================");
		
		ResultSet rs = trnCdService.executeService(request);
		
		log.debug("====================================================================================================");
		log.debug("callService 종료");
		log.debug("====================================================================================================");
		
		return rs;
	}
	
	@PostMapping(Constants.HEALTH_CHECK_URI)
	public ResultSet healthCheck(HttpServletRequest request) throws Exception {
		log.debug("HealthCheck 성공");
		return new ResultSet(ResultCode.SUCCESS.getCode(), null, new Object[] {});
	}
}
