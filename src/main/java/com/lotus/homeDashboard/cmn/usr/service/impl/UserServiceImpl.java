package com.lotus.homeDashboard.cmn.usr.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.lotus.homeDashboard.cmn.usr.constants.UserConstants;
import com.lotus.homeDashboard.cmn.usr.dao.LoginHistoryRepository;
import com.lotus.homeDashboard.cmn.usr.dao.PasswordErrorHistoryRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserGroupRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserLogRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserRepository;
import com.lotus.homeDashboard.cmn.usr.dao.UserTokenRepository;
import com.lotus.homeDashboard.cmn.usr.dao.spec.UserSpecification;
import com.lotus.homeDashboard.cmn.usr.entity.LoginHistoryEntity;
import com.lotus.homeDashboard.cmn.usr.entity.PasswordErrorHistoryEntity;
import com.lotus.homeDashboard.cmn.usr.entity.QGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.QUserGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserGroupEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserLogEntity;
import com.lotus.homeDashboard.cmn.usr.entity.UserTokenEntity;
import com.lotus.homeDashboard.cmn.usr.service.UserService;
import com.lotus.homeDashboard.common.component.CommonHeader;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.lotus.homeDashboard.common.component.ResultSet;
import com.lotus.homeDashboard.common.component.ServiceInvoker;
import com.lotus.homeDashboard.common.constants.Constants;
import com.lotus.homeDashboard.common.constants.ResultCode;
import com.lotus.homeDashboard.common.dao.spec.CommonSpecification;
import com.lotus.homeDashboard.common.exception.BizException;
import com.lotus.homeDashboard.common.jwt.JWTProvider;
import com.lotus.homeDashboard.common.utils.EncryptUtil;
import com.lotus.homeDashboard.common.utils.StringUtil;
import com.querydsl.core.Tuple;

import lombok.extern.slf4j.Slf4j;

