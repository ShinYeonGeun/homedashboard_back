package com.lotus.homeDashboard.cmn.usr.constants;

public class UserConstants {
	
	public enum USER_GROUP_CODE {
//		00	Admin	관리자
//		99	일반사용자	일반사용자
		ADMIN("00")
		, GENERAL("99")
		;
		
		private String code;

		private USER_GROUP_CODE(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	//회원상태코드
	public enum USER_STATE {
		
		  USE("1") //사용
		, LOCKED("5") //잠김
		, NOT_USED("9") //미사용
		;
		
		private String code;

		private USER_STATE(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}
	
	//회원변경구분코드
	public enum USER_CHG_TYPE_CD {
		
		  REG_BY_ADMIN("A0") //관리자 등록
		, CHG_BY_ADMIN("A1") //관리자 수정
		, DEL_BY_ADMIN("A2") //관리자 삭제
		;
		
		private String code;

		private USER_CHG_TYPE_CD(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
	
}
