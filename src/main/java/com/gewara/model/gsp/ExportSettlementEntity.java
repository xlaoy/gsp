package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

public class ExportSettlementEntity implements Serializable {
	private static final long serialVersionUID = 6914517504933819065L;
	private Long recordId;
	private String cinemaName;
	private String vendorCode;
	private String vendorName;
	private Long settleNumber;
	private Double settleAmount;
	private Timestamp startTime;
	private Timestamp endTime;

	/**
	 * @return recordId
	 */
	public Long getRecordId() {
		return recordId;
	}

	/**
	 * @param set
	 *            recordId
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return cinemaName
	 */
	public String getCinemaName() {
		return cinemaName;
	}

	/**
	 * @param set
	 *            cinemaName
	 */
	public void setCinemaName(String cinemaName) {
		this.cinemaName = cinemaName;
	}

	/**
	 * @return vendorCode
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**
	 * @param set
	 *            vendorCode
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}

	/**
	 * @return vendorName
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**
	 * @param set
	 *            vendorName
	 */
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	/**
	 * @return settleNumber
	 */
	public Long getSettleNumber() {
		return settleNumber;
	}

	/**
	 * @param set
	 *            settleNumber
	 */
	public void setSettleNumber(Long settleNumber) {
		this.settleNumber = settleNumber;
	}

	
	
	public Double getSettleAmount() {
		return settleAmount;
	}

	public void setSettleAmount(Double settleAmount) {
		this.settleAmount = settleAmount;
	}

	/**  
	 * @return startTime  
	 */
	public Timestamp getStartTime() {
		return startTime;
	}

	/**  
	 * @param set startTime 
	 */
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	/**  
	 * @return endTime  
	 */
	public Timestamp getEndTime() {
		return endTime;
	}

	/**  
	 * @param set endTime 
	 */
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public ExportSettlementEntity(){}
	
	public ExportSettlementEntity(Long recordId, String cinemaName,
			String vendorCode, String vendorName, Long settleNumber,
			Double settleAmount,Timestamp startTime,Timestamp endTime) {
		super();
		this.recordId = recordId;
		this.cinemaName = cinemaName;
		this.vendorCode = vendorCode;
		this.vendorName = vendorName;
		this.settleNumber = settleNumber;
		this.settleAmount = settleAmount;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
