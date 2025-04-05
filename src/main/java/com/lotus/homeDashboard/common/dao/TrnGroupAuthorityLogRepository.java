package com.lotus.homeDashboard.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityLogEntity;
import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityLogKeyEntity;

public interface TrnGroupAuthorityLogRepository extends JpaRepository<TrnGroupAuthorityLogEntity, TrnGroupAuthorityLogKeyEntity> {
	
}
