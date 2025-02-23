package com.lotus.homeDashboard.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.constants.REQ_RES_DSTCD;
import com.lotus.homeDashboard.common.constants.ResultCode;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.service.TrnLogService;
import com.lotus.homeDashboard.common.utils.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	@Autowired
	private TrnLogService trnLogService;
	
	@ExceptionHandler(BizException.class)
	public ResponseEntity<ResultSet> handleBizExceptionException(BizException e, HttpServletRequest request) {
		ContentCachingRequestWrapper req = null; 
        ResultSet rs = new ResultSet();
        CommonHeader header = null;
        TrnLogEntity logEntity = null;

		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        log.debug("__DBGLOG__ handleBizExceptionException Start");
        log.debug("__DBGLOG__ handleBizExceptionException BizException", e);
        //===================================================================================
		// Request 캐싱
		//===================================================================================
        req = (ContentCachingRequestWrapper) request;
        
        //===================================================================================
		// 공통헤더 생성
		//===================================================================================
        header = (CommonHeader) req.getAttribute(Keys.COM_HEADER.getKey());
		
		//===================================================================================
    	// 거래로그 저장
    	//===================================================================================
    	logEntity = new TrnLogEntity();
    	logEntity.setReqResDstcd(REQ_RES_DSTCD.RES.getCode());
    	logEntity.setResultCode(ResultCode.ERROR.getCode());
    	logEntity.setUri(header.getRequestUri());
		logEntity.setIp(header.getRequestIp());
		logEntity.setTrnCd(header.getTrnCd());
		logEntity.setTrnDtm(header.getCurrDtm());
		logEntity.setUid(header.getTrnUserId());
		logEntity.setUuid(header.getUuid());
        e.printStackTrace(pw);
        
		logEntity.setContent(sw.toString());
		trnLogService.saveTrnLogWithEntity(logEntity);
		
		//===================================================================================
		// 응답 조립
		//===================================================================================
        rs.setResultCd(ResultCode.ERROR.getCode());
        rs.setPayload(e.getMessage());
        rs.setUuid(header.getUuid());
        
        log.debug("__DBGLOG__ handleBizExceptionException End");
        
        return ResponseEntity.ok(rs);
    }
}
