package com.lotus.homeDashboard.cmn.mnm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lotus.homeDashboard.cmn.mnm.dao.MenuRepository;
import com.lotus.homeDashboard.cmn.mnm.dao.spec.MenuSpecification;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.cmn.mnm.service.MenuService;
import com.lotus.homeDashboard.cmn.usr.dao.UserMenuRepository;
import com.lotus.homeDashboard.cmn.usr.entity.UserMenuEntity;
import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.utils.CommonUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service("MenuService")
@Slf4j
@Transactional
public class MenuServiceImpl implements MenuService {

	@Autowired
	private MenuRepository menuRepository;
	
	@Autowired
	private UserMenuRepository userMenuRepository;
	
	@Autowired
	private ServiceInvoker serviceInvoker;
	
	@Override
	public MenuEntity findMenuById(int menuId) {
		Optional<MenuEntity> menu = menuRepository.findById(menuId);
		log.debug("★★ DBGLOG menu:{}", menu);
		return menu.get();
	}

	@Override
	public List<DataMap<String,Object>> inqUserMenuList(Request request) {
		
		List<String> grpCds = null;
		List<UserMenuEntity> list = null;
		List<UserMenuEntity> menuList = new ArrayList<>();
		List<DataMap<String,Object>> result = new ArrayList<>();
		String uid = request.getParameter().getString("uid", "");
		
		//사용자ID 있으면 그룹 조회 후 메뉴 조회
		if(StringUtil.isEmpty(uid)) {
			//grpCds = request.getParameter().get("grpCd") == null ? new ArrayList<>():(List<String>) request.getParameter().get("grpCd");
			grpCds = request.getParameter().get("grpCd") == null ? new ArrayList<>(): request.getParameter().getList("grpCd");
		} else {
			Request userGrpParams = new Request();
			DataMap<String, Object> userGrpParamMap = new DataMap<>();
			ResultSet userGrpResult = null;
			userGrpParamMap.put("uid", uid);
			userGrpParamMap.put("delYn", Constants.NO);
			userGrpParams.setHeader(request.getHeader());
			userGrpParams.setParameter(userGrpParamMap);
			List<DataMap<String, Object>> grpList = null;
			
			try {
				//grpList = (List<DataMap<String, Object>>) serviceInvoker.invoke("UserService", "inqUserGrpList", userGrpParams).getPayload();
				userGrpResult = serviceInvoker.callService("UserService", "inqUserGrpList", userGrpParams);
				if(userGrpResult == null) {
					grpList = new ArrayList<>();
				} else {
					grpList = userGrpResult.payloadAsDataMapList();
				}
				
			} catch (Exception e) {
				log.error("__ERRLOG__ userService.inqUserGrpList 오류", e);
			}
			grpCds = grpList.stream().map(map -> map.getString("grpCd")).collect(Collectors.toList());
		}
		
		list = userMenuRepository.findUserMenuList(grpCds);
		
		for(UserMenuEntity e : list) {
			Stream<UserMenuEntity> stream = list.stream();
			e.setChildren(stream.filter(i -> i.getUpperMenuId()!= null && e.getMenuId() == i.getUpperMenuId()).toList());
		}
		
		menuList = list.stream().filter(e -> e.getMenuId() == e.getRootMenuId()).toList();
		
		for(UserMenuEntity e : menuList) {
			log.debug("__DBGLOG__ 최종 메뉴 목록 :{}", e);
			result.add(this.userMenuEntityToMap(e));
		}
		
		return result;
	}
	
	protected DataMap<String,Object> userMenuEntityToMap(UserMenuEntity entity) {
		
		DataMap<String,Object> map = new DataMap<>();
		
		map.put("menuId", entity.getMenuId());
		map.put("menuNm", entity.getMenuNm());
		map.put("upperMenuId", entity.getUpperMenuId());
		map.put("path", entity.getPath());
		map.put("seq", entity.getSeq());
		map.put("level", entity.getLevel());

		if(entity.getChildren() != null && entity.getChildren().size() > 0) {
			List<DataMap<String,Object>> children = new ArrayList<>();
			for(UserMenuEntity e:entity.getChildren()) {
				children.add(this.userMenuEntityToMap(e));
			}
			
			map.put("children", children);
		}
		
		log.debug("__DBGLOG__ userMenuEntityToMap [ {} ]", map);
		return map;
	}

	@Override
	public DataMap<String, Object> inqMenuList(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		List<DataMap<String, Object>> menuList = null;
		Pageable pageable = null;
		Specification<MenuEntity> conditions = null;
		Page<MenuEntity> inqList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			menuList = new ArrayList<>();
			params = request.getParameter();
			conditions = Specification.where(CommonSpecification.alwaysTrue());
			pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("upperMenuId").and(Sort.by("seq"))); //Sort.by("menuId")
			
