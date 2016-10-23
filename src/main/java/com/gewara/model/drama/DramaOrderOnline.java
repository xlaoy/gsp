package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaOrderOnline extends BaseObject {

	private static final long serialVersionUID = 3649485256575542915L;
	
	//�����ţ����ֶ������ж������ظ�����˷ֿ��洢��
	private String tradeno;
	//����״̬
	private String status;
	//�ݳ�id
	private Long dramaid;
	//����ʱ��
	private Timestamp paidtime;
	//�û�id
	private Long memberid;
	//���ʱ��
	private Timestamp ordertime;
	//����ʱ��
	private Timestamp updatetime;
	//��������
	private Double otherfee;
	//�Ż�����
	private String disreason;
	//gsp���ʱ��
	private Timestamp gspupdatetime;
	
	public DramaOrderOnline() {
	}

	public DramaOrderOnline(String tradeno, String status, Long dramaid, Timestamp paidtime, Long memberid,
					  Timestamp ordertime, Timestamp updatetime, Double otherfee,  String disreason) {
		this.tradeno = tradeno;
		this.status = status;
		this.dramaid = dramaid;
		this.paidtime = paidtime;
		this.memberid = memberid;
		this.ordertime = ordertime;
		this.updatetime = updatetime;
		this.otherfee = otherfee;
		this.disreason = disreason;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}

	public Timestamp getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(Timestamp ordertime) {
		this.ordertime = ordertime;
	}

	public String getDisreason() {
		return disreason;
	}

	public void setDisreason(String disreason) {
		this.disreason = disreason;
	}

	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}



	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}



	public String getTradeno() {
		return tradeno;
	}



	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Long getDramaid() {
		return dramaid;
	}



	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}



	public Timestamp getPaidtime() {
		return paidtime;
	}



	public void setPaidtime(Timestamp paidtime) {
		this.paidtime = paidtime;
	}



	public Long getMemberid() {
		return memberid;
	}



	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	

	public Timestamp getUpdatetime() {
		return updatetime;
	}



	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Double getOtherfee() {
		return otherfee;
	}



	public void setOtherfee(Double otherfee) {
		this.otherfee = otherfee;
	}



	@Override
	public Serializable realId() {
		return tradeno;
	}

}
