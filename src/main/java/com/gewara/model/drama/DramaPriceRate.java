package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class DramaPriceRate extends BaseObject {

	private static final long serialVersionUID = -1832613195723331265L;

	private Long recordid;
	//�ݳ�����id
	private Long configid;
	//������
	private Double discount;
	//settle_config id
	private String settleid;
	//�۸�
	private Double price;
	//theatre_item_price id����
	private String itempriceid;
	//theatre_seat_price id����
	private String seatpriceid;
	//direcordid id����
	private String disrecordid;
	//�Ƿ�����Ʊ
	private String disticket;
	//gsp���ʱ��
	private Timestamp gspupdatetime;
	//״̬
	private String status;
	
	
	public DramaPriceRate() {
	}


	public String getDisticket() {
		return disticket;
	}



	public void setDisticket(String disticket) {
		this.disticket = disticket;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getItempriceid() {
		return itempriceid;
	}



	public void setItempriceid(String itempriceid) {
		this.itempriceid = itempriceid;
	}



	public String getSeatpriceid() {
		return seatpriceid;
	}



	public void setSeatpriceid(String seatpriceid) {
		this.seatpriceid = seatpriceid;
	}



	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Long getConfigid() {
		return configid;
	}



	public void setConfigid(Long configid) {
		this.configid = configid;
	}



	public Double getPrice() {
		return price;
	}



	public void setPrice(Double price) {
		this.price = price;
	}


	public String getSettleid() {
		return settleid;
	}



	public void setSettleid(String settleid) {
		this.settleid = settleid;
	}



	public Double getDiscount() {
		return discount;
	}



	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public String getDisrecordid() {
		return disrecordid;
	}


	public void setDisrecordid(String disrecordid) {
		this.disrecordid = disrecordid;
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
