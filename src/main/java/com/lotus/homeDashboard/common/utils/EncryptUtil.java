package com.lotus.homeDashboard.common.utils;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.lotus.homeDashboard.common.exception.BizException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptUtil {
	
	private static final byte[] IV = "HOMEDASHBOARD===".getBytes();
	private static final String AES_KEY = "HOMEDASHBOARD-AESKEY";
	
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
	
	public static String encryptAES256(String value) {
		return EncryptUtil.encryptAES256(value, EncryptUtil.AES_KEY);
	}
	
	public static String encryptAES256(String value, String key) {
		Cipher cipher = null;
		byte[] cipherText = null;
		IvParameterSpec ivParameter = null;
		String result = "";
		SecretKeySpec secretKey = null;
		
		try {

			// 키 길이 확인 (AES-256에서는 32바이트(256비트)여야 함)
            // SecretKeySpec을 사용해 사용자 입력 키를 SecretKey로 변환
            secretKey = new SecretKeySpec(StringUtil.rpad(key, 32, "=").getBytes(), "AES");
            
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ivParameter = new IvParameterSpec(IV);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameter);
			
			cipherText = cipher.doFinal(value.getBytes());
			result = Base64.getEncoder().encodeToString(cipherText);
					
		} catch (InvalidKeyException e) {
			log.error("__ERRLOG__ AES256 암호화 InvalidKeyException 오류", e);
			throw new BizException("error.encrypt");
		} catch (InvalidAlgorithmParameterException e) {
			log.error("__ERRLOG__ AES256 암호화 InvalidAlgorithmParameterException 오류", e);
			throw new BizException("error.encrypt");
		} catch (NoSuchAlgorithmException e) {
			log.error("__ERRLOG__ AES256 암호화 NoSuchAlgorithmException 오류", e);
			throw new BizException("error.encrypt");
		} catch (NoSuchPaddingException e) {
			log.error("__ERRLOG__ AES256 암호화 NoSuchPaddingException 오류", e);
			throw new BizException("error.encrypt");
		} catch (IllegalBlockSizeException e) {
			log.error("__ERRLOG__ AES256 암호화 IllegalBlockSizeException 오류", e);
			throw new BizException("error.encrypt");
		} catch (BadPaddingException e) {
			log.error("__ERRLOG__ AES256 암호화 BadPaddingException 오류", e);
			throw new BizException("error.encrypt");
		}
		
		return result;
	}
	
	public static String decryptAES256(String encValue) {
		return EncryptUtil.decryptAES256(encValue, EncryptUtil.AES_KEY);
	}
	
	public static String decryptAES256(String encValue, String key) {
		Cipher cipher = null;
		byte[] plainText = null;
		SecretKey secretKey = null;
		IvParameterSpec ivParameter = null;
		String result = "";
		
		try {
			// 키 길이 확인 (AES-256에서는 32바이트(256비트)여야 함)
            // SecretKeySpec을 사용해 사용자 입력 키를 SecretKey로 변환
            secretKey = new SecretKeySpec(StringUtil.rpad(key, 32, "=").getBytes(), "AES");
            
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			ivParameter = new IvParameterSpec(IV);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameter);
			
			plainText = cipher.doFinal(Base64.getDecoder().decode(encValue));
			result = new String(plainText);
					
		} catch (InvalidKeyException e) {
			log.error("__ERRLOG__ AES256 암호화 InvalidKeyException 오류", e);
			throw new BizException("error.decrypt");
		} catch (InvalidAlgorithmParameterException e) {
			log.error("__ERRLOG__ AES256 암호화 InvalidAlgorithmParameterException 오류", e);
			throw new BizException("error.decrypt");
		} catch (NoSuchAlgorithmException e) {
			log.error("__ERRLOG__ AES256 암호화 NoSuchAlgorithmException 오류", e);
			throw new BizException("error.decrypt");
		} catch (NoSuchPaddingException e) {
			log.error("__ERRLOG__ AES256 암호화 NoSuchPaddingException 오류", e);
			throw new BizException("error.decrypt");
		} catch (IllegalBlockSizeException e) {
			log.error("__ERRLOG__ AES256 암호화 IllegalBlockSizeException 오류", e);
			throw new BizException("error.decrypt");
		} catch (BadPaddingException e) {
			log.error("__ERRLOG__ AES256 암호화 BadPaddingException 오류", e);
			throw new BizException("error.decrypt");
		}
		
		return result;
	}
}
