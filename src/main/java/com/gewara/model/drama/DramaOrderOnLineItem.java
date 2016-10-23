package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaOrderOnLineItem extends BaseObject {

	private static final long serialVersionUID = -7128716248182430109L;
	
	private Long recordid;
	//������
	private String tradeno;
	//����id
	private Long placeid;
	//Ʊ��
	private Long quantity;
	//Ʊ����
	private Double totalfee;
	//�Żݽ��
	private Double disfee;
	//��ӳʱ��
	private Timestamp playtime;
	//����id
	private Long playid;
	//�۸�id
	private Long tipid;
	//��Ʊid
	private Long disid;
	//�������ƱҪͳ����Ʊ����
	private Long taopiaonum;
	//���㵥id
	private Long pricebillid;
	//�Ƿ�������Ʊ
	private String hasrefund;
	//
	private Long refundpricebillid;
	//gsp���ʱ��
	private Timestamp gspupdatetime;

	public DramaOrderOnLineItem() {
	}
	
	public DramaOrderOnLineItem(Long recordid, String tradeno, Long placeid, Long quantity, Double totalfee, Double disfee, Timestamp playtime, 
			Long playid, Long tipid, Long disid, Long taopiaonum) {
		this.recordid = recordid;
		this.tradeno = tradeno;
		this.placeid = placeid;
		this.quantity = quantity;
		this.totalfee = totalfee;
		this.disfee = disfee;
		this.playid = playid;
		this.playtime = playtime;
		this.tipid = tipid;
		this.disid = disid;
		this.taopiaonum = taopiaonum;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}

	
	
	public Long getRefundpricebillid() {
		return refundpricebillid;
	}

	public void setRefundpricebillid(Long refundpricebillid) {
		this.refundpricebillid = refundpricebillid;
	}

	public Long getPricebillid() {
		return pricebillid;
	}

	public void setPricebillid(Long pricebillid) {
		this.pricebillid = pricebillid;
	}

	public String getHasrefund() {
		return hasrefund;
	}

	public void setHasrefund(String hasrefund) {
		this.hasrefund = hasrefund;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}

	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}

	public Long getPlayid() {
		return playid;
	}

	public void setPlayid(Long playid) {
		this.playid = playid;
	}

	public Double getDisfee() {
		return disfee;
	}

	public void setDisfee(Double disfee) {
		this.disfee = disfee;
	}

	public Long getTipid() {
		return tipid;
	}

	public void setTipid(Long tipid) {
		this.tipid = tipid;
	}

	public Long getTaopiaonum() {
		return taopiaonum;
	}

	public void setTaopiaonum(Long taopiaonum) {
		this.taopiaonum = taopiaonum;
	}

	public Long getPlaceid() {
		return placeid;
	}


	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
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



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public String getTradeno() {
		return tradeno;
	}



	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}



	public Long getQuantity() {
		return quantity;
	}



	public void setQuantity(Long quantity) {
		this.quantity = quantity;
	}



	public Double getTotalfee() {
		return totalfee;
	}


	public void setTotalfee(Double totalfee) {
		this.totalfee = totalfee;
	}


	public Long getDisid() {
		return disid;
	}



	public void setDisid(Long disid) {
		this.disid = disid;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
