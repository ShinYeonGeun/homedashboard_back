package com.lotus.homeDashboard.common.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lotus.homeDashboard.cmn.usr.dao.UserTokenRepository;
import com.lotus.homeDashboard.cmn.usr.entity.UserTokenEntity;
import com.lotus.homeDashboard.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTProvider {
	
	@Value("${lotus.token_exp_sec}")
	private long tokenExpSec;
	
	@Autowired
	private UserTokenRepository userTokenRepository;

	public String create(String uid, String name, List<String> roles, String key, Instant currDatetime) {
		
		JWTCreator.Builder builder = JWT.create();
		
		log.debug("__DBGLOG__ 토큰 uid:[{}]", uid);
		log.debug("__DBGLOG__ 토큰 uid:[{}]", name);
		log.debug("__DBGLOG__ 토큰 roles:[{}]", roles);
		log.debug("__DBGLOG__ 토큰 key:[{}]", key);
		log.debug("__DBGLOG__ 토큰 currDatetime:[{}]", currDatetime);
		log.debug("__DBGLOG__ 토큰 유효시간:[{}]", this.tokenExpSec);
		
		builder.withIssuer("lotus");
		builder.withSubject(uid);
		builder.withClaim("name", name);
		builder.withClaim("roles", roles);
		builder.withClaim("loginDtm", currDatetime);
		builder.withClaim("uuid", key);
		builder.withExpiresAt(currDatetime.plusSeconds(this.tokenExpSec));
		
		return builder.sign(Algorithm.HMAC256(key));
	}
	
	public String extension(String prevToken, String key, List<String> roles) {
		DecodedJWT tokenInfo = null;
		JWTCreator.Builder builder = JWT.create();

		tokenInfo = this.validate(prevToken);
		log.debug("extension prevToken:{}", tokenInfo.getClaims());
		Instant loginDtm = Instant.ofEpochSecond(tokenInfo.getClaims().get("loginDtm").asLong());
		Instant expDtm = tokenInfo.getExpiresAtAsInstant();
		Instant now = Instant.now();
		String name = tokenInfo.getClaims().get("name").asString();
		
		builder.withIssuer(tokenInfo.getIssuer());
		builder.withSubject(tokenInfo.getSubject());
		builder.withClaim("name", name);
		builder.withClaim("roles", roles);
		builder.withClaim("loginDtm", loginDtm);
		builder.withClaim("uuid", key);
		builder.withExpiresAt(expDtm.plusSeconds(this.tokenExpSec - Duration.between(now, expDtm).getSeconds()));
		
		log.debug("__DBGLOG__ uuid {}", key);
		log.debug("__DBGLOG__ name {}", name);
		log.debug("__DBGLOG__ loginDtm {}", loginDtm);
		log.debug("__DBGLOG__ now {}", now);
		log.debug("__DBGLOG__ prev expDtm {}", expDtm);
		log.debug("__DBGLOG__ expires {}", expDtm.plusSeconds(this.tokenExpSec - Duration.between(now, expDtm).getSeconds()));
		
		return builder.sign(Algorithm.HMAC256(key));
	}
	
	public DecodedJWT decode(String token) {
		return JWT.decode(token);
	}
	
	public DecodedJWT validate(String token) {
		
		DecodedJWT decodeToken = null;
		Optional<UserTokenEntity> tokenInfo = null;
		Algorithm algorithm = null;
		JWTVerifier verifier = null;
		DecodedJWT result = null;
		
		log.debug("__DBGLOG__ token:[{}]", token);
		
		if(StringUtil.isEmpty(token)) {
			return null;
		}
		
		//===================================================================================
		// 토큰 디코딩
		//===================================================================================
		decodeToken = JWT.decode(token);
		
		log.debug("__DBGLOG__ 토큰 디코드 결과:[{}]", decodeToken);
		
		//===================================================================================
		// 발행한 토큰정보 조회
		//===================================================================================
		tokenInfo = userTokenRepository.findById(decodeToken.getSubject());
		log.debug("__DBGLOG__ 토큰정보 조회결과:[{}]", tokenInfo);
		
		//===================================================================================
		// 알고리즘 설정
		//===================================================================================
		algorithm = Algorithm.HMAC256(tokenInfo.get().getSecurityKey());
		
		//===================================================================================
		// 검증객체 생성
		//===================================================================================
		verifier = JWT.require(algorithm).withIssuer("lotus").build();
		
		//===================================================================================
		// 검증
		//===================================================================================
		result = verifier.verify(token);
		
		return result;
	}
}
