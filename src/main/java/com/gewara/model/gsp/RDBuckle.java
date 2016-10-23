package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDBuckle extends BaseObject {

	private static final long serialVersionUID = 3053212915315795030L;

	//编号
	private Long recordId;
	//关联编号结算单
	private Long relateId;
	//影院id
	private String placeId;
	//影院名称
	private String placeName;
	//标示
	private String special;
	//结算周期
	private String cycle;
	//商户确认时间
	private Timestamp confirmTime;
	//扣款前系统金额
	private Double buckleBefore;
	//扣款金额
	private Double buckleAmount;
	//扣款后系统金额
	private Double buckleAfter;
	//扣款时间
	private Timestamp startTime;
	private Timestamp endTime;
	private Timestamp buckleTime;
	//付款供应商编码
	private String vendorCode;
	//付款供应商名称
	private String vendorName;

	public RDBuckle() {
	}



	public String getVendorCode() {
		return vendorCode;
	}



	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}



	public String getVendorName() {
		return vendorName;
	}



	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}



	public Long getRelateId() {
		return relateId;
	}



	public void setRelateId(Long relateId) {
		this.relateId = relateId;
	}



	public String getPlaceId() {
		return placeId;
	}



	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}



	public String getPlaceName() {
		return placeName;
	}



	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}



	public String getSpecial() {
		return special;
	}



	public void setSpecial(String special) {
		this.special = special;
	}



	public String getCycle() {
		return cycle;
	}



	public void setCycle(String cycle) {
		this.cycle = cycle;
	}



	public Timestamp getConfirmTime() {
		return confirmTime;
	}



	public void setConfirmTime(Timestamp confirmTime) {
		this.confirmTime = confirmTime;
	}



	public Double getBuckleBefore() {
		return buckleBefore;
	}



	public void setBuckleBefore(Double buckleBefore) {
		this.buckleBefore = buckleBefore;
	}



	public Double getBuckleAmount() {
		return buckleAmount;
	}



	public void setBuckleAmount(Double buckleAmount) {
		this.buckleAmount = buckleAmount;
	}



	public Double getBuckleAfter() {
		return buckleAfter;
	}



	public void setBuckleAfter(Double buckleAfter) {
		this.buckleAfter = buckleAfter;
	}



	public Timestamp getBuckleTime() {
		return buckleTime;
	}



	public void setBuckleTime(Timestamp buckleTime) {
		this.buckleTime = buckleTime;
	}



	public Timestamp getStartTime() {
		return startTime;
	}



	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}



	public Timestamp getEndTime() {
		return endTime;
	}



	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
