package com.lotus.homeDashboard.cmn.usr.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.lotus.homeDashboard.cmn.usr.dao.UserLogDslRepository;
import com.lotus.homeDashboard.cmn.usr.entity.QUserLogEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class UserLogDslRepositoryImpl implements UserLogDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	@Override
	public Integer findMaxByTrnDtAndUid(String trnDt, String uid) {
		
		QUserLogEntity userLogEntity = QUserLogEntity.userLogEntity;
		
		Integer result = jpaQueryFactory.select(userLogEntity.seq.max())
										.from(userLogEntity)
										.where(userLogEntity.trnDt.eq(trnDt)
												, userLogEntity.uid.eq(uid) )
										.groupBy(userLogEntity.trnDt, userLogEntity.uid).fetchOne();
		
		return result == null ? 0 : result.intValue();
	}

}
