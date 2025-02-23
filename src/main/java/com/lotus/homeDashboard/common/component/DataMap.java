package com.lotus.homeDashboard.common.component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		return this.getString(k) == null ? defaultValue:this.getString(k);
	}

	public Integer getInteger(K k) {
		
		String str = this.getString(k);
				
		if(str == null) {
			return null;
		} 
		
		return str.trim().length() == 0 ? null : Integer.valueOf(str);
	}
	
	public int getInt(K k) {
		
		if(super.get(k) == null) {
			throw new NullPointerException();
		}
		
		return Integer.parseInt(this.getString(k));
	}
	
	public int getInt(K k, int defaultValue) {
		
		try {
			return this.getInt(k);
		} catch (NullPointerException npe) {
			return defaultValue;
		}
	}
	
	public long getLong(K k) {
		
		if(super.get(k) == null) {
			throw new NullPointerException();
		}
		
		return Long.parseLong(this.getString(k));
	}
	
	public long getLong(K k, long defaultValue) {
		
		try {
			return this.getLong(k);
		} catch (NullPointerException npe) {
			return defaultValue;
		}
	}
	
	public BigDecimal getBigDecimal(K k) {
		
		if(super.get(k) == null) {
			throw new NullPointerException();
		}
		
		return new BigDecimal(this.getString(k));
	}
	
	public BigDecimal getBigDecimal(K k, BigDecimal defaultValue) {
		
		try {
			return this.getBigDecimal(k);
		} catch (NullPointerException npe) {
			return defaultValue;
		}
	}
	
	@SuppressWarnings("unchecked")
	 public <T extends List<?>> T getList(K key) {
       Object value = super.get(key);
       if (value instanceof List<?>) {
           return (T) value;
       }
       return null;
   }
	
	@SuppressWarnings("unchecked")
	 public <T extends Map<?, ?>> T getMap(K key) {
       Object value = super.get(key);
       if (value instanceof Map<?, ?>) {
           return (T) value;
       }
       return null;
   }
}
