/** 
 */
package com.gewara.model.gsp;

import java.util.ArrayList;
import java.util.List;

/**
 * 应付单
 * @author Zhicheng.Peng Johnny.Resurgam@Gmail.com
 * 
 *         Apr 15, 2014 4:30:03 PM
 */
public class PayableBill {
	private String id = "";//	                  联结号 
	private String vouchertype = "P0";//          单据类型编码
	private String customercode = "";//          供应商编号
	private String code;//               单据编号 （00000000001由十位编码按顺序生成）
	private String relatevouchercode = "";//   对应单据号 
	private String date;//                 单据日期（录入的日期yyyy-mm-dd，8个字符）
	private String cust_vendor_code;//    单位编码（供应商档案的供应商编号）
	private String deptcode = "0406";//             部门编码 （业务部门编码，如上海0406）
	private String personcode = "";//           业务员编码 （员工工号，这个应该不是必填的，包场可以用上）
	private String item_classcode = "01";//       项目大类编码
	private String item_code = "C01";//           项目编码 （通过项目编码来核算如C01是电影非特别核算）
	private String digest;//				  摘要（结算单号和期间放进去）
	private String subjectcode = "22020101";//           科目编码 （例如票款：22020201）
	private String currency_name = "人民币";//       币种名称 （人民币）
	private String currency_rate = "1";//         汇率 （1）
	private String bdebitcredit = "0";//           借贷方向 （0表示贷方）
	private String natamount;//            原币金额 （结算单的金额）
	private String amount;//               本币金额 （原币金额*汇率=本币金额）
	private String paycondition_code = "";//    付款条件编码 
	private String operator = "GSP_U8";//              录入人  （以后应该的结算系统U8账号） 
	private String flag = "AP";//                   应收应付标志 
	private String quantity;//              数量 （结算票数）
	private String define1 = "";//               自定义项1 
	private String startflag = "0";//             期初标志 

	/**
	 * 
	 * @param code 单据编号 （00000000001由十位编码按顺序生成）
	 * @param date 单据日期（录入的日期yyyy-mm-dd，8个字符）
	 * @param cust_vendor_code 单位编码（供应商档案的供应商编号）
	 * @param digest 摘要（结算单号和期间放进去）
	 * @param natamount 原币金额 （结算单的金额）
	 * @param amount 本币金额 （原币金额*汇率=本币金额）
	 * @param quantity 数量 （结算票数）
	 */
	public PayableBill(String customercode, String code, String date, String cust_vendor_code  , String digest, String natamount, String amount, String quantity){
		this.customercode = customercode;
		this.code = code;
		this.date = date;
		this.cust_vendor_code = cust_vendor_code;
		this.digest = digest;
		this.natamount = natamount;
		this.amount = amount;
		this.quantity = quantity;
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

	private List<PayableBillEntity> entities = new ArrayList<PayableBillEntity>();
	
	/**
	 * 
	 * @param encust_vendor_code 单位编码（供应商档案的供应商编号）
	 * @param endigest 摘要（结算单号和期间放进去）
	 * @param ennatamount 原币金额 （结算单的金额）
	 * @param enamount 本币金额 （原币金额*汇率=本币金额）
	 */
	public void addEntity (String encustomercode, String encust_vendor_code, String endigest, String ennatamount, String enamount){
		PayableBillEntity en = new PayableBillEntity(encustomercode, encust_vendor_code, endigest, ennatamount, enamount);
		entities.add(en);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the startflag
	 */
	public String getStartflag() {
		return startflag;
	}

	/**
	 * @param startflag the startflag to set
	 */
	public void setStartflag(String startflag) {
		this.startflag = startflag;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the vouchertype
	 */
	public String getVouchertype() {
		return vouchertype;
	}

	/**
	 * @param vouchertype the vouchertype to set
	 */
	public void setVouchertype(String vouchertype) {
		this.vouchertype = vouchertype;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the relatevouchercode
	 */
	public String getRelatevouchercode() {
		return relatevouchercode;
	}

	/**
	 * @param relatevouchercode the relatevouchercode to set
	 */
	public void setRelatevouchercode(String relatevouchercode) {
		this.relatevouchercode = relatevouchercode;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
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
	 * @return the paycondition_code
	 */
	public String getPaycondition_code() {
		return paycondition_code;
	}

	/**
	 * @param paycondition_code the paycondition_code to set
	 */
	public void setPaycondition_code(String paycondition_code) {
		this.paycondition_code = paycondition_code;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the flag
	 */
	public String getFlag() {
		return flag;
	}

	/**
	 * @param flag the flag to set
	 */
	public void setFlag(String flag) {
		this.flag = flag;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
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

	/**
	 * @return the entities
	 */
	public List<PayableBillEntity> getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(List<PayableBillEntity> entities) {
		this.entities = entities;
	}

}
