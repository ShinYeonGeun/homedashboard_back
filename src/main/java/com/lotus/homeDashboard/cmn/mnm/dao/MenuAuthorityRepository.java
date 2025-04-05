package com.lotus.homeDashboard.cmn.mnm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.MenuAuthorityKeyEntity;

public interface MenuAuthorityRepository extends JpaRepository<MenuAuthorityEntity, MenuAuthorityKeyEntity> {
	public List<MenuAuthorityEntity> findAllByGrpCdAndMenuIdInAndDelYn(@Param("grpCd") String grpCd, @Param("menuIds") List<Integer> menuIds, String delYn);
	
	public List<MenuAuthorityEntity> findAllByGrpCdAndDelYn(String grpCd, String delYn);
}
