package com.gewara.model.gsp;
// default package
// Generated Jul 31, 2013 2:18:24 PM by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.BaseObject;
import com.gewara.util.RecordIdUtils;

/**
 * CheckBillId generated by hbm2java
 */
public class CheckBill extends BaseObject implements java.io.Serializable,Comparable {

	private static final long serialVersionUID = -5181000899929850212L;
	private Long recordId;
	private String configId;       //tag + "," + tag
	private Timestamp start;
	private Timestamp end;
	private String tag;
	private Long relateId;         //����ID
	private String status;
	private Double orderTotalNumber;
	private Double orderTotalAmount;
	private Double refundTotalNumber;
	private Double refundTotalAmount;
	private Double unrefundTotalNumber;
	private Double unrefundTotalAmount;
	private Double adjustTotalNumber;
	private Double adjustTotalAmount;
	private String adjustReason;
	private Long settlementId;
	//�����Ʊ��
	private Double diffPriceNumber;
	//����ܽ��
	private Double diffPriceAmount;
	//������Ʊ��
	private Double succTicketNumber;
	//�����ܽ��
	private Double succTicketAmount;
	//��������ʾ
	private String special;
	//��������
	private String playType;
	private Double billingamount;
	private Double placeallowance;
	
	public CheckBill() {
	}
	public CheckBill(Timestamp start, Timestamp end, String tag, Long relateId){
		this.configId = RecordIdUtils.contactRecordId(tag, relateId);
		this.start = start;
		this.end = end;
		this.tag = tag;
		this.relateId = relateId;
		this.status = CheckBillStatusEnums.NEW.getStatus();
	}
	
	/******************zyj******************/
	public CheckBill(Timestamp start, Timestamp end, String tag, Long relateId, String configId){
		this.configId = configId;
		this.start = start;
		this.end = end;
		this.tag = tag;
		this.relateId = relateId;
		this.status = CheckBillStatusEnums.NEW.getStatus();
		this.orderTotalNumber = 0.0;
		this.orderTotalAmount = 0.0;
		this.refundTotalNumber = 0.0;
		this.refundTotalAmount = 0.0;
		this.unrefundTotalNumber = 0.0;
		this.unrefundTotalAmount = 0.0;
		this.adjustTotalNumber = 0.0;
		this.adjustTotalAmount = 0.0;
		this.adjustReason = "";
		this.settlementId = null;
		this.diffPriceNumber = 0.0;
		this.diffPriceAmount = 0.0;
		this.succTicketAmount = 0.0;
		this.succTicketNumber = 0.0;
		this.billingamount = 0.0;
		this.placeallowance = 0d;
	}
	/******************zyj******************/
	
	public void initAggreData(){
		this.status = CheckBillStatusEnums.NEW.getStatus();
		orderTotalNumber = 0.0;
		orderTotalAmount = 0.0;
		refundTotalNumber = 0.0;
		refundTotalAmount = 0.0;
		unrefundTotalNumber = 0.0;
		unrefundTotalAmount = 0.0;
		adjustTotalNumber = 0.0;
		adjustTotalAmount = 0.0;
		adjustReason = "";
		settlementId = null;
		this.diffPriceNumber = 0.0;
		this.diffPriceAmount = 0.0;
		this.succTicketAmount = 0.0;
		this.succTicketNumber = 0.0;
		this.billingamount = 0.0;
		this.placeallowance = 0d;
	}
	
	public void initAggreDataExt(){
		this.status = CheckBillStatusEnums.NEW.getStatus();
		orderTotalNumber = 0.0;
		orderTotalAmount = 0.0;
		refundTotalNumber = 0.0;
		refundTotalAmount = 0.0;
		unrefundTotalNumber = 0.0;
		unrefundTotalAmount = 0.0;
		adjustTotalNumber = 0.0;
		adjustTotalAmount = 0.0;
		adjustReason = "";
		this.diffPriceNumber = 0.0;
		this.diffPriceAmount = 0.0;
		this.succTicketAmount = 0.0;
		this.succTicketNumber = 0.0;
		this.billingamount = 0.0;
		this.placeallowance = 0d;
	}
	
	public Double getPlaceallowance() {
		return placeallowance;
	}
	public void setPlaceallowance(Double placeallowance) {
		this.placeallowance = placeallowance;
	}
	public Double getBillingamount() {
		return billingamount;
	}
	public void setBillingamount(Double billingamount) {
		this.billingamount = billingamount;
	}
	public String getSpecial() {
		return special;
	}
	public void setSpecial(String special) {
		this.special = special;
	}
	/**
	 * @return the recordId
	 */
	public Long getRecordId() {
		return recordId;
	}
	
