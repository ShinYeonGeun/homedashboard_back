package com.lotus.homeDashboard.common.dao.spec;

import org.springframework.data.jpa.domain.Specification;

import com.lotus.homeDashboard.common.component.CommonEntity;

public class CommonSpecification {
	
	public static String buildLikeString(String arg) {
		return buildLikeString(true, arg, true);
	}
	
	public static String buildLikeString(boolean isPrefix, String arg, boolean isSuffix) {
		StringBuilder builder = new StringBuilder();
		
		if(isPrefix) {
			builder.append("%");
		}
		
		builder.append(arg);
		
		if(isSuffix) {
			builder.append("%");
		}
		
		return builder.toString();
	}
	
	public static <E extends CommonEntity> Specification<E> alwaysTrue() {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.literal(1), 1);
	}
	
	public static <E extends CommonEntity> Specification<E> hasDelYn(String delYn) {
		return (root, query, criteriaBuilder) ->
        	criteriaBuilder.equal(root.get("delYn"), delYn);
	}
}
