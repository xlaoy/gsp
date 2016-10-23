/** 
 */
package com.gewara.model.gsp;

import java.io.Serializable;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Dec 25, 2013  4:21:09 PM
 */
public class SettlementBillSnapshoot extends BaseObject{
	private static final long serialVersionUID = -2103270472883114554L;
	
	private long recordId;
	private Long settlementBillId;
	private Double orderTotalNumber;
	private Double orderTotalAmount;
	private String snapVersion;
	
	public SettlementBillSnapshoot(){
		this.snapVersion = DateUtil.format(DateUtil.currentTime(), "yyyyMMddHHmmss");
	}
	/**
	 * @return the recordId
	 */
	public long getRecordId() {
		return recordId;
	}
	
	/**
	 * @return the snapVersion
	 */
	public String getSnapVersion() {
		return snapVersion;
	}

	/**
	 * @param snapVersion the snapVersion to set
	 */
	public void setSnapVersion(String snapVersion) {
		this.snapVersion = snapVersion;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}
	/**
	 * @return the settlementBillId
	 */
	public Long getSettlementBillId() {
		return settlementBillId;
	}
	/**
	 * @param settlementBillId the settlementBillId to set
	 */
	public void setSettlementBillId(Long settlementBillId) {
		this.settlementBillId = settlementBillId;
	}
	/**
	 * @return the orderTotalNumber
	 */
	public Double getOrderTotalNumber() {
		return orderTotalNumber;
	}
	/**
	 * @param orderTotalNumber the orderTotalNumber to set
	 */
	public void setOrderTotalNumber(Double orderTotalNumber) {
		this.orderTotalNumber = orderTotalNumber;
	}
	/**
	 * @return the orderTotalAmount
	 */
	public Double getOrderTotalAmount() {
		return orderTotalAmount;
	}
	/**
	 * @param orderTotalAmount the orderTotalAmount to set
	 */
	public void setOrderTotalAmount(Double orderTotalAmount) {
		this.orderTotalAmount = orderTotalAmount;
	}
	/* (non-Javadoc)
	 * @see com.gewara.model.BaseObject#realId()
	 */
	@Override
	public Serializable realId() {
		return recordId;
	}
	
	 
}
