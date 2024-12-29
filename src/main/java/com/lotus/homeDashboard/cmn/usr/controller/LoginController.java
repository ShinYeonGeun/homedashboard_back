package com.lotus.homeDashboard.cmn.usr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lotus.homeDashboard.cmn.usr.entity.UserEntity;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.constants.ResultCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LoginController {
	
	@Autowired
	private ServiceInvoker caller;

	@PostMapping(value = Constants.LOGIN_URI)
	public ResultSet doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.debug("__DBGLOG__ LoginController.doLogin 시작");
		ResultSet loginServiceResult = null;
		String accessToken = "";
		
		try {
			loginServiceResult = caller.invoke("UserService", "doLogin", request);
			
			if(ResultCode.SUCCESS.getCode().equals(loginServiceResult.getResultCd())) {
				
				accessToken = ((DataMap<String, Object>) loginServiceResult.getPayload()).getString("accessToken");
				response.setHeader(Keys.ACCESS_TOKEN.getKey(), accessToken);
				
				log.info("__INFOLOG__ 로그인 성공: [{}]", (DataMap<String, Object>) loginServiceResult.getPayload());
			}
			
//			//사용자정보 리턴하면 안됨.
//			loginServiceResult.setPayload(null);
			return loginServiceResult;
			
		}catch (Exception e) {
			log.error("__ERRLOG__ LoginController Exception {}", e);
			throw e;
		}
	}
	
	@PostMapping(value = Constants.LOGOUT_URI)
	public ResultSet doLogout(HttpServletRequest request) {
		log.debug("__DBGLOG__ LoginController.doLogout 시작");
		ResultSet rs = new ResultSet();
		request.getSession().invalidate();
		log.debug("__DBGLOG__ LoginController.doLogout 종료");
		return rs;
	}
	
	@PostMapping(value = Constants.EXTENSION_JWT_PERIOD)
	public ResultSet extensionJWTPeriod(HttpServletRequest request) {
		log.debug("__DBGLOG__ LoginController.extensionJWTPeriod 시작");
		ResultSet rs = new ResultSet();
		try {
			rs = caller.invoke("UserService", "extensionJWTPeriod", request);
		} catch (Exception e) {
			log.error("__ERRLOG__ EXTENSION_JWT_PERIOD Exception {}", e);
		}
		log.debug("__DBGLOG__ LoginController.extensionJWTPeriod 종료");
		return rs;
	}
}
