package com.lotus.homeDashboard.cmn.mnm.entity;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuAuthorityLogKeyEntity implements Serializable {

	private static final long serialVersionUID = -5530543806793186262L;
	
	private Integer menuId;
	
	private String grpCd;
	
	private Integer seq;
}
