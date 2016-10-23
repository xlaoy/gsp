package com.gewara.model.gsp;

import java.io.Serializable;


/**
 * 
 * @ClassName: AutoAnalysObject
 * @Description:ӳ���Excel���������Ķ���,��ȡ����ϵͳ�Ĺ�������
 * @author yujun.su@gewara.com
 * @date 2015-4-1 ����4:12:21
 */
public class AutoAnalysObject implements Serializable {

	/**
	 * @Fields serialVersionUID
	 */
	private static final long serialVersionUID = -4659065966542261313L;
	private Long recordId;
	// ���㵥ID
	private Long settlementId;
	//������
	private String tradeNo;
	// ӰƬ
	private String movieName;
	// Ӱ��
	private String movieHall;
	// �µ�����
	private String useDate;
	// �µ�ʱ��
	private String useTime;
	// ��ӳ����
	private String showDate;
	// ��ӳʱ��
	private String showTime;
	// Ʊ��
	private Integer votes;
	// Ʊ��
	private Double totalPrice;
	// ȷ�Ϻţ�������
	private String reconCode;
	// �Ƿ�����
	private String orderStatus;
	// �쳣˵��
	private String remark;

	/**
	 * @return recordId
	 */
	public Long getRecordId() {
		return recordId;
	}

	/**
	 * @param set
	 *            recordId
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return settlementId
	 */
	public Long getSettlementId() {
		return settlementId;
	}

	/**
	 * @param set
	 *            settlementId
	 */
	public void setSettlementId(Long settlementId) {
		this.settlementId = settlementId;
	}

	
	/**  
	 * @return tradeNo  
	 */
	public String getTradeNo() {
		return tradeNo;
	}

	/**  
	 * @param set tradeNo 
	 */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * @return movieName
	 */
	public String getMovieName() {
		return movieName;
	}

	/**
	 * @param set
	 *            movieName
	 */
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	/**
	 * @return movieHall
	 */
	public String getMovieHall() {
		return movieHall;
	}

	/**
	 * @param set
	 *            movieHall
	 */
	public void setMovieHall(String movieHall) {
		this.movieHall = movieHall;
	}

	/**
	 * @return useDate
	 */
	public String getUseDate() {
		return useDate;
	}

	/**
	 * @param set
	 *            useDate
	 */
	public void setUseDate(String useDate) {
		this.useDate = useDate;
	}

	/**
	 * @return useTime
	 */
	public String getUseTime() {
		return useTime;
	}

	/**
	 * @param set
	 *            useTime
	 */
	public void setUseTime(String useTime) {
		this.useTime = useTime;
	}

	/**
	 * @return showDate
	 */
	public String getShowDate() {
		return showDate;
	}

	/**
	 * @param set
	 *            showDate
	 */
	public void setShowDate(String showDate) {
		this.showDate = showDate;
	}

	/**
	 * @return showTime
	 */
	public String getShowTime() {
		return showTime;
	}

	/**
	 * @param set
	 *            showTime
	 */
	public void setShowTime(String showTime) {
		this.showTime = showTime;
	}

	/**
	 * @return votes
	 */
	public Integer getVotes() {
		return votes;
	}

	/**
	 * @param set
	 *            votes
	 */
	public void setVotes(Integer votes) {
		this.votes = votes;
	}

	/**
	 * @return totalPrice
	 */
	public Double getTotalPrice() {
		return totalPrice;
	}

	/**
	 * @param set
	 *            totalPrice
	 */
	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	/**
	 * @return reconCode
	 */
	public String getReconCode() {
		return reconCode;
	}

	/**
	 * @param set
	 *            reconCode
	 */
	public void setReconCode(String reconCode) {
		this.reconCode = reconCode;
	}

	/**
	 * @return orderStatus
	 */
	public String getOrderStatus() {
		return orderStatus;
	}

	/**
	 * @param set
	 *            orderStatus
	 */
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	/**
	 * @return remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param set
	 *            remark
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public AutoAnalysObject(){}
	
	
	

	public AutoAnalysObject(Long settlementId, String tradeNo,
			String movieName, String movieHall, String useDate, String useTime,
			String showDate, String showTime, Integer votes,
			Double totalPrice, String reconCode, String orderStatus,
			String remark) {
		super();
		this.settlementId = settlementId;
		this.tradeNo = tradeNo;
		this.movieName = movieName;
		this.movieHall = movieHall;
		this.useDate = useDate;
		this.useTime = useTime;
		this.showDate = showDate;
		this.showTime = showTime;
		this.votes = votes;
		this.totalPrice = totalPrice;
		this.reconCode = reconCode;
		this.orderStatus = orderStatus;
		this.remark = remark;
	}

	@Override
	public int hashCode() {
		return getReconCode().hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if (obj == this)
		{
			return true;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		AutoAnalysObject o = (AutoAnalysObject) obj;
		return (this.getReconCode().equals(o.getReconCode()));
	}
}
