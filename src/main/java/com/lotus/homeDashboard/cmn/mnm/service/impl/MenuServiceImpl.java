package com.lotus.homeDashboard.cmn.mnm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.cmn.mnm.dao.MenuRepository;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.cmn.mnm.service.MenuService;
import com.lotus.homeDashboard.cmn.usr.dao.UserMenuRepository;
import com.lotus.homeDashboard.cmn.usr.entity.UserMenuEntity;
import com.lotus.homeDashboard.common.component.DataMap;

import lombok.extern.slf4j.Slf4j;

@Service("MenuService")
@Slf4j
public class MenuServiceImpl implements MenuService {

	@Autowired
	private MenuRepository menuRepository;
	
	@Autowired
	private UserMenuRepository userMenuRepository;
	
	@Override
	public MenuEntity findMenuById(int menuId) {
		Optional<MenuEntity> menu = menuRepository.findById(menuId);
		log.debug("★★ DBGLOG menu:{}", menu);
//		findUserMenuList();
		return menu.get();
	}

	@Override
	public List<DataMap<String,Object>> findUserMenuList(List<String> grpCds) {
		List<UserMenuEntity> list = userMenuRepository.findUserMenuList(grpCds);
		List<UserMenuEntity> menuList = new ArrayList<>();
		List<DataMap<String,Object>> result = new ArrayList<>();
		
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
	
	private DataMap<String,Object> userMenuEntityToMap(UserMenuEntity entity) {
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
