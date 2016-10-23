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
	 * ͬ���ݳ���Ŀ
	 *  ÿ��01:00:00��ʼͬ��
	 * 	ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
	 */
	public void syncDrama() {
		dramaJobService.syncDrama();
	}
	
	/**
	 * ͬ���ݳ���Ӧ����Ϣ
	 * 	ÿ��01:20:00��ʼͬ��
	 *  ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
	 */
	public void syncDramaSupplier() {
		dramaJobService.syncDramaSupplier();
	}
	
	/**
	 * ͬ���ݳ�������Ϣ
	 * 	ÿ��01:30:00��ʼͬ��
	 *  ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
	 */
	public void syncDramaPlace() {
		dramaJobService.syncDramaPlace();
	}
	
	/**
	 * ͬ���ݳ�������Ϣ
	 * 	ÿ��01:40:00��ʼͬ��
	 *  ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
	 */
	public void syncDramaPlayItem() {
		dramaJobService.syncDramaPlayItem();
	}
	
	/**
	 * ͬ���ݳ�����
	 *  ÿ��03:10:00��ʼͬ��
	 *  ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
	 */
	public void syncDramaConfig() {
		dramaJobService.syncDramaConfig();
		dramaJobService.sysWaitApprovalDramaConfig();
	}
	
	/**
	 * ͬ���ݳ�����
	 *  ÿ��04:00:00
	 *  ÿ��ͬ��ǰһ������ݣ�ͬ����ʱ�䷶Χ��00:00:00 <= t < 00:00:00
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
	 * ���ɽ��㵥��
	 * 	ÿ��04:40:00����
	 */
	public void generateDramaBill() {
		dramaJobService.generateSettltBill();
	}
	
	/**
	 * ���㵥��
	 *  ÿ��05:00:00����
	 */
	public void calculateBill() {
		//�������ͬ�����
		boolean flag = dramaJobService.checkSysDataJob();
		if(flag) {
			untransDramaService.calculateSettleBill();
		}
	}
	
	/**
	 * �������ɱ�����
	 *  ÿ��5�� 06:00:00����
	 */
	public void calculateJitiBill() {
		//�������ͬ�����
		boolean flag = dramaJobService.checkSysDataJob();
		if(flag) {
			untransDramaService.calculateJitiBill();
		}
	}
}
