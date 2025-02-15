package com.lotus.homeDashboard.common.utils;

import java.util.regex.Pattern;

public class StringUtil {
	
	public static boolean isNull(String arg) {
		return arg == null;
	}

	public static String nvl(String arg, String def) {
		return StringUtil.isNull(arg) ? def : arg;
	}
	
	public static boolean isEmpty(String arg) {
		return StringUtil.isNull(arg) || arg.trim().length() == 0;
	}
	
	public static String evl(String arg, String def) {
		return StringUtil.isEmpty(arg) ? def :  arg;
	}
	
	/**
	 * 영문 or 숫자로만 되어 있는지 여부
	 * @param arg
	 * @return
	 */
	public static boolean isAlphaNum(String arg) {
		return Pattern.matches("^[a-zA-Z0-9]*$", arg);
	}
	
	/**
	 * val 중에 arg가 있는지 여부
	 * @param arg
	 * @param val
	 * @return
	 */
	public static boolean isEqualToAny(String arg, String...val) {

		if(StringUtil.isEmpty(arg)) {
			return false;
		}
		
		for(String v:val) {
			if(arg.equals(v)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String lpad(String input, int length, String padChar) {
	    if (input.length() >= length) {
	        return input; // 이미 원하는 길이를 초과하면 그대로 반환
	    }
	    int padLength = length - input.length();
	    StringBuilder padding = new StringBuilder();
	    for (int i = 0; i < padLength; i++) {
	        padding.append(padChar);
	    }
	    return padding.append(input).toString(); // 패딩 + 입력 문자열
	}
	
	public static String rpad(String input, int length, String padChar) {
	    if (input.length() >= length) {
	        return input; // 이미 원하는 길이를 초과하면 그대로 반환
	    }
	    int padLength = length - input.length();
	    StringBuilder padding = new StringBuilder();
	    for (int i = 0; i < padLength; i++) {
	        padding.append(padChar);
	    }
	    return input + padding.toString(); // 입력 문자열 + 패딩
	}
	
	// 문자열을 가변 인자로 받아 합치는 함수
    public static String concat(String... strings) {
        StringBuilder result = new StringBuilder();

        for (String str : strings) {
            if (str != null) { // null 체크
                result.append(str);
            }
        }

        return result.toString();
    }
}
