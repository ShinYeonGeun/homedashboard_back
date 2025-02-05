package com.lotus.homeDashboard.common.spec;

import org.springframework.data.jpa.domain.Specification;

import com.lotus.homeDashboard.common.entity.TrnCdEntity;

public class TrnCdSpecification {
	
	public static Specification<TrnCdEntity> eqTrnCd(String trnCd) {
		return (root, query, criteriaBuilder) ->
		criteriaBuilder.equal(root.get("trnCd"), trnCd);
	}
	
	public static Specification<TrnCdEntity> containsTrnCd(String trnCd) {
		return (root, query, criteriaBuilder) ->
		criteriaBuilder.like(criteriaBuilder.upper(root.get("trnCd")), trnCd);
	}
	
	public static Specification<TrnCdEntity> containsTrnNm(String trnNm) {
		return (root, query, criteriaBuilder) ->
		criteriaBuilder.like(criteriaBuilder.upper(root.get("trnNm")), trnNm);
	}
	
	public static Specification<TrnCdEntity> containsSvcNm(String svcNm) {
		return (root, query, criteriaBuilder) ->
		criteriaBuilder.like(criteriaBuilder.upper(root.get("svcNm")), svcNm);
	}
	
	public static Specification<TrnCdEntity> containsMtdNm(String mtdNm) {
		return (root, query, criteriaBuilder) ->
		criteriaBuilder.like(criteriaBuilder.upper(root.get("mtdNm")), mtdNm);
	}
}
