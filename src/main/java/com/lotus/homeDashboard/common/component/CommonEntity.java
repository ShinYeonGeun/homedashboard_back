package com.lotus.homeDashboard.common.component;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class CommonEntity {
	@Column(name = "DEL_YN")
	private String delYn;
	
	@Column(name = "LAST_TRN_UUID")
	private UUID lastTrnUUID;
	
	@Column(name = "LAST_TRN_DTM", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Instant lastTrnDtm;
	
	@Column(name = "LAST_TRN_UID")
	private String lastTrnUid;
}
