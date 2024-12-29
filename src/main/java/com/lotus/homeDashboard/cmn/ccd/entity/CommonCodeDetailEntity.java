package com.lotus.homeDashboard.cmn.ccd.entity;

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
@Table(name="TCMN02", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommonCodeDetailKeyEntity.class)
public class CommonCodeDetailEntity extends CommonEntity {
	
	@Id
	private String code;
	
	@Id
	@Column(name = "CODE_VAL")
	private String codeVal;
	
	@Column(name = "CODE_VAL_CTNT")
	private String codeValCtnt;
}
