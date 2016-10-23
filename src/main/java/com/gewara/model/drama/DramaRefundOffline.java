package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaRefundOffline extends BaseObject {

	private static final long serialVersionUID = -4292915882334222037L;
	
	//订单号
	private String tradeno;
	//演出id
	private Long dramaid;
	//下单时间
	private Timestamp ordertime;
	//退单时间
	private Timestamp refundtime;
	//更新时间
	private Timestamp gspupdatetime;

	public DramaRefundOffline() {
	}

	public DramaRefundOffline(String tradeno, Long dramaid, Timestamp ordertime, Timestamp refundtime) {
		this.tradeno = tradeno;
		this.dramaid = dramaid;
		this.ordertime = ordertime;
		this.refundtime = refundtime;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
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
