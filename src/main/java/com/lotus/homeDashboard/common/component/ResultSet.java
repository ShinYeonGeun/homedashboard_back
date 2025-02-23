package com.lotus.homeDashboard.common.component;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSet {
	
	private String resultCd;
	private UUID uuid;
	private Object payload;
	
	// 제네릭 메서드를 사용하여 특정 타입으로 캐스팅
    public <T> T getPayloadAs(Class<T> type) {
        if (payload == null) {
            return null; // payload가 null인 경우 null 반환
        }
        if (type.isInstance(payload)) {
            return type.cast(payload); // 안전한 캐스팅
        } else {
            log.error("Payload cannot be cast to [{}]", type.getName());
            return null;
        }
    }
    
    
    @SuppressWarnings("unchecked")
	public DataMap<String, Object> payloadAsDataMap() {
    	return getPayloadAs(DataMap.class);
    }
    
    @SuppressWarnings("unchecked")
	public List<DataMap<String, Object>> payloadAsDataMapList() {
    	return getPayloadAs(ArrayList.class);
    }
}
