package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDPay extends BaseObject {

	private static final long serialVersionUID = 3965174862800497131L;
	
	//编号
	private Long recordId;
	//供应商编号
	private String vendorCode;
	//供应商名称
	private String vendorName;
	//充值前金额
	private Double payBefore;
	//充值金额
	private Double payAmount;
	//充值后金额
	private Double payAfter;
	//充值时间
	private Timestamp payTime;
	private Timestamp startTime;
	private Timestamp endTime;
	//操作人
	private String optUser;
	//支付方式
	private String payWay;
	//支付说明
	private String payContent;
	//请款单号
	private String reqMoneyId;

	public RDPay() {
	}
	


	public String getReqMoneyId() {
		return reqMoneyId;
	}



	public void setReqMoneyId(String reqMoneyId) {
		this.reqMoneyId = reqMoneyId;
	}



	public String getPayWay() {
		return payWay;
	}



	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
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



	public Double getPayBefore() {
		return payBefore;
	}



	public void setPayBefore(Double payBefore) {
		this.payBefore = payBefore;
	}



	public Double getPayAmount() {
		return payAmount;
	}



	public void setPayAmount(Double payAmount) {
		this.payAmount = payAmount;
	}



	public Double getPayAfter() {
		return payAfter;
	}



	public void setPayAfter(Double payAfter) {
		this.payAfter = payAfter;
	}



	public Timestamp getPayTime() {
		return payTime;
	}



	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	


	public String getOptUser() {
		return optUser;
	}



	public void setOptUser(String optUser) {
		this.optUser = optUser;
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



	public String getPayContent() {
		return payContent;
	}



	public void setPayContent(String payContent) {
		this.payContent = payContent;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
