package com.lotus.homeDashboard.common.component;

import java.io.Serializable;

import lombok.Data;

@Data
public class Request implements Serializable {
	private static final long serialVersionUID = 7862977490690835869L;
	private CommonHeader header;
	private DataMap<String, Object> parameter;
}
