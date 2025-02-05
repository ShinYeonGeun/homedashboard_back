package com.lotus.homeDashboard.cmn.mnm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.cmn.mnm.dao.MenuRepository;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.cmn.mnm.service.MenuService;
import com.lotus.homeDashboard.cmn.usr.dao.UserMenuRepository;
import com.lotus.homeDashboard.cmn.usr.entity.UserMenuEntity;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service("MenuService")
@Slf4j
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
			grpCds = request.getParameter().get("grpCd") == null ? new ArrayList<>():(List<String>) request.getParameter().get("grpCd");
		} else {
			Request userGrpParams = new Request();
			DataMap<String, Object> userGrpParamMap = new DataMap<>();
			userGrpParamMap.put("uid", uid);
			userGrpParamMap.put("delYn", Constants.NO);
			userGrpParams.setHeader(request.getHeader());
			userGrpParams.setParameter(userGrpParamMap);
			List<DataMap<String, Object>> grpList = null;
			try {
				grpList = (List<DataMap<String, Object>>) serviceInvoker.invoke("UserService", "inqUserGrpList", userGrpParams).getPayload();
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

}