	/**
	 * @return the adjustReason
	 */
	public String getAdjustReason() {
		return adjustReason;
	}
	/**
	 * @param adjustReason the adjustReason to set
	 */
	public void setAdjustReason(String adjustReason) {
		this.adjustReason = adjustReason;
	}
	/**
	 * @return the unrefundTotalNumber
	 */
	public Double getUnrefundTotalNumber() {
		return unrefundTotalNumber;
	}
	/**
	 * @param unrefundTotalNumber the unrefundTotalNumber to set
	 */
	public void setUnrefundTotalNumber(Double unrefundTotalNumber) {
		this.unrefundTotalNumber = unrefundTotalNumber;
	}
	/**
	 * @return the unrefundTotalAmount
	 */
	public Double getUnrefundTotalAmount() {
		return unrefundTotalAmount;
	}
	/**
	 * @param unrefundTotalAmount the unrefundTotalAmount to set
	 */
	public void setUnrefundTotalAmount(Double unrefundTotalAmount) {
		this.unrefundTotalAmount = unrefundTotalAmount;
	}
	/**
	 * @return the settlementId
	 */
	public Long getSettlementId() {
		return settlementId;
	}
	/**
	 * @param settlementId the settlementId to set
	 */
	public void setSettlementId(Long settlementId) {
		this.settlementId = settlementId;
	}
	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
	/**
	 * @return the configId
	 */
	public String getConfigId() {
		return configId;
	}
	/**
	 * @param configId the configId to set
	 */
	public void setConfigId(String configId) {
		this.configId = configId;
	}
	/**
	 * @return the start
	 */
	public Timestamp getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Timestamp start) {
		this.start = start;
	}
	/**
	 * @return the end
	 */
	public Timestamp getEnd() {
		return end;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(Timestamp end) {
		this.end = end;
	}
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the relateId
	 */
	public Long getRelateId() {
		return relateId;
	}
	/**
	 * @param relateId the relateId to set
	 */
	public void setRelateId(Long relateId) {
		this.relateId = relateId;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
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
	/**
	 * @return the refundTotalNumber
	 */
	public Double getRefundTotalNumber() {
		return refundTotalNumber;
	}
	/**
	 * @param refundTotalNumber the refundTotalNumber to set
	 */
	public void setRefundTotalNumber(Double refundTotalNumber) {
		this.refundTotalNumber = refundTotalNumber;
	}
	/**
	 * @return the refundTotalAmount
	 */
	public Double getRefundTotalAmount() {
		return refundTotalAmount;
	}
	/**
	 * @param refundTotalAmount the refundTotalAmount to set
	 */
	public void setRefundTotalAmount(Double refundTotalAmount) {
		this.refundTotalAmount = refundTotalAmount;
	}
	/**
	 * @return the adjustTotalNumber
	 */
	public Double getAdjustTotalNumber() {
		return adjustTotalNumber;
	}
	/**
	 * @param adjustTotalNumber the adjustTotalNumber to set
	 */
	public void setAdjustTotalNumber(Double adjustTotalNumber) {
		this.adjustTotalNumber = adjustTotalNumber;
	}
	/**
	 * @return the adjustTotalAmount
	 */
	public Double getAdjustTotalAmount() {
		return adjustTotalAmount;
	}
	/**
	 * @param adjustTotalAmount the adjustTotalAmount to set
	 */
	public void setAdjustTotalAmount(Double adjustTotalAmount) {
		this.adjustTotalAmount = adjustTotalAmount;
	}
	/* (non-Javadoc)
	 * @see com.gewara.model.BaseObject#realId()
	 */
	@Override
	public Serializable realId() {
		return recordId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		CheckBill newcb = (CheckBill)o;
		if (newcb.getRecordId().intValue() > this.recordId.intValue())
			return 1;
		else if (newcb.getRecordId().intValue() < this.recordId.intValue())
			return -1;
		return 0;
	}
	
	public Double getDiffPriceNumber() {
		return diffPriceNumber;
	}
	public void setDiffPriceNumber(Double diffPriceNumber) {
		this.diffPriceNumber = diffPriceNumber;
	}
	public Double getDiffPriceAmount() {
		return diffPriceAmount;
	}
	public void setDiffPriceAmount(Double diffPriceAmount) {
		this.diffPriceAmount = diffPriceAmount;
	}
	public Double getSuccTicketNumber() {
		return succTicketNumber;
	}
	public void setSuccTicketNumber(Double succTicketNumber) {
		this.succTicketNumber = succTicketNumber;
	}
	public Double getSuccTicketAmount() {
		return succTicketAmount;
	}
	public void setSuccTicketAmount(Double succTicketAmount) {
		this.succTicketAmount = succTicketAmount;
	}
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	
	
}