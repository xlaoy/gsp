package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaRefundOnline extends BaseObject {

	private static final long serialVersionUID = -4292915882334222037L;
	
	//订单号
	private String tradeno;
	//演出id
	private Long dramaid;
	//下单时间
	private Timestamp ordertime;
	//退单时间
	private Timestamp refundtime;
	//退票类型
	private String refundtype;
	//是否结算
	private String issettle;
	//更新时间
	private Timestamp gspupdatetime;

	public DramaRefundOnline() {
	}

	public DramaRefundOnline(String tradeno, Long dramaid, Timestamp ordertime, Timestamp refundtime, String refundtype, String issettle) {
		this.tradeno = tradeno;
		this.dramaid = dramaid;
		this.ordertime = ordertime;
		this.refundtime = refundtime;
		this.refundtype = refundtype;
		this.issettle = issettle;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public String getIssettle() {
		return issettle;
	}

	public void setIssettle(String issettle) {
		this.issettle = issettle;
	}

	public String getRefundtype() {
		return refundtype;
	}

	public void setRefundtype(String refundtype) {
		this.refundtype = refundtype;
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



	public Long getDramaid() {
		return dramaid;
	}



	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}


	public Timestamp getOrdertime() {
		return ordertime;
	}



	public void setOrdertime(Timestamp ordertime) {
		this.ordertime = ordertime;
	}



	public Timestamp getRefundtime() {
		return refundtime;
	}



	public void setRefundtime(Timestamp refundtime) {
		this.refundtime = refundtime;
	}

	@Override
	public Serializable realId() {
		return tradeno;
	}

}
