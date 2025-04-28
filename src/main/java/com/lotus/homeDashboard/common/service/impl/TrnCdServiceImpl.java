package com.lotus.homeDashboard.common.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.Keys;
import com.lotus.homeDashboard.common.dao.TrnCdRepository;
import com.lotus.homeDashboard.common.dao.TrnGroupAuthorityLogRepository;
import com.lotus.homeDashboard.common.dao.TrnGroupAuthorityRepository;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.entity.QTrnCdEntity;
import com.lotus.homeDashboard.common.entity.QTrnGroupAuthorityEntity;
import com.lotus.homeDashboard.common.entity.TrnCdEntity;
import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityEntity;
import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityKeyEntity;
import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityLogEntity;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.service.TrnCdService;
import com.lotus.homeDashboard.common.spec.TrnCdSpecification;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.Tuple;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service("TrnCdService")
@Slf4j
@Transactional
public class TrnCdServiceImpl implements TrnCdService {
	
	@Autowired
	private TrnCdRepository trnCdRepository;
	
	@Autowired
	private TrnGroupAuthorityRepository trnGroupAuthorityRepository;
	
	@Autowired
	private TrnGroupAuthorityLogRepository trnGroupAuthorityLogRepository;
	
	@Autowired
	private ServiceInvoker caller;
	
