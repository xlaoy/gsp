/** 
 */
package com.gewara.model.gsp;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Apr 15, 2014  5:17:55 PM
 */
public class PayableBillEntity {
	private String headid = "0001";//              联结号 为任意值，如果表体为多行，则此值必须相同
	private String customercode = "";//	           供应商编号
	private String cust_vendor_code = "'";//   单位编码（供应商档案的供应商编号）
	private String deptcode = "0406";//            部门编码  （跟表头的部门编码一致）
	private String personcode = "";//          业务员编码 
	private String item_classcode = "01";//     项目大类编码
	private String item_code = "C01";//          项目编码（跟表头的项目编码一致）
	private String digest;//               摘要 （跟表头的摘要一致）
	private String subjectcode = "500101";//         科目编码 （500101）
	private String currency_name = "人民币";//     币种名称  （人民币）
	private String currency_rate = "1";//       汇率 （1）
	private String bdebitcredit = "1";//       借贷方向（1表示借方）
	private String natamount = "";//          原币金额 （跟表头一致）
	private String amount;//             本币金额 （跟表头一致）
	private String define1 = "";//            自定义项 
	
	
	/**
	 * 
	 * @param cust_vendor_code 单位编码（供应商档案的供应商编号）
	 * @param digest 摘要 （跟表头的摘要一致）
	 * @param natamount 原币金额 （跟表头一致）
	 * @param amount 本币金额 （跟表头一致）
	 */
	public PayableBillEntity(String customercode, String cust_vendor_code, String digest, String natamount,  String amount){
		this.customercode = customercode;
		this.cust_vendor_code = cust_vendor_code;
		this.digest = digest;
		this.natamount = natamount;
		this.amount = amount;
	}


	/**
	 * @return the customercode
	 */
	public String getCustomercode() {
		return customercode;
	}


	/**
	 * @param customercode the customercode to set
	 */
	public void setCustomercode(String customercode) {
		this.customercode = customercode;
	}


	/**
	 * @return the headid
	 */
	public String getHeadid() {
		return headid;
	}


	/**
	 * @param headid the headid to set
	 */
	public void setHeadid(String headid) {
		this.headid = headid;
	}


	/**
	 * @return the cust_vendor_code
	 */
	public String getCust_vendor_code() {
		return cust_vendor_code;
	}


	/**
	 * @param cust_vendor_code the cust_vendor_code to set
	 */
	public void setCust_vendor_code(String cust_vendor_code) {
		this.cust_vendor_code = cust_vendor_code;
	}


	/**
	 * @return the deptcode
	 */
	public String getDeptcode() {
		return deptcode;
	}


	/**
	 * @param deptcode the deptcode to set
	 */
	public void setDeptcode(String deptcode) {
		this.deptcode = deptcode;
	}


	/**
	 * @return the personcode
	 */
	public String getPersoncode() {
		return personcode;
	}


	/**
	 * @param personcode the personcode to set
	 */
	public void setPersoncode(String personcode) {
		this.personcode = personcode;
	}


	/**
	 * @return the item_classcode
	 */
	public String getItem_classcode() {
		return item_classcode;
	}


	/**
	 * @param item_classcode the item_classcode to set
	 */
	public void setItem_classcode(String item_classcode) {
		this.item_classcode = item_classcode;
	}


	/**
	 * @return the item_code
	 */
	public String getItem_code() {
		return item_code;
	}


	/**
	 * @param item_code the item_code to set
	 */
	public void setItem_code(String item_code) {
		this.item_code = item_code;
	}


	/**
	 * @return the digest
	 */
	public String getDigest() {
		return digest;
	}


	/**
	 * @param digest the digest to set
	 */
	public void setDigest(String digest) {
		this.digest = digest;
	}


	/**
	 * @return the subjectcode
	 */
	public String getSubjectcode() {
		return subjectcode;
	}


	/**
	 * @param subjectcode the subjectcode to set
	 */
	public void setSubjectcode(String subjectcode) {
		this.subjectcode = subjectcode;
	}


	/**
	 * @return the currency_name
	 */
	public String getCurrency_name() {
		return currency_name;
	}


	/**
	 * @param currency_name the currency_name to set
	 */
	public void setCurrency_name(String currency_name) {
		this.currency_name = currency_name;
	}


	/**
	 * @return the currency_rate
	 */
	public String getCurrency_rate() {
		return currency_rate;
	}


	/**
	 * @param currency_rate the currency_rate to set
	 */
	public void setCurrency_rate(String currency_rate) {
		this.currency_rate = currency_rate;
	}


	/**
	 * @return the bdebitcredit
	 */
	public String getBdebitcredit() {
		return bdebitcredit;
	}


	/**
	 * @param bdebitcredit the bdebitcredit to set
	 */
	public void setBdebitcredit(String bdebitcredit) {
		this.bdebitcredit = bdebitcredit;
	}


	/**
	 * @return the natamount
	 */
	public String getNatamount() {
		return natamount;
	}


	/**
	 * @param natamount the natamount to set
	 */
	public void setNatamount(String natamount) {
		this.natamount = natamount;
	}


	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}


	/**
	 * @param amount the amount to set
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}


	/**
	 * @return the define1
	 */
	public String getDefine1() {
		return define1;
	}


	/**
	 * @param define1 the define1 to set
	 */
	public void setDefine1(String define1) {
		this.define1 = define1;
	}

	
}
