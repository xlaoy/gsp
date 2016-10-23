package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDDayCount extends BaseObject {

	private static final long serialVersionUID = -3666603327045683612L;

	//编号
	private Long recordId;
	//付款供应商编码
	private String vendorCode;
	//付款供应商名称
	private String vendorName;
	//开始时间
	private Timestamp startTime;
	//结束时间
	private Timestamp endTime;
	//总金额
	private Double amount;
	//总票数
	private Long num;
	
	public RDDayCount() {
	}
	
	
	
	public Long getNum() {
		return num;
	}



	public void setNum(Long num) {
		this.num = num;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Timestamp getStartTime() {
		return startTime;
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



	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}



	public Timestamp getEndTime() {
		return endTime;
	}



	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