	@Override
	public ResultSet executeService(HttpServletRequest request) {
		ResultSet rs = null;
		CommonHeader header = null;
		Optional<TrnCdEntity> trnCdEntity =  Optional.empty();
		TrnCdEntity trnCd = null;
		
		long authCnt = 0L;
		
		try {
			
			log.debug("====================================================================================================");
			log.debug("executeService 시작");
			log.debug("====================================================================================================");
			
			header = (CommonHeader) request.getAttribute(Keys.COM_HEADER.getKey());
			
			if(header == null) {
				throw new BadRequestException("header is null");
			}
			
			log.debug("__DBGLOG__ 공통헤더: {}", header);
			log.debug("__DBGLOG__ 입력 거래코드: {}", header.getTrnCd());
			
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug(" 거래코드조회 시작");
			log.debug("----------------------------------------------------------------------------------------------------");
			
			trnCdEntity = trnCdRepository.findById(header.getTrnCd());
			
			if(trnCdEntity.isEmpty()) {
				throw new BizException("error.service.not-found");
			}
			
			trnCd = trnCdEntity.get();
			
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug(" 거래코드조회 결과");
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug("거래코드: [{}]", trnCd.getTrnCd());
			log.debug("거래명 : [{}]", trnCd.getTrnNm());
			log.debug("서비스명: [{}]", trnCd.getSvcNm());
			log.debug("메소드명: [{}]", trnCd.getMtdNm());
			log.debug("----------------------------------------------------------------------------------------------------");
			
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug(" 거래권한조회 시작");
			log.debug("----------------------------------------------------------------------------------------------------");
			
			authCnt = trnCdRepository.findCountTrnGroupAuth(trnCd.getTrnCd(), header.getTrnUserId());
			log.debug("거래권한 개수: [{}]", authCnt);
			
			if(authCnt < 1L) {
				log.error("__ERRLOG__ 서비스 실행권한 없음.");
				throw new BizException("error.service.auth.forbidden"); 
			}
			
			log.debug("----------------------------------------------------------------------------------------------------");
			log.debug(" 거래권한조회 결과");
			log.debug("----------------------------------------------------------------------------------------------------");
			
			rs = caller.invokeRequiresNew(trnCd.getSvcNm(), trnCd.getMtdNm(), request, trnCd.getTmotMs());

			log.debug("====================================================================================================");
			log.debug("executeService 종료");
			log.debug("====================================================================================================");
			
			
		} catch (BizException be) {
			throw be;
			
		} catch (Exception e) {
			throw new BizException("error.service.invoke", e);
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
					data.put("description", entity.getDescription());
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
			throw new BizException("error.inquiry", e);
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
			throw new BizException("error.inquiry", e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> createTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity =  Optional.empty();
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
				throw new BizException("error.required", new String[] {"거래코드"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("delYn"))) {
				log.error("__ERRLOG__ 삭제여부 미입력");
				throw new BizException("error.required", new String[] {"삭제여부"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isPresent()) {
				trnCdEntity = optioinalTrnCdEntity.get();
				
				if(!Constants.YES.equals(trnCdEntity.getDelYn())) {
					log.error("__ERRLOG__ 기등록된 거래코드 존재 [{}]", trnCd);
					throw new BizException("error.regist.data.exists.msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
				}
				
			} else {
				trnCdEntity = new TrnCdEntity();
			}
			
			log.debug("__DBGLOG__ 거래코드조회 종료");
			log.debug("__DBGLOG__ 거래코드등록 시작");
			
			//===================================================================================
			// 거래코드 등록
			//===================================================================================
			trnCdEntity.setTrnCd(trnCd);
			trnCdEntity.setTrnNm(params.getString("trnNm"));
			trnCdEntity.setSvcNm(params.getString("svcNm"));
			trnCdEntity.setMtdNm(params.getString("mtdNm"));
			trnCdEntity.setDescription(params.getString("description"));
			trnCdEntity.setTmotMs(params.getLong("tmotMs", 0L));
			trnCdEntity.setDelYn(Constants.NO);
			trnCdEntity.setLastTrnUUID(header.getUuid());
			trnCdEntity.setLastTrnUid(header.getTrnUserId());
			trnCdEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				trnCdRepository.saveAndFlush(trnCdEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("error.regist.prefix", new String[] {"거래코드"}, e);
			}
			
			log.debug("__DBGLOG__ 거래코드등록 종료");
			log.debug("__DBGLOG__ 등록된 거래코드 조회 시작");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty()) {
				log.error("__ERRLOG__ createTrnCd 등록 후 조회 NOT FOUND");
				throw new BizException("error.data.not-found.msg", new String[] {StringUtil.concat("거래코드:", trnCd)});
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
			throw new BizException("error.regist.prefix", new String[] {"거래코드"}, e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> updateTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity = Optional.empty();
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
				throw new BizException("error.required", new String[] {"거래코드"}); 
			}
			
//			if(StringUtil.isEmpty(params.getString("delYn"))) {
//				log.error("__ERRLOG__ 삭제여부 미입력");
//				throw new BizException("error.required", new String[] {"삭제여부"}); 
//			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty() || Constants.YES.equals(optioinalTrnCdEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 등록된 거래코드 미존재 [{}]", trnCd);
				throw new BizException("error.data.not-found.msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
			} else {
				trnCdEntity = optioinalTrnCdEntity.get();
			}
			
			log.debug("__DBGLOG__ 거래코드조회 종료");
			log.debug("__DBGLOG__ 거래코드수정 시작");
			
			//===================================================================================
			// 거래코드 수정
			//===================================================================================
			trnCdEntity.setTrnCd(trnCd);
			trnCdEntity.setTrnNm(params.getString("trnNm"));
			trnCdEntity.setSvcNm(params.getString("svcNm"));
			trnCdEntity.setMtdNm(params.getString("mtdNm"));
			trnCdEntity.setDescription(params.getString("description"));
			trnCdEntity.setTmotMs(params.getLong("tmotMs", 0L));
			trnCdEntity.setDelYn(Constants.NO);
			trnCdEntity.setLastTrnUUID(header.getUuid());
			trnCdEntity.setLastTrnUid(header.getTrnUserId());
			trnCdEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				trnCdEntity = trnCdRepository.saveAndFlush(trnCdEntity);
				
				if(trnCdEntity == null) {
					log.error("__ERRLOG__ updateTrnCd saveAndFlush null");
					throw new BizException("error.modify.prefix", new String[] {"거래코드"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("error.modify.prefix", new String[] {"거래코드"}, e);
			}
			
			log.debug("__DBGLOG__ 거래코드수정 종료");

			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("trnCdInfo", trnCdEntity);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ updateTrnCd Exception 발생 : {}", e);
			throw new BizException("error.modify.prefix", new String[] {"거래코드"}, e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> deleteTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<TrnCdEntity> optioinalTrnCdEntity =  Optional.empty();
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
				throw new BizException("error.required", new String[] {"거래코드"}); 
			}
			
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 거래코드조회 시작");
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			optioinalTrnCdEntity = trnCdRepository.findById(trnCd);
			
			if(optioinalTrnCdEntity.isEmpty()) {
				log.error("__ERRLOG__ 거래코드 미존재 [{}]", trnCd);
				throw new BizException("error.data.not-found.msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
			}
			
			if(Constants.YES.equals(optioinalTrnCdEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 이미 삭제된 거래코드 [{}]", trnCd);
				throw new BizException("error.delete.already_msg", new String[] {StringUtil.concat("거래코드: ", trnCd)});
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
					throw new BizException("error.delete.msg", new String[] {"거래코드"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteTrnCd DataAccessException 발생 : {}", e);
				throw new BizException("error.delete.msg", new String[] {"거래코드"}, e);
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
			throw new BizException("error.delete.msg", new String[] {"거래코드"}, e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> deleteManyTrnCd(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		Request deleteRequest = null;
		DataMap<String, Object> deleteParams = null;
		List<String> trnCdList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			deleteRequest = new Request();
			
			trnCdList = params.getList("trnCdList");
			
			log.debug("__DBGLOG__ trnCdList; [{}]", trnCdList);
			
			deleteRequest.setHeader(header);
			
			for(String trnCd:trnCdList) {
				deleteParams = new DataMap<>();
				deleteParams.put("trnCd", trnCd);
				deleteRequest.setParameter(deleteParams);
				caller.callService("TrnCdService", "deleteTrnCd", deleteRequest);
			}
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteManyTrnCd Exception 발생 : {}", e);
			throw new BizException("error.delete.msg", new String[] {"거래코드"}, e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> inqTranByGroupPermissions(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		String grpCd = null;
		List<Tuple> inqList = null;
		Set<DataMap<String, Object>> svcSet = null;
		List<DataMap<String, Object>> svcList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			params = request.getParameter();
			svcList = new ArrayList<>();
			grpCd = params.getString("grpCd");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(grpCd)) {
				log.error("__ERRLOG__ 그룹코드 미입력");
				throw new BizException("error.required", new String[] {"그룹코드"}); 
			}
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			inqList = trnCdRepository.findAllSvcMthByGrpPermissions(grpCd, Constants.NO);
			svcSet = new HashSet<>();
			
			for(Tuple t : inqList) {
				DataMap<String, Object> map = new DataMap<>();
				map.put("svcNm", t.get(QTrnCdEntity.trnCdEntity).getSvcNm());
				map.put("trnCd", t.get(QTrnCdEntity.trnCdEntity).getSvcNm());
				svcSet.add(map);
			}
			
			for(DataMap<String, Object> svcs : svcSet) {
				List<DataMap<String, Object>> childrens = new ArrayList<>();
				for(Tuple t : inqList) {
					log.debug("__DBGLOG__ tuple: {}", t);
					DataMap<String, Object> map = new DataMap<>();
					if(svcs.getString("svcNm").equals(t.get(QTrnCdEntity.trnCdEntity).getSvcNm())) {
						map.put("trnCd", t.get(QTrnCdEntity.trnCdEntity).getTrnCd());
						map.put("trnNm", t.get(QTrnCdEntity.trnCdEntity).getTrnNm());
						map.put("mtdNm", t.get(QTrnCdEntity.trnCdEntity).getMtdNm());
						
						if(t.get(QTrnGroupAuthorityEntity.trnGroupAuthorityEntity) == null) {
							map.put("grpCd", "");
						} else {
							map.put("grpCd", t.get(QTrnGroupAuthorityEntity.trnGroupAuthorityEntity).getGrpCd());
						}
						map.put("svcNm", t.get(QTrnCdEntity.trnCdEntity).getMtdNm());
//						map.put("svcNm", StringUtil.concat(t.get(QTrnCdEntity.trnCdEntity).getMtdNm()
//														 , " "
//														 , "["
//														 , t.get(QTrnCdEntity.trnCdEntity).getTrnNm()
//														 , "/"
//														 , t.get(QTrnCdEntity.trnCdEntity).getTrnCd()
//														 , "]"));
						childrens.add(map);
					}
				}
				
				//메소드명 정렬
				childrens.sort(Comparator.comparing(
					(DataMap<String, Object> dataMap) -> dataMap.getString("mtdNm")
				));
				svcs.put("children", childrens);
			}
			
			svcList = new ArrayList<>(svcSet);
			
			//서비스명 정렬
			svcList.sort(Comparator.comparing(
				(DataMap<String, Object> map) -> map.getString("svcNm")
			));
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("svcList", svcList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqTranByGroupPermissions Exception 발생 : {}", e);
			throw new BizException("error.data.not-found.msg", new String[] {"거래권한"}, e);
		}
		
		return result;
	}


	@Override
	public DataMap<String, Object> saveTranByGroupPermissions(Request request) {
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		String grpCd = null;
		List<String> trnCds = null;
		List<TrnGroupAuthorityEntity> trnAuthList = null;
		
		Set<String> newTrnCds = null;
		Set<String> delTrnCds = null;
		Set<String> trnCdSet = null;
		Set<String> trnAuthIdSet = null;
		
		TrnGroupAuthorityEntity trnGrpEntity = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			params = request.getParameter();
			header = request.getHeader();
			trnCds = params.getList("trnCds");
			trnCdSet = new HashSet<>(trnCds);
			grpCd = params.getString("grpCd");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(grpCd)) {
				log.error("__ERRLOG__ 그룹코드 미입력");
				throw new BizException("error.required", new String[] {"grpCd"}); 
			}
			
			//===================================================================================
			// 해당 그룹의 메뉴권한 조회
			//===================================================================================
			trnAuthList = trnGroupAuthorityRepository.findAllByGrpCdAndDelYn(grpCd, Constants.NO);
			trnAuthIdSet = new HashSet<>(trnAuthList.stream().map(item -> item.getTrnCd()).collect(Collectors.toList()));
			log.debug("__DBGLOG__ 권한목록 {}", trnAuthList);
			
			//신규 메뉴ID
			newTrnCds = new HashSet<>(trnCdSet);
			newTrnCds.removeAll(trnAuthIdSet);
			
			log.debug("__DBGLOG__ new권한목록 {}", newTrnCds);
			
			//삭제 메뉴ID
			delTrnCds = new HashSet<>(trnAuthIdSet);
			delTrnCds.removeAll(trnCdSet);
			
			log.debug("__DBGLOG__ 삭제권한목록 {}", delTrnCds);
			
			//===================================================================================
			// 권한 및 로그등록
			//===================================================================================
			for(String newTrnCd : newTrnCds) {
				//권한등록
				trnGrpEntity = new TrnGroupAuthorityEntity();
				trnGrpEntity.setTrnCd(newTrnCd);
				trnGrpEntity.setGrpCd(grpCd);
				trnGrpEntity.setChgUid(header.getTrnUserId());
				trnGrpEntity.setChgDtm(header.getCurrDtm());
				trnGrpEntity.setDelYn(Constants.NO);
				trnGrpEntity.setLastTrnUUID(header.getUuid());
				trnGrpEntity.setLastTrnUid(header.getTrnUserId());
				trnGrpEntity.setLastTrnCd(header.getTrnCd());
				
				try {
					
					trnGroupAuthorityRepository.saveAndFlush(trnGrpEntity);
					
				} catch (DataAccessException e) {
					log.error("__ERRLOG__ saveMenusByGroupPermissions DataAccessException 발생 : {}", e);
					throw new BizException("error.delete.prefix", new String[] {"메뉴권한"}, e);
				}
				
				//로그등록
				saveTranByGroupPermissions(newTrnCd, grpCd, Constants.CHG_TYPE_CD.CREATE.getCode(), header.getCurrDtm(), header.getUuid(), header.getTrnUserId(), header.getTrnCd());
				
			}
			
			//===================================================================================
			// 권한삭제 및 로그등록
			//===================================================================================
			for(String delTrnCd : delTrnCds) {
				//권한삭제
				TrnGroupAuthorityKeyEntity key = new TrnGroupAuthorityKeyEntity(delTrnCd, grpCd);
				try {
					
					trnGroupAuthorityRepository.deleteById(key);
					
				} catch (DataAccessException e) {
					log.error("__ERRLOG__ saveMenusByGroupPermissions DataAccessException 발생 : {}", e);
					throw new BizException("error.regist.prefix", new String[] {"메뉴권한"}, e);
				}
				
				//로그등록
				saveTranByGroupPermissions(delTrnCd, grpCd, Constants.CHG_TYPE_CD.DELETE.getCode(), header.getCurrDtm(), header.getUuid(), header.getTrnUserId(), header.getTrnCd());
				
			}
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ saveTranByGroupPermissions Exception 발생 : {}", e);
			throw new BizException("error.regist.prefix", new String[] {"거래권한"}, e);
		}
		
		return result;
	}
	
	private void saveTranByGroupPermissions(String trnCd, String grpCd, String chgTypeCd, Instant trnDtm, UUID uuid, String uid, String execTrnCd) {
		TrnGroupAuthorityLogEntity logEntity = null;
		
		int seq = 0;
		
		try {
			
			seq = trnCdRepository.findMaxTrnAuthLogSeq(trnCd, grpCd);
			logEntity = new  TrnGroupAuthorityLogEntity();
			logEntity.setTrnCd(trnCd);
			logEntity.setGrpCd(grpCd);
			logEntity.setSeq(++seq);
			logEntity.setTrnDtm(trnDtm);
			logEntity.setAuthChgTypeCd(chgTypeCd);
			logEntity.setLastTrnUUID(uuid);
			logEntity.setLastTrnUid(uid);
			logEntity.setLastTrnCd(execTrnCd);
			
			trnGroupAuthorityLogRepository.saveAndFlush(logEntity);
			
		} catch (DataAccessException e) {
			log.error("__ERRLOG__ saveTranByGroupPermissions DataAccessException 발생 : {}", e);
			throw new BizException("error.regist.prefix", new String[] {"거래권한로그"}, e);
		}
	}
}