			//===================================================================================
			// 쿼리 조건 조립
			//===================================================================================
			if(!StringUtil.isEmpty(params.getString("menuNm"))) {
				conditions = conditions.and(MenuSpecification.containsMenuNm(params.getString("menuNm")));
			}
			
			if(!StringUtil.isEmpty(params.getString("path"))) {
				conditions = conditions.and(MenuSpecification.containsMenuNm(params.getString("path")));
			}
			
			if(!StringUtil.isEmpty(params.getString("upperMenuId"))) {
				conditions = conditions.and(MenuSpecification.eqUpperMenuId(params.getInt("upperMenuId")));
			}
			
			if(!StringUtil.isEmpty(params.getString("delYn"))) {
				conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
			}
			
			//===================================================================================
			// 메뉴목록 조회
			//===================================================================================
			inqList = menuRepository.findAll(conditions, pageable);
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.putAll(CommonUtil.extractPageValues(inqList));
			
			if(inqList.getSize() > 0) {
				for(MenuEntity entity:inqList.getContent()) {
					DataMap<String, Object> data = new DataMap<>();
					data.put("menuId", entity.getMenuId());
					data.put("menuNm", entity.getMenuNm());
					data.put("upperMenuId", entity.getUpperMenuId());
					data.put("path", entity.getPath());
					data.put("seq", entity.getSeq());
					data.put("delYn", entity.getDelYn());
					data.put("lastTrnDtm", entity.getLastTrnDtm());
					data.put("lastTrnCd", entity.getLastTrnCd());
					data.put("lastTrnUUID", entity.getLastTrnUUID());
					data.put("lastTrnUid", entity.getLastTrnUid());
					menuList.add(data);
				}
			}
			
