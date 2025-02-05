package com.lotus.homeDashboard.common.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="TCMN00", schema = "hdsbd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TrnLogKeyEntity.class)
public class TrnLogEntity {

	@Id
	private UUID uuid;
	
	@Id
	@Column(name = "REQ_RES_DSTCD")
	private String reqResDstcd;
	
	@Column(name = "TRN_DTM", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Instant trnDtm;
	
	@Column(name = "RESULT_CD")
	private String resultCode;
	
	@Column(name = "TRN_CD")
	private String trnCd;
	
	@Column
	private String uri;
	
	@Column
	private String ip;
	
	@Column
	private String uid;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
}
