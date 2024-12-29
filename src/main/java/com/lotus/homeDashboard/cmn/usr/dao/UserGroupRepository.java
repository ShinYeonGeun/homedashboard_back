package com.lotus.homeDashboard.cmn.usr.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.usr.entity.UserGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserGroupKeyEntity;


public interface UserGroupRepository extends JpaRepository <UserGroupEntity, UserGroupKeyEntity>, UserGroupDslRepository {
	
	public List<UserGroupEntity> findByUid(String uid);
	public List<UserGroupEntity> findByUidAndDelYn(String uid, String delYn);
}
