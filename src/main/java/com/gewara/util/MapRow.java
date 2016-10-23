package com.gewara.util;

import java.sql.Timestamp;
import java.util.Map;

public class MapRow {

	private Map<String, Object> map;
	
	public MapRow(Map<String, Object> map) {
		this.map = map;
	}
	
	public Integer getInteger(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? 0 : Integer.valueOf(map.get(key).toString());
	}
	
	public Integer getIntegerWithNull(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? null : Integer.valueOf(map.get(key).toString());
	}
	
	public Long getLong(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? 0l : Long.valueOf(map.get(key).toString());
	}
	
	public Long getLongWithNull(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? null : Long.valueOf(map.get(key).toString());
	}
	
	public Double getDouble(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? 0.0 : Double.valueOf(map.get(key).toString());
	}
	
	public Double getDoubleWithNull(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? null : Double.valueOf(map.get(key).toString());
	}
	
	public String getString(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	public String getStringWithNull(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? null : map.get(key).toString();
	}
	
	public Timestamp getTimestamp(String key) {
		if(map == null) {
			return null;
		}
		return map.get(key) == null ? null : (Timestamp)map.get(key);
	}
	
}
