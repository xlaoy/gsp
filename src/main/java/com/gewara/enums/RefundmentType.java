/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Dec 30, 2013  4:57:57 PM
 */
public enum RefundmentType {
	
	REFUND("REFUND","正常退款"),
	PRIVOUSREFUND("PRIVOUSREFUND","退上期订单");
	
	private String type;
	private String display;
	private RefundmentType(String type, String display){
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
