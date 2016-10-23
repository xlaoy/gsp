/** 
 */
package com.gewara.enums;

/**
 * 
 * 调整类型， statusTracker 共用此enum
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 7, 2013  6:42:39 PM
 */
public enum AdjustTypeEnums {
	ORDER("ORDER","订单"),
	CHECKBILL("CHECKBILL","对账单"),
	PLACE("PLACE","场馆"),
	SETTLEBILL("SETTLEBILL","结算单");
	
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
