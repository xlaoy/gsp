package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaOrderOffline extends BaseObject {

	private static final long serialVersionUID = 3649485256575542915L;
	
	//订单号（三种订单会有订单号重复，因此分开存储）
	private String tradeno;
	//订单状态
	private String status;
	//演出id
	private Long dramaid;
	//付款时间
	private Timestamp paidtime;
	//添加时间
	private Timestamp ordertime;
	//付款方式
	private String paymethod;
	//用户姓名
	private String username;
	//更新时间
	private Timestamp updatetime;
	//其他费用
	private Double otherfee;
	//gsp添加时间
	private Timestamp gspupdatetime;
	
	public DramaOrderOffline() {
	}

	public DramaOrderOffline(String tradeno, String status, Long dramaid, String username, Timestamp paidtime, 
			Timestamp ordertime, Timestamp updatetime, Double otherfee, String paymethod) {
		this.tradeno = tradeno;
		this.status = status;
		this.dramaid = dramaid;
		this.ordertime = ordertime;
		this.updatetime = updatetime;
		this.otherfee = otherfee;
		this.paymethod = paymethod;
		this.username = username;
		this.paidtime = paidtime;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	
	
	
	public Timestamp getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(Timestamp ordertime) {
		this.ordertime = ordertime;
	}

	public Timestamp getPaidtime() {
		return paidtime;
	}

	public void setPaidtime(Timestamp paidtime) {
		this.paidtime = paidtime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}



	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}



	public String getPaymethod() {
		return paymethod;
	}

	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
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
