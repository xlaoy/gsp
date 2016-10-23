/** 
 */
package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Oct 28, 2013  6:49:07 PM
 */
public class PricedPlayItem extends BaseObject{
	private static final long serialVersionUID = 3704950664366248121L;
	private Long playId;
	private Double actualPrice;
	private String movieName;
	private String roomName;
	private Long cinemaId;
	private Timestamp updateTime;
	private Timestamp playTime;
	
	public PricedPlayItem(){}
	
	public PricedPlayItem(Long playId, Double actualPrice, String movieName, String roomName, Long cinemaId, Timestamp updateTime, Timestamp playTime){
		this.playId = playId;
		this.actualPrice  = actualPrice;
		this.movieName = movieName;
		this.roomName = roomName;
		this.cinemaId = cinemaId;
		this.updateTime = updateTime;
		this.playTime = playTime;
	}
	
	/**
	 * @return the playTime
	 */
	public Timestamp getPlayTime() {
		return playTime;
	}

	/**
	 * @param playTime the playTime to set
	 */
	public void setPlayTime(Timestamp playTime) {
		this.playTime = playTime;
	}

	/**
	 * @return the movieName
	 */
	public String getMovieName() {
		return movieName;
	}

	/**
	 * @param movieName the movieName to set
	 */
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	/**
	 * @return the roomName
	 */
	public String getRoomName() {
		return roomName;
	}

	/**
	 * @param roomName the roomName to set
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return the cinemaId
	 */
	public Long getCinemaId() {
		return cinemaId;
	}

	/**
	 * @param cinemaId the cinemaId to set
	 */
	public void setCinemaId(Long cinemaId) {
		this.cinemaId = cinemaId;
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
	 * @return the playId
	 */
	public Long getPlayId() {
		return playId;
	}
	/**
	 * @param playId the playId to set
	 */
	public void setPlayId(Long playId) {
		this.playId = playId;
	}
	/**
	 * @return the actualPrice
	 */
	public Double getActualPrice() {
		return actualPrice;
	}
	/**
	 * @param actualPrice the actualPrice to set
	 */
	public void setActualPrice(Double actualPrice) {
		this.actualPrice = actualPrice;
	}
	
	@Override
	public Serializable realId() {
		return playId;
	}
}
