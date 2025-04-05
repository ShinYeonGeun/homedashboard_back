package com.lotus.homeDashboard.cmn.mnm.dao;

import java.time.Instant;

public interface MenuDslRepository {
	
	public int findMaxSeqByUpperMenuId(int upperMenuId);
	
	public int incrementSeqOfSiblingsFrom(int upperMenuId, int seq);
	
	public int findMaxMenuAuthLogSeq(int menuId, String grpCd);
}
