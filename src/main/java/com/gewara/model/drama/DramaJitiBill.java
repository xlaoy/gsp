package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaJitiBill extends BaseObject {
	
	private static final long serialVersionUID = -2213024752371302386L;

	private Long recordid;
	//�ݳ�id
	private Long dramaid;
	//��Ӧ�����
	private String suppliercode;
	//��ʼʱ��
	private Timestamp starttime;
	//����ʱ��
	private Timestamp endtime;
	private String month;
	//Ʊ����
	private Double ticketamount;
	//��Ʊ���
	private Double refundamount;
	//������
	private Double settleamount;
	//�̻�ȯ-�һ�ȯ�Żݽ��
	private Double madisamount;
	//�̻�ȯ-����ȯ����ֵȯ�Żݽ��
	private Double mbddisamount;
	//���ݲ���
	private Double butieamount;
	//Ӧ�����
	private Double payamount;
	//ë����
	private Double profitamount;
	//����id
	private Long configid;
	//gsp����
	private Timestamp gspupdatetime;
	
	
	public DramaJitiBill() {
	}

	public DramaJitiBill(Long dramaid, String suppliercode, Timestamp starttime,
						Timestamp endtime, Long configid) {
		this.dramaid = dramaid;
		this.suppliercode = suppliercode;
		this.starttime = starttime;
		this.endtime = endtime;
		this.ticketamount = 0.0;
		this.refundamount = 0.0;
		this.settleamount = 0.0;
		this.madisamount = 0.0;
		this.mbddisamount = 0.0;
		this.butieamount = 0.0;
		this.payamount = 0.0;
		this.profitamount = 0.0;
		this.configid = configid;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	
	public Double getButieamount() {
		return butieamount;
	}

	public void setButieamount(Double butieamount) {
		this.butieamount = butieamount;
	}

	public Long getConfigid() {
		return configid;
	}



	public void setConfigid(Long configid) {
		this.configid = configid;
	}



	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Double getProfitamount() {
		return profitamount;
	}

	public void setProfitamount(Double profitamount) {
		this.profitamount = profitamount;
	}

	public Long getRecordid() {
		return recordid;
	}



	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}



	public Long getDramaid() {
		return dramaid;
	}



	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}


	public String getSuppliercode() {
		return suppliercode;
	}



	public void setSuppliercode(String suppliercode) {
		this.suppliercode = suppliercode;
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



	public Double getTicketamount() {
		return ticketamount;
	}



	public void setTicketamount(Double ticketamount) {
		this.ticketamount = ticketamount;
	}



	public Double getRefundamount() {
		return refundamount;
	}



	public void setRefundamount(Double refundamount) {
		this.refundamount = refundamount;
	}



	public Double getSettleamount() {
		return settleamount;
	}



	public void setSettleamount(Double settleamount) {
		this.settleamount = settleamount;
	}

	public Double getMadisamount() {
		return madisamount;
	}

	public void setMadisamount(Double madisamount) {
		this.madisamount = madisamount;
	}

	public Double getMbddisamount() {
		return mbddisamount;
	}

	public void setMbddisamount(Double mbddisamount) {
		this.mbddisamount = mbddisamount;
	}

	public Double getPayamount() {
		return payamount;
	}



	public void setPayamount(Double payamount) {
		this.payamount = payamount;
	}



	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}



	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}



	@Override
	public Serializable realId() {
		return recordid;
	}

}
