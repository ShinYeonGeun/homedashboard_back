package com.lotus.homeDashboard.common.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.dao.TrnLogRepository;
import com.lotus.homeDashboard.common.entity.QTrnCdEntity;
import com.lotus.homeDashboard.common.entity.QTrnLogEntity;
import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.service.TrnLogService;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.Tuple;

import lombok.extern.slf4j.Slf4j;

@Service("TrnLogService")
@Slf4j
public class TrnLogServiceImpl implements TrnLogService {
	
	@Autowired
	private TrnLogRepository trnLogRepository;

	@Override
	public TrnLogEntity saveTrnLogWithEntity(TrnLogEntity entity) {
		return trnLogRepository.saveAndFlush(entity);
	}

	@Override
	public TrnLogEntity saveTrnLog(Request request) {
		TrnLogEntity logEntity = null;
		CommonHeader header = null;
		DataMap<String, Object> params = null;
		
		header = request.getHeader();
		params = request.getParameter();
		
		logEntity = new TrnLogEntity();
		logEntity.setUuid(header.getUuid());
		logEntity.setReqResDstcd(params.getString("reqResDstcd"));
		logEntity.setTrnCd(header.getTrnCd());
		logEntity.setUid(header.getTrnUserId());
		logEntity.setUri(header.getRequestUri());
		logEntity.setIp(header.getRequestIp());
		logEntity.setContent(params.getString("content"));
		logEntity.setResultCd(params.getString("resultCode"));
		return trnLogRepository.saveAndFlush(logEntity);
	}

	@Override
	public DataMap<String, Object> inqTrnLogList(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		List<DataMap<String, Object>> trnLogList = null;
		Pageable pageable = null;
		Page<Tuple> inqList = null;
		Instant from = null;
		Instant to = null;
		UUID uuid = null;
		
		DateTimeFormatter formatter = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			trnLogList = new ArrayList<>();
			params = request.getParameter();	
//			conditions = Specification.where(CommonSpecification.alwaysTrue());
			pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("trnDtm").descending());
			formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			
			//===================================================================================
			// 필수값 검증
			//===================================================================================
			if(StringUtil.isEmpty(params.getString("fromDt"))) {
				log.error("__ERRLOG__ 조회시작일자 미입력");
				throw new BizException("error.required", new String[] {"조회시작일자"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("toDt"))) {
				log.error("__ERRLOG__ 조회종료일자 미입력");
				throw new BizException("error.required", new String[] {"조회종료일자"}); 
			}

			//===================================================================================
			// 거래코드목록 조회
			//===================================================================================
			from = LocalDateTime.parse(StringUtil.concat(params.getString("fromDt"), params.getString("fromTime")), formatter).toInstant(ZoneOffset.UTC);
			to = LocalDateTime.parse(StringUtil.concat(params.getString("toDt"), params.getString("toTime")), formatter).toInstant(ZoneOffset.UTC);
			
			if(!StringUtil.isEmpty(params.getString("uuid"))) {
				uuid = UUID.fromString(params.getString("uuid"));
			}
			
			inqList = trnLogRepository.findAllTrnLogAndTrnInfo(from
															 , to
															 , params.getString("reqResDstcd", "")
															 , params.getString("resultCd", "")
															 , params.getString("trnCd", "")
															 , params.getString("uid", "")
															 , uuid
															 , pageable);
			log.debug("inqList ggggg");
			log.debug("inqList {}", inqList);
			for(Tuple t : inqList) {
				log.debug("{}", t);
			}
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.putAll(CommonUtil.extractPageValues(inqList));
			
			if(inqList.getSize() > 0) {
				
				for(Tuple tuple: inqList.getContent()) {
					DataMap<String, Object> data = new DataMap<>();
					
					String trnNm = "";
					
					data.put("trnDtm", tuple.get(QTrnLogEntity.trnLogEntity).getTrnDtm());
					data.put("trnCd", tuple.get(QTrnLogEntity.trnLogEntity).getTrnCd());
					
					if(tuple.get(QTrnCdEntity.trnCdEntity) != null) {
						trnNm = tuple.get(QTrnCdEntity.trnCdEntity).getTrnNm();
					}
					
					data.put("trnNm", trnNm);
					data.put("reqResDstcd", tuple.get(QTrnLogEntity.trnLogEntity).getReqResDstcd());
					data.put("resultCd", tuple.get(QTrnLogEntity.trnLogEntity).getResultCd());
					data.put("uid", tuple.get(QTrnLogEntity.trnLogEntity).getUid());
					data.put("uuid", tuple.get(QTrnLogEntity.trnLogEntity).getUuid());
					data.put("uri", tuple.get(QTrnLogEntity.trnLogEntity).getUri());
					data.put("content", tuple.get(QTrnLogEntity.trnLogEntity).getContent());
					trnLogList.add(data);
				}
				
			}
			
			result.put("trnLogList", trnLogList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqTrnCdList Exception 발생 : {}", e);
			throw new BizException("error.inquiry", e);
		}
		
		return result;
	}

}
