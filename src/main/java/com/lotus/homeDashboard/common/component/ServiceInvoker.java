package com.lotus.homeDashboard.common.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotus.homeDashboard.common.constants.Constants;
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
	
	private final ExecutorService executor = Executors.newCachedThreadPool(); //newFixedThreadPool(Constants.SVC_INVOKER_POOL_SIZE);
	
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private JpaTransactionManager transactionManager;
	
	private ResultSet invokeOrThrow(String serviceName, String methodName, Request request, long timeout) throws Exception {
		
		log.debug("============================================================== Service Invoker start");
		log.debug("서비스명 : [{}]", serviceName);
		log.debug("메소드명 : [{}]", methodName);
		log.debug("타임아웃 : [{}]", timeout);
		
		ResultSet result = null;
		Future<ResultSet> future = null;
		
		try {
			
			Callable<ResultSet> task = () -> {
				
				ResultSet rs = null;
				Method method = null;
				Object bean = null;
				Class<?> clazz = null;
				Class<?>[] parameterTypes = null;
				Object serviceResult = null;
				DataMap<String, Object> params = null; 
				
				try {
					
					rs = new ResultSet();
					bean = ctx.getBean(serviceName);
					
					clazz = bean.getClass();
					parameterTypes = new Class<?>[] {request.getClass()};
					method = clazz.getMethod(methodName, parameterTypes);
					
					log.debug("__DBGLOG__ {}", serviceName + "." + methodName + " START");
					log.debug("Parameter : {}", params);
					
					serviceResult = method.invoke(bean, request);
					
					rs.setResultCd(ResultCode.SUCCESS.getCode());
					rs.setPayload(serviceResult);
					
					log.debug("Result : {}", serviceResult);
					log.debug("__DBGLOG__ {}", serviceName + "." + methodName + " END");
					
					log.debug("============================================================== Service Invoker end");
					
					return rs;
					
				}catch(BizException be) {
					log.error("__ERRLOG__ invoker11 invokeOrThrow bizException", be);
					log.error("__ERRLOG__ invoker11 invokeOrThrow bizException {}", be.getMessage());
					throw be;
				}catch(InvocationTargetException ive) {
					
					if(ive.getTargetException() instanceof BizException) {
						log.error("__ERRLOG__ invoker11 invokeOrThrow InvocationTargetException bizException", ive.getTargetException());
						throw (BizException) ive.getTargetException();
					}else {
						log.error("__ERRLOG__ invoker11 invokeOrThrow InvocationTargetException", ive.getTargetException());
						throw ive;
					}
				}catch(Exception e) {
					log.error("__ERRLOG__ invoker11 invokeOrThrow Exception", e);
					throw e;
				}
			};
			
			future = executor.submit(task);
			
			//타임아웃 최솟값보다 작게주면 시간제한 없이 무한대기
			if(timeout < Constants.SVC_TIMEOUT_MIN) {
				log.debug("ServiceInvoker 타임아웃 무한");
				result = future.get();
			} else {
				result = future.get(timeout, TimeUnit.MILLISECONDS);
			}
			
		}catch (TimeoutException e) {
			log.error("__ERRLOG__ invoker Timeout Exception {}", e);
			future.cancel(true);
			throw new BizException("service_timeout", e);
		}catch(BizException be) {
			log.error("__ERRLOG__ invoker111 BizException {}", be);
			throw be;
		}catch(ExecutionException e) {
			if(e.getCause() instanceof BizException) {
				log.error("__ERRLOG__ invoker111 ExecutionException BizException {}", e);
				log.error("__ERRLOG__ invoker111 ExecutionException BizException2 {}", ((BizException) e.getCause()).getMessage());
				throw (BizException) e.getCause();
			}else {
				log.error("__ERRLOG__ invoker111 ExecutionException {}", e);
				throw new BizException("service_invoke_err", e);
			}
		}catch(Exception e) {

			if(e instanceof BizException) {
				log.error("__ERRLOG__ invoker111 BizException {}", e);
				throw (BizException) e;
			}else {
				log.error("__ERRLOG__ invoker111 Exception {}", e);
				throw new BizException("service_invoke_err", e);
			}
		}finally {
			
		}
		
		return result;
	}
	
	
	public ResultSet invoke(String serviceName, String methodName, Request request) {
		return this.invoke(serviceName, methodName, request, Constants.SVC_TIMEOUT_INF);
	}
	
	public ResultSet invoke(String serviceName, String methodName, Request request, long timeout) {
		ResultSet rs = new ResultSet();
		
		try {
			
			rs = this.invokeOrThrow(serviceName, methodName, request, timeout);
			
		}catch(BizException be) {
			log.error("__ERRLOG__ invoke 22 BizException ");
			throw be;	
		}catch(InvocationTargetException ive) {
			
			if(ive.getTargetException() instanceof BizException) {
				log.error("__ERRLOG__ invoke 22 InvocationTargetException BizException ");
				throw (BizException) ive.getTargetException();
			}else {
				log.error("__ERRLOG__ invoke 22 InvocationTargetException ");
				throw new BizException("service_invoke_err", ive);
			}
			
		}catch(Exception e) {
			log.error("__ERRLOG__ invoke 22 Exception ");
			throw new BizException("service_invoke_err", e);
		}
		
		return rs;
	}
	
	public ResultSet invoke(String serviceName, String methodName, HttpServletRequest req) {
		return this.invoke(serviceName, methodName, req, Constants.SVC_TIMEOUT_INF);
	}
	
	public ResultSet invoke(String serviceName, String methodName, HttpServletRequest req, long timeout) {
		
		ResultSet rs = null;
		Request request = null;
		ObjectMapper mapper = null;
		DataMap<String, Object> params = null; 
		ContentCachingRequestWrapper cachingRequest = null;
		
		try {
			
			rs = new ResultSet();
			cachingRequest = (ContentCachingRequestWrapper) req;
			mapper = new ObjectMapper();
			request = new Request();
			
			//헤더 조립
			request.setHeader((CommonHeader) cachingRequest.getAttribute(Keys.COM_HEADER.getKey()));
			
			//파라미터 조립
			params = mapper.readValue(cachingRequest.getContentAsByteArray(), new TypeReference<DataMap<String, Object>>(){});
			request.setParameter(params);
			
			rs = this.invokeOrThrow(serviceName, methodName, request, timeout);
			
		}catch(BizException be) {
			log.error("__ERRLOG__ invoke 33 BizException ");
			throw be;	
		}catch(InvocationTargetException ive) {
			
			if(ive.getTargetException() instanceof BizException) {
				log.error("__ERRLOG__ invoke 33 InvocationTargetException BizException ");
				throw (BizException) ive.getTargetException();
			}else {
				log.error("__ERRLOG__ invoke 33 InvocationTargetException ");
				throw new BizException("service_invoke_err", ive);
			}
			
		}catch(Exception e) {
			log.error("__ERRLOG__ invoke 33 Exception ");
			throw new BizException("service_invoke_err", e);
		}
		
		return rs;
	}
	
	public ResultSet invokeRequiresNew(String serviceName, String methodName, Request request) {
		return this.invokeRequiresNew(serviceName, methodName, request, Constants.SVC_TIMEOUT_INF);
	}
	
	public ResultSet invokeRequiresNew(String serviceName, String methodName, Request request, long timeout) {
		
		ResultSet rs = null;
		DefaultTransactionDefinition def = null;
		TransactionStatus status = null;
		
		try {
			
			def = new DefaultTransactionDefinition();
	        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        
	        status = transactionManager.getTransaction(def);
	        
			rs = this.invoke(serviceName, methodName, request, timeout);
			
			transactionManager.commit(status);
			
		}catch(Exception e) {
			
			if (status != null && !status.isCompleted()) { // 트랜잭션이 완료되지 않았다면 롤백
		        transactionManager.rollback(status);
		    }
			
			if(e instanceof BizException) {
				throw (BizException) e;
			}else {
				throw new BizException("service_invoke_err", e);
			}
		}
		
		return rs;
	}
	
	public ResultSet invokeRequiresNew(String serviceName, String methodName, HttpServletRequest req, long timeout) {
		
		ResultSet rs = null;
		DefaultTransactionDefinition def = null;
		TransactionStatus status = null;
		
		try {
			
			def = new DefaultTransactionDefinition();
	        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        
	        status = transactionManager.getTransaction(def);
	        
			rs = this.invoke(serviceName, methodName, req, timeout);
			
			transactionManager.commit(status);
			
		}catch(Exception e) {
			
			if (status != null && !status.isCompleted()) { // 트랜잭션이 완료되지 않았다면 롤백
		        transactionManager.rollback(status);
		    }
			
			if(e instanceof BizException) {
				throw (BizException) e;
			}else {
				throw new BizException("service_invoke_err", e);
			}
		}
		
		return rs;
	}
	
	public ResultSet invokeRequiresNew(String serviceName, String methodName, HttpServletRequest req) {
		return this.invokeRequiresNew(serviceName, methodName, req, Constants.SVC_TIMEOUT_INF);
	}
}
