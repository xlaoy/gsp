package com.gewara.model.gsp;
// default package
// Generated Jul 31, 2013 2:18:24 PM by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.PinYinUtils;
import com.gewara.util.RecordIdUtils;

/**
 * PlaceId generated by hbm2java
 */
public class Place extends BaseObject implements Comparable<Place>, java.io.Serializable {
	private static final long serialVersionUID = 3421576532565077665L;
	private String recordId;   //tag + , + relateId
	private String tag;
	private Long relateId;     //����ID
	private String cityCode;
	private String name;
	private Timestamp updateTime;
	private Long belongTo;
	private String isConfig;
	private String firstSpell;
	private String openType;
	private String brandName;
	private String pcid;
	private String owner;
	private String theatres;

	public Place() {
	}
	public Place(Long relateId, String tag, String cityCode, String name, Timestamp updateTime, 
							String openType, String isConfig, String brandName,String pcid){
		this.recordId = RecordIdUtils.contactRecordId(tag, relateId);
		this.relateId = relateId;
		this.tag = tag;
		this.cityCode = cityCode;
		this.name = name;
		this.updateTime = updateTime;
		this.isConfig = isConfig;
		this.firstSpell = PinYinUtils.getFirstSpell(name);
		this.openType = openType;
		this.brandName = brandName;
		this.pcid = pcid;
	}
	
	
	public String getTheatres() {
		return theatres;
	}
	public void setTheatres(String theatres) {
		this.theatres = theatres;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getPcid() {
		return pcid;
	}
	public void setPcid(String pcid) {
		this.pcid = pcid;
	}
	/**
	 * @return the firstSpell
	 */
	public String getFirstSpell() {
		return firstSpell;
	}
	
	/**
	 * @return the brandName
	 */
	public String getBrandName() {
		return brandName;
	}
	/**
	 * @param brandName the brandName to set
	 */
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	/**
	 * @return the openType
	 */
	public String getOpenType() {
		return openType;
	}
	/**
	 * @param openType the openType to set
	 */
	public void setOpenType(String openType) {
		this.openType = openType;
	}
	/**
	 * @param firstSpell the firstSpell to set
	 */
	public void setFirstSpell(String firstSpell) {
		this.firstSpell = firstSpell;
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
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the relateId
	 */
	public Long getRelateId() {
		return relateId;
	}
	/**
	 * @param relateId the relateId to set
	 */
	public void setRelateId(Long relateId) {
		this.relateId = relateId;
	}
	/**
	 * @return the cityCode
	 */
	public String getCityCode() {
		return cityCode;
	}
	/**
	 * @param cityCode the cityCode to set
	 */
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
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
	 * @return the updateTime
	 */
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	/**
	 * @param updateTime the updateTime to set
	 */
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	/**
	 * @return the belongTo
	 */
	public Long getBelongTo() {
		return belongTo;
	}
	/**
	 * @param belongTo the belongTo to set
	 */
	public void setBelongTo(Long belongTo) {
		this.belongTo = belongTo;
	}
	/**
	 * @return the isConfig
	 */
	public String getIsConfig() {
		return isConfig;
	}
	/**
	 * @param isConfig the isConfig to set
	 */
	public void setIsConfig(String isConfig) {
		this.isConfig = isConfig;
	}
	@Override
	public Serializable realId() {
		return recordId;
	}
	@Override
	public int compareTo(Place o) {
		return this.recordId.compareTo(o.getRecordId());
	}
	
}
