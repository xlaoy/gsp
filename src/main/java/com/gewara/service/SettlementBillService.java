/** 
 */
package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.vo.SettleBillVo;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 8, 2013  3:08:51 PM
 */
public interface SettlementBillService {
	
	ModelMap settlementbills(SettleBillVo billvo, Integer pageNo, Integer pageSize, ModelMap model);
	
	/**
	 * 为每个settleconfig 生成空的settlement bill
	 * @param lastSettleBillsTime 
	 * @param tag 
	 * @param settleConfigs 
	 */
	List<SettlementBill> generateSettlementBill(List<SettleConfig> settleConfigs, String tag);

	/**
	 * @param placeId
	 * @return
	 */
	//int countingSettleBills(String placeId,  Timestamp start, Timestamp end, String status,String billType,String playType);

	/**
	 * @param placeId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	//List<SettlementBill> querySettleMents(String placeId,  Timestamp start, Timestamp end, String status, String orderField,String billType, Integer pageNo,Integer pageSize,String playType);

	/**
	 * @param bill
	 */
	void fulFillSettlementBill(SettlementBill bill);

	/**
	 * @param placeIds
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	//List<SettlementBill> querySettleMents(List<String> placeIds, Timestamp start, Timestamp end, String status,String billType, Integer pageNo, Integer pageSize,String playType);
	/**
	 * @param placeIds
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<SettlementBill> querySettleMents(List<String> placeIds, Timestamp start, Timestamp end, String status, String orderField, 
			Integer pageNo, Integer pageSize, boolean isStatusNotEq,String billType,String reqMoneyStatus,String playType);

	/**
	 * @param placeIds
	 * @return
	 */
	//int countingSettleBills(List<String> placeIds, Timestamp start, Timestamp end, String status,String billType,String playType);
	/**
	 * @param placeIds
	 * @param billType 
	 * @return
	 */
	int countingSettleBills(List<String> placeIds, Timestamp start, Timestamp end, String status,  boolean isStatusNotEq, String billType,String reqMoneyStatus,String playType);

	/**
	 * @param recordId
	 * @param placeId
	 * @return
	 */
	ResultCode saveAdjustment(SettlementBill bill, String placeId, String comments, String attachePath, Long operator, String mname, String operatorCategory);


	/**
	 * @param bills
	 * @return
	 */
	ResultCode merchantBatchConfirn(List<SettlementBill> bills, Long uid, String uname);

	/**
	 * pay a settlement bill to merchant
	 * @param settleBill
	 * @param user
	 * @param isOnline 
	 * @return
	 */
	ResultCode paySettle(SettlementBill settleBill, User user, String isOnline);

	/**
	 * @param placeId
	 * @return
	 */
	int countingPayBills(String placeId);

	/**
	 * @param placeId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<SettlementBill> queryPayBills(String placeId, Integer pageNo,
			Integer pageSize);

	/**
	 * @param settleIds
	 * @return
	 */
	Map<Long, StatusTracker> queryMerchantConfirm(List<Long> settleIds);

	/**
	 * @param settleIds
	 * @return
	 */
	Map<Long, StatusTracker> queryGewaPayBill(List<Long> settleIds);

	/**
	 * 
	 * @param placeId
	 * @param start
	 * @param end
	 * @param status
	 * @param isStatusNotEq
	 * @return
	 */
	/*int countingSettleBills(String placeId, Timestamp start, Timestamp end,
			String status, boolean isStatusNotEq,String billType,String reqMoneyStatus,String playType);*/

	/**
	 * 
	 * @param placeId
	 * @param start
	 * @param end
	 * @param status
	 * @param orderField
	 * @param pageNo
	 * @param pageSize
	 * @param isStatusNotEq
	 * @return
	 */
	/*List<SettlementBill> querySettleMents(String placeId, Timestamp start,
			Timestamp end, String status, String orderField, Integer pageNo, Integer pageSize,
			boolean isStatusNotEq,String billType,String reqMoneyStatus,String playType);*/

	/**
	 * @param bill
	 * @param ad
	 */
	void afterAdjust(SettlementBill bill, Adjustment ad);

	/**
	 * @param settleBill
	 * @param user
	 * @return
	 */
	ResultCode gewaReadjust(SettlementBill settleBill, User user);

	/**
	 * ensure this settlement bill could to pay
	 * @param settleBill
	 * @param user
	 * @return
	 */
	ResultCode confirmPay(SettlementBill settleBill, User user);

	/**
	 * @param bill
	 * @param aggres
	 */
	//void doFulFilling(SettlementBill bill, Collection<Map<String, Object>> aggres);

	/**
	 * @param bill
	 * @param isFake
	 * @return
	 */
	//Collection<Map<String, Object>> doCollection(SettlementBill bill, boolean isFake);

	/**
	 * @param settleBill
	 */
	void autoToMerchant(SettlementBill settleBill);

	/**
	 * @param settleBill
	 * @param logonUser
	 * @return 
	 */
	ResultCode toMerchant(SettlementBill settleBill, User logonUser);

	/**
	 * @param settleBill
	 * @return
	 */
	ResultCode reverseSettleBill(SettlementBill settleBill);

	/**
	 * 
	 */
	//void temprevert();
	
	public Map<String/**config.recordid>
	 * @param attachePath **/, Timestamp> getLastSettleBillsEndTimeByConfigs(String tag);

	ResultCode saveApply(Long recordId, Double applyNumber,Double applyAmount, String mname /*, String attachePath*/);
	
	//void addChannelIdtoPlaceIds(String placeId, List<String> placeIds);
}
