/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 14, 2013  7:25:22 PM
 */
public enum AdjustmentStatusEnums{
	NEW("NEW","�½�"),
	REJECTED("REJECTED","����"),
	APPROVED("APPROVED","ͨ��");
	
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
