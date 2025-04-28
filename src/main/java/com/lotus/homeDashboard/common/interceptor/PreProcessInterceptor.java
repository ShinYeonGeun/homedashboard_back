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
	    	//UUID 채번
			//===================================================================================
			UUID uuid = UUID.randomUUID();
			request.setAttribute("uuid", uuid);
			
			//===================================================================================
			// 로그에 UUID 추가
			//===================================================================================
			MDC.put("UUID", uuid.toString());
			
			log.debug("====================================================================================================");
			log.debug("전체 선처리 시작");
			log.debug("====================================================================================================");
			
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug("거래코드(trnCd): [{}]", request.getHeader(Keys.TRN_CD.getKey()));
			log.debug("----------------------------------------------------------------------------------------------------");
			
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
			//if(!StringUtil.isEqualToAny(req.getRequestURI(), Constants.LOGIN_URI, Constants.HEALTH_CHECK_URI)) {
			if(StringUtil.isEqualToAny(req.getRequestURI(), Constants.LOGIN_URI, Constants.HEALTH_CHECK_URI)) {
				
				header.setTrnCd(Constants.LOGIN_URI.equals(req.getRequestURI()) ? "LOGIN":"HEALTH");
				
			} else {
				
				if(!Constants.AUTH.equals(header.getAuthorization())){
					throw new LoginException("error.invalid.auth");
				}
				
				tokenInfo = jWTProvider.validate(header.getAccessToken());
				
				if(tokenInfo == null) {
					throw new LoginException("error.login.not-login");
				}
				
				response.setHeader(Keys.ACCESS_TOKEN.getKey(), header.getAccessToken());
				
			}
			
			
			//===================================================================================
			// 거래로그 엔티티 조립
			//===================================================================================
			logEntity.setUid(header.getTrnUserId());
			logEntity.setResultCd(ResultCode.SUCCESS.getCode());
			trnLogService.saveTrnLogWithEntity(logEntity);
			
		}catch(JWTVerificationException e) {
			//JWTVerificationException
			String msgKey = "";
			if(e instanceof TokenExpiredException) {
				msgKey = "error.login.token.expired";
			} else {
				msgKey = "error.login.not-login";
			}
			log.error("====================================================================================================");
			log.error("__ERRLOR__ 전체 선처리 토큰 검증 오류", e);
			log.error("====================================================================================================");
			throw new LoginException(msgKey);
		}catch(Exception e) {
			log.error("__ERRLOR__ 전체 선처리 오류", e);
			logEntity.setResultCd(ResultCode.ERROR.getCode());
			logEntity.setContent(e.toString());
			trnLogService.saveTrnLogWithEntity(logEntity);
    		throw e;
		}finally {
			
			if(!request.getMethod().equals(HttpMethod.OPTIONS.name())) {
				log.debug("====================================================================================================");
				log.debug("전체 선처리 종료");
				log.debug("====================================================================================================");
			}
		}
        return true;//HandlerInterceptor.super.preHandle(req, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
    
    /**
     *  요청 처리가 완료된 후에 실행    
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    	ContentCachingRequestWrapper req = null;
    	ContentCachingResponseWrapper res = null;
    	TrnLogEntity logEntity = null;
    	CommonHeader header = null;
    	ResultSet rs = null;
    	try {
    		req = (ContentCachingRequestWrapper) request;
    		res = (ContentCachingResponseWrapper) response;
    		
    		if(request.getMethod().equals(HttpMethod.OPTIONS.name())) {
				return;
			}
    		
    		log.debug("====================================================================================================");
			log.debug("전체 후처리 시작");
			log.debug("====================================================================================================");
    		
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
	    	// BizException 발생한 경우 GlobalExceptionHandler에서 로그 저장함.
	    	//===================================================================================
	    	if(!ResultCode.SUCCESS.getCode().equals(rs.getResultCd())) {
	    		return;
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
	        	logEntity.setResultCd(rs.getResultCd());
	        	trnLogService.saveTrnLogWithEntity(logEntity);
	        }
//	    	else {
//	        	// 오류가 발생한 경우에 대한 처리
//	            // 예를 들어, 로깅이나 특정한 오류 페이지로 리다이렉션 등을 수행할 수 있음
//	            //response.sendRedirect("/error"); // 예시: "/error"로 리다이렉트
//	        	log.error("__ERRLOG__2 오류", ex);
//	        	
//	        	StringWriter sw = new StringWriter();
//	            PrintWriter pw = new PrintWriter(sw);
//	            ex.printStackTrace(pw);
//	            
//	            String stackTraceString = sw.toString();
//	        	
//	        	logEntity.setResultCd(ResultCode.ERROR.getCode());
//	        	logEntity.setContent(stackTraceString);
//	        }
//	        
//	        trnLogService.saveTrnLogWithEntity(logEntity);
	        
    	}catch(Exception e) {
    		log.error("====================================================================================================");
    		log.error("__ERRLOR__ 전체 후처리 오류", e);
    		log.error("====================================================================================================");
    	}finally {
    		if(!request.getMethod().equals(HttpMethod.OPTIONS.name())) {
    			log.debug("====================================================================================================");
    			log.debug("전체 후처리 종료");
    			log.debug("====================================================================================================");
        		
    		}
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
    		log.debug("====================================================================================================");
    		log.debug("__DBGLOG__ createCommonHeader 공통헤더생성 시작");
    		log.debug("====================================================================================================");
			
    		req = (ContentCachingRequestWrapper) request;
    		
    		//===================================================================================
    		// 공통헤더 생성
    		//===================================================================================
    		header = new CommonHeader();;
    		
    		//===================================================================================
	    	//UUID 가져옴.
    		//===================================================================================
			UUID uuid = (UUID) req.getAttribute("uuid");
			
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
			log.debug("__DBGLOG__ createHeader 거래코드(trnCd): [{}]", request.getHeader(Keys.TRN_CD.getKey()));
			header.setTrnCd(String.valueOf(request.getHeader(Keys.TRN_CD.getKey())));
			
			//IP
			header.setRequestIp(CommonUtil.getRequestIP(req));
			
			//uri
			header.setRequestUri(req.getRequestURI());

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
    		log.error("====================================================================================================");
    		log.error("__ERRLOR__ createCommonHeader 공통헤더생성 토큰 검증오류", jve);
    		log.error("====================================================================================================");
    	}catch(Exception e) {
    		log.error("====================================================================================================");
    		log.error("__ERRLOR__ createCommonHeader 공통헤더생성 오류", e);
    		log.error("====================================================================================================");
    	}finally {
    		log.debug("====================================================================================================");
    		log.debug("__DBGLOG__ createCommonHeader 공통헤더생성 종료");
    		log.debug("====================================================================================================");
			
		}
    	return header;
    }
}
