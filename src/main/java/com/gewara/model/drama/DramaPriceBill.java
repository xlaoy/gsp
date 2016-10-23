package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaPriceBill extends BaseObject {
	
	private static final long serialVersionUID = 881504745965567077L;

	private Long recordid;
	//�۸�
	private Double price;
	//�Ƿ�����Ʊ
	private String disticket;
	//����
	private String rate;
	//��Ʊ����
	private Long ticketnum;
	//Ʊ����
	private Double ticketamount;
	//��Ʊ����
	private Long refundnum;
	//��Ʊ���
	private Double refundamount;
	//������
	private Double settleamount;
	//�̻�ȯ-�һ�ȯ�Żݽ��
	private Double madisamount;
	//Ӧ�����
	private Double payamount;
	//���㵥id
	private Long settlebillid;
	//�۸����id
	private Long pricerateid;
	//����id
	private Long placeid;
	//�������ƱҪͳ����Ʊ����
	private Long taopiaonum;
	//gsp����
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
