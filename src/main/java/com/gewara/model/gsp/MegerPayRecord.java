package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class MegerPayRecord extends BaseObject {

	private static final long serialVersionUID = -3052832014554126837L;

	//��С���㵥��
	private Long minSettltId;
	//��ѯ�Ŀ�ʼʱ��
	private Timestamp queryStartTime;
	//��ѯ�Ľ���ʱ��
	private Timestamp queryEndTime;
	//���Ӧ��
	private String payVernderNo;
	//�ϲ�����ʱ��
	private Timestamp payTime;
	//�����Ľ��㵥��
	private String relateSettltIds;
	//�ܽ��
	private Double totalAmount;
	//������
	private String optUser;
	


	public Long getMinSettltId() {
		return minSettltId;
	}



	public void setMinSettltId(Long minSettltId) {
		this.minSettltId = minSettltId;
	}



	public Timestamp getQueryStartTime() {
		return queryStartTime;
	}



	public void setQueryStartTime(Timestamp queryStartTime) {
		this.queryStartTime = queryStartTime;
	}



	public Timestamp getQueryEndTime() {
		return queryEndTime;
	}



	public void setQueryEndTime(Timestamp queryEndTime) {
		this.queryEndTime = queryEndTime;
	}



	public String getPayVernderNo() {
		return payVernderNo;
	}



	public void setPayVernderNo(String payVernderNo) {
		this.payVernderNo = payVernderNo;
	}



	public Timestamp getPayTime() {
		return payTime;
	}



	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}



	public String getRelateSettltIds() {
		return relateSettltIds;
	}



	public void setRelateSettltIds(String relateSettltIds) {
		this.relateSettltIds = relateSettltIds;
	}



	public Double getTotalAmount() {
		return totalAmount;
	}



	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}



	public String getOptUser() {
		return optUser;
	}



	public void setOptUser(String optUser) {
		this.optUser = optUser;
	}



	@Override
	public Serializable realId() {
		return minSettltId;
	}

}
