package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDDayCount extends BaseObject {

	private static final long serialVersionUID = -3666603327045683612L;

	//���
	private Long recordId;
	//���Ӧ�̱���
	private String vendorCode;
	//���Ӧ������
	private String vendorName;
	//��ʼʱ��
	private Timestamp startTime;
	//����ʱ��
	private Timestamp endTime;
	//�ܽ��
	private Double amount;
	//��Ʊ��
	private Long num;
	
	public RDDayCount() {
	}
	
	
	
	public Long getNum() {
		return num;
	}



	public void setNum(Long num) {
		this.num = num;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Timestamp getStartTime() {
		return startTime;
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



	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}



	public Timestamp getEndTime() {
		return endTime;
	}



	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
