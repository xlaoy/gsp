/** 
 */
package com.gewara.service;

import java.util.List;

import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.SettlementBill;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 21, 2013  1:19:12 PM
 */
public interface AdjustmentService {
	public ResultCode saveAdjust(String tag, String relateIds, Double adjustNumber, Double amount, String reason, String comments, String userName, String attachFilePath);

	/**
	 * @param adjustedTradeNo
	 * @param type
	 * @return
	 */
	public List<Adjustment> queryListByTradeNo(List<String> adjustedTradeNo,
			String type);

	/**
	 * @param reason
	 * @param operator 
	 * @return
	 */
	public int countingUnApprovedAdjustment(String placeId, String reason, String operator);

	/**
	 * @param reason
	 * @param operator 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Adjustment> queryUnApprovedAdjustment(String placeId, String reason,
			String operator, Integer pageNo, Integer pageSize);

	/**
	 * @param ck
	 * @param ad
	 * @return
	 */
	public ResultCode approvedAdjust(SettlementBill bill, Adjustment ad);

	/**
	 * @param placeId
	 * @param reason
	 * @param status
	 * @param gewaUser 
	 * @return
	 */
	public int countingAdjustment(String placeId, String reason, String status, User gewaUser);

	/**
	 * @param placeId
	 * @param reason
	 * @param status
	 * @param gewaUser 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Adjustment> queryAdjustment(String placeId, String reason,
			String status, User gewaUser, Integer pageNo, Integer pageSize);

	/**
	 * @param settleBill
	 * @return
	 */
	List<Adjustment> queryAdsBySettleBill(SettlementBill settleBill);
}
