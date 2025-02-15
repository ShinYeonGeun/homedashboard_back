package com.lotus.homeDashboard.cmn.usr.entity;

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
@Table(name="TUSR05", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PasswordErrorHistoryKeyEntity.class)
public class PasswordErrorHistoryEntity extends CommonEntity {

	@Id
	@Column(name = "TRN_DT")
	private String trnDt;
	
	@Id
	private String  uid;
	
	@Id
	private Integer seq;
	
	@Column(name = "ERR_PSWD")
	private String errPswd;
}
