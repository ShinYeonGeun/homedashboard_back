package com.lotus.homeDashboard.cmn.ccd.dao;

import java.util.List;

import com.querydsl.core.Tuple;

public interface CommonCodeDslRepository {
	public List<Tuple> findAllCommonCodeNDetail(List<String> codeList, String codeDelYn, String codeDetailDelYn);
}
