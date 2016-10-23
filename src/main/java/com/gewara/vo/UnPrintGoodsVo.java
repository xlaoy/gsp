package com.gewara.vo;

import java.sql.Timestamp;

/**
 * 
 * @ClassName: UnPrintGoodsVo  
 * @Description: Õ¹Ê¾Î´  
 * @author yujun.su@gewara.com
 * @date 2014-10-23 ÉÏÎç10:02:47
 */
public class UnPrintGoodsVo {
	private Long cinemaId;
	private Timestamp startTime;
	private Timestamp endTime;
	private Integer unPrintQuantity;
	private Integer unPrintAmount;

	/**  
	 * @return cinemaId  
	 */
	public Long getCinemaId() {
		return cinemaId;
	}
	/**  
	 * @param set cinemaId 
	 */
	public void setCinemaId(Long cinemaId) {
		this.cinemaId = cinemaId;
	}
	/**  
	 * @return startTime  
	 */
	public Timestamp getStartTime() {
		return startTime;
	}
	/**  
	 * @param set startTime 
	 */
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	/**  
	 * @return endTime  
	 */
	public Timestamp getEndTime() {
		return endTime;
	}
	/**  
	 * @param set endTime 
	 */
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	/**  
	 * @return unPrintQuantity  
	 */
	public Integer getUnPrintQuantity() {
		return unPrintQuantity;
	}
	/**  
	 * @param set unPrintQuantity 
	 */
	public void setUnPrintQuantity(Integer unPrintQuantity) {
		this.unPrintQuantity = unPrintQuantity;
	}
	/**  
	 * @return unPrintAmount  
	 */
	public Integer getUnPrintAmount() {
		return unPrintAmount;
	}
	/**  
	 * @param set unPrintAmount 
	 */
	public void setUnPrintAmount(Integer unPrintAmount) {
		this.unPrintAmount = unPrintAmount;
	}
	public UnPrintGoodsVo(Long cinemaId, Timestamp startTime,
			Timestamp endTime, Integer unPrintQuantity, Integer unPrintAmount) {
		super();
		this.cinemaId = cinemaId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.unPrintQuantity = unPrintQuantity;
		this.unPrintAmount = unPrintAmount;
	}
	public UnPrintGoodsVo() {
		super();
	}
	
}
