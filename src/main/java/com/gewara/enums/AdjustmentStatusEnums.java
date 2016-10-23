/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 14, 2013  7:25:22 PM
 */
public enum AdjustmentStatusEnums{
	NEW("NEW","新建"),
	REJECTED("REJECTED","废弃"),
	APPROVED("APPROVED","通过");
	
	private String status;
	private String display;
	private AdjustmentStatusEnums(String type, String display){
		this.status = type;
		this.display = display;
	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @return the display
	 */
	public String getDisplay() {
		return display;
	}
}
