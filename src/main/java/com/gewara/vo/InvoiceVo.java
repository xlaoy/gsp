package com.gewara.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class InvoiceVo implements Serializable {

	private static final long serialVersionUID = -3893740172910394379L;

	private String invoicecode;
	private Long settleid;
	private String status;
	private String vendername;
	private Timestamp invoicedate;
	private Timestamp start;
	private Timestamp end;
	private String invoicetype;
	
	public String getInvoicetype() {
		return invoicetype;
	}
	public void setInvoicetype(String invoicetype) {
		this.invoicetype = invoicetype;
	}
	public String getVendername() {
		return vendername;
	}
	public void setVendername(String vendername) {
		this.vendername = vendername;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInvoicecode() {
		return invoicecode;
	}
	public void setInvoicecode(String invoicecode) {
		this.invoicecode = invoicecode;
	}
	public Long getSettleid() {
		return settleid;
	}
	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}
	public Timestamp getInvoicedate() {
		return invoicedate;
	}
	public void setInvoicedate(Timestamp invoicedate) {
		this.invoicedate = invoicedate;
	}
	public Timestamp getStart() {
		return start;
	}
	public void setStart(Timestamp start) {
		this.start = start;
	}
	public Timestamp getEnd() {
		return end;
	}
	public void setEnd(Timestamp end) {
		this.end = end;
	}
	
	
}
