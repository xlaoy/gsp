/** 
 */
package com.gewara.vo;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 12, 2013  5:23:07 PM
 */
public class PlaceVO {
	private String recordId;
	private String name;
	private String pinyin;
	
	public PlaceVO(){}
	
	public PlaceVO(String recordId, String name, String pinyin){
		this.recordId = recordId;
		this.name = name;
		this.pinyin = pinyin;
		
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the pinyin
	 */
	public String getPinyin() {
		return pinyin;
	}
	/**
	 * @param pinyin the pinyin to set
	 */
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	/**
	 * @return the recordId
	 */
	public String getRecordId() {
		return recordId;
	}
	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	
	
}
