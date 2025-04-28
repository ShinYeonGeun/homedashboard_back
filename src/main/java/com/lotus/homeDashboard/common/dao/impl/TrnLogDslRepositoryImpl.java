package com.lotus.homeDashboard.common.dao.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.lotus.homeDashboard.common.dao.TrnLogDslRepository;
import com.lotus.homeDashboard.common.entity.QTrnCdEntity;
import com.lotus.homeDashboard.common.entity.QTrnLogEntity;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class TrnLogDslRepositoryImpl implements TrnLogDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Override
	public Page<Tuple> findAllTrnLogAndTrnInfo(Instant from, Instant to, String reqResDstcd, String resultCd, String trnCd, String uid, UUID uuid,
			Pageable paging) {
		
		QTrnLogEntity trnLogEntity = QTrnLogEntity.trnLogEntity;
		QTrnCdEntity trnCdEntity = QTrnCdEntity.trnCdEntity;
		
		BooleanBuilder conditions = new BooleanBuilder();
		List<Tuple> list = null;
		JPAQuery<Tuple> query = jpaQueryFactory
								.select(trnLogEntity, trnCdEntity)
								.from(trnLogEntity)
								.leftJoin(trnCdEntity)
								.on(trnLogEntity.trnCd.eq(trnCdEntity.trnCd));
		
		conditions.and(trnLogEntity.trnDtm.goe(from));
		conditions.and(trnLogEntity.trnDtm.loe(to));
		
		if(!StringUtil.isEmpty(reqResDstcd)) {
			conditions.and(trnLogEntity.reqResDstcd.eq(reqResDstcd));
		}
		
		if(!StringUtil.isEmpty(resultCd)) {
			conditions.and(trnLogEntity.resultCd.eq(resultCd));
		}
		
		if(!StringUtil.isEmpty(uid)) {
			conditions.and(trnLogEntity.uid.eq(uid));
		}
		
		if(uuid != null) {
			conditions.and(trnLogEntity.uuid.eq(uuid));
		}
		
		query.where(conditions);
		query.offset(paging.getOffset()); //페이지번호
		query.limit(paging.getPageSize());
		
		list = query.fetch();
		
		JPAQuery<Long> countQuery = jpaQueryFactory
						.select(trnLogEntity.count())
						.from(trnLogEntity)
						.where(conditions);
		
		return PageableExecutionUtils.getPage(list, paging, countQuery::fetchOne);
	}

}
