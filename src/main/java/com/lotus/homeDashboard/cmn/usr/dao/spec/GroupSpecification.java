package com.lotus.homeDashboard.cmn.usr.dao.spec;

import org.springframework.data.jpa.domain.Specification;

import com.lotus.homeDashboard.cmn.usr.entity.GroupEntity;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;

public class GroupSpecification {

	public static Specification<GroupEntity> eqGrpCd(String grpCd) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("grpCd"), grpCd);
	}
	
	public static Specification<GroupEntity> containsGrpNm(String grpNm) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("grpNm")), CommonSpecification.buildLikeString(grpNm.toLowerCase()));
	}
	
	public static Specification<GroupEntity> containsRemark(String remark) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("remark")), CommonSpecification.buildLikeString(remark.toLowerCase()));
	}
}
