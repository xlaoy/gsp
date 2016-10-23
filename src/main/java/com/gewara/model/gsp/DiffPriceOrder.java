package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class DiffPriceOrder extends BaseObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5461634777450217475L;

	//订单号
	private String tradeno;
	//实际结算价
	private Double actualPrice;
	//票数
	private Integer quantity;
	//更新时间
	private Timestamp updateTime;
	//场次id
	private Long mpId;
	//影票日账单单号
	private Long chekcBillId;
	//影票结算单单号
	private Long settleBillId;
	private String refund;
	private Long refundsettleid;
	
	public Long getRefundsettleid() {
		return refundsettleid;
	}
	public void setRefundsettleid(Long refundsettleid) {
		this.refundsettleid = refundsettleid;
	}
	public String getRefund() {
		return refund;
	}
	public void setRefund(String refund) {
		this.refund = refund;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public Double getActualPrice() {
		return actualPrice;
	}
	public void setActualPrice(Double actualPrice) {
		this.actualPrice = actualPrice;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public Long getMpId() {
		return mpId;
	}
	public void setMpId(Long mpId) {
		this.mpId = mpId;
	}
	
	public Long getChekcBillId() {
		return chekcBillId;
	}
	public void setChekcBillId(Long chekcBillId) {
		this.chekcBillId = chekcBillId;
	}
	public Long getSettleBillId() {
		return settleBillId;
	}
	public void setSettleBillId(Long settleBillId) {
		this.settleBillId = settleBillId;
	}
	@Override
	public Serializable realId() {
		return tradeno;
	}
}
