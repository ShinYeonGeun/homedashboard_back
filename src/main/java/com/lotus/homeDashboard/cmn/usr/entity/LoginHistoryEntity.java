package com.lotus.homeDashboard.cmn.usr.entity;

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
@Table(name="TUSR02", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(LoginHistoryKeyEntity.class)
public class LoginHistoryEntity extends CommonEntity {
	
	@Id
	private String uid;
	
	@Id
	@Column(name = "LOGIN_DTM", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Instant loginDtm;
	
	@Column
	private String ip;

}
