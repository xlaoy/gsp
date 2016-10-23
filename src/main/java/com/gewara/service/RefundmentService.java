/** 
 */
package com.gewara.service;

import java.util.List;

import com.gewara.enums.RefundmentType;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettlementBill;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 16, 2013  6:02:21 PM
 */
public interface RefundmentService {

	/**
	 * @param settleId
	 * @param Type
	 * @return
	 */
	List<Refundment> queryRefundmentBySettleBill(SettlementBill settlementBill, RefundmentType type);

	/**
	 * @param re
	 */
	void upgradeR(List<Refundment> re);

}
