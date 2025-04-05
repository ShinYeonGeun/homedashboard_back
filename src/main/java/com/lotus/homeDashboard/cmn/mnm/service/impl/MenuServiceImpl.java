package com.lotus.homeDashboard.cmn.mnm.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

import com.lotus.homeDashboard.cmn.mnm.dao.MenuAuthorityGroupRepository;
import com.lotus.homeDashboard.cmn.mnm.dao.MenuAuthorityLogRepository;
import com.lotus.homeDashboard.cmn.mnm.dao.MenuAuthorityRepository;
import com.lotus.homeDashboard.cmn.mnm.dao.MenuRepository;
import com.lotus.homeDashboard.cmn.mnm.dao.spec.MenuSpecification;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityKeyEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityLogEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuGroupAuthorityEntity;
import com.lotus.homeDashboard.cmn.mnm.service.MenuService;
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
	private MenuAuthorityGroupRepository menuGroupAuthorityRepository;
	
	@Autowired
	private MenuAuthorityLogRepository menuAuthorityLogRepository;
	
	@Autowired
	private MenuAuthorityRepository menuAuthorityRepository;
	
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
		List<MenuGroupAuthorityEntity> list = null;
		List<MenuGroupAuthorityEntity> menuList = new ArrayList<>();
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
		
		list = menuGroupAuthorityRepository.findUserMenuList(grpCds);
		list  = list.stream()
                .collect(Collectors.toMap(
                		MenuGroupAuthorityEntity::getMenuId,   // menuId를 키로 사용
                        menu -> menu,            // 첫 번째 등장한 엔티티 유지
                        (existing, replacement) -> existing  // 중복시 기존 값 유지
                    ))
                    .values()  // Map의 값들만 추출
                    .stream()  // 다시 스트림으로 변환
                    .collect(Collectors.toList());  // 리스트로 변환
		
		for(MenuGroupAuthorityEntity e : list) {
			Stream<MenuGroupAuthorityEntity> stream = list.stream();
			e.setChildren(stream.filter(i -> i.getUpperMenuId()!= null && e.getMenuId() == i.getUpperMenuId()).toList());
		}
		
		menuList = list.stream().filter(e -> e.getMenuId() == e.getRootMenuId()).toList();
		
		for(MenuGroupAuthorityEntity e : menuList) {
			log.debug("__DBGLOG__ 최종 메뉴 목록 :{}", e);
			result.add(this.userMenuEntityToMap(e));
		}
		
		return result;
	}
	
	protected DataMap<String,Object> userMenuEntityToMap(MenuGroupAuthorityEntity entity) {
		
		DataMap<String,Object> map = new DataMap<>();
		
		map.put("menuId", entity.getMenuId());
		map.put("menuNm", entity.getMenuNm());
		map.put("upperMenuId", entity.getUpperMenuId());
		map.put("path", entity.getPath());
		map.put("seq", entity.getSeq());
		map.put("level", entity.getLevel());
		map.put("grpCd", entity.getGrpCd());

		if(entity.getChildren() != null && entity.getChildren().size() > 0) {
			List<DataMap<String,Object>> children = new ArrayList<>();
			for(MenuGroupAuthorityEntity e:entity.getChildren()) {
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
			throw new BizException("error.inquiry", e);
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
						throw new BizException("error.regist.data.exists.msg", new String[] {StringUtil.concat("메뉴: ", String.valueOf(menuId))});
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
				throw new BizException("error.regist.prefix", new String[] {"메뉴"}, e);
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
			throw new BizException("error.regist.prefix", new String[] {"메뉴"}, e);
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
				throw new BizException("error.required", new String[] {"메뉴ID"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 메뉴정보조회 시작");
			
			//===================================================================================
			// 메뉴정보 조회
			//===================================================================================
			optionalEntity = menuRepository.findById(menuId);
			
			if(optionalEntity.isEmpty() || Constants.YES.equals(optionalEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 등록된 메뉴 미존재 [{}]", menuId);
				throw new BizException("error.data.not-found.msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
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
					throw new BizException("error.modify.prefix", new String[] {"메뉴"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateMenu DataAccessException 발생 : {}", e);
				throw new BizException("error.modify.prefix", new String[] {"메뉴"}, e);
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
			throw new BizException("error.modify.prefix", new String[] {"거래코드"}, e);
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
				throw new BizException("error.required", new String[] {"메뉴ID"}); 
			}
			
			log.debug("__DBGLOG__ 메뉴정보조회 시작");
			
			//===================================================================================
			// 메뉴정보 조회
			//===================================================================================
			optionalEntity = menuRepository.findById(menuId);
			
			if(optionalEntity.isEmpty()) {
				log.error("__ERRLOG__ 등록된 메뉴 미존재 [{}]", menuId);
				throw new BizException("error.data.not-found.msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
			} 
			
			if(Constants.YES.equals(optionalEntity.get().getDelYn())) {
				log.error("__ERRLOG__ 이미 삭제된 메뉴 [{}]", menuId);
				throw new BizException("error.delete.already_msg", new String[] {StringUtil.concat("메뉴ID: ", String.valueOf(menuId))});
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
					throw new BizException("error.delete.prefix", new String[] {"메뉴"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteMenu DataAccessException 발생 : {}", e);
				throw new BizException("error.delete.prefix", new String[] {"메뉴"}, e);
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
			throw new BizException("error.delete.prefix", new String[] {"메뉴"}, e);
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
			throw new BizException("error.delete.msg", new String[] {"메뉴"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> inqMenusByGroupPermissions(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		String grpCd = null;
		List<MenuGroupAuthorityEntity> inqList = null;
		List<DataMap<String, Object>> menuList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			params = request.getParameter();
			menuList = new ArrayList<>();
			grpCd = params.getString("grpCd");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(grpCd)) {
				log.error("__ERRLOG__ 그룹코드 미입력");
				throw new BizException("error.required", new String[] {"grpCd"}); 
			}
			
			//===================================================================================
			// 거래코드 조회
			//===================================================================================
			inqList = menuGroupAuthorityRepository.findMenusByGroupPermissions(grpCd);
			
			for(MenuGroupAuthorityEntity e : inqList) {
				Stream<MenuGroupAuthorityEntity> stream = inqList.stream();
				e.setChildren(stream.filter(i -> i.getUpperMenuId()!= null && e.getMenuId() == i.getUpperMenuId()).toList());
			}
			
			inqList = inqList.stream().filter(e -> e.getMenuId() == e.getRootMenuId()).toList();
			
			for(MenuGroupAuthorityEntity e : inqList) {
				menuList.add(this.userMenuEntityToMap(e));
			}
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("menuList", menuList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqMenusByGroupPermissions Exception 발생 : {}", e);
			throw new BizException("error.data.not-found.msg", new String[] {"메뉴"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> saveMenusByGroupPermissions(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		List<Integer> menuIds = null;
		List<MenuAuthorityEntity> menuAuthList = null;
		
		Set<Integer> newIds = null;
		Set<Integer> delIds = null;
		Set<Integer> menuIdSet = null;
		Set<Integer> menuAuthIdSet = null;
		
		MenuAuthorityEntity menuAuthEntity = null;
		String grpCd = "";
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			params = request.getParameter();
			header = request.getHeader();
			menuIds = params.getList("menuIds");
			menuIdSet = new HashSet<>(menuIds);
			grpCd = params.getString("grpCd");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(grpCd)) {
				log.error("__ERRLOG__ 그룹코드 미입력");
				throw new BizException("error.required", new String[] {"그룹코드"}); 
			}
			
			//===================================================================================
			// 해당 그룹의 메뉴권한 조회
			//===================================================================================
			menuAuthList = menuAuthorityRepository.findAllByGrpCdAndDelYn(grpCd, Constants.NO);
			menuAuthIdSet = new HashSet<>(menuAuthList.stream().map(item -> item.getMenuId()).collect(Collectors.toList()));
			log.debug("__DBGLOG__ 권한목록 {}", menuAuthList);
			
			//신규 메뉴ID
			newIds = new HashSet<>(menuIdSet);
			newIds.removeAll(menuAuthIdSet);
			
			log.debug("__DBGLOG__ new권한목록 {}", newIds);
			
			//삭제 메뉴ID
			delIds = new HashSet<>(menuAuthIdSet);
			delIds.removeAll(menuIdSet);
			
			log.debug("__DBGLOG__ 삭제권한목록 {}", delIds);
			
			//===================================================================================
			// 권한 및 로그등록
			//===================================================================================
			for(int newId : newIds) {
				//권한등록
				menuAuthEntity = new MenuAuthorityEntity();
				menuAuthEntity.setMenuId(newId);
				menuAuthEntity.setGrpCd(grpCd);
				menuAuthEntity.setChgUid(header.getTrnUserId());
				menuAuthEntity.setChgDtm(header.getCurrDtm());
				menuAuthEntity.setDelYn(Constants.NO);
				menuAuthEntity.setLastTrnUUID(header.getUuid());
				menuAuthEntity.setLastTrnUid(header.getTrnUserId());
				menuAuthEntity.setLastTrnCd(header.getTrnCd());
				
				try {
					
					menuAuthorityRepository.saveAndFlush(menuAuthEntity);
					
				} catch (DataAccessException e) {
					log.error("__ERRLOG__ saveMenusByGroupPermissions DataAccessException 발생 : {}", e);
					throw new BizException("error.delete.prefix", new String[] {"메뉴권한"}, e);
				}
				
				//로그등록
				saveMenuAuthLog(newId, grpCd, Constants.CHG_TYPE_CD.CREATE.getCode(), header.getCurrDtm(), header.getUuid(), header.getTrnUserId(), header.getTrnCd());
				
			}
			
			//===================================================================================
			// 권한삭제 및 로그등록
			//===================================================================================
			for(int delId : delIds) {
				//권한삭제
				MenuAuthorityKeyEntity key = new MenuAuthorityKeyEntity(delId, grpCd);
				
				try {
					
					menuAuthorityRepository.deleteById(key);
					
				} catch (DataAccessException e) {
					log.error("__ERRLOG__ saveMenusByGroupPermissions DataAccessException 발생 : {}", e);
					throw new BizException("error.regist.prefix", new String[] {"메뉴권한"}, e);
				}
				
				//로그등록
				saveMenuAuthLog(delId, grpCd, Constants.CHG_TYPE_CD.DELETE.getCode(), header.getCurrDtm(), header.getUuid(), header.getTrnUserId(), header.getTrnCd());
				
			}
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ saveMenusByGroupPermissions Exception 발생 : {}", e);
			throw new BizException("error.regist.prefix", new String[] {"메뉴"}, e);
		}
		
		return result;
	}
	
	/**
	 * 메뉴변경로그 등록
	 * @param menuId
	 * @param grpCd
	 * @param chgTypeCd
	 * @param uuid
	 * @param uid
	 * @param trnCd
	 */
	private void saveMenuAuthLog(int menuId, String grpCd, String chgTypeCd, Instant trnDtm, UUID uuid, String uid, String trnCd) {
		
		MenuAuthorityLogEntity logEntity = null;
		int seq = 0;
		
		try {
			
			seq = menuRepository.findMaxMenuAuthLogSeq(menuId, grpCd);
			
			logEntity = new MenuAuthorityLogEntity();
			logEntity.setMenuId(menuId);
			logEntity.setGrpCd(grpCd);
			logEntity.setSeq(++seq);
			logEntity.setTrnDtm(trnDtm);
			logEntity.setAuthChgTypeCd(chgTypeCd);
			logEntity.setLastTrnUUID(uuid);
			logEntity.setLastTrnUid(uid);
			logEntity.setLastTrnCd(trnCd);
			
			menuAuthorityLogRepository.saveAndFlush(logEntity);
			
		} catch (DataAccessException e) {
			log.error("__ERRLOG__ saveMenuAuthLog DataAccessException 발생 : {}", e);
			throw new BizException("error.regist.prefix", new String[] {"메뉴권한로그"}, e);
		}
		
	}

}
