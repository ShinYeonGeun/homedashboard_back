package com.lotus.homeDashboard.cmn.usr.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lotus.homeDashboard.cmn.usr.entity.UserMenuEntity;

public interface UserMenuRepository extends JpaRepository<UserMenuEntity, Integer>{
	@Query(value  = ""
			+ "WITH RECURSIVE MENUS AS ("
			+ "    SELECT A.MENU_ID, A.MENU_NM, A.UPPER_MENU_ID, A.PATH, A.SEQ, 1 AS level, A.MENU_ID AS ROOT_MENU_ID"
			+ "      FROM hdsbd.TCMN03 A"
			+ "      JOIN hdsbd.TCMN04 AA"
			+ "		   ON AA.GRP_CD IN (:grpCds)"
			+ "		  AND AA.MENU_ID = A.MENU_ID"
			+ "     WHERE A.UPPER_MENU_ID IS NULL"
			+ "    UNION ALL"
			+ "    SELECT B.MENU_ID, B.MENU_NM, B.UPPER_MENU_ID, B.path, b.SEQ, C.level + 1 AS level, C.ROOT_MENU_ID"
			+ "      FROM hdsbd.TCMN03 B"
			+ "      JOIN MENUS C "
			+ "        ON B.UPPER_MENU_ID = C.MENU_ID"
			+ "      JOIN hdsbd.TCMN04 D"
			+ "		   ON D.GRP_CD IN (:grpCds)"
			+ "		  AND D.MENU_ID = B.MENU_ID)"
			+ "SELECT DISTINCT "
			+ "	      C.MENU_ID"
			+ "     , C.MENU_NM"
			+ "     , C.UPPER_MENU_ID"
			+ "     , C.PATH"
			+ "     , C.SEQ"
			+ "     , C.level"
			+ "     , C.ROOT_MENU_ID"
			+ "  FROM MENUS C "
			+ " ORDER BY C.ROOT_MENU_ID "
			+ "	       , C.LEVEL "
			+ "	       , C.SEQ; ", nativeQuery = true)
	public List<UserMenuEntity> findUserMenuList(@Param("grpCds") List<String> grpCds);
}
