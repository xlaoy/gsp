/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 6, 2013  10:40:01 AM
 */

public enum CheckBillStatusEnums {
	/**
	 * 等待财务审核
	 */
	NEW("NEW","等待系统处理"), // 弃用
	/**
	 * 等待商户确认
	 */
	GEWACONFIRMED("GEWACONFIRMED","等待商户确认"),
	/**
	 * 商户已确认
	 */
	MERCHANTCONFIRMED("MERCHANTCONFIRMED","商户已确认"), // 弃用
	/**
	 * 商户申请调整
	 */
	READJUST("READJUST","商户申请调整"), 
	/**
	 * 财务申请调整
	 */
	GEWAREADJUST("GEWAREADJUST","财务申请调整"), // 弃用
	/**
	 * 等待付款
	 */
	WAITINGPAY("WAITINGPAY","等待付款"),
	/**
	 * 系统处理
	 */
	FULFILLING("FULFILLING","系统处理"),
	/**
	 * 已付款
	 */
	SETTLED("SETTLED","已付款"),
	/**
	 * 无效
	 */
	INVALID("INVALID","无效");
	
	private String status;
	private String display;
	private CheckBillStatusEnums(String status, String display){
		this.status = status;
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
