package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class VendorCinemaRelation extends BaseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3723789920426407665L;
	private Long recordId;
	private String cinemaRecordId;
	private Long vendorRecordId;
	private Timestamp createTime;
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
	 * @return vendorRecordId  
	 */
	public Long getVendorRecordId() {
		return vendorRecordId;
	}
	/**  
	 * @param set vendorRecordId 
	 */
	public void setVendorRecordId(Long vendorRecordId) {
		this.vendorRecordId = vendorRecordId;
	}
	
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	/**  
	 * @return cinemaRecordId  
	 */
	public String getCinemaRecordId() {
		return cinemaRecordId;
	}
	/**  
	 * @param set cinemaRecordId 
	 */
	public void setCinemaRecordId(String cinemaRecordId) {
		this.cinemaRecordId = cinemaRecordId;
	}
	public VendorCinemaRelation() {
		super();
	}
	public VendorCinemaRelation(Long recordId, String cinemaRecordId,Long vendorRecordId) {
		super();
		this.recordId = recordId;
		this.cinemaRecordId = cinemaRecordId;
		this.vendorRecordId = vendorRecordId;
	}

}
