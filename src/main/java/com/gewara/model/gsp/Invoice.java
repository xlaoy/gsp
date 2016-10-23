package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Invoice extends BaseObject {

	private static final long serialVersionUID = 5723413514152491359L;
	
	//��Ϊ��Ʊ�ͷ�Ʊ����֮��û�б�Ȼ����ϵ������û�б��淢Ʊ����id
	
	private String invoicecode;
	//��Ʊ����
	private String invoicetype;
	//��Ʊ����
	private Timestamp invoicedate;
	//�ύ����
	private Timestamp addtime;
	//������λ����
	private String vendername;
	//˰��
	private Double taxrate;
	//˰��
	private Double taxamount;
	//��Ʊ���
	private Double invoiceamount;
	//��Ʊ״̬
	private String status;
	//��Ʊ��ע
	private String submitcontent;
	//�������㵥id����
	private String settleids;
	//ʵ�ʵֿ۽��
	private Double deductibleamount;
	//����˰���
	private Double excludetaxamount;
	//������
	private String optuser;
	//����ʱ��
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
