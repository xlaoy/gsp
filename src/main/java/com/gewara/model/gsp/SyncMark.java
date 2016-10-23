package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class SyncMark extends BaseObject implements java.io.Serializable{
	private static final long serialVersionUID = -8894978220325066619L;
	private String tag;
	private Timestamp lastExecuteTime;
	private Timestamp modifyTime;
	public SyncMark(){}
	
	public SyncMark(String tag,Timestamp initTime){
		this.tag = tag;
		this.lastExecuteTime = initTime;
		this.modifyTime = DateUtil.getCurFullTimestamp();
	}

	@Override
	public Serializable realId() {
		return tag;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the lastExecuteTime
	 */
	public Timestamp getLastExecuteTime() {
		return lastExecuteTime;
	}

	/**
	 * @param lastExecuteTime the lastExecuteTime to set
	 */
	public void setLastExecuteTime(Timestamp lastExecuteTime) {
		this.lastExecuteTime = lastExecuteTime;
	}

	/**
	 * @return the modifyTime
	 */
	public Timestamp getModifyTime() {
		return modifyTime;
	}

	/**
	 * @param modifyTime the modifyTime to set
	 */
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}
}
