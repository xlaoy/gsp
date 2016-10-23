package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class SettleJiti extends BaseObject {

	private static final long serialVersionUID = -8757326559979183783L;

	//���㵥����
	private Long recordid;
	//ӰԺ����
	private String placename;
	//��Ӧ������
	private String vendername;
	//��Ӧ�̱���
	private String vendercode;
	//��������
	private String billtype;
	//��ﳡ��
	private String special;
	//ϵͳ����
	private String playtype;
	//��ʼʱ��
	private Timestamp starttime;
	//����ʱ��
	private Timestamp endtime;
	//����Ʊ��
	private Long num;
	//������
	private Double amount;
	//��Ʊ���
	private Double kpamount;
	//˰��
	private Double taxrate;
	//˰��
	private Double taxamount;
	//����˰���
	private Double exclutax;
	//
	private Timestamp updatetime;
	
	public SettleJiti() {
		this.num = 0l;
		this.amount = 0.0;
		this.kpamount = 0.0;
		this.taxrate = 0.0;
		this.taxamount = 0.0;
		this.exclutax = 0.0;
		this.updatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public String getPlacename() {
		return placename;
	}



	public void setPlacename(String placename) {
		this.placename = placename;
	}



	public String getVendername() {
		return vendername;
	}



	public void setVendername(String vendername) {
		this.vendername = vendername;
	}



	public String getVendercode() {
		return vendercode;
	}



	public void setVendercode(String vendercode) {
		this.vendercode = vendercode;
	}



	public String getBilltype() {
		return billtype;
	}



	public void setBilltype(String billtype) {
		this.billtype = billtype;
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



	public Long getNum() {
		return num;
	}



	public void setNum(Long num) {
		this.num = num;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public Double getKpamount() {
		return kpamount;
	}



	public void setKpamount(Double kpamount) {
		this.kpamount = kpamount;
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



	public Double getExclutax() {
		return exclutax;
	}



	public void setExclutax(Double exclutax) {
		this.exclutax = exclutax;
	}



	public String getSpecial() {
		return special;
	}



	public void setSpecial(String special) {
		this.special = special;
	}



	public String getPlaytype() {
		return playtype;
	}



	public void setPlaytype(String playtype) {
		this.playtype = playtype;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
