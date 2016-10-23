/** 
 */
package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Apr 29, 2014  1:48:10 PM
 */
public class DownloadRecorder extends BaseObject{
	private static final long serialVersionUID = 8832211042938735495L;
	private Long recordId;
	private Long settlementId;
	//下载次数
	private Integer downloadCount;
	//最大下载次数
	private Integer maxDownCount;
	private Timestamp addTime;
	private Timestamp start;
	private Timestamp end;
	private Timestamp lastDownloadTime;
	private String billType;
	//请求次数
	private Integer requestedCount;
	private String special;
	private String vendorNo;
	private String nativeMoney;
	private String status;
	private String optUser;
	
	private DownloadRecorder(){}
	
	public DownloadRecorder(Long settlementId, BillTypeEnums billType){
		this();
		this.settlementId = settlementId;
		this.downloadCount = 0;
		this.maxDownCount = 1;
		this.addTime = DateUtil.getCurFullTimestamp();
		this.lastDownloadTime = addTime;
		this.billType = billType.getType();
		this.requestedCount = 0;
		this.status = SettleConstant.JSSUBMIT;
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

	public String getVendorNo() {
		return vendorNo;
	}

	public void setVendorNo(String vendorNo) {
		this.vendorNo = vendorNo;
	}

	public String getOptUser() {
		return optUser;
	}

	public void setOptUser(String optUser) {
		this.optUser = optUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNativeMoney() {
		return nativeMoney;
	}

	public void setNativeMoney(String nativeMoney) {
		this.nativeMoney = nativeMoney;
	}

	/**
	 * @return the maxDownCount
	 */
	public Integer getMaxDownCount() {
		return maxDownCount;
	}

	/**
	 * @param maxDownCount the maxDownCount to set
	 */
	public void setMaxDownCount(Integer maxDownCount) {
		this.maxDownCount = maxDownCount;
	}

	/**
	 * @return the recordId
	 */
	public Long getRecordId() {
		return recordId;
	}
	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
	
	/**
	 * @return the requestedCount
	 */
	public Integer getRequestedCount() {
		return requestedCount;
	}

	/**
	 * @param requestedCount the requestedCount to set
	 */
	public void setRequestedCount(Integer requestedCount) {
		this.requestedCount = requestedCount;
	}

	/**
	 * @return the settlementId
	 */
	public Long getSettlementId() {
		return settlementId;
	}
	/**
	 * @param settlementId the settlementId to set
	 */
	public void setSettlementId(Long settlementId) {
		this.settlementId = settlementId;
	}
	/**
	 * @return the downloadCount
	 */
	public Integer getDownloadCount() {
		return downloadCount;
	}
	/**
	 * @param downloadCount the downloadCount to set
	 */
	public void setDownloadCount(Integer downloadCount) {
		this.downloadCount = downloadCount;
	}
	/**
	 * @return the addTime
	 */
	public Timestamp getAddTime() {
		return addTime;
	}
	/**
	 * @param addTime the addTime to set
	 */
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
	/**
	 * @return the lastDownloadTime
	 */
	public Timestamp getLastDownloadTime() {
		return lastDownloadTime;
	}
	/**
	 * @param lastDownloadTime the lastDownloadTime to set
	 */
	public void setLastDownloadTime(Timestamp lastDownloadTime) {
		this.lastDownloadTime = lastDownloadTime;
	}

	/* (non-Javadoc)
	 * @see com.gewara.model.BaseObject#realId()
	 */
	@Override
	public Serializable realId() {
		return recordId;
	}

	/**
	 * @return the billType
	 */
	public String getBillType() {
		return billType;
	}

	/**
	 * @param billType the billType to set
	 */
	public void setBillType(String billType) {
		this.billType = billType;
	}


	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	
	
}
