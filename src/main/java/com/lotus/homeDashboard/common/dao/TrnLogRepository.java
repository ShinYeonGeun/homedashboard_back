package com.lotus.homeDashboard.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.common.entity.TrnLogEntity;
import com.lotus.homeDashboard.common.entity.TrnLogKeyEntity;

public interface TrnLogRepository extends JpaRepository<TrnLogEntity, TrnLogKeyEntity>, TrnLogDslRepository {

}
