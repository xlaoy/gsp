/** 
 */
package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 29, 2013  12:21:41 PM
 */
public class StatusTracker extends BaseObject{
	private static final long serialVersionUID = -3138647479642987164L;
	private Long recordId;
	private String tag;
	private Long relateId;
	private Timestamp addTime;
	private String operator;
	private String comments;
	private String otherInfo;
	private String oldStatus;
	private String newStatus;
	private String operatorCategory;
	/**
	 * @return the recordId
	 */
	
	public StatusTracker(){}
		
	public StatusTracker(String tag, Long relateId, String operator, String comments, String oldStatus, String newStatus, String operatorCategory){
		this.addTime = DateUtil.getCurFullTimestamp();
		this.tag = tag;
		this.relateId = relateId;
		this.operator = operator;
		this.comments = comments;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.operatorCategory = operatorCategory;
	}
	
	public Long getRecordId() {
		return recordId;
	}
	
	/**
	 * @return the operatorCategory
	 */
	public String getOperatorCategory() {
		return operatorCategory;
	}

	/**
	 * @param operatorCategory the operatorCategory to set
	 */
	public void setOperatorCategory(String operatorCategory) {
		this.operatorCategory = operatorCategory;
	}

	/**
	 * @return the oldStatus
	 */
	public String getOldStatus() {
		return oldStatus;
	}
	/**
	 * @param oldStatus the oldStatus to set
	 */
	public void setOldStatus(String oldStatus) {
		this.oldStatus = oldStatus;
	}
	/**
	 * @return the newStatus
	 */
	public String getNewStatus() {
		return newStatus;
	}
	/**
	 * @param newStatus the newStatus to set
	 */
	public void setNewStatus(String newStatus) {
		this.newStatus = newStatus;
	}
	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the relateId
	 */
	public Long getRelateId() {
		return relateId;
	}
	/**
	 * @param relateId the relateId to set
	 */
	public void setRelateId(Long relateId) {
		this.relateId = relateId;
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
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return the otherInfo
	 */
	public String getOtherInfo() {
		return otherInfo;
	}
	/**
	 * @param otherInfo the otherInfo to set
	 */
	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}

	/* (non-Javadoc)
	 * @see com.gewara.model.BaseObject#realId()
	 */
	@Override
	public Serializable realId() {
		return recordId;
	}
	
	
}
