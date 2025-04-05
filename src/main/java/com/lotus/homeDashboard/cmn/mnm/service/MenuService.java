package com.lotus.homeDashboard.cmn.mnm.service;

import java.util.List;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;

public interface MenuService {
	
	public MenuEntity findMenuById(int menuId);
	
	public List<DataMap<String,Object>> inqUserMenuList(Request request);
	
	public DataMap<String, Object> inqMenuList(Request request);
	
	public DataMap<String, Object> createMenu(Request request);
	
	public DataMap<String, Object> updateMenu(Request request);
	
	public DataMap<String, Object> deleteMenu(Request request);
	
	public DataMap<String, Object> deleteManyMenu(Request request);
	
	public DataMap<String, Object> inqMenusByGroupPermissions(Request request);
	
	public DataMap<String, Object> saveMenusByGroupPermissions(Request request);
}
