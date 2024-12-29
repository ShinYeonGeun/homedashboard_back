package com.lotus.homeDashboard.cmn.usr.entity;

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
@Table(name="TUSR11", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity extends CommonEntity{
	
	@Id
	@Column(name = "GRP_CD")
	private String grpCd;
	
	@Column(name = "GRP_NM")
	private String grpNm;
	
	@Column
	private String remark;
	
}
