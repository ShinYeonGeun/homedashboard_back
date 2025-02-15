package com.lotus.homeDashboard.cmn.ccd.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lotus.homeDashboard.cmn.ccd.dao.CommonCodeDslRepository;
import com.lotus.homeDashboard.cmn.ccd.entity.CommonCodeDetailEntity;
import com.lotus.homeDashboard.cmn.ccd.entity.CommonCodeEntity;
import com.lotus.homeDashboard.cmn.ccd.entity.QCommonCodeDetailEntity;
import com.lotus.homeDashboard.cmn.ccd.entity.QCommonCodeEntity;
import com.lotus.homeDashboard.cmn.ccd.service.CommonCodeService;
import com.lotus.homeDashboard.common.component.DataMap;
import com.lotus.homeDashboard.common.component.Request;
import com.querydsl.core.Tuple;

import lombok.extern.slf4j.Slf4j;

@Service("CommonCodeService")
@Slf4j
public class CommonCodeServiceImpl implements CommonCodeService {
	
	@Autowired
	private CommonCodeDslRepository commonCodeDslRepository;

	@Override
	public DataMap<String, Object> inqCommonCodeNAllDetail(Request request) {
		List<DataMap<String, Object>> list = new ArrayList<>();
		DataMap<String, Object> params = request.getParameter();
		DataMap<String, Object> result = new DataMap<>();
		List<String> paramCodeList = null;
		
		List<Tuple> tuples = null;
		//int recodeCount = 0;
		
		paramCodeList = params.get("codeList") == null ? new ArrayList<>():params.getList("codeList");
		
		tuples = commonCodeDslRepository.findAllCommonCodeNDetail( paramCodeList
																  , params.getString("codeDelYn")
																  , params.getString("codeDetailDelYn"));
		//recodeCount = tuples.size();
		
		log.debug("tuples {}", tuples);
		
		for(Tuple tuple : tuples) {
			List<DataMap<String, Object>> codeList = null;
			DataMap<String, Object> codeInfo = null;
			DataMap<String, Object> codeDetailInfo = null;
			CommonCodeEntity commonCode = tuple.get(QCommonCodeEntity.commonCodeEntity);
			CommonCodeDetailEntity commonCodeDetailEntity = tuple.get(QCommonCodeDetailEntity.commonCodeDetailEntity);

			if(result.containsKey(commonCode.getCode())) {
				codeInfo = result.getMap(commonCode.getCode());
			}
			
			//동일한 키가 없으면 새로 map 생성, 있으면 list 가져옴.
			if(codeInfo == null) {
				codeInfo = new DataMap<String, Object>();
				codeList = new ArrayList<DataMap<String, Object>>();
				
				codeInfo.put("code", commonCode.getCode());
				codeInfo.put("name", commonCode.getName());
				codeInfo.put("description", commonCode.getDescription());
				codeInfo.put("delYn", commonCode.getDelYn());
				codeInfo.put("codeList", codeList);
				//list.add(codeInfo);
				result.put(commonCode.getCode(), codeInfo);
			} else {
				codeList = codeInfo.getList("codeList");
			}
			
			//item 추가
			codeDetailInfo = new DataMap<String, Object>();
			codeDetailInfo.put("codeVal", commonCodeDetailEntity.getCodeVal());
			codeDetailInfo.put("codeValCtnt", commonCodeDetailEntity.getCodeValCtnt());
			codeDetailInfo.put("delYn", commonCodeDetailEntity.getDelYn());
			
			codeList.add(codeDetailInfo);
			
		}
		
//		result.put("codeList", list);
		
		return result;
	}

}
