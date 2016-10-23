/** 
 */
package com.gewara.enums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  May 26, 2014  1:42:23 PM
 */
public enum BillTypeEnums {
	
	ESTIMATE("ESTIMATE","�ݹ�"),
	PAYBILL("PAYBILL","֧����"),
	PAYABLEBILL("PAYABLEBILL","Ӧ����");
	private String type;
	private String display;
	private BillTypeEnums(String type, String display){
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
