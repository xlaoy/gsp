package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class InvoiceConfig extends BaseObject {

	private static final long serialVersionUID = -1854794564089373165L;

	private Long recordid;
	private String placeid;
	private String vendername;
	private Double taxrate;
	private String invoicetype;
	private String taxcondition;
	private String content;
	private Timestamp updatetime;
	
	public InvoiceConfig() {
	}
	
	
	



	public String getContent() {
		return content;
	}






	public void setContent(String content) {
		this.content = content;
	}






	public Long getRecordid() {
		return recordid;
	}






	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}






	public String getPlaceid() {
		return placeid;
	}






	public void setPlaceid(String placeid) {
		this.placeid = placeid;
	}






	public String getTaxcondition() {
		return taxcondition;
	}






	public void setTaxcondition(String taxcondition) {
		this.taxcondition = taxcondition;
	}






	public Timestamp getUpdatetime() {
		return updatetime;
	}






	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
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



	public String getInvoicetype() {
		return invoicetype;
	}



	public void setInvoicetype(String invoicetype) {
		this.invoicetype = invoicetype;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
