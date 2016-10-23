package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaJitiBill extends BaseObject {
	
	private static final long serialVersionUID = -2213024752371302386L;

	private Long recordid;
	//演出id
	private Long dramaid;
	//供应闪编号
	private String suppliercode;
	//开始时间
	private Timestamp starttime;
	//结束时间
	private Timestamp endtime;
	private String month;
	//票面金额
	private Double ticketamount;
	//退票金额
	private Double refundamount;
	//结算金额
	private Double settleamount;
	//商户券-兑换券优惠金额
	private Double madisamount;
	//商户券-补差券、低值券优惠金额
	private Double mbddisamount;
	//场馆补贴
	private Double butieamount;
	//应付金额
	private Double payamount;
	//毛利润
	private Double profitamount;
	//配置id
	private Long configid;
	//gsp更新
	private Timestamp gspupdatetime;
	
	
	public DramaJitiBill() {
	}

	public DramaJitiBill(Long dramaid, String suppliercode, Timestamp starttime,
						Timestamp endtime, Long configid) {
		this.dramaid = dramaid;
		this.suppliercode = suppliercode;
		this.starttime = starttime;
		this.endtime = endtime;
		this.ticketamount = 0.0;
		this.refundamount = 0.0;
		this.settleamount = 0.0;
		this.madisamount = 0.0;
		this.mbddisamount = 0.0;
		this.butieamount = 0.0;
		this.payamount = 0.0;
		this.profitamount = 0.0;
		this.configid = configid;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Double getButieamount() {
		return butieamount;
	}

	public void setButieamount(Double butieamount) {
		this.butieamount = butieamount;
	}

	public Long getConfigid() {
		return configid;
	}



	public void setConfigid(Long configid) {
		this.configid = configid;
	}



	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Double getProfitamount() {
		return profitamount;
	}

	public void setProfitamount(Double profitamount) {
		this.profitamount = profitamount;
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


	public String getSuppliercode() {
		return suppliercode;
	}



	public void setSuppliercode(String suppliercode) {
		this.suppliercode = suppliercode;
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



	public Double getTicketamount() {
		return ticketamount;
	}



	public void setTicketamount(Double ticketamount) {
		this.ticketamount = ticketamount;
	}



	public Double getRefundamount() {
		return refundamount;
	}



	public void setRefundamount(Double refundamount) {
		this.refundamount = refundamount;
	}



	public Double getSettleamount() {
		return settleamount;
	}



	public void setSettleamount(Double settleamount) {
		this.settleamount = settleamount;
	}

	public Double getMadisamount() {
		return madisamount;
	}

	public void setMadisamount(Double madisamount) {
		this.madisamount = madisamount;
	}

	public Double getMbddisamount() {
		return mbddisamount;
	}

	public void setMbddisamount(Double mbddisamount) {
		this.mbddisamount = mbddisamount;
	}

	public Double getPayamount() {
		return payamount;
	}



	public void setPayamount(Double payamount) {
		this.payamount = payamount;
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
