/** 
 */
package com.gewara.service;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  10:13:30 AM
 */
public interface DataExtractionService {
	/**
	 * 同步订单
	 */
	public void syncOrder(String OrderType);
	/**
	 * 同步场馆
	 */
	public void syncPlace();
	/**
	 * 同步退款
	 */
	public void syncRefundment();
	/**
	 * @param start
	 * @param end
	 */
	void syncOrder(Timestamp start, Timestamp end , String orderType);
	
	/**
	 * 同步场次价格 
	 */
	public void syncPlayItemPrice();
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncGoods  
	 * @Description:同步卖品
	 * @param orderType
	 * @return void
	 */
	void syncGoods(String orderType);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncGoods  
	 * @Description:
	 * @param orderType
	 * @param startTime
	 * @param endTime
	 * @return void
	 */
	void syncGoods(String orderType,Timestamp startTime,Timestamp endTime);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncSettleConfig  
	 * @Description:同步结算配置
	 * @return void
	 */
	void syncSettleConfig();
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncChannleConfig  
	 * @Description:通道费配置相关信息同步
	 * @param 
	 * @return void
	 * @throws
	 */
	void syncChannleConfig();
	
	/**
	 * zyj
	 * 同步异价订单
	 */
	void sysDiffPriceOrder();
	
	/**
	 * zyj
	 * 同步包场列表
	 */
	void sysBaoChang();
	
	String getOuterId(Map<String, Object> row);
}
