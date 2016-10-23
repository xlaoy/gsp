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
	 * ͬ������
	 */
	public void syncOrder(String OrderType);
	/**
	 * ͬ������
	 */
	public void syncPlace();
	/**
	 * ͬ���˿�
	 */
	public void syncRefundment();
	/**
	 * @param start
	 * @param end
	 */
	void syncOrder(Timestamp start, Timestamp end , String orderType);
	
	/**
	 * ͬ�����μ۸� 
	 */
	public void syncPlayItemPrice();
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncGoods  
	 * @Description:ͬ����Ʒ
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
	 * @Description:ͬ����������
	 * @return void
	 */
	void syncSettleConfig();
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: syncChannleConfig  
	 * @Description:ͨ�������������Ϣͬ��
	 * @param 
	 * @return void
	 * @throws
	 */
	void syncChannleConfig();
	
	/**
	 * zyj
	 * ͬ����۶���
	 */
	void sysDiffPriceOrder();
	
	/**
	 * zyj
	 * ͬ�������б�
	 */
	void sysBaoChang();
	
	String getOuterId(Map<String, Object> row);
}
