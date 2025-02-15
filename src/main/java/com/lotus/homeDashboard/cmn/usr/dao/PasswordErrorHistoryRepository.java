package com.lotus.homeDashboard.cmn.usr.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.usr.entity.PasswordErrorHistoryEntity;
import com.lotus.homeDashboard.cmn.usr.entity.PasswordErrorHistoryKeyEntity;


public interface PasswordErrorHistoryRepository extends JpaRepository<PasswordErrorHistoryEntity, PasswordErrorHistoryKeyEntity>, PasswordErrorHistoryDslRepository {

}
