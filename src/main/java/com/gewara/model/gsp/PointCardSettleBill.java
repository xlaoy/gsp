package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PointCardSettleBill extends BaseObject {

	private static final long serialVersionUID = -6016935384474789688L;

	private Long recordid;
	//关联商家
	private Long partnerid;
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
	
	public PointCardSettleBill() {
	}
	
	public PointCardSettleBill(Long partnerid, String ctype, Timestamp starttime, Timestamp endtime, Long relatedbillid) {
		this.partnerid = partnerid;
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
