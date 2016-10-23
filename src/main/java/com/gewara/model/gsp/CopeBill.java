package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class CopeBill extends BaseObject {

	private static final long serialVersionUID = 3316399174503019185L;

	//主子表关联字段
	private String clink;
	//单据编号(结算单号)
	private String cvouchID;
	//单据日期 (结算单生成日期)
	private Timestamp dvouchDate;
	//供应商编码(场馆/供应商)
	private String cdwCode;
	//科目编码 
	private String ccode;
	//金额 (结算金额)
	private double iamount = 0.00;
	//项目大类编码 
	private String citemClass;
	//项目编码 
	private String citemCode;
	//摘要
	private String cdigest;
	//付款条件编码 
	private String cpayCode;
	//录入人 
	private String coperator;
	//数据状态
	private String status;
	//数据记录
	private long recordId;
	//暂估冲销金额
	private double reversalAmount;
	
	public String getClink() {
		return clink;
	}



	public void setClink(String clink) {
		this.clink = clink;
	}



	public String getCvouchID() {
		return cvouchID;
	}



	public void setCvouchID(String cvouchID) {
		this.cvouchID = cvouchID;
	}



	public Timestamp getDvouchDate() {
		return dvouchDate;
	}



	public void setDvouchDate(Timestamp dvouchDate) {
		this.dvouchDate = dvouchDate;
	}



	public String getCdwCode() {
		return cdwCode;
	}



	public void setCdwCode(String cdwCode) {
		this.cdwCode = cdwCode;
	}



	public String getCcode() {
		return ccode;
	}



	public void setCcode(String ccode) {
		this.ccode = ccode;
	}



	public Double getIamount() {
		return iamount;
	}



	public void setIamount(Double iamount) {
		this.iamount = iamount;
	}



	public String getCitemClass() {
		return citemClass;
	}



	public void setCitemClass(String citemClass) {
		this.citemClass = citemClass;
	}



	public String getCitemCode() {
		return citemCode;
	}



	public void setCitemCode(String citemCode) {
		this.citemCode = citemCode;
	}



	public String getCdigest() {
		return cdigest;
	}



	public void setCdigest(String cdigest) {
		this.cdigest = cdigest;
	}



	public String getCpayCode() {
		return cpayCode;
	}



	public void setCpayCode(String cpayCode) {
		this.cpayCode = cpayCode;
	}



	public String getCoperator() {
		return coperator;
	}



	public void setCoperator(String coperator) {
		this.coperator = coperator;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public long getRecordId() {
		return recordId;
	}



	public void setRecordId(long recordId) {
		this.recordId = recordId;
	}



	public double getReversalAmount() {
		return reversalAmount;
	}



	public void setReversalAmount(double reversalAmount) {
		this.reversalAmount = reversalAmount;
	}



	@Override
	public Serializable realId() {
		return null;
	}

}
