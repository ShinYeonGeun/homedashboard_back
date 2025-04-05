package com.lotus.homeDashboard.common.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.lotus.homeDashboard.common.entity.TrnCdEntity;

public interface TrnCdRepository extends JpaRepository<TrnCdEntity, String>, JpaSpecificationExecutor<TrnCdEntity>, TrnCdDslRepository {

}
