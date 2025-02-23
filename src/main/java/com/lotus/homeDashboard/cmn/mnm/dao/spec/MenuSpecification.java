package com.lotus.homeDashboard.cmn.mnm.dao.spec;

import org.springframework.data.jpa.domain.Specification;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;

public class MenuSpecification {
	
	public static Specification<MenuEntity> eqMenuId(String menuId) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("menuId"), menuId);
	}
	
	public static Specification<MenuEntity> eqUpperMenuId(int upperMenuId) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.equal(root.get("upperMenuId"), upperMenuId);
	}
	
	public static Specification<MenuEntity> containsMenuNm(String menuNm) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("menuNm")), CommonSpecification.buildLikeString(menuNm.toLowerCase()));
	}
	
	public static Specification<MenuEntity> containsPath(String path) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(criteriaBuilder.lower(root.get("path")), CommonSpecification.buildLikeString(path.toLowerCase()));
	}
	
	// 같거나 큰 조건
	public static Specification<MenuEntity> greaterThanEqualSeq(int seq) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.greaterThanOrEqualTo(root.get("seq"), seq);
	}
	
	// 같거나 작은
	public static Specification<MenuEntity> lessThanEqualSeq(int seq) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.lessThanOrEqualTo(root.get("seq"), seq);
	}
}
