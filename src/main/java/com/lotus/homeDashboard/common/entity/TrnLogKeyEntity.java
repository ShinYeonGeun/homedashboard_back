package com.lotus.homeDashboard.common.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrnLogKeyEntity implements Serializable {

	private static final long serialVersionUID = -9095177728220498184L;

	private UUID uuid;
	private String reqResDstcd;
}
