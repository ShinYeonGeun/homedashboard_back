package com.lotus.homeDashboard.common.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
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
			
			trnCdEntity = trnCdRepository.findById(header.getTrnCd());
			
			if(trnCdEntity.isEmpty()) {
				throw new BizException("service_not_found");
			}
			
			trnCd = trnCdEntity.get();
			
			rs = caller.invokeRequiresNew(trnCd.getSvcNm(), trnCd.getMtdNm(), request, trnCd.getTmotMs());

			log.debug("__DBGLOG__ callService 종료");
			
		} catch (BizException be) {
			throw be;
			
		} catch (Exception e) {
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
					data.put("lastTrnDtm", entity.getLastTrnDtm());
					data.put("lastTrnCd", entity.getLastTrnCd());
					data.put("lastTrnUUID", entity.getLastTrnUUID());
					data.put("lastTrnUid", entity.getLastTrnUid());
					trnCdList.add(data);
				}
				
			}
			
			result.put("trnCdList", trnCdList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqTrnCdList Exception 발생 : {}", e);
			new BizException("inquiry_err", e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> inqCntTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		Specification<TrnCdEntity> conditions = null;
		long cnt = 0;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			params = request.getParameter();	
			conditions = Specification.where(CommonSpecification.alwaysTrue());
			conditions = conditions.and(CommonSpecification.hasDelYn(Constants.NO)); //삭제여부 Y 포함 데이터 조회는 별도로 만들어서 사용.
			
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
			
			
			//===================================================================================
			// 거래코드목록 조회
			//===================================================================================
			cnt = trnCdRepository.count(conditions);
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("count", cnt);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqCntTrnCd Exception 발생 : {}", e);
			new BizException("inquiry_err", e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> createTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity = null;
		TrnCdEntity trnCdEntity = null;
		
		String trnCd = "";
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			trnCd = params.getString("trnCd");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(trnCd)) {
				log.error("__ERRLOG__ 거래코드 미입력");
				throw new BizException("val_required", new String[] {"거래코드"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("delYn"))) {
				log.error("__ERRLOG__ 삭제여부 미입력");
				throw new BizException("val_required", new String[] {"삭제여부"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			if(optioinalTrnCdEntity.isPresent()) {
				if(!Constants.YES.equals(optioinalTrnCdEntity.get().getDelYn())) {
					log.error("__ERRLOG__ 기등록된 거래코드 존재 [{}]", trnCd);
					throw new BizException("reg_data_exists_msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
				}
			}
			
			log.debug("__DBGLOG__ 거래코드조회 종료");
			log.debug("__DBGLOG__ 거래코드등록 시작");
			
			//===================================================================================
			// 거래코드 등록
			//===================================================================================
			trnCdEntity = new TrnCdEntity();
			trnCdEntity.setTrnCd(trnCd);
			trnCdEntity.setTrnNm(params.getString("trnNm"));
			trnCdEntity.setSvcNm(params.getString("svcNm"));
			trnCdEntity.setMtdNm(params.getString("mtdNm"));
			trnCdEntity.setTmotMs(params.getLong("tmotMs", 0L));
			trnCdEntity.setDelYn(Constants.NO);
			trnCdEntity.setLastTrnUUID(header.getUuid());
			trnCdEntity.setLastTrnUid(header.getTrnUserId());
			trnCdEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				trnCdRepository.saveAndFlush(trnCdEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"거래코드"}, e);
			}
			
			log.debug("__DBGLOG__ 거래코드등록 종료");
			log.debug("__DBGLOG__ 등록된 거래코드 조회 시작");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty()) {
				log.error("__ERRLOG__ createTrnCd 등록 후 조회 NOT FOUND");
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("거래코드:", trnCd)});
			}
			
			log.debug("__DBGLOG__ 등록된 거래코드 조회 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("trnCdInfo", optioinalTrnCdEntity.get());
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ createTrnCd Exception 발생 : {}", e);
			new BizException("inquiry_err", e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> updateTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity = null;
		TrnCdEntity trnCdEntity = null;
		
		String trnCd = "";
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			trnCd = params.getString("trnCd");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(trnCd)) {
				log.error("__ERRLOG__ 거래코드 미입력");
				throw new BizException("val_required", new String[] {"거래코드"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("delYn"))) {
				log.error("__ERRLOG__ 삭제여부 미입력");
				throw new BizException("val_required", new String[] {"삭제여부"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty() || Constants.YES.equals(optioinalTrnCdEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 등록된 거래코드 미존재 [{}]", trnCd);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
			} else {
				trnCdEntity = optioinalTrnCdEntity.get();
			}
			
			log.debug("__DBGLOG__ 거래코드조회 종료");
			log.debug("__DBGLOG__ 거래코드수정 시작");
			
			//===================================================================================
			// 거래코드 등록
			//===================================================================================
			trnCdEntity.setTrnCd(trnCd);
			trnCdEntity.setTrnNm(params.getString("trnNm"));
			trnCdEntity.setSvcNm(params.getString("svcNm"));
			trnCdEntity.setMtdNm(params.getString("mtdNm"));
			trnCdEntity.setTmotMs(params.getLong("tmotMs", 0L));
			trnCdEntity.setDelYn(Constants.NO);
			trnCdEntity.setLastTrnUUID(header.getUuid());
			trnCdEntity.setLastTrnUid(header.getTrnUserId());
			trnCdEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				trnCdEntity = trnCdRepository.saveAndFlush(trnCdEntity);
				
				if(trnCdEntity == null) {
					log.error("__ERRLOG__ updateTrnCd saveAndFlush null");
					throw new BizException("chg_error_prefix", new String[] {"거래코드"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("chg_error_prefix", new String[] {"거래코드"}, e);
			}
			
			log.debug("__DBGLOG__ 거래코드수정 종료");

			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("trnCdInfo", optioinalTrnCdEntity.get());
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ updateTrnCd Exception 발생 : {}", e);
			new BizException("inquiry_err", e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> deleteTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity = null;
		TrnCdEntity trnCdEntity = null;
		
		String trnCd = "";
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			trnCd = params.getString("trnCd");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(trnCd)) {
				log.error("__ERRLOG__ 거래코드 미입력");
				throw new BizException("val_required", new String[] {"거래코드"}); 
			}
			
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty()) {
				log.error("__ERRLOG__ 거래코드 미존재 [{}]", trnCd);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
			}
			
			if(Constants.YES.equals(optioinalTrnCdEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 이미 삭제된 거래코드 [{}]", trnCd);
				throw new BizException("del_already_msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
			}
			
			log.debug("__DBGLOG__ 거래코드조회 종료");
			log.debug("__DBGLOG__ 거래코드삭제 시작");
			
			//===================================================================================
			// 거래코드 등록
			//===================================================================================
			trnCdEntity = optioinalTrnCdEntity.get();
			trnCdEntity.setTrnCd(trnCd);
			trnCdEntity.setDelYn(Constants.YES);
			trnCdEntity.setLastTrnUUID(header.getUuid());
			trnCdEntity.setLastTrnUid(header.getTrnUserId());
			trnCdEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				trnCdEntity = trnCdRepository.saveAndFlush(trnCdEntity);
				
				if(trnCdEntity == null) {
					log.error("__ERRLOG__ deleteTrnCd saveAndFlush null");
					throw new BizException("del_error_msg", new String[] {"거래코드"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("del_error_msg", new String[] {"거래코드"}, e);
			}
			
			log.debug("__DBGLOG__ 거래코드수정 종료");

			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("trnCdInfo", optioinalTrnCdEntity.get());
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteTrnCd Exception 발생 : {}", e);
			new BizException("inquiry_err", e);
		}
		
		return result;
	}
}
