package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class DiffPriceBill extends BaseObject implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1498595446432783043L;

	
	//影票日账单单号
	private Long checkBillId;
	//影票结算单单号
	private Long settleBillId;
	//对账周期开始时间
	private Timestamp start;
	//对账周期结束时间
	private Timestamp end;
	//异价票数量
	private Long diffOrderNum;
	//异价票退票数量
	private Long diffRefundNum;
	//差额
	private Double diffAmount;
	//备注
	private String desc;
	
	
	
	
	public Long getCheckBillId() {
		return checkBillId;
	}




	public void setCheckBillId(Long checkBillId) {
		this.checkBillId = checkBillId;
	}




	public Long getSettleBillId() {
		return settleBillId;
	}




	public void setSettleBillId(Long settleBillId) {
		this.settleBillId = settleBillId;
	}




	public Timestamp getStart() {
		return start;
	}




	public void setStart(Timestamp start) {
		this.start = start;
	}




	public Timestamp getEnd() {
		return end;
	}




	public void setEnd(Timestamp end) {
		this.end = end;
	}




	public Long getDiffOrderNum() {
		return diffOrderNum;
	}




	public void setDiffOrderNum(Long diffOrderNum) {
		this.diffOrderNum = diffOrderNum;
	}




	public Long getDiffRefundNum() {
		return diffRefundNum;
	}




	public void setDiffRefundNum(Long diffRefundNum) {
		this.diffRefundNum = diffRefundNum;
	}




	public Double getDiffAmount() {
		return diffAmount;
	}




	public void setDiffAmount(Double diffAmount) {
		this.diffAmount = diffAmount;
	}




	public String getDesc() {
		return desc;
	}




	public void setDesc(String desc) {
		this.desc = desc;
	}




	@Override
	public Serializable realId() {
		// TODO Auto-generated method stub
		return null;
	}

}
