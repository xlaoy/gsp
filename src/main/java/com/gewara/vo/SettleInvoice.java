package com.gewara.vo;

import java.io.Serializable;
import java.sql.Timestamp;

public class SettleInvoice implements Serializable {
	
	private static final long serialVersionUID = -1719937485261439701L;
	
	private Long recordid;
	private Timestamp starttime;
	private Timestamp endtime;
	private String placename;
	private String vendercode;
	private double taxrate = 0.0;
	
	private double jsamount = 0.0;
	private double jskpamount = 0.0;
	private double jsbkpamount = 0.0;
	private double jsbhsamount = 0.0;
	private double jstaxamount = 0.0;
	
	private double jtamount = 0.0;
	private double jtkpamount = 0.0;
	private double jtbkpamount = 0.0;
	private double jtbhsamount = 0.0;
	private double jttaxamount = 0.0;
	
	private double tzamount = 0.0;
	private double tzkpamount = 0.0;
	private double tzbkpamount = 0.0;
	private double tzbhsamount = 0.0;
	private double tztaxamount = 0.0;

	public SettleInvoice() {
	}

	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	public Timestamp getStarttime() {
		return starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public Timestamp getEndtime() {
		return endtime;
	}

	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}

	public String getPlacename() {
		return placename;
	}

	public void setPlacename(String placename) {
		this.placename = placename;
	}

	public String getVendercode() {
		return vendercode;
	}

	public void setVendercode(String vendercode) {
		this.vendercode = vendercode;
	}

	public double getTaxrate() {
		return taxrate;
	}

	public void setTaxrate(double taxrate) {
		this.taxrate = taxrate;
	}

	public double getJsamount() {
		return jsamount;
	}

	public void setJsamount(double jsamount) {
		this.jsamount = jsamount;
	}

	public double getJskpamount() {
		return jskpamount;
	}

	public void setJskpamount(double jskpamount) {
		this.jskpamount = jskpamount;
	}

	public double getJsbkpamount() {
		return this.getJsamount() - this.getJskpamount();
	}

	public void setJsbkpamount(double jsbkpamount) {
		this.jsbkpamount = jsbkpamount;
	}

	public double getJsbhsamount() {
		if(taxrate == 0) {
			return 0;
		}
		double tmp = 1 + this.getTaxrate() / 100;
		return Double.valueOf(String.format("%.4f", this.getJskpamount() / tmp));
	}

	public void setJsbhsamount(double jsbhsamount) {
		this.jsbhsamount = jsbhsamount;
	}

	public double getJstaxamount() {
		if(taxrate == 0) {
			return 0;
		}
		double tmp = this.getTaxrate() / 100;
		return Double.valueOf(String.format("%.4f", this.getJsbhsamount() * tmp));
	}

	public void setJstaxamount(double jstaxamount) {
		this.jstaxamount = jstaxamount;
	}
	

	public double getJtamount() {
		return jtamount;
	}

	public void setJtamount(double jtamount) {
		this.jtamount = jtamount;
	}

	public double getJtkpamount() {
		return jtkpamount;
	}

	public void setJtkpamount(double jtkpamount) {
		this.jtkpamount = jtkpamount;
	}

	public double getJtbkpamount() {
		return this.getJtamount() - this.getJtkpamount();
	}

	public void setJtbkpamount(double jtbkpamount) {
		this.jtbkpamount = jtbkpamount;
	}

	public double getJtbhsamount() {
		return jtbhsamount;
	}

	public void setJtbhsamount(double jtbhsamount) {
		this.jtbhsamount = jtbhsamount;
	}

	public double getJttaxamount() {
		return jttaxamount;
	}

	public void setJttaxamount(double jttaxamount) {
		this.jttaxamount = jttaxamount;
	}

	public double getTzamount() {
		return this.getJsamount() - this.getJtamount();
	}

	public void setTzamount(double tzamount) {
		this.tzamount = tzamount;
	}

	public double getTzkpamount() {
		return this.getJskpamount() - this.getJtkpamount();
	}

	public void setTzkpamount(double tzkpamount) {
		this.tzkpamount = tzkpamount;
	}

	public double getTzbkpamount() {
		return this.getJsbkpamount() - this.getJtbkpamount();
	}

	public void setTzbkpamount(double tzbkpamount) {
		this.tzbkpamount = tzbkpamount;
	}

	public double getTzbhsamount() {
		return this.getJsbhsamount() - this.getJtbhsamount();
	}

	public void setTzbhsamount(double tzbhsamount) {
		this.tzbhsamount = tzbhsamount;
	}

	public double getTztaxamount() {
		return this.getJstaxamount() - this.getJttaxamount();
	}

	public void setTztaxamount(double tztaxamount) {
		this.tztaxamount = tztaxamount;
	}
	
}
