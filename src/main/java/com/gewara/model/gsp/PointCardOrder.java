package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PointCardOrder extends BaseObject {
	
	private static final long serialVersionUID = -5921069280093784180L;

	private Long recordid;
	//合作商id
	private Long partnerid;
	//外部订单号
	private String outtradeno;
	//内部订单号
	private String tradeno;
	//订单状态
	private String status;
	//关联id
	private Long relatedid;
	//卡号
	private String cardno;
	//外部订单总金额
	private Double orderamount;
	//外部支付金额
	private Double outamount;
	//卡支付金额
	private Double carduseamount;
	//外部下单时间
	private Timestamp ordertime;
	//更新时间
	private Timestamp updatetime;
	//卡券类型
	private String cardtype;
	//订单类型(电影演出赛事)
	private String ordertype;
	//影院id
	private Long cinemaid;
	//影院名称
	private String cinemaname;
	//放映时间
	private Timestamp playtime;
	//订票数
	private Integer ticketnum;
	//其他信息
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
