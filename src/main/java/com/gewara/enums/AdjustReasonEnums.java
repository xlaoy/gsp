/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 7, 2013  6:42:39 PM
 */
public enum AdjustReasonEnums {
	PRICE("PRICE","结算单价设置错误"),
	REFUND("REFUND","退款订单错误"),
	SETTLE("SETTLE","上期结算差异"),
	SYSTEMERROR("SYSTEMERROR","影院系统差异"),
	GEWASYSERROR("GEWASYSERROR","格瓦系统差异"),
	SERVICEFEE("SERVICEFEE","服务费调整"),
	VIRTUAL("VIRTUAL","虚拟场"),
	BOOKWHOLE("BOOKWHOLE","包场"),
	GOODS("GOODS","卖品"),
	OTHER("OTHER","其它说明"),
	TIMEDIFF("TIMEDIFF","时间差");
	
	private String type;
	private String display;
	private AdjustReasonEnums(String type, String display){
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
