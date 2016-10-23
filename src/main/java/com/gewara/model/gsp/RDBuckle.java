package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDBuckle extends BaseObject {

	private static final long serialVersionUID = 3053212915315795030L;

	//���
	private Long recordId;
	//������Ž��㵥
	private Long relateId;
	//ӰԺid
	private String placeId;
	//ӰԺ����
	private String placeName;
	//��ʾ
	private String special;
	//��������
	private String cycle;
	//�̻�ȷ��ʱ��
	private Timestamp confirmTime;
	//�ۿ�ǰϵͳ���
	private Double buckleBefore;
	//�ۿ���
	private Double buckleAmount;
	//�ۿ��ϵͳ���
	private Double buckleAfter;
	//�ۿ�ʱ��
	private Timestamp startTime;
	private Timestamp endTime;
	private Timestamp buckleTime;
	//���Ӧ�̱���
	private String vendorCode;
	//���Ӧ������
	private String vendorName;

	public RDBuckle() {
	}



	public String getVendorCode() {
		return vendorCode;
	}



	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}



	public String getVendorName() {
		return vendorName;
	}



	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}



	public Long getRelateId() {
		return relateId;
	}



	public void setRelateId(Long relateId) {
		this.relateId = relateId;
	}



	public String getPlaceId() {
		return placeId;
	}



	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}



	public String getPlaceName() {
		return placeName;
	}



	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}



	public String getSpecial() {
		return special;
	}



	public void setSpecial(String special) {
		this.special = special;
	}



	public String getCycle() {
		return cycle;
	}



	public void setCycle(String cycle) {
		this.cycle = cycle;
	}



	public Timestamp getConfirmTime() {
		return confirmTime;
	}



	public void setConfirmTime(Timestamp confirmTime) {
		this.confirmTime = confirmTime;
	}



	public Double getBuckleBefore() {
		return buckleBefore;
	}



	public void setBuckleBefore(Double buckleBefore) {
		this.buckleBefore = buckleBefore;
	}



	public Double getBuckleAmount() {
		return buckleAmount;
	}



	public void setBuckleAmount(Double buckleAmount) {
		this.buckleAmount = buckleAmount;
	}



	public Double getBuckleAfter() {
		return buckleAfter;
	}



	public void setBuckleAfter(Double buckleAfter) {
		this.buckleAfter = buckleAfter;
	}



	public Timestamp getBuckleTime() {
		return buckleTime;
	}



	public void setBuckleTime(Timestamp buckleTime) {
		this.buckleTime = buckleTime;
	}



	public Timestamp getStartTime() {
		return startTime;
	}



	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}



	public Timestamp getEndTime() {
		return endTime;
	}



	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
