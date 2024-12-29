package com.lotus.homeDashboard.common.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
	public static String encryptSHA256(String value, String salt) {
		MessageDigest md = null;
		String hex = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			// 평문+salt 암호화
			md.update((value + salt).getBytes());
			hex = String.format("%064x", new BigInteger(1, md.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hex;
	}
}
