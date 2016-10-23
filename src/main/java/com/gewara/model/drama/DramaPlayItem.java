package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaPlayItem extends BaseObject {

	private static final long serialVersionUID = -3830260023991714509L;

	private Long recordid;
	//dpid
	private Long dpid;
	//演出id
	private Long dramaid;
	//演出时间
	private Timestamp playtime;
	
	private Timestamp gspupdatetime;
	
	public DramaPlayItem() {
	}
	
	public DramaPlayItem(Long recordid, Long dpid, Long dramaid, Timestamp playtime) {
		this.recordid = recordid;
		this.dpid = dpid;
		this.dramaid = dramaid;
		this.playtime = playtime;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}

	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}

	public Long getDpid() {
		return dpid;
	}



	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}



	public Long getDramaid() {
		return dramaid;
	}



	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}



	public Timestamp getPlaytime() {
		return playtime;
	}



	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
