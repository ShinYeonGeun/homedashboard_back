package com.lotus.homeDashboard.cmn.usr.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="TCMN03", schema = "hdsbd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuEntity {
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
	
	@Column
	private int level;
	
	@Column(name = "ROOT_MENU_ID")
	private int rootMenuId;
	
	@Transient
	private List<UserMenuEntity> children;
}
