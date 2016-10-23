package com.gewara.job.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.SettleConstant;
import com.gewara.job.JobService;
import com.gewara.model.GspJob;
import com.gewara.service.DaoService;
import com.gewara.service.RongDuanPreventService;

public class RongDuanJob extends JobService {
	
	@Autowired
	@Qualifier("daoService")
	protected DaoService daoService;

	@Autowired
	@Qualifier("rongDuanPreventService")
	private RongDuanPreventService rongDuanPreventService;
	
	/**
	 * ÿ����۶Ϲ�Ӧ��ӰƱͳ��
	 * ִ��ʱ��ÿ��4:30:00
	 */
	public void everyDayCollection() {
		rongDuanPreventService.everyDayCollection();
	}
	
	/**
	 * �۶�ͳ��
	 */
	public void rongduanCollection() {
		GspJob job = daoService.getObject(GspJob.class, "rd-autoTomerchant");
		job.setStatus(SettleConstant.RUNNING);
		daoService.updateObject(job);
		
		rongDuanPreventService.rongduanCollection();
		
		job.setStatus(SettleConstant.FINISH);
		daoService.updateObject(job);
	}
}
