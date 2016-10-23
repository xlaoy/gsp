package com.gewara.model.gsp;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class AutoreconExceptionResult extends BaseObject {

	private static final long serialVersionUID = -4898443702961853449L;

	private Long recordid;
	private Long settleid;
	private String recencode;
	private Integer num;
	//商户订单号
	private String mtradeno;
	private Double mprice;
	private Double mamount;
	//格瓦订单号
	private String gtradeno;
	private Double oprice;
	private Double oamount;
	private Double yprice;
	private Double yamount;
	private Double gprice;
	private Double gamount;
	
	public AutoreconExceptionResult() {
	}
	
	public Double getXiangcha() {
		gamount = gamount == null ? 0.0 : gamount;
		mamount = mamount == null ? 0.0 : mamount;
		return gamount - mamount;
	}

	public String getMtradeno() {
		return mtradeno;
	}

	public void setMtradeno(String mtradeno) {
		this.mtradeno = mtradeno;
	}

	public Double getMprice() {
		return mprice;
	}

	public void setMprice(Double mprice) {
		this.mprice = mprice;
	}

	public Double getMamount() {
		return mamount;
	}

	public void setMamount(Double mamount) {
		this.mamount = mamount;
	}

	public String getGtradeno() {
		return gtradeno;
	}

	public void setGtradeno(String gtradeno) {
		this.gtradeno = gtradeno;
	}

	public Double getGprice() {
		return gprice;
	}

	public void setGprice(Double gprice) {
		this.gprice = gprice;
	}

	public Double getGamount() {
		return gamount;
	}

	public void setGamount(Double gamount) {
		this.gamount = gamount;
	}

	public Double getOprice() {
		return oprice;
	}

	public void setOprice(Double oprice) {
		this.oprice = oprice;
	}

	public Double getOamount() {
		return oamount;
	}

	public void setOamount(Double oamount) {
		this.oamount = oamount;
	}

	public Double getYprice() {
		return yprice;
	}

	public void setYprice(Double yprice) {
		this.yprice = yprice;
	}

	public Double getYamount() {
		return yamount;
	}

	public void setYamount(Double yamount) {
		this.yamount = yamount;
	}

	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Long getSettleid() {
		return settleid;
	}



	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}




	public String getRecencode() {
		return recencode;
	}



	public void setRecencode(String recencode) {
		this.recencode = recencode;
	}


	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
