package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 
 * @ClassName: AutoReconciledInfo
 * @Description: 商户自动对账信息实体类
 * @author yujun.su@gewara.com
 * @date 2015-3-30 下午5:11:24
 */
public class AutoReconciledInfo extends BaseObject {
	/**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = 5961849757379243450L;

	private Long recordId;
	private Long settlementId;
	private String fileName;
	private String filePath;
	private String openType;
	private String status;
	private String placeId;
	private String remark;
	private Timestamp uploadTime;
	private String uploadUser;
	private Timestamp updateTime;
	private String updateUser;
	private Double gewaOrderNumber;
	private Double gewaOrderAmount;
	private Double merchantOrderNumber;
	private Double merchantOrderAmount;
	
	public Double getChapiao() {
		gewaOrderNumber = gewaOrderNumber == null ? 0.0 : gewaOrderNumber;
		merchantOrderNumber = merchantOrderNumber == null ? 0.0 : merchantOrderNumber;
		double r =  gewaOrderNumber - merchantOrderNumber;
		if(r == 0) {
			return null;
		} else {
			return r;
		}
	}

	public Double getChae() {
		gewaOrderAmount = gewaOrderAmount == null ? 0.0 : gewaOrderAmount;
		merchantOrderAmount = merchantOrderAmount == null ? 0.0 : merchantOrderAmount;
		double r = gewaOrderAmount - merchantOrderAmount;
		if(r == 0) {
			return null;
		} else {
			return r;
		}
	}

	/**
	 * @return recordId
	 */
	public Long getRecordId() {
		return recordId;
	}

	/**
	 * @param set
	 *            recordId
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return settlementId
	 */
	public Long getSettlementId() {
		return settlementId;
	}

	/**
	 * @param set
	 *            settlementId
	 */
	public void setSettlementId(Long settlementId) {
		this.settlementId = settlementId;
	}

	/**
	 * @return fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param set
	 *            fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param set
	 *            filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @return openType
	 */
	public String getOpenType() {
		return openType;
	}

	/**
	 * @param set
	 *            openType
	 */
	public void setOpenType(String openType) {
		this.openType = openType;
	}

	/**
	 * @return status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param set
	 *            status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return placeId
	 */
	public String getPlaceId() {
		return placeId;
	}

	/**
	 * @param set
	 *            placeId
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	/**
	 * @return remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param set
	 *            remark
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * @return createTime
	 */
	public Timestamp getUploadTime() {
		return uploadTime;
	}

	/**
	 * @param set
	 *            createTime
	 */
	public void setUploadTime(Timestamp uploadTime) {
		this.uploadTime = uploadTime;
	}

	/**
	 * @return uploadUser
	 */
	public String getUploadUser() {
		return uploadUser;
	}

	/**
	 * @param set
	 *            uploadUser
	 */
	public void setUploadUser(String uploadUser) {
		this.uploadUser = uploadUser;
	}

	/**
	 * @return updateTime
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param set
	 *            updateTime
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
	 * @return gewaOrderNumber
	 */
	public Double getGewaOrderNumber() {
		return gewaOrderNumber;
	}

	/**
	 * @param set
	 *            gewaOrderNumber
	 */
	public void setGewaOrderNumber(Double gewaOrderNumber) {
		this.gewaOrderNumber = gewaOrderNumber;
	}

	/**  
	 * @return gewaOrderAmount  
	 */
	public Double getGewaOrderAmount() {
		return gewaOrderAmount;
	}

	/**  
	 * @param set gewaOrderAmount 
	 */
	public void setGewaOrderAmount(Double gewaOrderAmount) {
		this.gewaOrderAmount = gewaOrderAmount;
	}

	/**
	 * @return merchantOrderNumber
	 */
	public Double getMerchantOrderNumber() {
		return merchantOrderNumber;
	}

	/**
	 * @param set
	 *            merchantOrderNumber
	 */
	public void setMerchantOrderNumber(Double merchantOrderNumber) {
		this.merchantOrderNumber = merchantOrderNumber;
	}

	/**
	 * @return merchantOrderAmount
	 */
	public Double getMerchantOrderAmount() {
		return merchantOrderAmount;
	}

	/**
	 * @param set
	 *            merchantOrderAmount
	 */
	public void setMerchantOrderAmount(Double merchantOrderAmount) {
		this.merchantOrderAmount = merchantOrderAmount;
	}

	/**
	 * @param set
	 *            updateUser
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public AutoReconciledInfo() {
	}

	

	public AutoReconciledInfo(Long settlementId, String fileName,
			String filePath, String openType, String status, String placeId,
			String remark, Timestamp uploadTime, String uploadUser,
			Timestamp updateTime, String updateUser) {
		super();
		this.settlementId = settlementId;
		this.fileName = fileName;
		this.filePath = filePath;
		this.openType = openType;
		this.status = status;
		this.placeId = placeId;
		this.remark = remark;
		this.uploadTime = uploadTime;
		this.uploadUser = uploadUser;
		this.updateTime = updateTime;
		this.updateUser = updateUser;
	}

	@Override
	public Serializable realId() {
		return recordId;
	}

}
