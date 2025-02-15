package com.lotus.homeDashboard.common.component;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class CommonEntity {
	@Column(name = "DEL_YN" , insertable = false /*, columnDefinition = "VARCHAR(1) NOT NULL DEFAULT 'N'"*/)
	private String delYn;
	
	@Column(name = "LAST_TRN_UUID")
	private UUID lastTrnUUID;
	
	//insert 시 sql에 포함시키지 않아서 DB에 설정된 default값 적용
	//update는 매번 현재시각으로 preUpdate로 처리함.
	@Column(name = "LAST_TRN_DTM" /*, updatable = false*/, insertable = false) 
	private Instant lastTrnDtm;
	
	@Column(name = "LAST_TRN_UID")
	private String lastTrnUid;
	
	@Column(name = "LAST_TRN_CD")
	private String lastTrnCd;
	
	@PrePersist
    public void prePersist() {
        if (this.delYn == null) {
            this.delYn = "N";
        }
    }
	
	@PreUpdate
    public void preUpdate() {
		this.lastTrnDtm = Instant.now();
	}
}
