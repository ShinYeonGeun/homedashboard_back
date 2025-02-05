package com.lotus.homeDashboard.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.dao.TrnCdRepository;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.entity.TrnCdEntity;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.service.TrnCdService;
import com.lotus.homeDashboard.common.spec.TrnCdSpecification;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service("TrnCdService")
@Slf4j
public class TrnCdServiceImpl implements TrnCdService {
	
	@Autowired
	private TrnCdRepository trnCdRepository;
	
	@Autowired
	private ServiceInvoker caller;
	
	@Autowired
	private JpaTransactionManager transactionManager;
	

	@Override
	public ResultSet executeService(HttpServletRequest request) {
		ResultSet rs = null;
		CommonHeader header = null;
		Optional<TrnCdEntity> trnCdEntity = null;
		TrnCdEntity trnCd = null;
		DefaultTransactionDefinition def = null;
		TransactionStatus status = null;
		
		try {
			
			log.debug("__DBGLOG__ callService 시작");
			
			header = (CommonHeader) request.getAttribute(Keys.COM_HEADER.getKey());
			
			if(header == null) {
				throw new BadRequestException("header is null");
			}
			
			log.debug("__DBGLOG__ 거래코드: {}", header);
			
			trnCdEntity = trnCdRepository.findById(header.getTrnCd());
			
			if(trnCdEntity.isEmpty()) {
				throw new BizException("service_not_found");
			}
			
			trnCd = trnCdEntity.get();
			
			def = new DefaultTransactionDefinition();
	        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        
	        status = transactionManager.getTransaction(def);
	        
			rs = caller.invoke(trnCd.getSvcNm(), trnCd.getMtdNm(), request, trnCd.getTmotMs());

			transactionManager.commit(status);
			
			log.debug("__DBGLOG__ callService 종료");
			
		} catch (BizException be) {
			log.error("__ERRLOG__ executeService bizException");
			if (status != null && !status.isCompleted()) { // 트랜잭션이 완료되지 않았다면 롤백
		        transactionManager.rollback(status);
		    }
			
			throw be;
			
		} catch (Exception e) {
			log.error("__ERRLOG__ executeService Exception" ,e);
			
			if (status != null && !status.isCompleted()) { // 트랜잭션이 완료되지 않았다면 롤백
		        transactionManager.rollback(status);
		    }
			
			throw new BizException("service_invoke_err", e);
		}
		
		return rs;
	}


	@Override
	public DataMap<String, Object> inqTrnCdList(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		List<DataMap<String, Object>> trnCdList = null;
		Pageable pageable = null;
		Specification<TrnCdEntity> conditions = null;
		Page<TrnCdEntity> inqList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			trnCdList = new ArrayList<>();
			params = request.getParameter();	
			conditions = Specification.where(CommonSpecification.alwaysTrue());
			pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("trnCd"));
			
			//===================================================================================
			// 쿼리 조건 조립
			//===================================================================================
			if(!StringUtil.isEmpty(params.getString("trnCd"))) {
				conditions = conditions.and(TrnCdSpecification.containsTrnCd(params.getString("trnCd")));
			}
			
			if(!StringUtil.isEmpty(params.getString("trnNm"))) {
				conditions = conditions.and(TrnCdSpecification.containsTrnNm(params.getString("trnNm")));
			}
			
			if(!StringUtil.isEmpty(params.getString("svcNm"))) {
				conditions = conditions.and(TrnCdSpecification.containsSvcNm(params.getString("svcNm")));
			}
			
			if(!StringUtil.isEmpty(params.getString("mtdNm"))) {
				conditions = conditions.and(TrnCdSpecification.containsMtdNm(params.getString("mtdNm")));
			}
			
			if(!StringUtil.isEmpty(params.getString("delYn"))) {
				conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
			}
			
			//===================================================================================
			// 거래코드목록 조회
			//===================================================================================
			inqList = trnCdRepository.findAll(conditions, pageable);
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.putAll(CommonUtil.extractPageValues(inqList));
			
			if(inqList.getSize() > 0) {
				
				for(TrnCdEntity entity: inqList.getContent()) {
					DataMap<String, Object> data = new DataMap<>();
					data.put("trnCd", entity.getTrnCd());
					data.put("trnNm", entity.getTrnNm());
					data.put("svcNm", entity.getSvcNm());
					data.put("mtdNm", entity.getMtdNm());
					data.put("tmotMs", entity.getTmotMs());
					data.put("delYn", entity.getDelYn());
					data.put("lastTrnUUID", entity.getLastTrnUUID());
					data.put("lastTrnDtm", entity.getLastTrnDtm());
					data.put("lastTrnUid", entity.getLastTrnUid());
					trnCdList.add(data);
				}
				
			}
			
			result.put("trnCdList", trnCdList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqTrnCdList Exception 발생 : {}", e);
			new BizException("inquiry_err");
		}
		
		return result;
	}
}
