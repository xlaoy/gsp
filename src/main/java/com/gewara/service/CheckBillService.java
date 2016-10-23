/** 
 */
package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.SettlementBill;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 5, 2013  4:31:59 PM
 */
public interface CheckBillService {
	/**
	 * ���ڽ�������Ϊÿ���������ɶ��˵���ÿ�죩
	 */
	//void initNewCheckBill(List<SettleConfig> configs,String tag);

	/**
	 * @param lastBillTime
	 * @param c
	 */
	//void generateNewCheckBill(Timestamp lastBillTime, SettleConfig c ,String tag);
	
	/**
	 * 
	 * @param placeId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<CheckBill> queryCheckBill(String placeId, String isSettle,String tag, Timestamp start, Timestamp end, int maxSize);

	/**
	 * @param end 
	 * @param start 
	 * @param isSettle 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	int countingCheckBill(String placeId, String isSettle, Timestamp start, Timestamp end,String tag);

	/**
	 * @param cks
	 * @param configMap
	 * @return
	 */
	//Map<Long, Map<String, Object>> queryAggre(SettlementBill settleBill, List<CheckBill> cks, Map<String, SettleConfig> configs);

	/**
	 * @return
	 */
	//List<SettleConfig> getAllSettleConfig();

	/**
	 * @param lastEndTime
	 * @param endTime
	 * @return
	 */
	//List<CheckBill> queryTimePeriodCheckBill(SettlementBill settleBill);
	
	/**
	 * @param lastEndTime
	 * @param endTime
	 * @return
	 */
	List<CheckBill> createCheckBillBySettleBill(SettlementBill settleBill);
	
	 
	
	/**
	 * ******************zyj******************
	 * @param lastEndTime
	 * @param endTime
	 * @return
	 */
	//int queryPlaceCountByRelateId(Long relateId, Timestamp fristDay);
	 /******************zyj******************/ 
	 
	
	/**
	 * @param cks
	 * @param configs
	 * @param isFake
	 * @return
	 */
	//Map<Long, Map<String, Object>> aggreCheckbills(SettlementBill settleBill, List<CheckBill> cks, Map<String, SettleConfig> configs, boolean isFake);

	/**
	 * @param placeIds
	 * @param status
	 * @param start
	 * @param end
	 * @param maxSize
	 * @return
	 */
	List<CheckBill> queryCheckBill(List<String> placeIds, String status,String tag,
			Timestamp start, Timestamp end, int maxSize);

	/**
	 * @param placeId
	 * @param status
	 * @param start
	 * @param end
	 * @param isDefaulfStatus
	 * @return
	 */
	int countingCheckBill(String placeId, String status, Timestamp start,
			Timestamp end, boolean isDefaulfStatus,String tag);

	/**
	 * @param placeId
	 * @param status
	 * @param start
	 * @param end
	 * @param i
	 * @param isStatusNotEq
	 * @return
	 */
	List<CheckBill> queryCheckBill(String placeId, String status,
			Timestamp start, Timestamp end, int i, boolean isStatusNotEq,String tag);

	/**
	 * @param settleBill
	 * @param status
	 */
	void settleBillStatusChanged(SettlementBill settleBill, String status);

	/**
	 * @return
	 */
	//List<SettleConfig> getAvaliableSettleConfig();

	/**
	 * @param ck
	 * @param ad
	 * @return
	 */
	ResultCode afterAdjust(CheckBill ck, Adjustment ad);

	/**
	 * @param configs
	 * @param tag
	 */
	//Map<String/**config.recordid>**/, Timestamp> getLastCheckeBllByConfigs(List<SettleConfig> configs,String tag);
	
	/**
	 * @param configs
	 * @param tag
	 */
	//Map<String/**config.recordid>**/, Timestamp> getLastCheckeBllByConfigsExt(List<SettleConfig> configs,String tag);
	
	/**
	 * ͨ���Ѷ��˵��ۺ�
	 */
	//Map<Long, Map<String, Object>> aggreChannelCheckbills(SettlementBill settleBill, List<CheckBill> cks,
		//	Map<String, ChannelSettleConfig> configs, boolean isFake);

	/**
	 * ��Ʒ���˵��ۺ�
	 */
	//Map<Long, Map<String, Object>> aggreGoodsCheckbills(
		//	SettlementBill settleBill, List<CheckBill> cks,
		//	Map<String, SettleConfig> configs, boolean isFake);
	
	/**
	 * ӰƱ���㵥����
	 * @param settleBill
	 * @return
	 */
	List<CheckBill> ticketCheckBillCollection(SettlementBill settleBill);
	
	/**
	 * ��Ʒ���㵥����
	 * @param settleBill
	 * @return
	 */
	List<CheckBill> goodsCheckBillCollection(SettlementBill settleBill);
	
	/**
	 * ����ͨ���ѷǵ��ҵ�ӰԺ�˵�
	 * @param lastEndTime
	 * @param endTime
	 * @return
	 */
	List<CheckBill> createPlaceCheckBillBySettleBill(SettlementBill settleBill);
	
	/**
	 * ͨ���ѽ��㵥����
	 * @param settleBill
	 * @return
	 */
	List<CheckBill> channelCheckBillCollection(SettlementBill settleBill);
	
	/**
	 * ϵͳ��ͨ���ѽ��㵥����
	 * @param settleBill
	 * @return
	 */
	List<CheckBill> channelSysCheckBillCollection(SettlementBill settleBill, ChannelSettleConfig csc);

}
