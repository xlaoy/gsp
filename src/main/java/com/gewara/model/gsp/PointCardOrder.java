package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PointCardOrder extends BaseObject {
	
	private static final long serialVersionUID = -5921069280093784180L;

	private Long recordid;
	//������id
	private Long partnerid;
	//�ⲿ������
	private String outtradeno;
	//�ڲ�������
	private String tradeno;
	//����״̬
	private String status;
	//����id
	private Long relatedid;
	//����
	private String cardno;
	//�ⲿ�����ܽ��
	private Double orderamount;
	//�ⲿ֧�����
	private Double outamount;
	//��֧�����
	private Double carduseamount;
	//�ⲿ�µ�ʱ��
	private Timestamp ordertime;
	//����ʱ��
	private Timestamp updatetime;
	//��ȯ����
	private String cardtype;
	//��������(��Ӱ�ݳ�����)
	private String ordertype;
	//ӰԺid
	private Long cinemaid;
	//ӰԺ����
	private String cinemaname;
	//��ӳʱ��
	private Timestamp playtime;
	//��Ʊ��
	private Integer ticketnum;
	//������Ϣ
	private String otherinfo;
	
	@Override
	public Serializable realId() {
		return recordid;
	}

	public PointCardOrder() {
	}

	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	public Long getPartnerid() {
		return partnerid;
	}

	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}

	public String getOuttradeno() {
		return outtradeno;
	}

	public void setOuttradeno(String outtradeno) {
		this.outtradeno = outtradeno;
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

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public Double getOrderamount() {
		return orderamount;
	}

	public void setOrderamount(Double orderamount) {
		this.orderamount = orderamount;
	}

	public Double getOutamount() {
		return outamount;
	}

	public void setOutamount(Double outamount) {
		this.outamount = outamount;
	}

	public Double getCarduseamount() {
		if(carduseamount == null) {
			return 0.0;
		}
		return carduseamount;
	}

	public void setCarduseamount(Double carduseamount) {
		this.carduseamount = carduseamount;
	}

	public Timestamp getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(Timestamp ordertime) {
		this.ordertime = ordertime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}

	public String getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public String getCinemaname() {
		return cinemaname;
	}

	public void setCinemaname(String cinemaname) {
		this.cinemaname = cinemaname;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}

	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}

	public Integer getTicketnum() {
		if(ticketnum == null) {
			return 0;
		}
		return ticketnum;
	}

	public void setTicketnum(Integer ticketnum) {
		this.ticketnum = ticketnum;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	
	
}
