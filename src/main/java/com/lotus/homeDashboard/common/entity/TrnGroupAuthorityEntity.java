package com.lotus.homeDashboard.common.entity;

import java.time.Instant;

import com.lotus.homeDashboard.common.component.CommonEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="TCMN07", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TrnGroupAuthorityKeyEntity.class)
public class TrnGroupAuthorityEntity extends CommonEntity {

	@Id
	@Column(name = "TRN_CD")
	private String trnCd;
	
	@Id
	@Column(name = "GRP_CD")
	private String grpCd;
	
	@Column(name = "CHG_UID")
	private String chgUid;
	
	@Column(name = "CHG_DTM")
	private Instant chgDtm;

}
