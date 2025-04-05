package com.lotus.homeDashboard.cmn.usr.dao.spec;

import org.springframework.data.jpa.domain.Specification;

import com.lotus.homeDashboard.cmn.usr.entity.UserEntity;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;

public class UserSpecification {
	
	public static Specification<UserEntity> eqUid(String uid) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("uid"), uid);
	}
	
	public static Specification<UserEntity> containsUid(String uid) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("uid")), CommonSpecification.buildLikeString(uid.toLowerCase()));
	}
	
	public static Specification<UserEntity> eqUserState(String state) {
		return (root, query, criteriaBuilder) ->
    		criteriaBuilder.equal(root.get("userState"), state);
	}
}
