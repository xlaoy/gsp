package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class RDPay extends BaseObject {

	private static final long serialVersionUID = 3965174862800497131L;
	
	//���
	private Long recordId;
	//��Ӧ�̱��
	private String vendorCode;
	//��Ӧ������
	private String vendorName;
	//��ֵǰ���
	private Double payBefore;
	//��ֵ���
	private Double payAmount;
	//��ֵ����
	private Double payAfter;
	//��ֵʱ��
	private Timestamp payTime;
	private Timestamp startTime;
	private Timestamp endTime;
	//������
	private String optUser;
	//֧����ʽ
	private String payWay;
	//֧��˵��
	private String payContent;
	//����
	private String reqMoneyId;

	public RDPay() {
	}
	


	public String getReqMoneyId() {
		return reqMoneyId;
	}



	public void setReqMoneyId(String reqMoneyId) {
		this.reqMoneyId = reqMoneyId;
	}



	public String getPayWay() {
		return payWay;
	}



	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}



	public Long getRecordId() {
		return recordId;
	}



	public void setRecordId(Long recordId) {
		this.recordId = recordId;
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



	public Double getPayBefore() {
		return payBefore;
	}



	public void setPayBefore(Double payBefore) {
		this.payBefore = payBefore;
	}



	public Double getPayAmount() {
		return payAmount;
	}



	public void setPayAmount(Double payAmount) {
		this.payAmount = payAmount;
	}



	public Double getPayAfter() {
		return payAfter;
	}



	public void setPayAfter(Double payAfter) {
		this.payAfter = payAfter;
	}



	public Timestamp getPayTime() {
		return payTime;
	}



	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	


	public String getOptUser() {
		return optUser;
	}



	public void setOptUser(String optUser) {
		this.optUser = optUser;
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



	public String getPayContent() {
		return payContent;
	}



	public void setPayContent(String payContent) {
		this.payContent = payContent;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

}
