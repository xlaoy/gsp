/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 2, 2013  5:17:08 PM
 */
public enum SettleBaseEnums {
	ORDERTIME("ORDERTIME","下单时间"),
	USETIME("USETIME","消费时间");
	private String type;
	private String display;
	private SettleBaseEnums(String type, String display){
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
