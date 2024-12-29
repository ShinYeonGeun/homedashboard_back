package com.lotus.homeDashboard.cmn.usr.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.usr.entity.LoginHistoryEntity;
import com.lotus.homeDashboard.cmn.usr.entity.LoginHistoryKeyEntity;

public interface LoginHistoryRepository extends JpaRepository<LoginHistoryEntity, LoginHistoryKeyEntity>{

}
