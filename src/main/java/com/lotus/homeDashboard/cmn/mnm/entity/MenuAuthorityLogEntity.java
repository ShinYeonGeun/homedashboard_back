package com.lotus.homeDashboard.cmn.mnm.entity;

import java.time.Instant;
import java.util.UUID;

import com.lotus.homeDashboard.common.component.CommonEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="TCMN05", schema = "hdsbd")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MenuAuthorityLogKeyEntity.class)
public class MenuAuthorityLogEntity {
	
	
	@Id
	@Column(name = "MENU_ID")
	private Integer menuId;
	
	@Id
	@Column(name = "GRP_CD")
	private String grpCd;
	
	@Id
	@Column
	private Integer seq;
	
	@Column(name = "TRN_DTM")
	private Instant trnDtm;
	
	@Column(name = "AUTH_CHG_TYPE_CD")
	private String authChgTypeCd;
	
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
	
	@PreUpdate
    public void preUpdate() {
		this.lastTrnDtm = Instant.now();
	}
	
	
}
