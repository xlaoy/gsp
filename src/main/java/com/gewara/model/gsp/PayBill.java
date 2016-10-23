/** 
 */
package com.gewara.model.gsp;

/**
 * @author Zhicheng.Peng Johnny.Resurgam@Gmail.com
 * 
 *         Apr 15, 2014 4:30:03 PM
 */
public class PayBill {
	private String gsprecordId;   //GSP downloadBill recordId;
	private String vendorNo;      //GSP ��Ӧ�̱��
	
	private String iID; // (U861) 	���ؼ���		�����ݣ�U8ʹ������
	private String cBillID; // (U861) 	Ʊ�ݺ�		��ʽ��yyyyMMddHHmmss
	private String dBillDate; // (U861) 	Ʊ������		֧������������: yyyyMMddHHmmss
	private String mOriginMoney; // (U861) 	ԭ�ҽ��         ������*����
	private String cPaySpeed = "��ͨ"; // (U861) 	����ٶ�	����ֵΪ:��ͨ|�Ӽ�	�̶�ֵ����ͨ
	private String sExchRate = "1"; // (U861) 	����		�̶�ֵ��1
	private String mNativeMoney; // (U861) 	���ҽ��		������
	private String cMoneyType = "�����"; // (U861) 	��������		�̶�ֵ�������
	private String cAccountFrom = "31001563000050011746"; // (U861) 	֧����ת���ʺ�		�̶�ֵ��31001563000050011746
	private String cAccountTo; // (U861) 	�շ��ʺ�		�����ݣ�U8����
	private String cComeFrom = "¼��"; // (U861) 	��Դ	����ֵΪ:¼��	�̶�ֵ:¼��
	private String cOperator = ""; // (U861) 	�Ƶ���		�̶�ֵ��GSP_U8
	private String cApprover = "GSP_U8-R";             //������
	private String cExaminer = "GSP_U8-R";             //������
	private String iBillStatus = "0"; // (U861) 	֧��״̬	0	�̶�ֵ��0
	private String cSettle = "02"; // (U861) 	���㷽ʽ����	����SettleStyle��cSSCode�ֶ�	�̶�ֵ��02
	private String cPurpose; // (U861) 	��;���	w	������
	private String cAppend; // (U861) 	����		�磺������Ʊ��-20140501:20140601-11231
	private String cRemark; //��ע
	
	private PayBill(){}
	
	/**
	 * 
	 * @param gsprecordId GSP downloadBill recordId;
	 * @param cBillID   Ʊ�ݺ�		��ʽ��yyyyMMddHHmmss
	 * @param dBillDate Ʊ������		֧������������: yyyyMMddHHmmss
	 * @param mNativeMoney 	���ҽ��		������
	 * @param cAppend 	����		�磺��ƽӰ��11231
	 */
	public PayBill(String gsprecordId, String cBillID, String dBillDate,  String mNativeMoney, String cAppend, String vendorNo, String cRemark, String cOperator){
		this();
		this.gsprecordId = gsprecordId;
		this.cBillID = cBillID;
		this.dBillDate = dBillDate;
		this.mNativeMoney = mNativeMoney;
		this.cAppend = cAppend;
		mOriginMoney = mNativeMoney;  //�����Ϊ1�� �˴�Ӳ����ָ��
		this.vendorNo = vendorNo;
		this.cRemark = cRemark;
		this.cOperator = cOperator;
	}

	/**
	 * @return the cRemark
	 */
	public String getcRemark() {
		return cRemark;
	}

	/**
	 * @param cRemark the cRemark to set
	 */
	public void setcRemark(String cRemark) {
		this.cRemark = cRemark;
	}

	/**
	 * @return the cExaminer
	 */
	public String getcExaminer() {
		return cExaminer;
	}

	/**
	 * @param cExaminer the cExaminer to set
	 */
	public void setcExaminer(String cExaminer) {
		this.cExaminer = cExaminer;
	}

	/**
	 * @return the vendorNo
	 */
	public String getVendorNo() {
		return vendorNo;
	}

	/**
	 * @param vendorNo the vendorNo to set
	 */
	public void setVendorNo(String vendorNo) {
		this.vendorNo = vendorNo;
	}

	/**
	 * @return the cApprover
	 */
	public String getcApprover() {
		return cApprover;
	}

	/**
	 * @param cApprover the cApprover to set
	 */
	public void setcApprover(String cApprover) {
		this.cApprover = cApprover;
	}

	/**
	 * @return the gsprecordId
	 */
	public String getGsprecordId() {
		return gsprecordId;
	}

