package com.lotus.homeDashboard.cmn.mnm.service;

import java.util.List;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;

public interface MenuService {
	public MenuEntity findMenuById(int menuId);
	public List<DataMap<String,Object>> inqUserMenuList(Request request);
}
