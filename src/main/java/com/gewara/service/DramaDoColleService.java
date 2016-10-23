package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;


public interface DramaDoColleService {

	//手动同步演出
	String syncDramaByDramaid(List<Long> dramaidlist);
	
	//手动同步场次
	String syncPlayItemByDramaid(List<Long> dramaidlist);
	
	//手动同步配置版本
	String syncDramConfigByDramaId(Long dramaid);
	
	//处理同步演出项目版本数据
	void updateDrama(List<Map<String, Object>> mapList);
	
	//处理同步演出项目配置版本数据
	String processDramaVersionConfig(Map<String, Object> configmap, boolean sysjob);
	
	//更新演出配置信息
	String updateDramConfig(DramaConfig dconfig, boolean sysjob);
	
	//生成结算单据
	String createSettleBill(DramaConfig config);
	
	//重置初始化单据
	void initDramaBill(DramaSettleBill bill);
	
	//计算单据
	String fullSettleBill(DramaSettleBill bill, boolean sysjob);
	
	//计算单据
	String fullJitiBill(DramaJitiBill bill);
}
