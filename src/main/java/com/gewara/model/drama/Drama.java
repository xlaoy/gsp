package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Drama extends BaseObject {

	private static final long serialVersionUID = 4134351256733423671L;
	
	private Long recordid;
	//演出项目名
	private String dramaname;
	//代售类型
	private String pretype;
	//项目组织
	private String origin;
	//添加时间
	private Timestamp addtime;
	//gsp添加时间
	private Timestamp gspupdatetime;

	public Drama() {
	}
	
	
	
	public String getOrigin() {
		return origin;
	}



	public void setOrigin(String origin) {
		this.origin = origin;
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



	public String getDramaname() {
		return dramaname;
	}



	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}



	public String getPretype() {
		return pretype;
	}



	public void setPretype(String pretype) {
		this.pretype = pretype;
	}



	public Timestamp getAddtime() {
		return addtime;
	}



	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	@Override
	public Serializable realId() {
		return recordid;
	}

}
