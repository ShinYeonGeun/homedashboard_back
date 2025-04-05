package com.lotus.homeDashboard.common.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityEntity;
import com.lotus.homeDashboard.common.entity.TrnGroupAuthorityKeyEntity;

public interface TrnGroupAuthorityRepository extends JpaRepository<TrnGroupAuthorityEntity, TrnGroupAuthorityKeyEntity> {
	public List<TrnGroupAuthorityEntity> findAllByGrpCdAndDelYn(String grpCd, String delYn);
}
