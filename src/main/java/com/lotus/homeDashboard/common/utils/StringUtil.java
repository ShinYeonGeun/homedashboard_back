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
	public static boolean isOrEquals(String arg, String...val) {

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
}
