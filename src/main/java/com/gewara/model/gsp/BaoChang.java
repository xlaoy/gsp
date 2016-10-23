package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class BaoChang extends BaseObject {
	
	private static final long serialVersionUID = -3155984668972498224L;

	//����id
	private Long recordId;
	//ӰԺid
	private String placeId;
	//��������
	private String deptName;
	//����id
	private Long playId;
	//��������
	private String playType;
	//��Ӱ����
	private String filmName;
	//�Ƿ�Ԥ��
	private String prePay;
	//��ӳʱ��
	private Timestamp playTime;
	private Timestamp startTime;
	private Timestamp endTime;
	//����Ʊ��
	private Integer bcNum;
	//�������
	private Double bcAmount;
	//Ԥ�����
	private Double preAmount;
	//�û���Ʊ����
	private Integer successNum;
	//�û���Ʊ���
	private Double userAmount;
	//�ܶ�������ɱ�
	private Integer totalNum;
	//�ܶ�������ɱ�
	private Double successAmount;
	//��Ʊ��
	private Integer refundNum;
	//��Ʊ�ɱ�
	private Double refundAmount;
	//�û���Ʊ�����
	private Double successRate;
	//״̬
	private String status;
	//�������㵥��
	private Long settleId;
	private Long goodsSettleId;
	//����ʱ��
	private Timestamp updateTime;
	//��������
	private String bcType;
	//����ʱ��
	private Timestamp payTime;
	//������
	private String optUser;
	//
	private String special;
	//���״̬
	private String reqbillStatus;
	//��Ŀ���
	private String pjtCode;
	private Double kpamount;
	
	private Integer goodsNum;
	private Double goodsAmount;
	
	public BaoChang() {
	}
	
	public Long getGoodsSettleId() {
		return goodsSettleId;
	}

	public void setGoodsSettleId(Long goodsSettleId) {
		this.goodsSettleId = goodsSettleId;
	}

	public Integer getGoodsNum() {
		return goodsNum == null ? 0 : goodsNum;
	}

	public void setGoodsNum(Integer goodsNum) {
		this.goodsNum = goodsNum;
	}

	public Double getGoodsAmount() {
		return goodsAmount == null ? 0 : goodsAmount;
	}

	public void setGoodsAmount(Double goodsAmount) {
		this.goodsAmount = goodsAmount;
	}

	public Double getKpamount() {
		return kpamount;
	}

	public void setKpamount(Double kpamount) {
		this.kpamount = kpamount;
	}

	public Integer getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}

	public Integer getRefundNum() {
		return refundNum;
	}

	public void setRefundNum(Integer refundNum) {
		this.refundNum = refundNum;
	}

	public Double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}

	public Double getUserAmount() {
		return userAmount;
	}

	public void setUserAmount(Double userAmount) {
		this.userAmount = userAmount;
	}

	public String getPjtCode() {
		return pjtCode;
	}

	public void setPjtCode(String pjtCode) {
		this.pjtCode = pjtCode;
	}

	public Long getRecordId() {
		return recordId;
	}


	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}


	public String getReqbillStatus() {
		return reqbillStatus;
	}

	public void setReqbillStatus(String reqbillStatus) {
		this.reqbillStatus = reqbillStatus;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getDeptName() {
		return deptName;
	}



	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}



	public Long getPlayId() {
		return playId;
	}



	public void setPlayId(Long playId) {
		this.playId = playId;
	}



	public String getPlayType() {
		return playType;
	}



	public void setPlayType(String playType) {
		this.playType = playType;
	}



	public String getFilmName() {
		return filmName;
	}



	public void setFilmName(String filmName) {
		this.filmName = filmName;
	}



	public Timestamp getPlayTime() {
		return playTime;
	}



	public void setPlayTime(Timestamp playTime) {
		this.playTime = playTime;
	}



	public Integer getBcNum() {
		return bcNum;
	}

	public void setBcNum(Integer bcNum) {
		this.bcNum = bcNum;
	}

	public Double getBcAmount() {
		return bcAmount;
	}



	public void setBcAmount(Double bcAmount) {
		this.bcAmount = bcAmount;
	}



	public Integer getSuccessNum() {
		return successNum;
	}



	public void setSuccessNum(Integer successNum) {
		this.successNum = successNum;
	}



	public Double getSuccessAmount() {
		return successAmount;
	}



	public void setSuccessAmount(Double successAmount) {
		this.successAmount = successAmount;
	}



	public Double getSuccessRate() {
		return successRate;
	}



	public void setSuccessRate(Double successRate) {
		this.successRate = successRate;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Long getSettleId() {
		return settleId;
	}



	public void setSettleId(Long settleId) {
		this.settleId = settleId;
	}



	@Override
	public Serializable realId() {
		return recordId;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getBcType() {
		return bcType;
	}

	public void setBcType(String bcType) {
		this.bcType = bcType;
	}

	public Timestamp getPayTime() {
		return payTime;
	}

	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	public String getOptUser() {
		return optUser;
	}

	public void setOptUser(String optUser) {
		this.optUser = optUser;
	}

	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	public String getPrePay() {
		return prePay;
	}

	public void setPrePay(String prePay) {
		this.prePay = prePay;
	}

	public Double getPreAmount() {
		return preAmount;
	}

	public void setPreAmount(Double preAmount) {
		this.preAmount = preAmount;
	}

	/**
	 * ��������
	 * @return
	 */
	public Double getBuchaAmount() {
		this.bcAmount = this.bcAmount == null ? 0 : this.bcAmount;
		this.successAmount = this.successAmount == null ? 0 : this.successAmount;
		this.refundAmount = this.refundAmount == null ? 0 : this.refundAmount;
		return this.bcAmount - (this.successAmount - this.refundAmount);
	}
	
	/**
	 * ��������
	 * @return
	 */
	public Integer getBuchaNum() {
		this.bcNum = this.bcNum == null ? 0 : this.bcNum;
		this.totalNum = this.totalNum == null ? 0 : this.totalNum;
		this.refundNum = this.refundNum == null ? 0 : this.refundNum;
		return this.bcNum - (this.totalNum - this.refundNum);
	}
	
	/**
	 * �ܳɱ�
	 * @return
	 */
	public Double getCostAmount() {
		this.bcAmount = this.bcAmount == null ? 0 : this.bcAmount;
		this.goodsAmount = this.goodsAmount == null ? 0 : this.goodsAmount;
		return this.bcAmount + this.goodsAmount;
	}
	
}
