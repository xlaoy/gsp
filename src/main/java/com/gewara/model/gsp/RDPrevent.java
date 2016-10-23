package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDPrevent extends BaseObject {

	private static final long serialVersionUID = -1027312798169156539L;
	
	//付款供应商编码
	private String vendorCode;
	//付款供应商名称
	private String vendorName;
	//充值总额
	private Double totalAmount;
	//预付余额
	private Double canUseAmount;
	//未确认结算单金额
	private Double waitingPayAmount;
	//未确认结算单关联编号
	private String waitingPayRelation;
	//未统计订单金额
	private Double uncountAmount;
	//关联未统计订单金额日账单
	private String uncountRelation;
	//可用余额
	private Double surplusAmount;
	//预警金额
	private Double waringAmount;
	//状态
	private String status;
	//未统计订单开始时间
	private Timestamp startTime;
	//未统计订单开结束时间
	private Timestamp endTime;
	//截止统计时间
	private Timestamp updateTime;
	//
	private String vendorFullName;

	public RDPrevent() {
	}
	

	public String getVendorFullName() {
		return vendorFullName;
	}


	public void setVendorFullName(String vendorFullName) {
		this.vendorFullName = vendorFullName;
	}


	public Timestamp getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
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


	public String getVendorCode() {
		return vendorCode;
	}




	public Double getWaringAmount() {
		return waringAmount;
	}



	public void setWaringAmount(Double waringAmount) {
		this.waringAmount = waringAmount;
	}



	public void setSurplusAmount(Double surplusAmount) {
		this.surplusAmount = surplusAmount;
	}



	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}



	public Double getSurplusAmount() {
		return surplusAmount;
	}



	public String getVendorName() {
		return vendorName;
	}



	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}



	public Double getTotalAmount() {
		return totalAmount;
	}



	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}



	public Double getCanUseAmount() {
		return canUseAmount;
	}



	public void setCanUseAmount(Double canUseAmount) {
		this.canUseAmount = canUseAmount;
	}




	public Double getUncountAmount() {
		return uncountAmount;
	}



	public void setUncountAmount(Double uncountAmount) {
		this.uncountAmount = uncountAmount;
	}



	public String getUncountRelation() {
		return uncountRelation;
	}



	public void setUncountRelation(String uncountRelation) {
		this.uncountRelation = uncountRelation;
	}



	public Double getWaitingPayAmount() {
		return waitingPayAmount;
	}



	public void setWaitingPayAmount(Double waitingPayAmount) {
		this.waitingPayAmount = waitingPayAmount;
	}



	public String getWaitingPayRelation() {
		return waitingPayRelation;
	}



	public void setWaitingPayRelation(String waitingPayRelation) {
		this.waitingPayRelation = waitingPayRelation;
	}

	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	@Override
	public Serializable realId() {
		return vendorCode;
	}

}
