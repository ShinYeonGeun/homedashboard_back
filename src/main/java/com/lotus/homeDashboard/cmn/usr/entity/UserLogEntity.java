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
@Table(name="TUSR04", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserLogKeyEntity.class)
public class UserLogEntity extends CommonEntity {	
	
	@Id
	@Column(name = "TRAN_DT")
	private String tranDt;
	
	@Id
	private String  uid;
	
	@Id
	private Integer seq;
	
	@Column(name = "USER_CHG_TYPE_CD")
	private String userChgTypeCd;

}
