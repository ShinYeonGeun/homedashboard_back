package com.lotus.homeDashboard.cmn.mnm.dao.impl;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;

import com.lotus.homeDashboard.cmn.mnm.dao.MenuDslRepository;
import com.lotus.homeDashboard.cmn.mnm.entity.QMenuAuthorityLogEntity;
import com.lotus.homeDashboard.cmn.mnm.entity.QMenuEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class MenuDslRepositoryImpl implements MenuDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Override
	public int findMaxSeqByUpperMenuId(int upperMenuId) {
		
		QMenuEntity entity = QMenuEntity.menuEntity;
		
		Integer result = jpaQueryFactory.select(entity.seq.max())
							.from(entity)
							.where(entity.upperMenuId.eq(upperMenuId))
							.fetchOne();

		return result == null ? 0 : result.intValue();
	}

	@Override
	public int incrementSeqOfSiblingsFrom(int upperMenuId, int seq) {

		QMenuEntity entity = QMenuEntity.menuEntity;
		
		long result = jpaQueryFactory.update(entity)
										.set(entity.seq, entity.seq.add(1))
										.where(entity.upperMenuId.eq(upperMenuId)
											 , entity.seq.goe(seq))
										.execute();
										
		
		return (int) result;
	}

	@Override
	public int findMaxMenuAuthLogSeq(int menuId, String grpCd) {
		QMenuAuthorityLogEntity entity = QMenuAuthorityLogEntity.menuAuthorityLogEntity;
		Integer result = jpaQueryFactory.select(entity.seq.max())
						.from(entity)
						.where(entity.menuId.eq(menuId)
								, entity.grpCd.eq(grpCd))
						.fetchOne();
		
		return result == null ? 0 : result.intValue();
	}

}
