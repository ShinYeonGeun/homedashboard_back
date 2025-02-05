package com.lotus.homeDashboard.cmn.ccd.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.lotus.homeDashboard.cmn.ccd.dao.CommonCodeDslRepository;
import com.lotus.homeDashboard.cmn.ccd.entity.QCommonCodeDetailEntity;
import com.lotus.homeDashboard.cmn.ccd.entity.QCommonCodeEntity;
import com.lotus.homeDashboard.cmn.usr.dao.impl.UserGroupDslRepositoryImpl;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Repository
public class CommonCodeDslRepositoryImpl implements CommonCodeDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	@Override
	public List<Tuple> findAllCommonCodeNDetail(List<String> codeList, String codeDelYn, String codeDetailDelYn) {
		QCommonCodeEntity commonCodeEntity = QCommonCodeEntity.commonCodeEntity;
		QCommonCodeDetailEntity commonCodeDetailEntity = QCommonCodeDetailEntity.commonCodeDetailEntity;
		JPAQuery<Tuple> select = null;
		BooleanBuilder conditions = new BooleanBuilder(); //where 1 = 1 
		
		select = jpaQueryFactory
				.select(commonCodeEntity, commonCodeDetailEntity)
				.from(commonCodeEntity)
				.leftJoin(commonCodeDetailEntity)
				.on(commonCodeEntity.code.eq(commonCodeDetailEntity.code));
		
		if(!codeList.isEmpty()) {
			conditions.and(commonCodeEntity.code.in(codeList));
		}
		
		if(!StringUtil.isEmpty(codeDelYn)) {
			conditions.and(commonCodeEntity.delYn.eq(codeDelYn));
		}
		
		if(!StringUtil.isEmpty(codeDetailDelYn)) {
			conditions.and(commonCodeDetailEntity.delYn.eq(codeDetailDelYn));
		}
		
		select.orderBy(commonCodeEntity.code.asc(), commonCodeDetailEntity.codeVal.asc());
		
		//조건추가
		select.where(conditions);
		
		return select.fetch();
	}
	
}
