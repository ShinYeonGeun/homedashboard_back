package com.lotus.homeDashboard.common.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.lotus.homeDashboard.cmn.usr.entity.QUserGroupEntity;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.dao.TrnCdDslRepository;
import com.lotus.homeDashboard.common.entity.QTrnCdEntity;
import com.lotus.homeDashboard.common.entity.QTrnGroupAuthorityEntity;
import com.lotus.homeDashboard.common.entity.QTrnGroupAuthorityLogEntity;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class TrnCdDslRepositoryImpl implements TrnCdDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Tuple> findAllSvcMthByGrpPermissions(String grpCd, String delYn) {
		QTrnGroupAuthorityEntity trnGroupAuthorityEntity = QTrnGroupAuthorityEntity.trnGroupAuthorityEntity;
		QTrnCdEntity trnCdEntity = QTrnCdEntity.trnCdEntity;
		
		BooleanBuilder conditions = new BooleanBuilder();
		JPAQuery<Tuple> list = jpaQueryFactory
								.select(trnCdEntity, trnGroupAuthorityEntity)
								.from(trnCdEntity)
								.leftJoin(trnGroupAuthorityEntity)
								.on(trnCdEntity.trnCd.eq(trnGroupAuthorityEntity.trnCd)
									, trnGroupAuthorityEntity.grpCd.eq(grpCd)
									, trnGroupAuthorityEntity.delYn.eq(Constants.NO));
		
		if(!StringUtil.isEmpty(delYn)) {
			conditions.and(trnCdEntity.delYn.eq(delYn));
		}
		
		list.where(conditions);
		list.orderBy(trnCdEntity.svcNm.asc(), trnCdEntity.mtdNm.asc());
		
		return list.fetch();
	}

	@Override
	public long findCountTrnGroupAuth(String trnCd, String uid) {
		
		QUserGroupEntity userGroup = QUserGroupEntity.userGroupEntity;
		QTrnGroupAuthorityEntity trnGroupAuth = QTrnGroupAuthorityEntity.trnGroupAuthorityEntity;
		
		long cnt = jpaQueryFactory.select(trnGroupAuth.count())
					.from(trnGroupAuth, userGroup)
					.where(
						trnGroupAuth.grpCd.eq(userGroup.grpCd)
						, trnGroupAuth.trnCd.eq(trnCd)
						, trnGroupAuth.delYn.eq(Constants.NO)
						, userGroup.uid.eq(uid)
						, userGroup.delYn.eq(Constants.NO)
							).fetchOne();
		
		return cnt;
	}

	@Override
	public int findMaxTrnAuthLogSeq(String trnCd, String grpCd) {
		QTrnGroupAuthorityLogEntity entity = QTrnGroupAuthorityLogEntity.trnGroupAuthorityLogEntity;
		Integer result = jpaQueryFactory.select(entity.seq.max())
				.from(entity)
				.where(entity.trnCd.eq(trnCd)
						, entity.grpCd.eq(grpCd))
				.fetchOne();

		return result == null ? 0 : result.intValue();
	}

}
