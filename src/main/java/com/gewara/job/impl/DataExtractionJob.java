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
	 * 同步结算配置
	 */
	public void syncSettleConfig(){
		dataExtractionService.syncSettleConfig();
	}

	/**
	 * 只同步院线下面关联的影院信息
	 */
	public void syncChannleConfig(){
		dataExtractionService.syncChannleConfig();
	}
	
	/**
	 * zyj
	 * 同步异价订单
	 */
	public void sysDiffPriceOrder() {
		dataExtractionService.sysDiffPriceOrder();
	}
	
}
