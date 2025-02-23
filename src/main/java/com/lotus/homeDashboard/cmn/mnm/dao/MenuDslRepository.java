package com.lotus.homeDashboard.cmn.mnm.dao;

public interface MenuDslRepository {
	public int findMaxSeqByUpperMenuId(int upperMenuId);
	
	public int incrementSeqOfSiblingsFrom(int upperMenuId, int seq);
}