@Service("UserService")
@Slf4j
@Transactional
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
	private ServiceInvoker serviceInvoker;
	
	@Autowired
	private UserLogRepository userLogRepository;
	
	@Autowired
	private PasswordErrorHistoryRepository passwordErrorHistoryRepository;
		
	@Override
	public DataMap<String, Object> doLogin(Request request) {
		
		DataMap<String, Object> params = request.getParameter();
		DataMap<String, Object> result = null;
		DataMap<String, Object> commCodeMap = null;
		List<DataMap<String,Object>> menuList = null;
		List<DataMap<String, Object>> userGrpList = null;
		List<String> roles = null;
		Optional<UserEntity> userEntity = Optional.empty();
		UserEntity userInfo = null;
		LoginHistoryEntity historyEntity = null;
		ResultSet menuInqResult = null;
		ResultSet commCodeInqResult = null;
		ResultSet handleInvalidPswdResult = null;
		UserTokenEntity tokenEntity = null;
		Instant loginDtm = null;
		CommonHeader header = null;
		String encPw = "";
		String decPw = "";
		String accessToken = "";
		DecodedJWT decodeToken = null;
		
		try {
			
			log.debug("__DBGLOG__ doLogin 시작");
			
			//===================================================================================
			// 비로그인상태라 헤더에 거래UID 세팅
			//===================================================================================
			header = request.getHeader();
			header.setTrnUserId(params.getString("loginId"));
			
			//===================================================================================
			// 입력값 체크
			//===================================================================================
			if(StringUtil.isEmpty(params.getString("loginId"))) {
				log.error("__ERRLOG__ 로그인ID 미입력");
				throw new BizException("val_required", new String[] {"로그인ID"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("pswd"))) {
				log.error("__ERRLOG__ 비밀번호 미입력");
				throw new BizException("val_required", new String[] {"비밀번호"}); 
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
				throw new BizException("login_not_found_id"); 
			} 
			
			userInfo = userEntity.get();
			
			//삭제여부 체크
			if(Constants.YES.equals(userInfo.getDelYn())) {
				throw new BizException("login_deleted_id");
			}
			
			//잠금여부 체크
			if(UserConstants.USER_STATE.LOCKED.getCode().equals(userInfo.getUserState())) {
				throw new BizException("login_lock_id");
			}
			
			//비밀번호 복호화
			decPw = EncryptUtil.decryptAES256(params.getString("pswd"));
			
			//입력한 비밀번호 암호화
			encPw = EncryptUtil.encryptSHA256(decPw, params.getString("loginId"));
			log.debug("__DBGLOG__ 암호화 결과: [{}]", encPw);
			log.debug("__DBGLOG__ DB 값: [{}]", userInfo.getPswd());
			
			//비밀번호 체크
			if(!userInfo.getPswd().equals(encPw)) {
				log.error("__ERRLOG__ 저장된 비밀번호와 전달된 비밀번호가 일치하지 않음");
				
				//===================================================================================
				// 비밀번호 오류등록
				//===================================================================================
				Request invalidPswdParams = new Request();
				DataMap<String, Object> invalidPswdParamMap = new DataMap<>();
				invalidPswdParamMap.put("uid", params.getString("loginId"));
				invalidPswdParamMap.put("errPswd", decPw);
				invalidPswdParams.setHeader(header);
				invalidPswdParams.setParameter(invalidPswdParamMap);
				
				handleInvalidPswdResult = serviceInvoker.invokeRequiresNew("UserService", "handleInvalidPassword", invalidPswdParams);
				
				if(ResultCode.SUCCESS.getCode().equals(handleInvalidPswdResult.getResultCd())) {
					DataMap<String, Object> data = handleInvalidPswdResult.payloadAsDataMap();
					
					if(UserConstants.USER_STATE.LOCKED.getCode().equals(data.getString("userState", ""))) {
						throw new BizException("login_pswd_not_match_lock", new String[] {String.valueOf(UserConstants.MAX_PSWD_ERR_CNT)}); 
					}
				}
				
				throw new BizException("login_pswd_not_match"); 
			}
			
			//===================================================================================
			// 이용자 정보 갱신
			//===================================================================================
			userInfo.setPswdErrCnt(0);
			userInfo.setLastLoginDtm(loginDtm);
			userInfo.setLastTrnUUID(header.getUuid());
			userInfo.setLastTrnUid(params.getString("loginId"));
			
			try {
				userRepository.saveAndFlush(userInfo);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ login DataAccessException 발생 : {}", e);
				throw new BizException("chg_error_prefix", new String[] {"로그인 정보"}, e);
			}
			
			//===================================================================================
			// 권한 조회 후 세팅
			//===================================================================================
			userGrpList = this.findUserGrpList(userInfo.getUid(), Constants.NO);
			roles = userGrpList.stream().map(map -> map.getString("grpNm")).collect(Collectors.toList());
			
			log.debug("__DBGLOG__ roles => {}", roles);
			
			//===================================================================================
			// 로그인이력 저장
			//===================================================================================
			historyEntity = new LoginHistoryEntity();
			historyEntity.setUid(userInfo.getUid());
			historyEntity.setLoginDtm(loginDtm);
			historyEntity.setIp(request.getHeader().getRequestIp());
			historyEntity.setLastTrnUUID(request.getHeader().getUuid());
			historyEntity.setLastTrnUid(userInfo.getUid());
			historyEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				loginHistoryRepository.saveAndFlush(historyEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ login DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"로그인 이력"},e);
			}
			
			//===================================================================================
			// JWT 토큰 발급
			//===================================================================================
			accessToken = jWTProvider.create(userInfo.getUid(), userInfo.getName(), roles, request.getHeader().getUuid().toString(), loginDtm);
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
			
			try {
				userTokenRepository.saveAndFlush(tokenEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ login DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"로그인 토큰"},e);
			}
			
			//===================================================================================
			// 사용자 메뉴 조회
			//===================================================================================
			Request userMenuParams = new Request();
			DataMap<String, Object> userMenuParamMap = new DataMap<>();
			userMenuParamMap.put("grpCd", userGrpList.stream().map(map -> map.getString("grpCd")).collect(Collectors.toList()));
			userMenuParams.setHeader(header);
			userMenuParams.setParameter(userMenuParamMap);
			menuInqResult = serviceInvoker.callService("MenuService", "inqUserMenuList", userMenuParams);
			
			log.debug("__DBGLOG__ menuInqResult : {}", menuInqResult);
			
			if(menuInqResult != null) {
				menuList = menuInqResult.payloadAsDataMapList();
			}
			
			log.debug("__DBGLOG__ USER MENU : {}", menuList);
			
			//===================================================================================
			// 공통코드 조회
			//===================================================================================
			Request commonCodeParams = new Request();
			DataMap<String, Object> commonCodeParamMap = new DataMap<>();
			commonCodeParamMap.put("codeDelYn", Constants.NO);
			commonCodeParamMap.put("codeDetailDelYn", Constants.NO);
			commonCodeParams.setHeader(header);
			commonCodeParams.setParameter(commonCodeParamMap);
			commCodeInqResult = serviceInvoker.callService("CommonCodeService", "inqCommonCodeNAllDetail", commonCodeParams);
			
			if(commCodeInqResult != null) {
				commCodeMap = commCodeInqResult.payloadAsDataMap();
			}
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result = new DataMap<String, Object>();
			result.put("accessToken", accessToken);
			result.put("loginId", userInfo.getUid());
			result.put("expTime", decodeToken.getExpiresAtAsInstant());
			result.put("roles", roles);
			result.put("menuList", menuList);
			result.put("commCodeInfo", commCodeMap);
			
			log.debug("__DBGLOG__ doLogin 종료");
			
		} catch (BizException be) {
			log.error("_ERRLOG__ 로그인 에러", be);
			throw be;
		} catch (Exception e) {
			throw new BizException("process_err", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> inqUserList(Request request) {
		
		DataMap<String, Object> result = new DataMap<>();
		DataMap<String, Object> params = request.getParameter();
		List<DataMap<String, Object>> userList = new ArrayList<>();
		Pageable pageable = null;
		Specification<UserEntity> conditions = Specification.where(CommonSpecification.alwaysTrue());
		
		try {
			
			pageable = PageRequest.of(params.getInt("pageNo"), params.getInt("pageSize"), Sort.by("uid"));
			
			//조건 결합
			if(!StringUtil.isEmpty(params.getString("uid"))) {
				log.debug("__DBGLOG__ add uid : [{}]", params.getString("uid"));
				conditions = conditions.and(UserSpecification.containsUid(params.getString("uid")));
			}
			
			if(!StringUtil.isEmpty(params.getString("userState"))) {
				log.debug("__DBGLOG__ add userState : [{}]", params.getString("userState"));
				conditions = conditions.and(UserSpecification.eqUserState(params.getString("userState")));
			}
			
			if(!StringUtil.isEmpty(params.getString("delYn"))) {
				log.debug("__DBGLOG__ add delYn : [{}]", params.getString("delYn"));
				conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
			}
			
			Page<UserEntity> inqList = userRepository.findAll(conditions, pageable);
			
			log.debug("__DBGLOG__ inqList : {}", inqList);
			
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
					data.put("name", userEntity.getName());
					data.put("pswdErrCnt", userEntity.getPswdErrCnt());
					data.put("userState", userEntity.getUserState());
					data.put("lastLoginDtm", userEntity.getLastLoginDtm());
					data.put("delYn", userEntity.getDelYn());
					data.put("lastTrnUUID", userEntity.getLastTrnUUID());
					data.put("lastTrnDtm", userEntity.getLastTrnDtm());
					data.put("lastTrnUid", userEntity.getLastTrnUid());
					data.put("lastTrnCd", userEntity.getLastTrnCd());
					userList.add(data);
				}
			}
			
			result.put("userList", userList);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqUserList Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> extensionJWTPeriod(Request request ) {
		
		DataMap<String, Object> result = null;
		Optional<UserTokenEntity> tokenEntity = Optional.empty();
		List<DataMap<String, Object>> userGrpList = null;
		List<String> roles = null;
		String accessToken = "";
		
		log.debug("__DBGLOG__ extensionJWTPeriod 시작");
		
		//===================================================================================
		// 권한 조회 후 세팅
		//===================================================================================
		userGrpList = this.findUserGrpList(request.getHeader().getTrnUserId(), Constants.NO);
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
		userTokenRepository.saveAndFlush(tokenEntity.get());
		
		//===================================================================================
		// 출력값 조립
		//===================================================================================
		result = new DataMap<String, Object>();
		result.put("accessToken", accessToken);
		
		log.debug("__DBGLOG__ extensionJWTPeriod 종료");
		
		return result;
	}
	
	private List<DataMap<String, Object>> findUserGrpList(String uid, String delYn) {
		
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

	@Override
	public List<DataMap<String, Object>> inqUserGrpList(Request request) {
		
		List<DataMap<String, Object>> userGrpList = null;
		String uid = "";
		String delYn = "";
		
		try {
			
			uid = request.getParameter().getString("uid", "");
			delYn = request.getParameter().getString("delYn", "");
			
			userGrpList = this.findUserGrpList(uid, delYn);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqUserGrpList Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		
		return userGrpList;
	}

	@Override
	public DataMap<String, Object> inqUserInfo(Request request) {
		
		DataMap<String, Object> params = null;
		DataMap<String, Object> userInfo = null;
		Optional<UserEntity> inqResult = Optional.empty();
		Specification<UserEntity> conditions = Specification.where(CommonSpecification.alwaysTrue());
		
		try {
			
			params = request.getParameter();
			
			//조건 결합
			if(!StringUtil.isEmpty(params.getString("uid"))) {
				log.debug("__DBGLOG__ add uid : [{}]", params.getString("uid"));
				conditions = conditions.and(UserSpecification.eqUid(params.getString("uid")));
			}
			
			if(!StringUtil.isEmpty(params.getString("userState"))) {
				log.debug("__DBGLOG__ add userState : [{}]", params.getString("userState"));
				conditions = conditions.and(UserSpecification.eqUserState(params.getString("userState")));
			}
			if(!StringUtil.isEmpty(params.getString("delYn"))) {
				log.debug("__DBGLOG__ add delYn : [{}]", params.getString("delYn"));
				conditions = conditions.and(CommonSpecification.hasDelYn(params.getString("delYn")));
			}
			
			inqResult = userRepository.findOne(conditions);
			
			if(inqResult.isEmpty()) {
				throw new BizException("not_found");
			} else {
				UserEntity userEntity = inqResult.get();
				userInfo = new DataMap<String, Object>();
				userInfo.put("uid", userEntity.getUid());
				userInfo.put("name", userEntity.getName());
				userInfo.put("pswdErrCnt", userEntity.getPswdErrCnt());
				userInfo.put("userState", userEntity.getUserState());
				userInfo.put("lastLoginDtm", userEntity.getLastLoginDtm());
				userInfo.put("delYn", userEntity.getDelYn());
				userInfo.put("lastTrnUUID", userEntity.getLastTrnUUID());
				userInfo.put("lastTrnDtm", userEntity.getLastTrnDtm());
				userInfo.put("lastTrnUid", userEntity.getLastTrnUid());
				userInfo.put("lastTrnCd", userEntity.getLastTrnCd());
			}
			
		} catch (BizException be) {
			log.error("__ERRLOG__ inqUserInfo BizException", be);
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqUserInfo Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		return userInfo;
	}

	@Override
	public DataMap<String, Object> inqCntUser(Request request) {
		
		DataMap<String, Object> params = null;
		DataMap<String, Object> result = null;
		Specification<UserEntity> conditions = Specification.where(CommonSpecification.alwaysTrue());
		log.debug("__DBGLOG__ inqCntUser @@@");
		long inqResult = 0L;
		
		try {
			
			params = request.getParameter();
			
			//조건 결합
			if(!StringUtil.isEmpty(params.getString("uid"))) {
				log.debug("__DBGLOG__ add uid : [{}]", params.getString("uid"));
				conditions = conditions.and(UserSpecification.eqUid(params.getString("uid")));
			}
			
			if(!StringUtil.isEmpty(params.getString("userState"))) {
				log.debug("__DBGLOG__ add userState : [{}]", params.getString("userState"));
				conditions = conditions.and(UserSpecification.eqUserState(params.getString("userState")));
			}
			
			//삭제여부 Y 포함 데이터 조회는 별도로 만들어서 사용.
			conditions = conditions.and(CommonSpecification.hasDelYn(Constants.NO)); 
			
			inqResult = userRepository.count(conditions);
			
			result = new DataMap<String, Object>();
			result.put("count", inqResult);
						
		} catch (BizException be) {
			log.error("__ERRLOG__ inqUserInfo BizException", be);
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqUserInfo Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> inqCntUserByUid(Request request) {
		
		DataMap<String, Object> params = null;
		DataMap<String, Object> result = null;
		ResultSet rs = null;
		
		try {
			
			params = request.getParameter();
			result = new DataMap<String, Object>();
			
			if(StringUtil.isEmpty(params.getString("uid"))) {
				log.error("__ERRLOG__ 이용자ID 미입력");
				throw new BizException("val_required", new String[] {"이용자ID"}); 
			}
			
			rs = serviceInvoker.callService("UserService", "inqCntUser", request);
			
			if(rs != null) {
				result = rs.payloadAsDataMap();
			}
						
		} catch (BizException be) {
			log.error("__ERRLOG__ inqUserInfo BizException", be);
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ inqUserInfo Exception 발생 : {}", e);
			throw new BizException("inquiry_err", e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> createUser(Request request) {
		
		DataMap<String, Object> params = null;
		DataMap<String, Object> result = null;
		Optional<UserEntity> userInfo = Optional.empty();
		UserEntity userEntity  = null;
		UserLogEntity userLogEntity = null;
		UserGroupEntity userGroupEntity = null;
		CommonHeader header = null;
		int maxSeq = 0;
		String uid = "";
		String oldUserState = "";
		String newUserState = "";
		
		try {
			
			header = request.getHeader();
			params = request.getParameter();
			uid = params.getString("uid");
			newUserState = params.getString("userState", "");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			//Thread.sleep(6000);
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(uid)) {
				log.error("__ERRLOG__ 이용자ID 미입력");
				throw new BizException("val_required", new String[] {"이용자ID"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("userState"))) {
				log.error("__ERRLOG__ 이용자상태 미입력");
				throw new BizException("val_required", new String[] {"이용자상태"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("delYn"))) {
				log.error("__ERRLOG__ 삭제여부 미입력");
				throw new BizException("val_required", new String[] {"삭제여부"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 이용자 조회 시작");
			
			//===================================================================================
			// 이용자 조회
			//===================================================================================
			userInfo = userRepository.findById(uid);
			
			if(userInfo.isPresent()) {
				userEntity = userInfo.get();
				
				log.debug("__DBGLOG__ 이용자ID:[{}]", userEntity.getUid());
				log.debug("__DBGLOG__ 삭제여부:[{}]", userEntity.getDelYn());
				
				//삭제된 데이터면 업데이트
				if(!Constants.YES.equals(userInfo.get().getDelYn())) {
					log.error("__ERRLOG__ 기등록된 이용자 존재 [{}]", params.getString("uid"));
					throw new BizException("reg_data_exists_msg", new String[] {StringUtil.concat("이용자ID: ", params.getString("uid"))});
				}
				
				oldUserState = userEntity.getUserState();
				
			} else {
				userEntity = new UserEntity();
			}
			
			log.debug("__DBGLOG__ 이용자 조회 종료");
			log.debug("__DBGLOG__ 이용자 등록 시작");
			
			//===================================================================================
			// 이용자 등록
			//===================================================================================
			userEntity.setUid(uid);
			userEntity.setName(params.getString("name", ""));
			userEntity.setPswd(EncryptUtil.encryptSHA256(uid, uid));
			userEntity.setUserState(UserConstants.USER_STATE.USE.getCode());
			userEntity.setDelYn(Constants.NO);
			userEntity.setLastTrnUUID(header.getUuid());
			userEntity.setLastTrnUid(header.getTrnUserId());
			userEntity.setLastTrnCd(header.getTrnCd());
			
			//기존에 잠금상태에서 사용으로 변경 시 비밀번호 오류횟수 초기화
			if(UserConstants.USER_STATE.LOCKED.getCode().equals(oldUserState) &&
			   UserConstants.USER_STATE.USE.getCode().equals(newUserState)) {
				userEntity.setPswdErrCnt(0);
			}

			try {
				userRepository.save(userEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createUser DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"이용자 정보"},e);
			}
			
			log.debug("__DBGLOG__ 이용자 등록 종료");
			log.debug("__DBGLOG__ 이용자권한 등록(일반사용자) 시작");
			
			//===================================================================================
			// 이용자권한 등록(일반사용자)
			//===================================================================================
			userGroupEntity = new UserGroupEntity();
			userGroupEntity.setUid(uid);
			userGroupEntity.setGrpCd(UserConstants.USER_GROUP_CODE.GENERAL.getCode());
			userGroupEntity.setLastTrnUUID(header.getUuid());
			userGroupEntity.setLastTrnUid(header.getTrnUserId());
			userGroupEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				userGroupRepository.save(userGroupEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createUser DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"이용자 권한"}, e);
			}
			
			log.debug("__DBGLOG__ 이용자권한 등록(일반사용자) 종료");
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 시작");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 조회
			//===================================================================================
			maxSeq = userLogRepository.findMaxSeqByTrnDtAndUid(header.getCurrDate(), uid);
			
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 종료");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 증가
			//===================================================================================
			maxSeq++;
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 시작");
			
			//===================================================================================
			// 이용자 변경로그 등록
			//===================================================================================
			userLogEntity = new UserLogEntity();
			userLogEntity.setTrnDt(header.getCurrDate());
			userLogEntity.setUid(uid);
			userLogEntity.setSeq(maxSeq);
			userLogEntity.setUserChgTypeCd(UserConstants.USER_CHG_TYPE_CD.REG_BY_ADMIN.getCode());
			userLogEntity.setLastTrnUUID(header.getUuid());
			userLogEntity.setLastTrnUid(header.getTrnUserId());
			userLogEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				userLogRepository.save(userLogEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ createUser DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"이용자 변경로그"}, e);
			}
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 종료");
			log.debug("__DBGLOG__ 이용자정보 조회 시작");
			
			//===================================================================================
			// 이용자 정보 조회
			//===================================================================================
			userInfo = userRepository.findById(uid);
			
			if(userInfo.isEmpty()) {
				log.error("__ERRLOG__ createUser 등록 후 조회 NOT FOUND");
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("이용자ID:", uid)});
			}
			
			userEntity = null;
			userEntity = userInfo.get();
			
			result = new DataMap<String, Object>();
			result.put("userInfo", userEntity);
			
			log.debug("__DBGLOG__ 이용자정보 조회 종료");
			
			//===================================================================================
			// 서비스 결과 로깅
			//===================================================================================
			log.debug("__DBGLOG__ [{}]", result);
			
		} catch (BizException be) {
			log.error("__ERRLOG__ createUser BizException", be);
			log.error("__ERRLOG__ createUser BizException {}", be.getMessage());
			throw be;
		} catch (Exception e) {
			log.error("__ERRLOG__ createUser Exception 발생 : {}", e);
			throw new BizException("reg_error_prefix", new String[] {"이용자"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> handleInvalidPassword(Request request) {
		
		DataMap<String, Object> params = null;
		DataMap<String, Object> result = null;
		CommonHeader header = null;
		Optional<UserEntity> optionalUserInfo = null;
		UserEntity userInfo = null;
		PasswordErrorHistoryEntity pswdErrHistEntity = null;
		String uid = "";
		String userState = "";
		int errCnt = 0;
		int maxSeq = 0;
		
		try {
			
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			
			uid = params.getString("uid");
			
			log.debug("__DBGLOG__ 필수값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(uid)) {
				log.error("__ERRLOG__ 이용자ID 미입력");
				throw new BizException("val_required", new String[] {"이용자ID"}); 
			}
			
			log.debug("__DBGLOG__ 필수값 체크 종료");
			log.debug("__DBGLOG__ 이용자 조회 시작");
			
			//===================================================================================
			// 이용자정보 조회
			//===================================================================================
			optionalUserInfo = userRepository.findById(uid);
			
			// 이용자 정보 없으면 오류
			if(optionalUserInfo.isEmpty()) {
				log.error("__ERRLOG__ 이용자 미존재 [{}]", uid);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("이용자ID: ", uid)});
			} else {
				userInfo = optionalUserInfo.get();
			}
			
			log.debug("__DBGLOG__ 이용자 조회 종료");
			log.debug("__DBGLOG__ 잠금여부 체크 시작");
			
			//===================================================================================
			// 잠금여부 체크
			//===================================================================================
			errCnt = userInfo.getPswdErrCnt() == null ? 0 : userInfo.getPswdErrCnt();
			
			log.debug("__DBGLOG__ 비밀번호 오류횟수:[{}]", errCnt);
			
			if(++errCnt < UserConstants.MAX_PSWD_ERR_CNT) {
				userState = userInfo.getUserState();
			} else {
				userState = UserConstants.USER_STATE.LOCKED.getCode();
			}
			
			log.debug("__DBGLOG__ 잠금여부 체크 종료");
			log.debug("__DBGLOG__ 비밀번호 오류내역 등록 시작");
			
			//===================================================================================
			// 비밀번호 오류내역 최대일련번호 조회
			//===================================================================================
			maxSeq = passwordErrorHistoryRepository.findMaxSeqByTrnDtAndUid(header.getCurrDate(), uid);
			
			log.debug("__DBGLOG__ 현재 최대일련번호 [{}]", maxSeq);
			
			//===================================================================================
			// 비밀번호 오류내역 등록
			//===================================================================================
			pswdErrHistEntity = new PasswordErrorHistoryEntity();
			pswdErrHistEntity.setTrnDt(header.getCurrDate());
			pswdErrHistEntity.setUid(uid);
			pswdErrHistEntity.setSeq(++maxSeq);
			pswdErrHistEntity.setErrPswd(params.getString("errPswd"));
			pswdErrHistEntity.setDelYn(Constants.NO);
			pswdErrHistEntity.setLastTrnUid(header.getTrnUserId());
			pswdErrHistEntity.setLastTrnUUID(header.getUuid());
			pswdErrHistEntity.setLastTrnCd(header.getTrnCd());
			
			log.debug("__DBGLOG__ 오류내역 Entity: [{}]", pswdErrHistEntity);
			
			try {
				passwordErrorHistoryRepository.saveAndFlush(pswdErrHistEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ handleInvalidPassword DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"비밀번호 오류내역"}, e);
			}
			
			log.debug("__DBGLOG__ 비밀번호 오류내역 등록 종료");
			log.debug("__DBGLOG__ 이용자 정보 갱신 시작");
			
			//===================================================================================
			// 이용자정보 갱신
			//===================================================================================
			userInfo.setPswdErrCnt(errCnt);
			userInfo.setUserState(userState);
			userInfo.setLastTrnUid(header.getTrnUserId());
			userInfo.setLastTrnUUID(header.getUuid());
			userInfo.setLastTrnCd(header.getTrnCd());
			
			try {
				userRepository.saveAndFlush(userInfo);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ handleInvalidPassword DataAccessException 발생 : {}", e);
				throw new BizException("chg_error_prefix", new String[] {"이용자 정보"}, e);
			}
			
			log.debug("__DBGLOG__ 이용자 정보 갱신 종료");
			
			//===================================================================================
			// 결과조립
			//===================================================================================
			result.put("uid", userInfo.getUid());
			result.put("pswdErrCnt", userInfo.getPswdErrCnt());
			result.put("userState", userState);
			
		} catch (BizException be) {
			throw be;
		} catch (Exception e) {
			throw new BizException("process_err", e);
		}
		return result;
	}

	@Override
	public DataMap<String, Object> updateUser(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<UserEntity> optionalUserEntity = Optional.empty();
		UserEntity userEntity = null;
		UserLogEntity userLogEntity = null;
		String uid = "";
		String oldUserState = "";
		String newUserState = "";
		int maxSeq = 0;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			uid = params.getString("uid");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(uid)) {
				log.error("__ERRLOG__ 이용자아이디 미입력");
				throw new BizException("val_required", new String[] {"이용자아이디"}); 
			}
			
			if(StringUtil.isEmpty(params.getString("delYn"))) {
				log.error("__ERRLOG__ 삭제여부 미입력");
				throw new BizException("val_required", new String[] {"삭제여부"}); 
			}
			
			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 이용자 조회 시작");

			//===================================================================================
			// 이용자정보 조회
			//===================================================================================
			optionalUserEntity = userRepository.findById(uid);
			
			// 이용자 정보 없으면 오류
			if(optionalUserEntity.isEmpty()) {
				log.error("__ERRLOG__ 이용자 미존재 [{}]", uid);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("이용자ID: ", uid)});
			} else {
				userEntity = optionalUserEntity.get();
				oldUserState = userEntity.getUserState();
				newUserState = params.getString("userState", "");
			}
			
			log.debug("__DBGLOG__ 이용자 조회 종료");
			log.debug("__DBGLOG__ 이용자 수정 시작");
			
			//===================================================================================
			// 이용자정보 수정
			//===================================================================================
			userEntity.setName(params.getString("name", ""));
			userEntity.setUserState(newUserState);
			userEntity.setDelYn(Constants.NO);
			userEntity.setLastTrnUUID(header.getUuid());
			userEntity.setLastTrnUid(header.getTrnUserId());
			userEntity.setLastTrnCd(header.getTrnCd());
			
			//기존에 잠금상태에서 사용으로 변경 시 비밀번호 오류횟수 초기화
			if(UserConstants.USER_STATE.LOCKED.getCode().equals(oldUserState) &&
			   UserConstants.USER_STATE.USE.getCode().equals(newUserState)) {
				userEntity.setPswdErrCnt(0);
			}
			
			try {
				
				userEntity = userRepository.saveAndFlush(userEntity);
				
				if(userEntity == null) {
					log.error("__ERRLOG__ updateUser saveAndFlush null");
					throw new BizException("chg_error_prefix", new String[] {"이용자정보"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateUser DataAccessException 발생 : {}", e);
				throw new BizException("chg_error_prefix", new String[] {"이용자 정보"},e);
			}
			
			log.debug("__DBGLOG__ 이용자 수정 종료");
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 시작");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 조회
			//===================================================================================
			maxSeq = userLogRepository.findMaxSeqByTrnDtAndUid(header.getCurrDate(), uid);
			
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 종료");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 증가
			//===================================================================================
			maxSeq++;
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 시작");
			
			//===================================================================================
			// 이용자 변경로그 등록
			//===================================================================================
			userLogEntity = new UserLogEntity();
			userLogEntity.setTrnDt(header.getCurrDate());
			userLogEntity.setUid(uid);
			userLogEntity.setSeq(maxSeq);
			userLogEntity.setUserChgTypeCd(UserConstants.USER_CHG_TYPE_CD.CHG_BY_ADMIN.getCode());
			userLogEntity.setLastTrnUUID(header.getUuid());
			userLogEntity.setLastTrnUid(header.getTrnUserId());
			userLogEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				userLogRepository.saveAndFlush(userLogEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ updateUser DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"이용자 변경로그"}, e);
			}
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("userInfo", userEntity);
		
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ updateUser Exception 발생 : {}", e);
			throw new BizException("chg_error_prefix", new String[] {"이용자"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> deleteUser(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		
		Optional<UserEntity> optionalUserEntity = Optional.empty();
		UserEntity userEntity = null;
		UserLogEntity userLogEntity = null;
		String uid = "";
		int maxSeq = 0;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			uid = params.getString("uid");
			
			log.debug("__DBGLOG__ 입력값 체크 시작");
			
			//===================================================================================
			// 필수값 체크
			//===================================================================================
			if(StringUtil.isEmpty(uid)) {
				log.error("__ERRLOG__ 이용자아이디 미입력");
				throw new BizException("val_required", new String[] {"이용자아이디"}); 
			}

			log.debug("__DBGLOG__ 입력값 체크 종료");
			log.debug("__DBGLOG__ 이용자 조회 시작");

			//===================================================================================
			// 이용자정보 조회
			//===================================================================================
			optionalUserEntity = userRepository.findById(uid);
			
			// 이용자 정보 없으면 오류
			if(optionalUserEntity.isEmpty()) {
				log.error("__ERRLOG__ 이용자 미존재 [{}]", uid);
				throw new BizException("not_found_msg", new String[] {StringUtil.concat("이용자ID: ", uid)});
			} else {
				
				userEntity = optionalUserEntity.get();
				
				if(Constants.YES.equals(userEntity.getDelYn())) {
					log.error("__ERRLOG__ 이미 삭제된 이용자 [{}]", uid);
					throw new BizException("del_already_msg", new String[] {StringUtil.concat("이용자아이디: ", uid)});
				}
			}
			
			log.debug("__DBGLOG__ 이용자 조회 종료");
			log.debug("__DBGLOG__ 이용자 삭제 시작");
			
			//===================================================================================
			// 이용자정보 수정
			//===================================================================================
			userEntity.setDelYn(Constants.YES);
			userEntity.setLastTrnUUID(header.getUuid());
			userEntity.setLastTrnUid(header.getTrnUserId());
			userEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				
				userEntity = userRepository.saveAndFlush(userEntity);
				
				if(userEntity == null) {
					log.error("__ERRLOG__ deleteUser saveAndFlush null");
					throw new BizException("chg_error_prefix", new String[] {"이용자정보"});
				}
				
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteUser DataAccessException 발생 : {}", e);
				throw new BizException("del_error_prefix", new String[] {"이용자 정보"},e);
			}
			
			log.debug("__DBGLOG__ 이용자 삭제 종료");
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 시작");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 조회
			//===================================================================================
			maxSeq = userLogRepository.findMaxSeqByTrnDtAndUid(header.getCurrDate(), uid);
			
			log.debug("__DBGLOG__ 이용자변경로그 최대일련번호 조회 종료");
			
			//===================================================================================
			// 이용자 변경로그 최대 일련번호 증가
			//===================================================================================
			maxSeq++;
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 시작");
			
			//===================================================================================
			// 이용자 변경로그 등록
			//===================================================================================
			userLogEntity = new UserLogEntity();
			userLogEntity.setTrnDt(header.getCurrDate());
			userLogEntity.setUid(uid);
			userLogEntity.setSeq(maxSeq);
			userLogEntity.setUserChgTypeCd(UserConstants.USER_CHG_TYPE_CD.DEL_BY_ADMIN.getCode());
			userLogEntity.setLastTrnUUID(header.getUuid());
			userLogEntity.setLastTrnUid(header.getTrnUserId());
			userLogEntity.setLastTrnCd(header.getTrnCd());
			
			try {
				userLogRepository.saveAndFlush(userLogEntity);
			} catch (DataAccessException e) {
				log.error("__ERRLOG__ deleteUser DataAccessException 발생 : {}", e);
				throw new BizException("reg_error_prefix", new String[] {"이용자 변경로그"}, e);
			}
			
			log.debug("__DBGLOG__ 이용자변경로그 등록 종료");
			
			//===================================================================================
			// 출력값 조립
			//===================================================================================
			result.put("userInfo", userEntity);
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteUser Exception 발생 : {}", e);
			throw new BizException("del_error_msg", new String[] {"이용자"}, e);
		}
		
		return result;
	}

	@Override
	public DataMap<String, Object> deleteManyUser(Request request) {
		
		DataMap<String, Object> result = null;
		DataMap<String, Object> params = null;
		CommonHeader header = null;
		Request deleteRequest = null;
		DataMap<String, Object> deleteParams = null;
		List<String> uidList = null;
		
		try {
			
			//===================================================================================
			// 변수 초기값 세팅
			//===================================================================================
			result = new DataMap<>();
			header = request.getHeader();
			params = request.getParameter();
			deleteRequest = new Request();
			
			uidList = params.getList("uidList");
			
			log.debug("__DBGLOG__ uidList; [{}]", uidList);
			
			deleteRequest.setHeader(header);
			
			for(String uid:uidList) {
				deleteParams = new DataMap<>();
				deleteParams.put("uid", uid);
				deleteRequest.setParameter(deleteParams);
				serviceInvoker.callService("UserService", "deleteUser", deleteRequest);
			}
			
		} catch (BizException be) {
			throw be;	
		} catch (Exception e) {
			log.error("__ERRLOG__ deleteManyUser Exception 발생 : {}", e);
			throw new BizException("del_error_msg", new String[] {"이용자"}, e);
		}
		
		return result;
	}

}
