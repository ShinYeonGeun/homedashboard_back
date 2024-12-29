package com.lotus.homeDashboard.cmn.usr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="TUSR03", schema = "hdsbd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenEntity {
	
	@Id
	private String uid;
	
	@Column(name = "SECURITY_KEY")
	private String securityKey;
	
	@Column(name = "ACCESS_TOKEN")
	private String accessToken;
	
	@Column(name = "REFRESH_TOKEN")
	private String refreshToken;
	
}
