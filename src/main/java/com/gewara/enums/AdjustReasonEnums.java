/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 7, 2013  6:42:39 PM
 */
public enum AdjustReasonEnums {
	PRICE("PRICE","���㵥�����ô���"),
	REFUND("REFUND","�˿������"),
	SETTLE("SETTLE","���ڽ������"),
	SYSTEMERROR("SYSTEMERROR","ӰԺϵͳ����"),
	GEWASYSERROR("GEWASYSERROR","����ϵͳ����"),
	SERVICEFEE("SERVICEFEE","����ѵ���"),
	VIRTUAL("VIRTUAL","���ⳡ"),
	BOOKWHOLE("BOOKWHOLE","����"),
	GOODS("GOODS","��Ʒ"),
	OTHER("OTHER","����˵��"),
	TIMEDIFF("TIMEDIFF","ʱ���");
	
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
