package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class BaoChangReqBill extends BaseObject {

	private static final long serialVersionUID = -1681621616374325106L;

	//记录id（流水号）
	private Long recordId;
	//立项编号
	private String pjtCode;
	//关联编号（场次id）
	private String relatedId;
	//电影名称（项目名称）
	private String movieName;
	//款项说明
	private String pjtDesc;
	//供应商编号
	private String vendorNo;
	//供应商名称（收款单位）
	private String vendorName;
	//款项金额（包场金额）
	private Double amount;
	//请款人
	private String optUser;
	//请款时间
	private Timestamp reqTime;
	//影院id
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
