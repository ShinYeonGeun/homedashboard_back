package com.lotus.homeDashboard.cmn.usr.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.lotus.homeDashboard.cmn.usr.dao.UserGroupDslRepository;
import com.lotus.homeDashboard.cmn.usr.entity.QGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.QUserGroupEntity;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class UserGroupDslRepositoryImpl implements UserGroupDslRepository {
	
	@Autowired
	private JPAQueryFactory jpaQueryFactory;

	@Override
	public List<Tuple> findUserGroupJoinGrpInfo(String uid, String delYn) {
		QUserGroupEntity userGroupEntity = QUserGroupEntity.userGroupEntity;
		QGroupEntity groupEntity = QGroupEntity.groupEntity;
		BooleanBuilder conditions = new BooleanBuilder(userGroupEntity.uid.eq(uid));
		
		JPAQuery<Tuple> list = jpaQueryFactory
							.select(userGroupEntity, groupEntity)
							.from(userGroupEntity)
							.leftJoin(groupEntity)
							.on(userGroupEntity.grpCd.eq(groupEntity.grpCd));
		
		
		if(!StringUtil.isEmpty(delYn)) {
			conditions.and(userGroupEntity.delYn.eq(delYn));
		}
		
		list.where(conditions);
		
		return list.fetch();
	}

}
