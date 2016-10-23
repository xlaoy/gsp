package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DecimalFormat;

import com.gewara.model.BaseObject;

public class ChannelSettleConfig extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2669883595361582400L;
	private Long recordId;
	private String vendorName;
	private String vendorCode;
	private String vendorType;
	private String settleCycle;
	private Integer settleDays;
	private Double settleMoney;
	private Double orderPercent;
	private Timestamp firstSettleDate;
	private Timestamp createTime;
	private String createUser;
	private Timestamp updateTime;
	private String updateUser;
	private String isSettleRefund;
	private Timestamp nextExeTime;
	//Y.有效 N.无效
	private String status;
	//审核状态
	private String verifyStatus;
	
	private Long houtaiId;
	private String pjtcode;
	//系统方编码
	private String syscode;
	//时间点
	private String timecut;
	private String settletime;
	
	public String getSettletime() {
		return settletime;
	}

	public void setSettletime(String settletime) {
		this.settletime = settletime;
	}

	public String getTimecut() {
		return timecut;
	}

	public void setTimecut(String timecut) {
		this.timecut = timecut;
	}

	public String getSyscode() {
		return syscode;
	}

	public void setSyscode(String syscode) {
		this.syscode = syscode;
	}

	/**  
	 * @return houtaiId  
	 */
	public Long getHoutaiId() {
		return houtaiId;
	}

	/**  
	 * @param set houtaiId 
	 */
	public void setHoutaiId(Long houtaiId) {
		this.houtaiId = houtaiId;
	}

	@Override
	public Serializable realId() {
		return recordId;
	}

	/**  
	 * @return recordId  
	 */
	public Long getRecordId() {
		return recordId;
	}

	/**  
	 * @param set recordId 
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	/**  
	 * @return vendorName  
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**  
	 * @param set vendorName 
	 */
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	/**  
	 * @return vendorCode  
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**  
	 * @param set vendorCode 
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}

	
	/**  
	 * @return vendorType  
	 */
	public String getVendorType() {
		return vendorType;
	}

	/**  
	 * @param set vendorType 
	 */
	public void setVendorType(String vendorType) {
		this.vendorType = vendorType;
	}

	/**  
	 * @return settleCycle  
	 */
	public String getSettleCycle() {
		return settleCycle;
	}

	/**  
	 * @param set settleCycle 
	 */
	public void setSettleCycle(String settleCycle) {
		this.settleCycle = settleCycle;
	}

	/**  
	 * @return settleDays  
	 */
	public Integer getSettleDays() {
		return settleDays;
	}

	/**  
	 * @param set settleDays 
	 */
	public void setSettleDays(Integer settleDays) {
		this.settleDays = settleDays;
	}

	/**  
	 * @return settleMoney  
	 */
	public Double getSettleMoney() {
		if(settleMoney == null) {
			return settleMoney;
		}
		DecimalFormat df = new DecimalFormat("0.00");
		String num = df.format(settleMoney);
		return Double.valueOf(num);
	}

	/**  
	 * @param set settleMoney 
	 */
	public void setSettleMoney(Double settleMoney) {
		this.settleMoney = settleMoney;
	}

	/**  
	 * @return orderPercent  
	 */
	public Double getOrderPercent() {
		if(orderPercent == null) {
			return orderPercent;
		}
		DecimalFormat df = new DecimalFormat("0.00");
		String num = df.format(orderPercent);
		return Double.valueOf(num);
	}

	/**  
	 * @param set orderPercent 
	 */
	public void setOrderPercent(Double orderPercent) {
		this.orderPercent = orderPercent;
	}

	/**  
	 * @return firstSettleDate  
	 */
	public Timestamp getFirstSettleDate() {
		return firstSettleDate;
	}

	/**  
	 * @param set firstSettleDate 
	 */
	public void setFirstSettleDate(Timestamp firstSettleDate) {
		this.firstSettleDate = firstSettleDate;
	}

	/**  
	 * @return createTime  
	 */
	public Timestamp getCreateTime() {
		return createTime;
	}

	/**  
	 * @param set createTime 
	 */
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	/**  
	 * @return createUser  
	 */
	public String getCreateUser() {
		return createUser;
	}

	/**  
	 * @param set createUser 
	 */
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	/**  
	 * @return updateTime  
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}

	/**  
	 * @param set updateTime 
	 */
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	/**  
	 * @return updateUser  
	 */
	public String getUpdateUser() {
		return updateUser;
	}

	/**  
	 * @param set updateUser 
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/**  
	 * @return isSettleRefund  
	 */
	public String getIsSettleRefund() {
		return isSettleRefund;
	}

	/**  
	 * @param set isSettleRefund 
	 */
	public void setIsSettleRefund(String isSettleRefund) {
		this.isSettleRefund = isSettleRefund;
	}

	/**  
	 * @return nextExeTime  
	 */
	public Timestamp getNextExeTime() {
		return nextExeTime;
	}

	/**  
	 * @param set nextExeTime 
	 */
	public void setNextExeTime(Timestamp nextExeTime) {
		this.nextExeTime = nextExeTime;
	}

	/**  
	 * @return status  
	 */
	public String getStatus() {
		return status;
	}

	/**  
	 * @param set status 
	 */
	public void setStatus(String status) {
		this.status = status;
	}


	/**  
	 * @return verifyStatus  
	 */
	public String getVerifyStatus() {
		return verifyStatus;
	}

	/**  
	 * @param set verifyStatus 
	 */
	public void setVerifyStatus(String verifyStatus) {
		this.verifyStatus = verifyStatus;
	}

	

	public ChannelSettleConfig() {
	}

	public String getPjtcode() {
		return pjtcode;
	}

	public void setPjtcode(String pjtcode) {
		this.pjtcode = pjtcode;
	}
	
}
