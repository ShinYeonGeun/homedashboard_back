package com.lotus.homeDashboard.common.entity;

import java.io.Serializable;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrnGroupAuthorityKeyEntity implements Serializable {
	
	private static final long serialVersionUID = -2268843094134475851L;
	private String trnCd;
	private String grpCd;
	
}
