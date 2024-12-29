package com.lotus.homeDashboard.cmn.usr.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lotus.homeDashboard.cmn.mnm.service.MenuService;
import com.lotus.homeDashboard.cmn.usr.dao.LoginHistoryRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserGroupRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserTokenRepository;
import com.lotus.homeDashboard.cmn.usr.dao.spec.UserSpecification;
import com.lotus.homeDashboard.cmn.usr.entity.LoginHistoryEntity;
import com.lotus.homeDashboard.cmn.usr.entity.QGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.QUserGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserMenuEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserTokenEntity;
import com.lotus.homeDashboard.cmn.usr.service.UserService;
import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.Users;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.jwt.JWTProvider;
import com.lotus.homeDashboard.common.utils.EncryptUtil;
import com.lotus.homeDashboard.common.utils.MessageUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.Tuple;

import lombok.extern.slf4j.Slf4j;

@Service("UserService")
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserTokenRepository userTokenRepository;
	
	@Autowired
	private LoginHistoryRepository loginHistoryRepository;
	
	@Autowired
	private JWTProvider jWTProvider;
	
	@Autowired
	private UserGroupRepository userGroupRepository;
	
	@Autowired
	private MenuService menuService;
	
	@Transactional
	@Override
	public DataMap<String, Object> doLogin(Request request) {
		DataMap<String, Object> params = request.getParameter();
		DataMap<String, Object> result = null;
		Optional<UserEntity> userEntity = null;
		UserEntity userInfo = null;
		LoginHistoryEntity historyEntity = null;
		List<DataMap<String,Object>> menuList = null;
		UserTokenEntity tokenEntity = null;
		Instant loginDtm = null;
		CommonHeader header = null;
		String encPw = "";
		String accessToken = "";
		DecodedJWT decodeToken = null;
		List<DataMap<String, Object>> userGrpList = null;
		List<String> roles = null;
		
		log.debug("__DBGLOG__ doLogin 시작");
		
		//===================================================================================
		// 입력값 체크
		//===================================================================================
		if(StringUtil.isEmpty(params.getString("loginId"))) {
			log.error("__ERRLOG__ 로그인ID 미입력");
			throw new BizException(MessageUtil.getMessage("val_required", new String[] {"로그인ID"})); 
		}
		
		if(StringUtil.isEmpty(params.getString("pswd"))) {
			log.error("__ERRLOG__ 비밀번호 미입력");
			throw new BizException(MessageUtil.getMessage("val_required", new String[] {"비밀번호"})); 
		}
		
		//===================================================================================
		// 헤더에서 로그인 일시 구함
		//===================================================================================
		header = request.getHeader();
		loginDtm = header.getCurrDtm();
		
		log.debug("__DBGLOG__ 로그인일시:[{}]", loginDtm);

		//===================================================================================
		// Login ID 로 DB 조회
		//===================================================================================
		userEntity = userRepository.findById(params.getString("loginId"));
		log.debug("__DBGLOG__ User Entity: {}", userEntity);
		
		//ID 체크
		if(userEntity.isEmpty()) {
			throw new BizException(MessageUtil.getMessage("login_not_found_id")); 
		} 
		
		userInfo = userEntity.get();
		
		//삭제여부 체크
		if(Constants.YES.equals(userInfo.getDelYn())) {
			throw new BizException(MessageUtil.getMessage("login_deleted_id"));
		}
		
		//입력한 비밀번호 암호화
		encPw = EncryptUtil.encryptSHA256(params.getString("pswd"), params.getString("loginId"));
		log.debug("__DBGLOG__ 암호화 결과: [{}]", encPw);
		log.debug("__DBGLOG__ DB 값: [{}]", userInfo.getPswd());
		
		//비밀번호 체크
		if(!userInfo.getPswd().equals(encPw)) {
			log.error("__ERRLOG__ 저장된 비밀번호와 전달된 비밀번호가 일치하지 않음");
			throw new BizException(MessageUtil.getMessage("login_pwd_not_match")); 
		}
		
		//잠금여부 체크
		//TODO 상수로
		if("2".equals(userInfo.getUserState())) {
			throw new BizException(MessageUtil.getMessage("login_lock_id"));
		}
		
		userInfo.setLastLoginDtm(loginDtm);
		userInfo.setLastTrnDtm(loginDtm);
		userInfo.setLastTrnUUID(header.getUuid());
		userInfo.setLastTrnUid(Users.LOGIN.getUid());
		
		//===================================================================================
		// 권한 조회 후 세팅
		//===================================================================================
		userGrpList = this.getUserGrpList(userInfo.getUid(), Constants.NO);
		roles = userGrpList.stream().map(map -> map.getString("grpNm")).collect(Collectors.toList());
		
		log.debug("__DBGLOG__ roles => {}", roles);
		
		//===================================================================================
		// 로그인이력 저장
		//===================================================================================
		historyEntity = new LoginHistoryEntity();
		historyEntity.setUid(userInfo.getUid());
		historyEntity.setLoginDtm(loginDtm);
		historyEntity.setIp(request.getHeader().getRequestIp());
		historyEntity.setDelYn(Constants.NO);
		historyEntity.setLastTrnUUID(request.getHeader().getUuid());
		historyEntity.setLastTrnUid(userInfo.getUid());
		historyEntity.setLastTrnDtm(loginDtm);
		loginHistoryRepository.save(historyEntity);
		
		//===================================================================================
		// JWT 토큰 발급
		//===================================================================================
		accessToken = jWTProvider.create(userInfo.getUid(), roles, request.getHeader().getUuid().toString(), loginDtm);
		log.debug("__DBGLOG__ JWT ACCESS TOKEN [{}]", accessToken);
		
		decodeToken = jWTProvider.decode(accessToken);
		
		//===================================================================================
		// JWT 토큰 정보 저장
		//===================================================================================
		tokenEntity = new UserTokenEntity();
		tokenEntity.setUid(userInfo.getUid());
		tokenEntity.setSecurityKey(request.getHeader().getUuid().toString());
		tokenEntity.setAccessToken(accessToken);
		tokenEntity.setRefreshToken(Constants.BLANK);
		userTokenRepository.save(tokenEntity);
		
		//===================================================================================
		// 사용자 메뉴 조회
		//===================================================================================
		menuList = menuService.findUserMenuList(userGrpList.stream().map(map -> map.getString("grpCd")).collect(Collectors.toList()));
				
		//===================================================================================
		// 출력값 조립
		//===================================================================================
		result = new DataMap<String, Object>();
		result.put("accessToken", accessToken);
		result.put("loginId", userInfo.getUid());
		result.put("expTime", decodeToken.getExpiresAtAsInstant());
		result.put("roles", roles);
		result.put("menuList", menuList);
		
		log.debug("__DBGLOG__ doLogin 종료");
		return result;
	}

	@Override
	public DataMap<String, Object> inqUserList(Request request) {
		DataMap<String, Object> result = new DataMap<>();
		DataMap<String, Object> params = request.getParameter();
		List<DataMap<String, Object>> userList = new ArrayList<>();
		Pageable pageable = null;
		Specification<UserEntity> conditions = Specification.where(CommonSpecification.alwaysTrue());
		log.debug("__DBGLOG__ inqUserList 시작");
		
		pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("uid"));
		
		//조건 결합
		if(!StringUtil.isEmpty(params.getString("uid"))) {
			log.debug("__DBGLOG__ add uid");
			conditions = conditions.and(UserSpecification.containsUid(params.getString("uid")));
		}
		
		if(!StringUtil.isEmpty(params.getString("userState"))) {
			log.debug("__DBGLOG__ add userState");
			conditions = conditions.and(UserSpecification.hasUserState(params.getString("userState")));
		}
		
		if(!StringUtil.isEmpty(params.getString("delYn"))) {
			log.debug("__DBGLOG__ add delYn");
			conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
		}
		
		Page<UserEntity> inqList = userRepository.findAll(conditions, pageable);

		
		result.put("totalPages", inqList.getTotalPages());
		result.put("totalElements", inqList.getTotalElements());
		result.put("size", inqList.getSize());
		result.put("sort", inqList.getSort());
		result.put("first", inqList.isFirst());
		result.put("last", inqList.isLast());
		result.put("empty", inqList.isEmpty());
		result.put("last", inqList.isLast());
		result.put("number", inqList.getNumber());
		result.put("numberOfElements", inqList.getNumberOfElements());
		result.put("pageable", inqList.getPageable());
		
		if(inqList.getSize() > 0) {
			for(UserEntity userEntity : inqList.getContent()) {
				DataMap<String, Object> data = new DataMap<>();
				data.put("uid", userEntity.getUid());
				data.put("pswdErrCnt", userEntity.getPswdErrCnt());
				data.put("userState", userEntity.getUserState());
				data.put("lastLoginDtm", userEntity.getLastLoginDtm());
				data.put("delYn", userEntity.getDelYn());
				data.put("lastTrnUUID", userEntity.getLastTrnUUID());
				data.put("lastTrnDtm", userEntity.getLastTrnDtm());
				data.put("lastTrnUid", userEntity.getLastTrnUid());
				userList.add(data);
			}
		}
		
		result.put("userList", userList);
		
		log.debug("__DBGLOG__ inqUserList 종료");
		return result;
	}

	@Override
	public DataMap<String, Object> extensionJWTPeriod(Request request ) {
		DataMap<String, Object> result = null;
		Optional<UserTokenEntity> tokenEntity = null;
		List<DataMap<String, Object>> userGrpList = null;
		List<String> roles = null;
		String accessToken = "";
		
		log.debug("__DBGLOG__ extensionJWTPeriod 시작");
		
		//===================================================================================
		// 권한 조회 후 세팅
		//===================================================================================
		userGrpList = this.getUserGrpList(request.getHeader().getTrnUserId(), Constants.NO);
		roles = userGrpList.stream().map(map -> map.getString("grpNm")).collect(Collectors.toList());
		
		//===================================================================================
		// 만료시각 다시 산출해서 토큰 발급
		//===================================================================================
		accessToken = jWTProvider.extension(request.getHeader().getAccessToken(), request.getHeader().getUuid().toString(), roles);
		
		//===================================================================================
		// 토큰 발급정보 갱신
		//===================================================================================
		tokenEntity = userTokenRepository.findById(request.getHeader().getTrnUserId());
		tokenEntity.get().setUid(request.getHeader().getTrnUserId());
		tokenEntity.get().setSecurityKey(request.getHeader().getUuid().toString());
		tokenEntity.get().setAccessToken(accessToken);
		tokenEntity.get().setRefreshToken(Constants.BLANK);
		userTokenRepository.save(tokenEntity.get());
		
		//===================================================================================
		// 출력값 조립
		//===================================================================================
		result = new DataMap<String, Object>();
		result.put("accessToken", accessToken);
		
		log.debug("__DBGLOG__ extensionJWTPeriod 종료");
		
		return result;
	}
	
	private List<DataMap<String, Object>> getUserGrpList(String uid, String delYn) {
		List<DataMap<String, Object>> userGrpList = null;
		List<Tuple> grpList = null;
		
		userGrpList = new ArrayList<>();

		grpList = userGroupRepository.findUserGroupJoinGrpInfo(uid, delYn);
		
		for(Tuple t : grpList) {
			DataMap<String, Object> map = new DataMap<>();
			map.put("grpCd", t.get(QUserGroupEntity.userGroupEntity).getGrpCd());
			map.put("grpNm", t.get(QGroupEntity.groupEntity).getGrpNm());
			userGrpList.add(map);
		}
		
		return userGrpList;
	}

}
