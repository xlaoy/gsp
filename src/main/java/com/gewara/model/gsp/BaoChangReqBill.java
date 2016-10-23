package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class BaoChangReqBill extends BaseObject {

	private static final long serialVersionUID = -1681621616374325106L;

	//��¼id����ˮ�ţ�
	private Long recordId;
	//������
	private String pjtCode;
	//������ţ�����id��
	private String relatedId;
	//��Ӱ���ƣ���Ŀ���ƣ�
	private String movieName;
	//����˵��
	private String pjtDesc;
	//��Ӧ�̱��
	private String vendorNo;
	//��Ӧ�����ƣ��տλ��
	private String vendorName;
	//�����������
	private Double amount;
	//�����
	private String optUser;
	//���ʱ��
	private Timestamp reqTime;
	//ӰԺid
	private String placeId;
	
	
	public BaoChangReqBill() {
	}
	
	
	
	public String getPlaceId() {
		return placeId;
	}



	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}



	public String getPjtCode() {
		return pjtCode;
	}



	public void setPjtCode(String pjtCode) {
		this.pjtCode = pjtCode;
	}



	public String getRelatedId() {
		return relatedId;
	}



	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}


	public String getMovieName() {
		return movieName;
	}



	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}



	public String getPjtDesc() {
		return pjtDesc;
	}



	public void setPjtDesc(String pjtDesc) {
		this.pjtDesc = pjtDesc;
	}



	public String getVendorNo() {
		return vendorNo;
	}



	public void setVendorNo(String vendorNo) {
		this.vendorNo = vendorNo;
	}



	public String getVendorName() {
		return vendorName;
	}



	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public String getOptUser() {
		return optUser;
	}



	public void setOptUser(String optUser) {
		this.optUser = optUser;
	}



	public Timestamp getReqTime() {
		return reqTime;
	}



	public void setReqTime(Timestamp reqTime) {
		this.reqTime = reqTime;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
