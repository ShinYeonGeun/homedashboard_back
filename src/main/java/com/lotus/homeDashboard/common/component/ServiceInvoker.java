package com.lotus.homeDashboard.common.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.constants.ResultCode;
import com.lotus.homeDashboard.common.exception.BizException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 서비스를 동적으로 호출한다.
 */
@Component
@Slf4j
public class ServiceInvoker {
	@Autowired
	private ApplicationContext ctx;
	
	public ResultSet invoke(String serviceName, String methodName, HttpServletRequest req) throws Exception {
		
		ResultSet rs = null;
		Method method = null;
		Object bean = null;
		Class<?> clazz = null;
		Class<?>[] parameterTypes = null;
		Object serviceResult = null;
		Request request = null;
		ObjectMapper mapper = null;
		DataMap<String, Object> params = null; 
		ContentCachingRequestWrapper cachingRequest = null;
		
		try {
			log.debug("============================================================== Service Invoker start");
			
			log.debug("서비스명 : [{}]", serviceName);
			log.debug("메소드명 : [{}]", methodName);
			
			rs = new ResultSet();
			bean = ctx.getBean(serviceName);
			cachingRequest = (ContentCachingRequestWrapper) req;
			mapper = new ObjectMapper();
			request = new Request();
			
			//헤더 조립
			request.setHeader((CommonHeader) cachingRequest.getAttribute(Keys.COM_HEADER.getKey()));
			
			//파라미터 조립
			params = mapper.readValue(cachingRequest.getContentAsByteArray(), new TypeReference<DataMap<String, Object>>(){});
			request.setParameter(params);
			
			clazz = bean.getClass();
			parameterTypes = new Class<?>[] {request.getClass()};
			method = clazz.getMethod(methodName, parameterTypes);
			
			log.debug("__DBGLOG__ {}", serviceName + "." + methodName + " START");
			log.debug("Parameter : {}", params);
			
			serviceResult = method.invoke(bean, request);

			log.debug("Result : {}", serviceResult);
			log.debug("__DBGLOG__ {}", serviceName + "." + methodName + " END");
			
			rs.setResultCd(ResultCode.SUCCESS.getCode());
			rs.setPayload(serviceResult);
			
			log.debug("============================================================== Service Invoker end");
			
		}catch(InvocationTargetException ive) {
			
			log.error("__ERRLOG__ InvocationTargetException {}", ive.getTargetException());
			
			rs.setResultCd(ResultCode.ERROR.getCode());
			
			if(ive.getTargetException() instanceof BizException) {
				rs.setPayload(ive.getTargetException());
			}else {
				rs.setPayload(ive);
			}
			
		}catch(Exception e) {
			log.error("__ERRLOG__ Exception {}", e);
			rs.setResultCd(ResultCode.ERROR.getCode());
			rs.setPayload(e);
		}
		
		return rs;
	}
}