	/**
	 * @param gsprecordId the gsprecordId to set
	 */
	public void setGsprecordId(String gsprecordId) {
		this.gsprecordId = gsprecordId;
	}

	/**
	 * @return the iID
	 */
	public String getiID() {
		return iID;
	}

	/**
	 * @param iID the iID to set
	 */
	public void setiID(String iID) {
		this.iID = iID;
	}

	/**
	 * @return the cBillID
	 */
	public String getcBillID() {
		return cBillID;
	}

	/**
	 * @param cBillID the cBillID to set
	 */
	public void setcBillID(String cBillID) {
		this.cBillID = cBillID;
	}

	/**
	 * @return the dBillDate
	 */
	public String getdBillDate() {
		return dBillDate;
	}

	/**
	 * @param dBillDate the dBillDate to set
	 */
	public void setdBillDate(String dBillDate) {
		this.dBillDate = dBillDate;
	}

	/**
	 * @return the mOriginMoney
	 */
	public String getmOriginMoney() {
		return mOriginMoney;
	}

	/**
	 * @param mOriginMoney the mOriginMoney to set
	 */
	public void setmOriginMoney(String mOriginMoney) {
		this.mOriginMoney = mOriginMoney;
	}

	/**
	 * @return the cPaySpeed
	 */
	public String getcPaySpeed() {
		return cPaySpeed;
	}

	/**
	 * @param cPaySpeed the cPaySpeed to set
	 */
	public void setcPaySpeed(String cPaySpeed) {
		this.cPaySpeed = cPaySpeed;
	}

	/**
	 * @return the sExchRate
	 */
	public String getsExchRate() {
		return sExchRate;
	}

	/**
	 * @param sExchRate the sExchRate to set
	 */
	public void setsExchRate(String sExchRate) {
		this.sExchRate = sExchRate;
	}

	/**
	 * @return the mNativeMoney
	 */
	public String getmNativeMoney() {
		return mNativeMoney;
	}

	/**
	 * @param mNativeMoney the mNativeMoney to set
	 */
	public void setmNativeMoney(String mNativeMoney) {
		this.mNativeMoney = mNativeMoney;
	}

	/**
	 * @return the cMoneyType
	 */
	public String getcMoneyType() {
		return cMoneyType;
	}

	/**
	 * @param cMoneyType the cMoneyType to set
	 */
	public void setcMoneyType(String cMoneyType) {
		this.cMoneyType = cMoneyType;
	}

	/**
	 * @return the cAccountFrom
	 */
	public String getcAccountFrom() {
		return cAccountFrom;
	}

	/**
	 * @param cAccountFrom the cAccountFrom to set
	 */
	public void setcAccountFrom(String cAccountFrom) {
		this.cAccountFrom = cAccountFrom;
	}

	/**
	 * @return the cAccountTo
	 */
	public String getcAccountTo() {
		return cAccountTo;
	}

	/**
	 * @param cAccountTo the cAccountTo to set
	 */
	public void setcAccountTo(String cAccountTo) {
		this.cAccountTo = cAccountTo;
	}

	/**
	 * @return the cComeFrom
	 */
	public String getcComeFrom() {
		return cComeFrom;
	}

	/**
	 * @param cComeFrom the cComeFrom to set
	 */
	public void setcComeFrom(String cComeFrom) {
		this.cComeFrom = cComeFrom;
	}

	/**
	 * @return the cOperator
	 */
	public String getcOperator() {
		return cOperator;
	}

	/**
	 * @param cOperator the cOperator to set
	 */
	public void setcOperator(String cOperator) {
		this.cOperator = cOperator;
	}

	/**
	 * @return the iBillStatus
	 */
	public String getiBillStatus() {
		return iBillStatus;
	}

	/**
	 * @param iBillStatus the iBillStatus to set
	 */
	public void setiBillStatus(String iBillStatus) {
		this.iBillStatus = iBillStatus;
	}

	/**
	 * @return the cSettle
	 */
	public String getcSettle() {
		return cSettle;
	}

	/**
	 * @param cSettle the cSettle to set
	 */
	public void setcSettle(String cSettle) {
		this.cSettle = cSettle;
	}

	/**
	 * @return the cPurpose
	 */
	public String getcPurpose() {
		return cPurpose;
	}

	/**
	 * @param cPurpose the cPurpose to set
	 */
	public void setcPurpose(String cPurpose) {
		this.cPurpose = cPurpose;
	}

	/**
	 * @return the cAppend
	 */
	public String getcAppend() {
		return cAppend;
	}

	/**
	 * @param cAppend the cAppend to set
	 */
	public void setcAppend(String cAppend) {
		this.cAppend = cAppend;
	}
	
}
