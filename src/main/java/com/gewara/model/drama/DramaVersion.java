package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class DramaVersion extends BaseObject {
	
	private static final long serialVersionUID = 934460335971201277L;
	
	private Long recordid;
	//项目id
	private Long dramaid;
	//演出开始时间
	private Timestamp starttime;
	//演出结束时间
	private Timestamp endtime;
	//跟新时间
	private Timestamp updatetime;
	//结算更新时间
	private Timestamp gspupdatetime;

	public DramaVersion() {
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



	public Long getDramaid() {
		return dramaid;
	}



	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
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
