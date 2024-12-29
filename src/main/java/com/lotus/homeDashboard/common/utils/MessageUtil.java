package com.lotus.homeDashboard.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {
	private static MessageSource messageSouce;
	
	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		MessageUtil.messageSouce = messageSource;
    }
	
	public static String getMessage(String key) {
		return MessageUtil.messageSouce.getMessage(key, null, null, null);
	}
	
	public static String getMessage(String key, String defaultMsg) {
		return MessageUtil.messageSouce.getMessage(key, null, defaultMsg, null);
	}
	
	public static String getMessage(String key, String[] replaceStrs) {
		return MessageUtil.messageSouce.getMessage(key, replaceStrs, null, null);
	}
}
