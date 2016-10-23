package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaDiscountItem extends BaseObject {
	
	private static final long serialVersionUID = -555458199396011183L;

	private Long recordid;
	//¹ØÁªid
	private String relatedid;
	//
	private String tag;
	//
	private String cardtype;
	//
	private String soldtype;
	//
	private Double amount;
	//
	private Timestamp gspupdatetime;
	
	public DramaDiscountItem() {
	}
	
	public DramaDiscountItem(Long recordid, String relatedid, String tag, String cardtype, String soldtype, Double amount) {
		this.recordid = recordid;
		this.relatedid = relatedid;
		this.tag = tag;
		this.cardtype = cardtype;
		this.soldtype = soldtype;
		this.amount = amount;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	public String getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(String relatedid) {
		this.relatedid = relatedid;
	}

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}

	public String getSoldtype() {
		return soldtype;
	}

	public void setSoldtype(String soldtype) {
		this.soldtype = soldtype;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}

	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}

	@Override
	public Serializable realId() {
		return recordid;
	}

}
