//package com.lotus.homeDashboard.common.config;
//
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SaltPasswordEncoder implements PasswordEncoder {
//	
//	private String salt;
//	
//	public SaltPasswordEncoder() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//
//	public SaltPasswordEncoder(String salt) {
//		super();
//		this.salt = salt;
//	}
//
//	@Override
//	public String encode(CharSequence rawPassword) {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		return encoder.encode(rawPassword + this.salt);
//	}
//
//	@Override
//	public boolean matches(CharSequence rawPassword, String encodedPassword) {
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        return encoder.matches(rawPassword + salt, encodedPassword);
//	}
//
//}
