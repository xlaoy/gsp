package com.gewara.service;

public interface DramaJobService {

	//同步演出项目
	void syncDrama();
	
	//同步演出配置
	void syncDramaConfig();
	
	//同步线上订单
	void syncOnlineOrder();
	
	//同步线上退单
	void syncOnlineRefund();
	
	//同步线下订单
	void syncOfflineOrder();
	
	//同步线下退单
	void syncOfflineRefund();
	
	//同步票务订单
	void syncPiaowuOrder();
	
	//同步票务退单
	void syncPiaowuRefund();
	
	//自动更新未审核的配置
	void sysWaitApprovalDramaConfig();
	
	//自动检查同步基础数据的job
	boolean checkSysDataJob();
	
	//同步演出供应商信息
	void syncDramaSupplier();
	
	//同步演出场馆信息
	void syncDramaPlace();
	
	//生成结算单据
	void generateSettltBill();
	
	//同步演出场次信息
	void syncDramaPlayItem();
	
}
