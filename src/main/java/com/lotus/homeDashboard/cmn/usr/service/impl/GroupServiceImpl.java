package com.lotus.homeDashboard.cmn.usr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lotus.homeDashboard.cmn.usr.dao.GroupRepository;
import com.lotus.homeDashboard.cmn.usr.dao.spec.GroupSpecification;
import com.lotus.homeDashboard.cmn.usr.entity.GroupEntity;
import com.lotus.homeDashboard.cmn.usr.service.GroupService;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service("GroupService")
@Slf4j
@Transactional
public class GroupServiceImpl implements GroupService {
	
	@Autowired
	private GroupRepository groupRepository;
	
	@Override
	public DataMap<String, Object> inqGroupList(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		List<DataMap<String, Object>> groupList = null;
		Pageable pageable = null;
		Specification<GroupEntity> conditions = null;
		Page<GroupEntity> inqList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			groupList = new ArrayList<>();
			params = request.getParameter();
			conditions = Specification.where(CommonSpecification.alwaysTrue());
			pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("grpCd").and(Sort.by("grpNm"))); 
			
			//===================================================================================
			// 쿼리 조건 조립
			//===================================================================================
			if(!StringUtil.isEmpty(params.getString("grpCd"))) {
				conditions = conditions.and(GroupSpecification.eqGrpCd(params.getString("grpCd")));
			}
			
			if(!StringUtil.isEmpty(params.getString("grpNm"))) {
				conditions = conditions.and(GroupSpecification.containsGrpNm(params.getString("grpNm")));
			}
			
			if(!StringUtil.isEmpty(params.getString("remark"))) {
				conditions = conditions.and(GroupSpecification.containsRemark(params.getString("remark")));
			}
			
			if(!StringUtil.isEmpty(params.getString("delYn"))) {
				conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
			}
			
			//===================================================================================
			// 메뉴목록 조회
			//===================================================================================
			inqList = groupRepository.findAll(conditions, pageable);
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.putAll(CommonUtil.extractPageValues(inqList));
			
			if(inqList.getSize() > 0) {
				for(GroupEntity entity:inqList.getContent()) {
					DataMap<String, Object> data = new DataMap<>();
					data.put("grpCd", entity.getGrpCd());
					data.put("grpNm", entity.getGrpNm());
					data.put("remark", entity.getRemark());
					data.put("delYn", entity.getDelYn());
					data.put("lastTrnDtm", entity.getLastTrnDtm());
					data.put("lastTrnCd", entity.getLastTrnCd());
					data.put("lastTrnUUID", entity.getLastTrnUUID());
					data.put("lastTrnUid", entity.getLastTrnUid());
					groupList.add(data);
				}
			}
			
			result.put("groupList", groupList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqGroupList Exception 발생 : {}", e);
			throw new BizException("error.inquiry", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> createGroup(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataMap<String, Object> updateGroup(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataMap<String, Object> deleteGroup(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataMap<String, Object> deleteManyGroup(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
