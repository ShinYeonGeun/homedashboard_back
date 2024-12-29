package com.lotus.homeDashboard.cmn.usr.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.usr.entity.UserTokenEntity;

public interface UserTokenRepository extends JpaRepository<UserTokenEntity, String> {

}
