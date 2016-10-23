package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Invoice extends BaseObject {

	private static final long serialVersionUID = 5723413514152491359L;
	
	//因为发票和发票配置之间没有必然的联系，所以没有保存发票配置id
	
	private String invoicecode;
	//发票类型
	private String invoicetype;
	//开票日期
	private Timestamp invoicedate;
	//提交日期
	private Timestamp addtime;
	//销货单位名称
	private String vendername;
	//税率
	private Double taxrate;
	//税额
	private Double taxamount;
	//发票金额
	private Double invoiceamount;
	//发票状态
	private String status;
	//发票备注
	private String submitcontent;
	//关联结算单id序列
	private String settleids;
	//实际抵扣金额
	private Double deductibleamount;
	//不含税金额
	private Double excludetaxamount;
	//操作人
	private String optuser;
	//更新时间
	private Timestamp updatetime;
	//
	private String dikoumonth;

	public Invoice() {
	}

	public String getDikoumonth() {
		return dikoumonth;
	}

	public void setDikoumonth(String dikoumonth) {
		this.dikoumonth = dikoumonth;
	}

	public Double getExcludetaxamount() {
		return excludetaxamount;
	}

	public void setExcludetaxamount(Double excludetaxamount) {
		this.excludetaxamount = excludetaxamount;
	}

	public String getSettleids() {
		return settleids;
	}


	public void setSettleids(String settleids) {
		this.settleids = settleids;
	}




	public Double getDeductibleamount() {
		return deductibleamount;
	}



	public void setDeductibleamount(Double deductibleamount) {
		this.deductibleamount = deductibleamount;
	}



	public String getOptuser() {
		return optuser;
	}



	public void setOptuser(String optuser) {
		this.optuser = optuser;
	}



	public Timestamp getUpdatetime() {
		return updatetime;
	}



	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}


	public String getInvoicetype() {
		return invoicetype;
	}



	public void setInvoicetype(String invoicetype) {
		this.invoicetype = invoicetype;
	}



	public Timestamp getInvoicedate() {
		return invoicedate;
	}



	public void setInvoicedate(Timestamp invoicedate) {
		this.invoicedate = invoicedate;
	}



	public Timestamp getAddtime() {
		return addtime;
	}



	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}



	public String getVendername() {
		return vendername;
	}



	public void setVendername(String vendername) {
		this.vendername = vendername;
	}



	public Double getTaxrate() {
		return taxrate;
	}



	public void setTaxrate(Double taxrate) {
		this.taxrate = taxrate;
	}



	public Double getTaxamount() {
		return taxamount;
	}



	public void setTaxamount(Double taxamount) {
		this.taxamount = taxamount;
	}



	public Double getInvoiceamount() {
		return invoiceamount;
	}



	public void setInvoiceamount(Double invoiceamount) {
		this.invoiceamount = invoiceamount;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getSubmitcontent() {
		return submitcontent;
	}

	public void setSubmitcontent(String submitcontent) {
		this.submitcontent = submitcontent;
	}

	public String getInvoicecode() {
		return invoicecode;
	}

	public void setInvoicecode(String invoicecode) {
		this.invoicecode = invoicecode;
	}

	@Override
	public Serializable realId() {
		return invoicecode;
	}

}
