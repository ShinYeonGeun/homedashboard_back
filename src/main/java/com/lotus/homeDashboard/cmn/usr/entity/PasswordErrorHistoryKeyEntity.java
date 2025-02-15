package com.lotus.homeDashboard.cmn.usr.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PasswordErrorHistoryKeyEntity implements Serializable {

	private static final long serialVersionUID = 5346465886674574603L;

	private String trnDt;

	private String uid;
	
	private Integer seq;
}