			result.put("menuList", menuList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqMenuList Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> createMenu(Request request) {
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<MenuEntity> optionalEntity =  Optional.empty();
		MenuEntity entity = null;
		int menuId = 0;
		int maxSeq = 0;
		Integer newSeq = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			
			menuId = params.getInt("menuId", 0);
			newSeq = params.getInteger("seq");
			
			log.debug("__DBGLOG__ 메뉴조회 시작");
			
			//===================================================================================
			// 메뉴 조회
			//===================================================================================
			if(0 < menuId) {
				optionalEntity = menuRepository.findById(menuId);
				
				if(optionalEntity.isPresent()) {
					entity = optionalEntity.get();
					if(!Constants.YES.equals(entity.getDelYn())) {
						log.error("__ERRLOG__ 기등록된 메뉴 존재 [{}]", menuId);
						throw new BizException("reg_data_exists_msg", new String[] {StringUtil.concat("메뉴: ", String.valueOf(menuId))});
					}
				} else {
					entity = new MenuEntity();
				}
			} else {
				entity = new MenuEntity();
			}
			
			log.debug("__DBGLOG__ 메뉴조회 종료");
			log.debug("__DBGLOG__ 메뉴등록 시작");
			
			//===================================================================================
			// 메뉴 등록
			//===================================================================================
			entity.setMenuNm(params.getString("menuNm"));
			entity.setUpperMenuId(params.getInteger("upperMenuId"));
			entity.setPath(params.getString("path"));
			
			if(entity.getUpperMenuId() != null) {
				
				log.debug("__DBGLOG__ upperMenuId 존재:[{}]", entity.getUpperMenuId());
				
				//상위메뉴가 있으면 최대순번값 조회
				maxSeq = menuRepository.findMaxSeqByUpperMenuId(entity.getUpperMenuId());
				log.debug("__DBGLOG__ 최대순번 조회결과: [{}]", maxSeq);
			} 
			
			if(newSeq == null) {
				log.debug("__DBGLOG__ seq 미존재 최대일련번호:[{}]", maxSeq);
				
				newSeq = ++maxSeq;
				
				log.debug("__DBGLOG__ seq 미존재 다음일련번호:[{}]", newSeq);
			} else {
				log.debug("__DBGLOG__ seq 존재:[{}]", newSeq);
				if(entity.getUpperMenuId() != null) {
					//시퀀스를 지정한 경우 형제메뉴 중 해당 시퀀스 포함 뒤 시퀀스 전부 + 1
					menuRepository.incrementSeqOfSiblingsFrom(entity.getUpperMenuId(), newSeq);
				}
			}
			
			entity.setSeq(newSeq);
			entity.setDelYn(Constants.NO);
			entity.setLastTrnUUID(header.getUuid());
			entity.setLastTrnUid(header.getTrnUserId());
			entity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				entity = menuRepository.saveAndFlush(entity);
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createMenu DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"메뉴"}, e);
			}
			
			log.debug("__DBGLOG__ 메뉴등록 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("menuInfo", entity);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ createMenu Exception 발생 : {}", e);
			throw new BizException("reg_error_prefix", new String[] {"메뉴"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> updateMenu(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<MenuEntity> optionalEntity =  Optional.empty();
		MenuEntity entity = null;
		
		int menuId = 0;
		Integer newSeq = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			menuId = params.getInt("menuId", 0);
			newSeq = params.getInteger("seq");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(menuId < 1) {
				log.error("__ERRLOG__ 메뉴ID 미입력");
				throw new BizException("val_required", new String[] {"메뉴ID"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 메뉴정보조회 시작");
			
			//===================================================================================
			// 메뉴정보 조회
			//===================================================================================
			optionalEntity = menuRepository.findById(menuId);
			
			if(optionalEntity.isEmpty() || Constants.YES.equals(optionalEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 등록된 메뉴 미존재 [{}]", menuId);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
			} else {
				entity = optionalEntity.get();
			}
			
			log.debug("__DBGLOG__ 메뉴정보조회 종료");
			log.debug("__DBGLOG__ 메뉴정보수정 시작");
			
			//===================================================================================
			// 메뉴정보 수정
			//===================================================================================
			entity.setMenuNm(params.getString("menuNm"));
			entity.setUpperMenuId(params.getInteger("upperMenuId"));
			entity.setPath(params.getString("path"));
			entity.setSeq(newSeq);
			entity.setDelYn(Constants.NO);
			entity.setLastTrnUUID(header.getUuid());
			entity.setLastTrnUid(header.getTrnUserId());
			entity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				entity = menuRepository.saveAndFlush(entity);
				
				if(entity == null) {
					log.error("__ERRLOG__ updateMenu saveAndFlush null");
					throw new BizException("chg_error_prefix", new String[] {"메뉴"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateMenu DataAccessException 발생 : {}", e);
				throw new BizException("chg_error_prefix", new String[] {"메뉴"}, e);
			}
			
			log.debug("__DBGLOG__ 메뉴정보수정 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("menuInfo", entity);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ updateMenu Exception 발생 : {}", e);
			throw new BizException("chg_error_prefix", new String[] {"거래코드"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> deleteMenu(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<MenuEntity> optionalEntity =  Optional.empty();
		MenuEntity entity = null;
		
		int menuId = 0;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			menuId = params.getInt("menuId", 0);
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(menuId < 1) {
				log.error("__ERRLOG__ 메뉴ID 미입력");
				throw new BizException("val_required", new String[] {"메뉴ID"}); 
			}
			
			log.debug("__DBGLOG__ 메뉴정보조회 시작");
			
			//===================================================================================
			// 메뉴정보 조회
			//===================================================================================
			optionalEntity = menuRepository.findById(menuId);
			
			if(optionalEntity.isEmpty()) {
				log.error("__ERRLOG__ 등록된 메뉴 미존재 [{}]", menuId);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
			} 
			
			if(Constants.YES.equals(optionalEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 이미 삭제된 메뉴 [{}]", menuId);
				throw new BizException("del_already_msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
			}
			
			log.debug("__DBGLOG__ 메뉴정보조회 종료");
			log.debug("__DBGLOG__ 메뉴정보삭제 시작");
			
			//===================================================================================
			// 메뉴정보 조회
			//===================================================================================
			entity = optionalEntity.get();
			entity.setDelYn(Constants.YES);
			entity.setLastTrnUUID(header.getUuid());
			entity.setLastTrnUid(header.getTrnUserId());
			entity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				entity = menuRepository.saveAndFlush(entity);
				
				if(entity == null) {
					log.error("__ERRLOG__ deleteMenu saveAndFlush null");
					throw new BizException("del_error_prefix", new String[] {"메뉴"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteMenu DataAccessException 발생 : {}", e);
				throw new BizException("del_error_prefix", new String[] {"메뉴"}, e);
			}
			
			log.debug("__DBGLOG__ 메뉴정보삭제 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("menuInfo", entity);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteMenu Exception 발생 : {}", e);
			throw new BizException("del_error_prefix", new String[] {"메뉴"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> deleteManyMenu(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		Request deleteRequest = null;
		DataMap<String, Object> deleteParams = null;
		List<Integer> menuIdList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			deleteRequest = new Request();
			
			menuIdList = params.getList("menuIdList");
			
			log.debug("__DBGLOG__ menuIdList; [{}]", menuIdList);
			
			deleteRequest.setHeader(header);
			
			for(Integer menuId:menuIdList) {
				deleteParams = new DataMap<>();
				deleteParams.put("menuId", menuId);
				deleteRequest.setParameter(deleteParams);
				serviceInvoker.callService("MenuService", "deleteMenu", deleteRequest);
			}
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteManyMenu Exception 발생 : {}", e);
			throw new BizException("del_error_msg", new String[] {"메뉴"}, e);
		}
		
		return result;
	}

}
