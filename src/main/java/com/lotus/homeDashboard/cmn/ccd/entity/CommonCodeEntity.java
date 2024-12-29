package com.lotus.homeDashboard.cmn.ccd.entity;

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
@Table(name="TCMN01", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeEntity extends CommonEntity {

	@Id
	private String code;
	
	@Column
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;

}
