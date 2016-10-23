/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 2, 2013  5:14:30 PM
 */
public enum SettleCycleEnums {
	WEEKLY("WEEKLY","����"),
	MONTHLY("MONTHLY","����"),
	MIDDLE("MIDDLE","����"),
	TENDAY("TENDAY","10��"),
	TN("TN","T+N");
	
	private String type;
	private String display;
	private SettleCycleEnums(String type, String display){
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
