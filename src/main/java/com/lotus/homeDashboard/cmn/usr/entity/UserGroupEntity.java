package com.lotus.homeDashboard.cmn.usr.entity;

import com.lotus.homeDashboard.common.component.CommonEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="TUSR12", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserGroupKeyEntity.class)
public class UserGroupEntity extends CommonEntity {
	
	@Id
	private String uid;
	
	@Id
	@Column(name = "GRP_CD")
	private String grpCd;
	
}
