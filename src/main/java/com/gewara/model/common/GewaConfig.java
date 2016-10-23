package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class GewaConfig extends BaseObject{
	private static final long serialVersionUID = 4476980910614491968L;
	private String ckey;
	private String content;			//内容
	private String description;		//描述
	private Timestamp updatetime;	//更新时间
	@Override
	public Serializable realId() {
		return ckey;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getCkey() {
		return ckey;
	}
	public void setCkey(String ckey) {
		this.ckey = ckey;
	}
}
