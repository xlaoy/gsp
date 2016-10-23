/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  3:12:30 PM
 */
public enum OrderTypeEnums {
	TICKET("TICKET","µÁ”∞"),
	GOODS("GOODS","¬Ù∆∑");
	private String type;
	private String display;
	private OrderTypeEnums(String type, String display){
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
