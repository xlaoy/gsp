/** 
 */
package com.gewara.job.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.SettleConstant;
import com.gewara.job.JobService;
import com.gewara.service.DataExtractionService;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  10:11:30 AM
 */
public class DataExtractionJob extends JobService{

	@Autowired
	@Qualifier("dataExtractionService")
	private DataExtractionService dataExtractionService;
	
	public void syncOrder(){
		dataExtractionService.syncOrder(SettleConstant.ORDER_TYPE_TICKET);
		dataExtractionService.syncRefundment();
	}
	public void syncPlace(){
		dataExtractionService.syncPlace();
	}
	public void syncPlayItemPrice(){
		dataExtractionService.syncPlayItemPrice();
	}
	
	public void syncGoodsOrder(){
		dataExtractionService.syncGoods(SettleConstant.ORDER_TYPE_GOODS);
	}
	
	/**
	 * ͬ����������
	 */
	public void syncSettleConfig(){
		dataExtractionService.syncSettleConfig();
	}

	/**
	 * ֻͬ��Ժ�����������ӰԺ��Ϣ
	 */
	public void syncChannleConfig(){
		dataExtractionService.syncChannleConfig();
	}
	
	/**
	 * zyj
	 * ͬ����۶���
	 */
	public void sysDiffPriceOrder() {
		dataExtractionService.sysDiffPriceOrder();
	}
	
}
