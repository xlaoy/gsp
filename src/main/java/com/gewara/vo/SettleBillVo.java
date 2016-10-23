package com.gewara.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class SettleBillVo implements Serializable {
	
	private static final long serialVersionUID = 8276696720506916362L;

	private Long recordId;
	private String placeId; 
	private String placeFirstLetter;
	private Timestamp start;
	private Timestamp end; 
	private String status;
	private String isNotEq;
	private String billType;
	private Long vendorId;
	private String reqMoneyStatus;
	private String playType;
	private String channelType;
	private String isXls;
	private Integer pageNo; 
	private Integer pageSize;
	private String url;
	private String goodsType;
	
	public SettleBillVo() {
	}

	public String getChannelType() {
		return channelType;
	}

	public String getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(String goodsType) {
		this.goodsType = goodsType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getIsXls() {
		return isXls;
	}

	public void setIsXls(String isXls) {
		this.isXls = isXls;
	}

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getPlaceFirstLetter() {
		return placeFirstLetter;
	}

	public void setPlaceFirstLetter(String placeFirstLetter) {
		this.placeFirstLetter = placeFirstLetter;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public Timestamp getEnd() {
		return end;
	}

	public void setEnd(Timestamp end) {
		this.end = end;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIsNotEq() {
		return isNotEq;
	}

	public void setIsNotEq(String isNotEq) {
		this.isNotEq = isNotEq;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public String getReqMoneyStatus() {
		return reqMoneyStatus;
	}

	public void setReqMoneyStatus(String reqMoneyStatus) {
		this.reqMoneyStatus = reqMoneyStatus;
	}

	public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}
	
	
}
