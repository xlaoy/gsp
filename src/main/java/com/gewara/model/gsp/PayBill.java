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
	private String vendorNo;      //GSP 供应商编号
	
	private String iID; // (U861) 	主关键字		不传递，U8使用序列
	private String cBillID; // (U861) 	票据号		格式：yyyyMMddHHmmss
	private String dBillDate; // (U861) 	票据日期		支付单生成日期: yyyyMMddHHmmss
	private String mOriginMoney; // (U861) 	原币金额         结算金额*汇率
	private String cPaySpeed = "普通"; // (U861) 	汇款速度	输入值为:普通|加急	固定值：普通
	private String sExchRate = "1"; // (U861) 	汇率		固定值：1
	private String mNativeMoney; // (U861) 	本币金额		结算金额
	private String cMoneyType = "人民币"; // (U861) 	币种名称		固定值：人民币
	private String cAccountFrom = "31001563000050011746"; // (U861) 	支付或转出帐号		固定值：31001563000050011746
	private String cAccountTo; // (U861) 	收方帐号		不传递，U8生成
	private String cComeFrom = "录入"; // (U861) 	来源	输入值为:录入	固定值:录入
	private String cOperator = ""; // (U861) 	制单人		固定值：GSP_U8
	private String cApprover = "GSP_U8-R";             //审批人
	private String cExaminer = "GSP_U8-R";             //复核人
	private String iBillStatus = "0"; // (U861) 	支付状态	0	固定值：0
	private String cSettle = "02"; // (U861) 	结算方式编码	参照SettleStyle表cSSCode字段	固定值：02
	private String cPurpose; // (U861) 	用途编号	w	不传递
	private String cAppend; // (U861) 	附言		如：格瓦拉票款-20140501:20140601-11231
	private String cRemark; //备注
	
	private PayBill(){}
	
	/**
	 * 
	 * @param gsprecordId GSP downloadBill recordId;
	 * @param cBillID   票据号		格式：yyyyMMddHHmmss
	 * @param dBillDate 票据日期		支付单生成日期: yyyyMMddHHmmss
	 * @param mNativeMoney 	本币金额		结算金额
	 * @param cAppend 	附言		如：和平影都11231
	 */
	public PayBill(String gsprecordId, String cBillID, String dBillDate,  String mNativeMoney, String cAppend, String vendorNo, String cRemark, String cOperator){
		this();
		this.gsprecordId = gsprecordId;
		this.cBillID = cBillID;
		this.dBillDate = dBillDate;
		this.mNativeMoney = mNativeMoney;
		this.cAppend = cAppend;
		mOriginMoney = mNativeMoney;  //因汇率为1， 此处硬编码指定
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
