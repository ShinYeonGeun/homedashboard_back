package com.lotus.homeDashboard.common.service.impl;

import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.dao.TrnCdRepository;
import com.lotus.homeDashboard.common.entity.TrnCdEntity;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.service.TrnCdService;
import com.lotus.homeDashboard.common.utils.MessageUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TrnCdServiceImpl implements TrnCdService {
	@Autowired
	private TrnCdRepository trnCdRepository;
	
	@Autowired
	private ServiceInvoker caller;
	
	@Override
	public ResultSet executeService(HttpServletRequest request) {
		ResultSet rs = null;
		CommonHeader header = null;
		Optional<TrnCdEntity> trnCdEntity = null;
		TrnCdEntity trnCd = null;
		try {
			log.debug("__DBGLOG__ callService 시작");
			
			header = (CommonHeader) request.getAttribute(Keys.COM_HEADER.getKey());
			
			if(header == null) {
				throw new BadRequestException("header is null");
			}
			
			log.debug("__DBGLOG__ 거래코드: {}", header);
			
			//TODO test조건 삭제, 메뉴 서비스코드 추가
			if("testTranCd".equals(header.getTrnCd())) {
				rs = caller.invoke("MenuService", "findUserMenuList", request);
				log.debug("__DBGLOG__ 서비스 결과: {}", rs);
			}else {
				trnCdEntity = trnCdRepository.findById(header.getTrnCd());
				
				if(trnCdEntity.isEmpty()) {
					throw new BizException(MessageUtil.getMessage("service_not_found"));
				}
				
				trnCd = trnCdEntity.get();
				rs = caller.invoke(trnCd.getSvcNm(), trnCd.getMtdNm(), request);
			}
			
			log.debug("__DBGLOG__ callService 종료");
			
		} catch (Exception e) {
			throw new BizException(MessageUtil.getMessage("service_invoke_err"));
		}
		
		return rs;
	}
}
