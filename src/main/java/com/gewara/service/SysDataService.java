package com.gewara.service;

import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;

import com.gewara.model.gsp.SysData;

public interface SysDataService {

	
	ModelMap querySysData(SysData data, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	<T extends Object> Map<String, SysData> getSysDataMap(List<T> codeList, String type, Class<T> cls);
	
	Map<String, SysData> getSysDataMap(String type);
	
	<T extends Object> SysData getSysData(T code, String type, Class<T> clz);
}
