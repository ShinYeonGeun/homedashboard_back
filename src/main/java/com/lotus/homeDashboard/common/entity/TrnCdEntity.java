package com.lotus.homeDashboard.common.entity;

import com.lotus.homeDashboard.common.component.CommonEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="TCMN05", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrnCdEntity extends CommonEntity {

	@Id
	@Column(name = "TRN_CD")
	private String trnCd;
	
	@Column(name = "TRN_NM")
	private String trnNm;
	
	@Column(name = "SVC_NM")
	private String svcNm;
	
	@Column(name = "MTD_NM")
	private String mtdNm;
}
