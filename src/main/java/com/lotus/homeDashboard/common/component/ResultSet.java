package com.lotus.homeDashboard.common.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSet {
	private String resultCd;
	private Object payload;
}
