package com.lotus.homeDashboard.cmn.mnm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityLogEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityLogKeyEntity;

public interface MenuAuthorityLogRepository extends JpaRepository<MenuAuthorityLogEntity, MenuAuthorityLogKeyEntity> {

}
