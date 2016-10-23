package com.gewara.job.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.job.JobService;
import com.gewara.service.DramaJobService;
import com.gewara.untrans.UntransDramaService;

public class DramaJob extends JobService {

	@Autowired
	@Qualifier("dramaJobServiceImpl")
	protected DramaJobService dramaJobService;
	
	@Autowired
	@Qualifier("untransDramaServiceImpl")
	protected UntransDramaService untransDramaService;
	
	/**
	 * 同步演出项目
	 *  每天01:00:00开始同步
	 * 	每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDrama() {
		dramaJobService.syncDrama();
	}
	
	/**
	 * 同步演出供应商信息
	 * 	每天01:20:00开始同步
	 *  每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDramaSupplier() {
		dramaJobService.syncDramaSupplier();
	}
	
	/**
	 * 同步演出场馆信息
	 * 	每天01:30:00开始同步
	 *  每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDramaPlace() {
		dramaJobService.syncDramaPlace();
	}
	
	/**
	 * 同步演出场次信息
	 * 	每天01:40:00开始同步
	 *  每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDramaPlayItem() {
		dramaJobService.syncDramaPlayItem();
	}
	
	/**
	 * 同步演出配置
	 *  每天03:10:00开始同步
	 *  每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDramaConfig() {
		dramaJobService.syncDramaConfig();
		dramaJobService.sysWaitApprovalDramaConfig();
	}
	
	/**
	 * 同步演出订单
	 *  每天04:00:00
	 *  每天同步前一天的数据，同步的时间范围从00:00:00 <= t < 00:00:00
	 */
	public void syncDramaOrder() {
		dramaJobService.syncOnlineOrder();
		dramaJobService.syncOnlineRefund();
		dramaJobService.syncOfflineOrder();
		dramaJobService.syncOfflineRefund();
		//dramaJobService.syncPiaowuOrder();
		//dramaJobService.syncPiaowuRefund();
	}
	
	/**
	 * 生成结算单据
	 * 	每天04:40:00运行
	 */
	public void generateDramaBill() {
		dramaJobService.generateSettltBill();
	}
	
	/**
	 * 计算单据
	 *  每天05:00:00运行
	 */
	public void calculateBill() {
		//检查数据同步情况
		boolean flag = dramaJobService.checkSysDataJob();
		if(flag) {
			untransDramaService.calculateSettleBill();
		}
	}
	
	/**
	 * 计算计提成本单据
	 *  每月5号 06:00:00运行
	 */
	public void calculateJitiBill() {
		//检查数据同步情况
		boolean flag = dramaJobService.checkSysDataJob();
		if(flag) {
			untransDramaService.calculateJitiBill();
		}
	}
}
