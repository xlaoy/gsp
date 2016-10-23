package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.BaseObject;

/**
 * 
 * @ClassName: ReqMoneyBill  
 * @Description: ��
 * @author yujun.su@gewara.com
 * @date 2015-8-13 ����1:48:38
 */
public class ReqMoneyBill extends BaseObject {
	/**  
	 * @Fields serialVersionUID  
	 */  
	private static final long serialVersionUID = -8402488061611800994L;
	//�����
	private Long recordId;
	//place  channel
	private String vendorType;
	//VendorID  placeid or channleconfigid
	private Long vendorId;
	//��Ӧ������  ӰԺ����/ϵͳ������/Ժ������
	private String vendorName;
	//��Ӧ�̱��
	private String vendorCode;
	//���ڿ�ʼʱ��
	private Timestamp minTime;
	//���ڽ���ʱ��
	private Timestamp maxTime;
	//����ʱ��
	private Date payTime;
	//ӰƱ����
	private Double ticketAmount = 0.0;
	//��Ʒ����
	private Double goodsAmount = 0.0;
	//ͨ������
	private Double channelAmount = 0.0;
	//����ܷ���
	private Double totalAmount = 0.0;
	//version
	private Integer version = 0;
	//relate settlebillid  pattern 1,2,3,4
	private String relateSettleId;
	//������
	private Long payUserId;
	
	/**  
	 * @return recordId  
	 */
	public Long getRecordId() {
		return recordId;
	}

	/**  
	 * @param set recordId 
	 */
	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	/**  
	 * @return vendorType  
	 */
	public String getVendorType() {
		return vendorType;
	}

	/**  
	 * @param set vendorType 
	 */
	public void setVendorType(String vendorType) {
		this.vendorType = vendorType;
	}

	/**  
	 * @return vendorId  
	 */
	public Long getVendorId() {
		return vendorId;
	}

	/**  
	 * @param set vendorId 
	 */
	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	/**  
	 * @return vendorName  
	 */
	public String getVendorName() {
		return vendorName;
	}

	/**  
	 * @param set vendorName 
	 */
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	/**  
	 * @return vendorCode  
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**  
	 * @param set vendorCode 
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}

	/**  
	 * @return minTime  
	 */
	public Timestamp getMinTime() {
		return minTime;
	}

	/**  
	 * @param set minTime 
	 */
	public void setMinTime(Timestamp minTime) {
		this.minTime = minTime;
	}

	/**  
	 * @return maxTime  
	 */
	public Timestamp getMaxTime() {
		return maxTime;
	}

	/**  
	 * @param set maxTime 
	 */
	public void setMaxTime(Timestamp maxTime) {
		this.maxTime = maxTime;
	}

	/**  
	 * @return payTime  
	 */
	public Date getPayTime() {
		return payTime;
	}

	/**  
	 * @param set payTime 
	 */
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	/**  
	 * @return ticketAmount  
	 */
	public Double getTicketAmount() {
		return ticketAmount;
	}

	/**  
	 * @param set ticketAmount 
	 */
	public void setTicketAmount(Double ticketAmount) {
		this.ticketAmount = ticketAmount;
	}

	/**  
	 * @return goodsAmount  
	 */
	public Double getGoodsAmount() {
		return goodsAmount;
	}

	/**  
	 * @param set goodsAmount 
	 */
	public void setGoodsAmount(Double goodsAmount) {
		this.goodsAmount = goodsAmount;
	}

	/**  
	 * @return channelAmount  
	 */
	public Double getChannelAmount() {
		return channelAmount;
	}

	/**  
	 * @param set channelAmount 
	 */
	public void setChannelAmount(Double channelAmount) {
		this.channelAmount = channelAmount;
	}

	/**  
	 * @return totalAmount  
	 */
	public Double getTotalAmount() {
		return totalAmount;
	}

	/**  
	 * @param set totalAmount 
	 */
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	/**  
	 * @return version  
	 */
	public Integer getVersion() {
		return version;
	}

	/**  
	 * @param set version 
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**  
	 * @return relateSettleId  
	 */
	public String getRelateSettleId() {
		return relateSettleId;
	}

	/**  
	 * @param set relateSettleId 
	 */
	public void setRelateSettleId(String relateSettleId) {
		this.relateSettleId = relateSettleId;
	}

	/**  
	 * @return payUserId  
	 */
	public Long getPayUserId() {
		return payUserId;
	}

	/**  
	 * @param set payUserId 
	 */
	public void setPayUserId(Long payUserId) {
		this.payUserId = payUserId;
	}

	@Override
	public Serializable realId() {
		return recordId;
	}

}
