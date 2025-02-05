package com.lotus.homeDashboard.common.component;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class CommonEntity {
	@Column(name = "DEL_YN", nullable = false /*, columnDefinition = "VARCHAR(1) NOT NULL DEFAULT 'N'"*/)
	private String delYn;
	
	@Column(name = "LAST_TRN_UUID")
	private UUID lastTrnUUID;
	
	@Column(name = "LAST_TRN_DTM", nullable = false, /*updatable = false, insertable = false,*/ columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private Instant lastTrnDtm;
	
	@Column(name = "LAST_TRN_UID")
	private String lastTrnUid;
	
	@PrePersist
    public void prePersist() {
        if (this.delYn == null) {
            this.delYn = "N";
        }
        if(this.lastTrnDtm == null) {
        	this.lastTrnDtm = Instant.now();
        }
    }
}
