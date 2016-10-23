package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaBaseData extends BaseObject {
	
	private static final long serialVersionUID = -5635234315166597052L;

	private Long recordid;
	private String code;
	private String name;
	private String type;
	private String special;
	private Timestamp updatetime;
	//gspÃÌº” ±º‰
	private Timestamp gspupdatetime;
	
	public DramaBaseData() {
	}
	
	public DramaBaseData(String code, String name, String type, Timestamp updatetime) {
		this.code = code;
		this.name = name;
		this.type = type;
		this.updatetime = updatetime;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}



	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}



	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public String getCode() {
		return code;
	}



	public void setCode(String code) {
		this.code = code;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



	public String getSpecial() {
		return special;
	}



	public void setSpecial(String special) {
		this.special = special;
	}



	public Timestamp getUpdatetime() {
		return updatetime;
	}



	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
