package com.gewara.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class GspJob extends BaseObject {
	private static final long serialVersionUID = -2424567445154720143L;
	
	private String tag;
	private String status;
	private Timestamp updateTime;

	
	
	public String getTag() {
		return tag;
	}



	public void setTag(String tag) {
		this.tag = tag;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Timestamp getUpdateTime() {
		return updateTime;
	}



	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}



	@Override
	public Serializable realId() {
		return this.tag;
	}

}
