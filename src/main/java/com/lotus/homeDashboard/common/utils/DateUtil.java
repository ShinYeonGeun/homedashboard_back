package com.lotus.homeDashboard.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
	public static LocalDateTime parse(String datetime, String patturn) {
		return LocalDateTime.from(DateTimeFormatter.ofPattern(patturn).parse(datetime));
	}
}
