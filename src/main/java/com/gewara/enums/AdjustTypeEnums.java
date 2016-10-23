/** 
 */
package com.gewara.enums;

/**
 * 
 * �������ͣ� statusTracker ���ô�enum
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 7, 2013  6:42:39 PM
 */
public enum AdjustTypeEnums {
	ORDER("ORDER","����"),
	CHECKBILL("CHECKBILL","���˵�"),
	PLACE("PLACE","����"),
	SETTLEBILL("SETTLEBILL","���㵥");
	
	private String type;
	private String display;
	private AdjustTypeEnums(String type, String display){
		this.type = type;
		this.display = display;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @return the display
	 */
	public String getDisplay() {
		return display;
	}
}
