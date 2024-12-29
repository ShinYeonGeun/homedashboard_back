package com.lotus.homeDashboard.cmn.usr.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lotus.homeDashboard.cmn.usr.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
	

}
