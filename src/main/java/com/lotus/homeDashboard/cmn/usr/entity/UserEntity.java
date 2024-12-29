package com.lotus.homeDashboard.cmn.usr.entity;

import java.time.Instant;

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
@Table(name="TUSR01", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends CommonEntity {
	@Id
	private String uid;
	
	@Column
	private String pswd;
	
	@Column(name = "LAST_LOGIN_DTM", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Instant lastLoginDtm;
	
	@Column(name = "PSWD_ERR_CNT")
	private Integer pswdErrCnt;
	
	@Column(name = "USER_STATE")
	private String userState;

}
