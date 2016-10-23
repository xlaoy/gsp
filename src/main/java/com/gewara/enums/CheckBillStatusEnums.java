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
	 * �ȴ��������
	 */
	NEW("NEW","�ȴ�ϵͳ����"), // ����
	/**
	 * �ȴ��̻�ȷ��
	 */
	GEWACONFIRMED("GEWACONFIRMED","�ȴ��̻�ȷ��"),
	/**
	 * �̻���ȷ��
	 */
	MERCHANTCONFIRMED("MERCHANTCONFIRMED","�̻���ȷ��"), // ����
	/**
	 * �̻��������
	 */
	READJUST("READJUST","�̻��������"), 
	/**
	 * �����������
	 */
	GEWAREADJUST("GEWAREADJUST","�����������"), // ����
	/**
	 * �ȴ�����
	 */
	WAITINGPAY("WAITINGPAY","�ȴ�����"),
	/**
	 * ϵͳ����
	 */
	FULFILLING("FULFILLING","ϵͳ����"),
	/**
	 * �Ѹ���
	 */
	SETTLED("SETTLED","�Ѹ���"),
	/**
	 * ��Ч
	 */
	INVALID("INVALID","��Ч");
	
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
