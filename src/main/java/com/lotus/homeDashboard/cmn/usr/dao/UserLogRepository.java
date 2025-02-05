package com.lotus.homeDashboard.cmn.usr.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.usr.entity.UserLogEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserLogKeyEntity;

public interface UserLogRepository extends JpaRepository<UserLogEntity, UserLogKeyEntity>, UserLogDslRepository {
	
}
