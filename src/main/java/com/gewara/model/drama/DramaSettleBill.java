package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.DramaConst;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaSettleBill extends BaseObject {

	private static final long serialVersionUID = -3938434911531058787L;
	
	private Long recordid;
	//演出id
	private Long dramaid;
	//供应闪编号
	private String suppliercode;
	//开始时间
	private Timestamp starttime;
	//结束时间
	private Timestamp endtime;
	//出票数量
	private Long ticketnum;
	//票面金额
	private Double ticketamount;
	//退票数量
	private Long refundnum;
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
	//系统计算应付金额
	private Double syspayamount;
	//实际应付金额
	private Double payamount;
	//调整票数
	private Long adjustnum;
	//调整金额
	private Double adjustamount;
	//调整说明
	private String adjustdesc;
	//状态
	private String status;
	//是否请款
	private String reqmoney;
	//配置id
	private Long configid;
	//是否是最后一期结算单
	private String lastbill;
	//gsp更新
	private Timestamp gspupdatetime;
	
	public DramaSettleBill() {
	}
	
	public DramaSettleBill(Long dramaid, String suppliercode, Timestamp starttime,
			Timestamp endtime, Long configid, String lastbill) {
		this.dramaid = dramaid;
		this.suppliercode = suppliercode;
		this.starttime = starttime;
		this.endtime = endtime;
		this.ticketnum = 0l;
		this.ticketamount = 0.0;
		this.refundnum = 0l;
		this.refundamount = 0.0;
		this.settleamount = 0.0;
		this.madisamount = 0.0;
		this.mbddisamount = 0.0;
		this.butieamount = 0.0;
		this.syspayamount = 0.0;
		this.payamount = 0.0;
		this.adjustnum = 0l;
		this.adjustamount = 0.0;
		this.status = DramaConst.NEW;
		this.configid = configid;
		this.reqmoney = DramaConst.N;
		this.lastbill = lastbill;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	

	public Double getButieamount() {
		return butieamount;
	}

	public void setButieamount(Double butieamount) {
		this.butieamount = butieamount;
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

	public Double getSyspayamount() {
		return syspayamount;
	}

	public void setSyspayamount(Double syspayamount) {
		this.syspayamount = syspayamount;
	}

	public String getAdjustdesc() {
		return adjustdesc;
	}

	public void setAdjustdesc(String adjustdesc) {
		this.adjustdesc = adjustdesc;
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



	public String getLastbill() {
		return lastbill;
	}

	public void setLastbill(String lastbill) {
		this.lastbill = lastbill;
	}

	public Long getAdjustnum() {
		return adjustnum;
	}

	public void setAdjustnum(Long adjustnum) {
		this.adjustnum = adjustnum;
	}

	public Double getAdjustamount() {
		return adjustamount;
	}

	public void setAdjustamount(Double adjustamount) {
		this.adjustamount = adjustamount;
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



	public Long getTicketnum() {
		return ticketnum;
	}



	public void setTicketnum(Long ticketnum) {
		this.ticketnum = ticketnum;
	}



	public Double getTicketamount() {
		return ticketamount;
	}



	public void setTicketamount(Double ticketamount) {
		this.ticketamount = ticketamount;
	}



	public Long getRefundnum() {
		return refundnum;
	}



	public void setRefundnum(Long refundnum) {
		this.refundnum = refundnum;
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


	public Double getPayamount() {
		return payamount;
	}



	public void setPayamount(Double payamount) {
		this.payamount = payamount;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}

	

	public String getReqmoney() {
		return reqmoney;
	}

	public void setReqmoney(String reqmoney) {
		this.reqmoney = reqmoney;
	}

	public Long getConfigid() {
		return configid;
	}



	public void setConfigid(Long configid) {
		this.configid = configid;
	}

	@Override
	public Serializable realId() {
		return recordid;
	}

}
