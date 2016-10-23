package com.gewara.model.gsp;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * 发票和结算单关系
 * @author user
 *
 */
public class InvoiceSettleRelate extends BaseObject {

	private static final long serialVersionUID = 690335324696498473L;
	
	private Long recordid;
	private String invoicecode;
	private Long settleid;
	private String placeid;
	//业务供应商编号
	private String vendercode;
	
	public InvoiceSettleRelate() {
	}
	
	public InvoiceSettleRelate(String invoicecode,  Long settleid, String placeid, String vendercode) {
		this.invoicecode = invoicecode;
		this.settleid = settleid;
		this.placeid = placeid;
		this.vendercode = vendercode;
	}
	
	public String getVendercode() {
		return vendercode;
	}

	public void setVendercode(String vendercode) {
		this.vendercode = vendercode;
	}

	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
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

	public String getPlaceid() {
		return placeid;
	}



	public void setPlaceid(String placeid) {
		this.placeid = placeid;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
