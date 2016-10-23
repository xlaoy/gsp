package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaPriceBill extends BaseObject {
	
	private static final long serialVersionUID = 881504745965567077L;

	private Long recordid;
	//价格
	private Double price;
	//是否是套票
	private String disticket;
	//扣率
	private String rate;
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
	//应付金额
	private Double payamount;
	//结算单id
	private Long settlebillid;
	//价格扣率id
	private Long pricerateid;
	//场馆id
	private Long placeid;
	//如果是套票要统计套票数量
	private Long taopiaonum;
	//gsp更新
	private Timestamp gspupdatetime;
	
	public DramaPriceBill() {
	}
	
	public DramaPriceBill(Double price, String disticket, String rate, Long settlebillid, Long pricerateid) {
		this.price = price;
		this.disticket = disticket;
		this.rate = rate;
		this.ticketnum = 0l;
		this.ticketamount = 0.0;
		this.refundnum = 0l;
		this.refundamount = 0.0;
		this.settleamount = 0.0;
		this.madisamount = 0.0;
		this.payamount = 0.0;
		this.taopiaonum = 0l;
		this.settlebillid = settlebillid;
		this.pricerateid = pricerateid;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}

	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}
	
	public String getDisticket() {
		return disticket;
	}



	public Long getTaopiaonum() {
		return taopiaonum;
	}

	public void setTaopiaonum(Long taopiaonum) {
		this.taopiaonum = taopiaonum;
	}

	public void setDisticket(String disticket) {
		this.disticket = disticket;
	}



	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Double getPrice() {
		return price;
	}



	public void setPrice(Double price) {
		this.price = price;
	}



	public String getRate() {
		return rate;
	}



	public void setRate(String rate) {
		this.rate = rate;
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

	public Double getMadisamount() {
		return madisamount;
	}

	public void setMadisamount(Double madisamount) {
		this.madisamount = madisamount;
	}

	
	public Double getPayamount() {
		return payamount;
	}



	public void setPayamount(Double payamount) {
		this.payamount = payamount;
	}



	public Long getSettlebillid() {
		return settlebillid;
	}



	public void setSettlebillid(Long settlebillid) {
		this.settlebillid = settlebillid;
	}



	public Long getPricerateid() {
		return pricerateid;
	}



	public void setPricerateid(Long pricerateid) {
		this.pricerateid = pricerateid;
	}



	public Long getPlaceid() {
		return placeid;
	}



	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
