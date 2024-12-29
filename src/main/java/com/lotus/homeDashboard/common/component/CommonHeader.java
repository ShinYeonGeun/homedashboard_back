package com.lotus.homeDashboard.common.component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommonHeader {
	private Instant currDtm;
	private String currDate; //현재일자
	private String currTime;//현재시각
	private String trnCd;//거래코드
	private UUID uuid;//UUID
	private String trnChnlCd;//채널코드
	private String trnUserId;
	private String requestIp;
	private String accessToken;
	
	private String authorization;
}
