package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class SysData extends BaseObject {

	private static final long serialVersionUID = 3320865276620910392L;

	private Long recordid;
	
	private String type;
	
	private String code;
	
	private String name;
	
	private String special;
	
	private Timestamp updatetime;
	
	public SysData() {
	}
	
	public SysData(String type, String code, String name, String special) {
		this.type = type;
		this.code = code;
		this.name = name;
		this.special = special;
		this.updatetime = DateUtil.getCurFullTimestamp();
		
	}
	
	
	
	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
