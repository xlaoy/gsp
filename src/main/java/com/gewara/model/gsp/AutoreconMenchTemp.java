package com.gewara.model.gsp;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class AutoreconMenchTemp extends BaseObject {

	private static final long serialVersionUID = -4898443702961853449L;

	private Long recordid;
	private Long settleid;
	private String tradeno;
	private String recencode;
	private Double num;
	private Double price;
	private Double amount;
	
	public AutoreconMenchTemp() {
	}
	
	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Long getSettleid() {
		return settleid;
	}



	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}



	public String getTradeno() {
		return tradeno;
	}



	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}



	public String getRecencode() {
		return recencode;
	}



	public void setRecencode(String recencode) {
		this.recencode = recencode;
	}

	public Double getNum() {
		return num;
	}

	public void setNum(Double num) {
		this.num = num;
	}

	public Double getPrice() {
		return price;
	}



	public void setPrice(Double price) {
		this.price = price;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
