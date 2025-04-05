package com.lotus.homeDashboard.cmn.mnm.entity;

import java.io.Serializable;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuAuthorityKeyEntity implements Serializable {
	
	private static final long serialVersionUID = -2087502139762845540L;
	
	private Integer menuId;
	
	private String grpCd;
}
