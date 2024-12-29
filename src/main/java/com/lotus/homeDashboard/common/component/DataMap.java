package com.lotus.homeDashboard.common.component;

import java.util.HashMap;
import java.util.Map;

import com.lotus.homeDashboard.common.utils.StringUtil;

public class DataMap <K,V> extends HashMap<K, V> {
	
	private static final long serialVersionUID = 9166392270193192990L;

	public DataMap() {
		super();
	}

	public DataMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public DataMap(int initialCapacity) {
		super(initialCapacity);
	}

	public DataMap(Map<? extends K, ? extends V> m) {
		super(m);
	}

	public String getString(K k) {
		V v = super.get(k);
		
		if(v == null) {
			return null;
		}
		
		return String.valueOf(v);
	}
	
	public String getString(K k, String defaultValue) {
		return StringUtil.evl(this.getString(k), defaultValue);
	}

	public int getInt(K k) {
		V v = super.get(k);
		
		if(v == null) {
			throw new NullPointerException();
		}
		
		return Integer.valueOf(this.getString(k));
	}
}
