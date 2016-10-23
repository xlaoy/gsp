package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PointCardPlaceBill extends BaseObject {

	private static final long serialVersionUID = -6016935384474789688L;

	private Long recordid;
	//影院id
	private Long cinemaid;
	//影院名称
	private String cinemaname;
	//统计类型
	private String ctype;
	//开始时间
	private Timestamp starttime;
	//结束时间
	private Timestamp endtime;
	//出票数量
	private Integer successnum;
	//出票点卡金额
	private Double successamount;
	//退票数量
	private Integer refundnum;
	//退票点卡金额
	private Double refundamount;
	//结算票数
	private Integer totalnum;
	//结算点卡金额
	private Double totalamount;
	//开票金额
	private Double kpamount;
	//关联单号（日账单关联月账单id）
	private Long relatedbillid;
	//关联商家
	private Long partnerid;
	
	public PointCardPlaceBill() {
	}
	
	public PointCardPlaceBill(Long partnerid, Long cinemaid, String cinemaname, String ctype, Timestamp starttime, 
			Timestamp endtime, Long relatedbillid) {
		this.partnerid = partnerid;
		this.cinemaid = cinemaid;
		this.cinemaname = cinemaname;
		this.ctype = ctype;
		this.starttime = starttime;
		this.endtime = endtime;
		this.relatedbillid = relatedbillid;
		this.successnum = 0;
		this.successamount = 0.0;
		this.refundnum = 0;
		this.refundamount = 0.0;
		this.totalnum = 0;
		this.totalamount = 0.0;
		this.kpamount = 0.0;
	}
	
	public Long getPartnerid() {
		return partnerid;
	}


	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}


	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
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


	public String getCtype() {
		return ctype;
	}



	public void setCtype(String ctype) {
		this.ctype = ctype;
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



	public Integer getSuccessnum() {
		return successnum;
	}



	public void setSuccessnum(Integer successnum) {
		this.successnum = successnum;
	}



	public Double getSuccessamount() {
		return successamount;
	}



	public void setSuccessamount(Double successamount) {
		this.successamount = successamount;
	}



	public Integer getRefundnum() {
		return refundnum;
	}



	public void setRefundnum(Integer refundnum) {
		this.refundnum = refundnum;
	}



	public Double getRefundamount() {
		return refundamount;
	}



	public void setRefundamount(Double refundamount) {
		this.refundamount = refundamount;
	}



	public Integer getTotalnum() {
		return totalnum;
	}



	public void setTotalnum(Integer totalnum) {
		this.totalnum = totalnum;
	}



	public Double getTotalamount() {
		return totalamount;
	}



	public void setTotalamount(Double totalamount) {
		this.totalamount = totalamount;
	}



	public Double getKpamount() {
		return kpamount;
	}



	public void setKpamount(Double kpamount) {
		this.kpamount = kpamount;
	}



	public Long getRelatedbillid() {
		return relatedbillid;
	}



	public void setRelatedbillid(Long relatedbillid) {
		this.relatedbillid = relatedbillid;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
