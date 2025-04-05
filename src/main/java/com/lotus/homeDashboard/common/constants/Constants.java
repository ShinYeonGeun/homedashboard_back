package com.lotus.homeDashboard.common.constants;

public class Constants {
	public static final String CHANNEL_CODE = "DBO"; //채널코드 대시보드 온라인
	public static final String LOGIN_URI = "/login"; //로그인 URI
	public static final String LOGOUT_URI = "/logout"; //로그인 URI
	public static final String EXTENSION_JWT_PERIOD = "/extensionJWTPeriod";
	public static final String HEALTH_CHECK_URI = "/healthCheck"; //HealthCheck URI
	
	public static final String YES = "Y";
	public static final String NO = "N";
	
	public static final String BLANK = ""; //빈 문자열
	public static final String DOT = ".";
	public static final String ESCAPE_DOT = "\\.";
	
	public static final String AUTH = "Bearer";
	
	public static final long SVC_TIMEOUT = 30000L; //타임아웃시간
	public static final long SVC_TIMEOUT_INF = -1L; //타임아웃시간 무한
	public static final long SVC_TIMEOUT_MIN = 0L; //타임아웃시간 최솟값
	
	public static final int SVC_INVOKER_POOL_SIZE = 10; //serviceInvoker threadpool size
	
	
	public enum CHG_TYPE_CD {
		
		CREATE("C"),
		MODIFY("U"),
		DELETE("D")
		;
		
		private String code;

		private CHG_TYPE_CD(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
}
