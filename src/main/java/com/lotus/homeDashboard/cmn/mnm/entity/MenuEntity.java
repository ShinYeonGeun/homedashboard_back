package com.lotus.homeDashboard.cmn.mnm.entity;


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
@Table(name="TCMN03", schema = "hdsbd")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MenuEntity extends CommonEntity {
	
	@Id
	@Column(name="MENU_ID")
	private int menuId;
	
	@Column(name="MENU_NM")
	private String menuNm;
	
	@Column(name = "UPPER_MENU_ID")
	private Integer upperMenuId;
	
	@Column
	private String path;
	
	@Column
	private int seq;

}
