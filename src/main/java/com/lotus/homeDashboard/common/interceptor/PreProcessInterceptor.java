package com.lotus.homeDashboard.common.interceptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.constants.REQ_RES_DSTCD;
import com.lotus.homeDashboard.common.constants.ResultCode;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.exception.LoginException;
import com.lotus.homeDashboard.common.jwt.JWTProvider;
import com.lotus.homeDashboard.common.service.TrnLogService;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.MessageUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreProcessInterceptor implements HandlerInterceptor {
	
		
	@Autowired
	private TrnLogService trnLogService;
	
	@Autowired
	private JWTProvider jWTProvider;
	

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.debug("__DBGLOG__ 전체 선처리 시작");

		ContentCachingRequestWrapper req = null;
		TrnLogEntity logEntity = null;
		CommonHeader header = null;
		DecodedJWT tokenInfo = null;
		
		try {
			
			//===================================================================================
			// preflight 시 처리 안함
			//===================================================================================
			if(request.getMethod().equals(HttpMethod.OPTIONS.name())) {
				return true;
			}
			
			//===================================================================================
			// Body 재사용을 위해 Wrapping
			//===================================================================================
			req = (ContentCachingRequestWrapper) request;
			
			//===================================================================================
			// header 생성
			//===================================================================================
			header = this.createCommonHeader(request);
			req.setAttribute(Keys.COM_HEADER.getKey(), header);
			
			//===================================================================================
			// 로그에 UUID 추가
			//===================================================================================
			MDC.put("UUID", header.getUuid().toString());
			
			//===================================================================================
			// 거래로그 엔티티 조립
			//===================================================================================
			logEntity = new TrnLogEntity();
			logEntity.setUuid(header.getUuid());
			logEntity.setReqResDstcd(REQ_RES_DSTCD.REQ.getCode());
			logEntity.setTrnCd(header.getTrnCd());
			logEntity.setUri(req.getRequestURI());
			logEntity.setIp(CommonUtil.getRequestIP(req));
			logEntity.setContent(req.getContentAsString());
			
			//===================================================================================
			// 토큰 유효성검사
			//===================================================================================
			if(!StringUtil.isOrEquals(req.getRequestURI(), Constants.LOGIN_URI, Constants.HEALTH_CHECK_URI)) {
				
				//if(!accessToken.startsWith(Constants.AUTH)) {
				if(!Constants.AUTH.equals(header.getAuthorization())){
					throw new LoginException("val_invalid_auth");
				}
				
				tokenInfo = jWTProvider.validate(header.getAccessToken());
				
				if(tokenInfo == null) {
					throw new LoginException(MessageUtil.getMessage("login_need"));
				}
				
				response.setHeader(Keys.ACCESS_TOKEN.getKey(), header.getAccessToken());
			}
			
			
			//===================================================================================
			// 거래로그 엔티티 조립
			//===================================================================================
			logEntity.setUid(header.getTrnUserId());
			logEntity.setResultCode(ResultCode.SUCCESS.getCode());
			trnLogService.saveTrnLog(logEntity);
			
		}catch(JWTVerificationException e) {
			//JWTVerificationException
			String msgKey = "";
			if(e instanceof TokenExpiredException) {
				msgKey = "login_exp_token";
			} else {
				msgKey = "login_need";
			}
			log.error("__ERRLOR__ 전체 선처리 토큰 검증 오류", e);
			throw new LoginException(MessageUtil.getMessage(msgKey));
		}catch(Exception e) {
			log.error("__ERRLOR__ 전체 선처리 오류", e);
			logEntity.setResultCode(ResultCode.ERROR.getCode());
			logEntity.setContent(e.toString());
			trnLogService.saveTrnLog(logEntity);
    		throw e;
		}finally {
			
			log.debug("__DBGLOG__ 전체 선처리 종료");
		}
        return true;//HandlerInterceptor.super.preHandle(req, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    	log.debug("__DBGLOG__ 전체 후처리 시작");
        log.debug("Request URI ===> " + request.getRequestURI());
        
        log.debug("__DBGLOG__ 전체 후처리 종료");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
    
    /**
     *  요청 처리가 완료된 후에 실행    
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    	log.debug("__DBGLOG__ 전체 후처리 시작");
    	ContentCachingRequestWrapper req = null;
    	ContentCachingResponseWrapper res = null;
    	TrnLogEntity logEntity = null;
    	CommonHeader header = null;
    	ResultSet rs = null;
    	try {
    		req = (ContentCachingRequestWrapper) request;
    		res = (ContentCachingResponseWrapper) response;
    		
    		log.debug("__DBGLOG__ Request URI: [{}]", request.getRequestURI());
	    	log.debug("__DBGLOG__ Response Status: [{}]", res.getStatus());
	    	log.debug("__DBGLOG__ Response content: [{}]", new String(res.getContentAsByteArray()));
	    	
    	
	    	//===================================================================================
	    	// Body 재사용을 위해 Wrapping
	    	//===================================================================================
	    	if(res.getContentAsByteArray().length > 0) {
	    		rs = new ObjectMapper().readValue(res.getContentAsByteArray(), ResultSet.class);
	    	}else {
	    		rs = new ResultSet();
	    	}
	    	
	    	//===================================================================================
	    	// 거래로그 저장
	    	//===================================================================================
	    	logEntity = new TrnLogEntity();
	    	logEntity.setReqResDstcd(REQ_RES_DSTCD.RES.getCode());
	    	
	    	header = (CommonHeader) req.getAttribute(Keys.COM_HEADER.getKey());
	    	
	    	log.debug("__DBGLOG__ header:[{}]", header);
	    	
	    	if(header != null) {
	    		logEntity.setUuid(header.getUuid());
	    		logEntity.setTrnCd(header.getTrnCd());
	    		logEntity.setUid(header.getTrnUserId());
	    	}
	    	
			logEntity.setUri(req.getRequestURI());
			logEntity.setIp(CommonUtil.getRequestIP(req));
			logEntity.setContent(new String(res.getContentAsByteArray()));
				    	
	        if (ex == null) {
	        	log.debug("__DBGLOG__ 정상 {}", rs);
	        	logEntity.setResultCode(rs.getResultCd());
	        }else {
	        	// 오류가 발생한 경우에 대한 처리
	            // 예를 들어, 로깅이나 특정한 오류 페이지로 리다이렉션 등을 수행할 수 있음
	            //response.sendRedirect("/error"); // 예시: "/error"로 리다이렉트
	        	log.error("__ERRLOG__ 오류 {}", ex);
	        	logEntity.setResultCode(ResultCode.ERROR.getCode());
	        	rs.setResultCd(ResultCode.ERROR.getCode());
	        	rs.setPayload(ex);
	        }
	        
	        trnLogService.saveTrnLog(logEntity);
	        
    	}catch(Exception e) {
    		log.error("__ERRLOR__ 전체 후처리 오류", e);
    		throw e;
    	}finally {
    		log.debug("__DBGLOG__ 전체 후처리 종료");
		}
    }
    
    /**
     * 공통헤더 생성
     * @param request
     * @return
     */
    private CommonHeader createCommonHeader(HttpServletRequest request) {
    	
    	ContentCachingRequestWrapper req = null; 
    	CommonHeader header = null;
    	DecodedJWT tokenInfo = null;
    	String accessToken = "";
    	
    	try {
    		
    		log.debug("__DBGLOG__ createCommonHeader 시작");
    		
    		req = (ContentCachingRequestWrapper) request;
    		
    		//===================================================================================
    		// 공통헤더 생성
    		//===================================================================================
    		header = new CommonHeader();;
    		
    		//===================================================================================
	    	//UUID 채번
    		//===================================================================================
			UUID uuid = UUID.randomUUID();
			
			//===================================================================================
			// 공통헤더 데이터 적재
			//===================================================================================
			header.setUuid(uuid);
			
			//현재일시 조립
			ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
			header.setCurrDtm(dateTime.toInstant());
			header.setCurrDate(dateTime.format(DateTimeFormatter.ofPattern("YYYYMMdd")).toString());
			header.setCurrTime(dateTime.format(DateTimeFormatter.ofPattern("HHMMssSSS")).toString());
			
			log.debug("__DBGLOG__ ZonedDateTime: {}", dateTime);
			
			////채널코드 조립
			//header.setTrnChnlCd(Constants.CHANNEL_CODE);
			
			//거래코드 조립
			header.setTrnCd(String.valueOf(request.getHeader(Keys.TRAN_CD.getKey())));
			
			//IP
			header.setRequestIp(CommonUtil.getRequestIP(req));

    		//===================================================================================
    		// 토큰 취득
    		//===================================================================================
    		accessToken = request.getHeader(Keys.AUTHORIZATION.getKey());
    		
    		log.debug("__DBGLOG__ accessToken : {}", accessToken);
    		
    		if(!StringUtil.isEmpty(accessToken)) {
        		if(accessToken.startsWith(Constants.AUTH)) {
        			header.setAuthorization(accessToken.substring(0, Constants.AUTH.length()));
        			accessToken = accessToken.substring(Constants.AUTH.length() + 1);
        		}
        		
        		if(StringUtil.isEmpty(accessToken) || "NULL".equalsIgnoreCase(accessToken)) {
	        		log.debug("__DBGLOG__ token empty");
        		}else {
        			tokenInfo = jWTProvider.validate(accessToken);
	    			
	    			//Access token
	    			header.setAccessToken(accessToken);
	    			
	    			if(tokenInfo != null) {
	    				header.setTrnUserId(tokenInfo.getSubject());
	    			}
        		}
    		}
			
			log.debug("__DBGLOG__ Create Common Header : {}", header);
    	}catch(JWTVerificationException jve) {
    		log.error("__ERRLOR__ createCommonHeader 토큰 검증오류", jve);
    	}catch(Exception e) {
    		log.error("__ERRLOR__ createCommonHeader 오류", e);
    	}finally {
    		log.debug("__DBGLOG__ createCommonHeader 종료");
		}
    	return header;
    }
}
