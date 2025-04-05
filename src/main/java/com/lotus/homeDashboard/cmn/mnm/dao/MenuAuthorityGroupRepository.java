package com.lotus.homeDashboard.cmn.mnm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuGroupAuthorityEntity;

public interface MenuAuthorityGroupRepository extends JpaRepository<MenuGroupAuthorityEntity, Integer> {
	
	@Query(value  = ""
			+ "WITH RECURSIVE MENUS AS ("
			+ "    SELECT A.MENU_ID, A.MENU_NM, A.UPPER_MENU_ID, A.PATH, A.SEQ, 1 AS level, A.MENU_ID AS ROOT_MENU_ID, AA.GRP_CD"
			+ "      FROM hdsbd.TCMN03 A"
			+ "      JOIN hdsbd.TCMN04 AA"
			+ "		   ON AA.GRP_CD IN (:grpCds)"
			+ "		  AND AA.MENU_ID = A.MENU_ID"
			+ "       AND AA.DEL_YN = 'N'"
			+ "     WHERE A.UPPER_MENU_ID IS NULL"
			+ "       AND A.DEL_YN = 'N'"
			+ "    UNION"
			+ "    SELECT B.MENU_ID, B.MENU_NM, B.UPPER_MENU_ID, B.path, b.SEQ, C.level + 1 AS level, C.ROOT_MENU_ID, D.GRP_CD"
			+ "      FROM hdsbd.TCMN03 B"
			+ "      JOIN MENUS C "
			+ "        ON B.UPPER_MENU_ID = C.MENU_ID"
			+ "      JOIN hdsbd.TCMN04 D"
			+ "		   ON D.GRP_CD IN (:grpCds)"
			+ "		  AND D.MENU_ID = B.MENU_ID"
			+ "       AND D.DEL_YN = 'N'"
			+ "     WHERE B.DEL_YN = 'N')"
			+ "SELECT C.MENU_ID"
			+ "     , C.MENU_NM"
			+ "     , C.UPPER_MENU_ID"
			+ "     , C.PATH"
			+ "     , C.SEQ"
			+ "     , C.level"
			+ "     , C.ROOT_MENU_ID"
			+ "     , C.GRP_CD"
			+ "  FROM MENUS C "
			+ " ORDER BY C.LEVEL "
			+ "	       , C.SEQ; ", nativeQuery = true)
	public List<MenuGroupAuthorityEntity> findUserMenuList(@Param("grpCds") List<String> grpCds);
	
	@Query(value  = ""
			+ "WITH RECURSIVE MENUS AS ("
			+ "    SELECT A.MENU_ID, A.MENU_NM, A.UPPER_MENU_ID, A.PATH, A.SEQ, 1 AS level, A.MENU_ID AS ROOT_MENU_ID, AA.GRP_CD"
			+ "      FROM hdsbd.TCMN03 A"
			+ "      LEFT OUTER JOIN hdsbd.TCMN04 AA"
			+ "		   ON AA.GRP_CD = :grpCd"
			+ "		  AND AA.MENU_ID = A.MENU_ID"
			+ "       AND AA.DEL_YN = 'N'"
			+ "     WHERE A.UPPER_MENU_ID IS NULL"
			+ "       AND A.DEL_YN = 'N'"
			+ "    UNION"
			+ "    SELECT B.MENU_ID, B.MENU_NM, B.UPPER_MENU_ID, B.path, b.SEQ, C.level + 1 AS level, C.ROOT_MENU_ID, D.GRP_CD"
			+ "      FROM hdsbd.TCMN03 B"
			+ "      JOIN MENUS C"
			+ "        ON B.UPPER_MENU_ID = C.MENU_ID"
			+ "      LEFT OUTER JOIN hdsbd.TCMN04 D"
			+ "		   ON D.GRP_CD = :grpCd"
			+ "		  AND D.MENU_ID = B.MENU_ID"
			+ "       AND D.DEL_YN = 'N'"
			+ "     WHERE B.DEL_YN = 'N')"
			+ "SELECT C.MENU_ID"
			+ "     , C.MENU_NM"
			+ "     , C.UPPER_MENU_ID"
			+ "     , C.PATH"
			+ "     , C.SEQ"
			+ "     , C.level"
			+ "     , C.ROOT_MENU_ID"
			+ "     , C.GRP_CD"
			+ "  FROM MENUS C"
			+ " ORDER BY C.LEVEL" 
			+ "	       , C.SEQ;", nativeQuery = true)
	public List<MenuGroupAuthorityEntity> findMenusByGroupPermissions(@Param("grpCd") String grpCd);
}
