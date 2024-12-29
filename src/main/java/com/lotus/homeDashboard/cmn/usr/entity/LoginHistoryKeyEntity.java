package com.lotus.homeDashboard.cmn.usr.entity;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryKeyEntity implements Serializable {
	
	private static final long serialVersionUID = -3310695413538370518L;
	
	private String uid;
	private Instant loginDtm;

}
