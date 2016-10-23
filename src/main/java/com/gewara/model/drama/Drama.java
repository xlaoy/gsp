package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Drama extends BaseObject {

	private static final long serialVersionUID = 4134351256733423671L;
	
	private Long recordid;
	//�ݳ���Ŀ��
	private String dramaname;
	//��������
	private String pretype;
	//��Ŀ��֯
	private String origin;
	//���ʱ��
	private Timestamp addtime;
	//gsp���ʱ��
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
