package com.lotus.homeDashboard.common.dao;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.Tuple;

public interface TrnLogDslRepository {
	
	public Page<Tuple> findAllTrnLogAndTrnInfo(Instant from, Instant to, String reqResDstcd, String resultCd, String trnCd, String uid, UUID uuid, Pageable paging);
	
}
