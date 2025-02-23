package com.lotus.homeDashboard.cmn.usr.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.lotus.homeDashboard.cmn.usr.dao.PasswordErrorHistoryDslRepository;
import com.lotus.homeDashboard.cmn.usr.entity.QPasswordErrorHistoryEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class PasswordErrorHistoryDslRepositoryImpl implements PasswordErrorHistoryDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Override
	public Integer findMaxSeqByTrnDtAndUid(String trnDt, String uid) {
		QPasswordErrorHistoryEntity entity = QPasswordErrorHistoryEntity.passwordErrorHistoryEntity;
		
		Integer result = jpaQueryFactory.select(entity.seq.max().coalesce(0))
							.from(entity)
							.where( entity.trnDt.eq(trnDt)
								  , entity.uid.eq(uid))
							.groupBy(entity.trnDt, entity.uid).fetchOne();
		
		return result == null ? 0 : result.intValue();
	}

}
