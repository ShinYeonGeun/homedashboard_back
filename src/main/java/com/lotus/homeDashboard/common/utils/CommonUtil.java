package com.lotus.homeDashboard.common.utils;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lotus.homeDashboard.common.component.DataMap;

import jakarta.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	public static String getRequestIP(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("WL-Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("HTTP_CLIENT_IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("X-Real-IP"); 
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("X-RealIP"); 
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.getRemoteAddr(); 
        }
        return ip;
	}
	
	public static DataMap<String, Object> convertToDataMap(Object o) {
		return new ObjectMapper().registerModule(new JavaTimeModule()).convertValue(o, new TypeReference<DataMap<String, Object>>() {});
	}
	
	public static DataMap<String, Object> extractPageValues(Page<?> list) {
		DataMap<String, Object> map = new DataMap<>();
		map.put("totalPages", list.getTotalPages());
		map.put("totalElements", list.getTotalElements());
		map.put("size", list.getSize());
		map.put("sort", list.getSort());
		map.put("first", list.isFirst());
		map.put("last", list.isLast());
		map.put("empty", list.isEmpty());
		map.put("last", list.isLast());
		map.put("number", list.getNumber());
		map.put("numberOfElements", list.getNumberOfElements());
		map.put("pageable", list.getPageable());
		
		return map;
	}

}
