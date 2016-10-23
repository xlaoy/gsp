package com.gewara.model.gsp;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class SettlementBillExtend extends BaseObject {

	private static final long serialVersionUID = -808189963648554019L;

	private Long recordid;
	//格瓦拉开票金额
	private Double billingamount;
	//包场预付费
	private Double bcPrePay;
	//包场补差
	private Double bcBucha;
	//特价折扣
	private Double placeallowance;
	
	public SettlementBillExtend() {
	}
	
	public void initData() {
		this.billingamount = 0d;
		this.bcPrePay = 0d;
		this.bcBucha = 0d;
		this.placeallowance = 0d;
	}
	
	public SettlementBillExtend(Long recordid) {
		this.recordid = recordid;
		this.billingamount = 0d;
		this.bcPrePay = 0d;
		this.bcBucha = 0d;
		this.placeallowance = 0d;
	}
	
	
	
	public Double getPlaceallowance() {
		return placeallowance;
	}

	public void setPlaceallowance(Double placeallowance) {
		this.placeallowance = placeallowance;
	}

	public Double getBcPrePay() {
		return bcPrePay;
	}



	public void setBcPrePay(Double bcPrePay) {
		this.bcPrePay = bcPrePay;
	}



	public Double getBcBucha() {
		return bcBucha;
	}



	public void setBcBucha(Double bcBucha) {
		this.bcBucha = bcBucha;
	}



	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Double getBillingamount() {
		return billingamount;
	}



	public void setBillingamount(Double billingamount) {
		this.billingamount = billingamount;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
