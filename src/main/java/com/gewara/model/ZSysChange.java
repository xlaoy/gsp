package com.gewara.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class ZSysChange extends BaseObject {
	
	private static final long serialVersionUID = 312820159259989593L;
	
	private Long recordid;
	private String placeid;
	private Timestamp starttime;
	private Timestamp endtime;
	private String category;
	private Long num;
	private Double amount;
	private Timestamp updatetime;
	
	public ZSysChange() {
	}
	
	
	public Timestamp getUpdatetime() {
		return updatetime;
	}


	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}


	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public String getPlaceid() {
		return placeid;
	}



	public void setPlaceid(String placeid) {
		this.placeid = placeid;
	}



	public Timestamp getStarttime() {
		return starttime;
	}



	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}



	public Timestamp getEndtime() {
		return endtime;
	}



	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}



	public String getCategory() {
		return category;
	}



	public void setCategory(String category) {
		this.category = category;
	}



	public Long getNum() {
		return num;
	}



	public void setNum(Long num) {
		this.num = num;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}

	@Override
	public Serializable realId() {
		return null;
	}

}
