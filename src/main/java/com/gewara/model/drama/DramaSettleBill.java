package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.DramaConst;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaSettleBill extends BaseObject {

	private static final long serialVersionUID = -3938434911531058787L;
	
	private Long recordid;
	//�ݳ�id
	private Long dramaid;
	//��Ӧ�����
	private String suppliercode;
	//��ʼʱ��
	private Timestamp starttime;
	//����ʱ��
	private Timestamp endtime;
	//��Ʊ����
	private Long ticketnum;
	//Ʊ����
	private Double ticketamount;
	//��Ʊ����
	private Long refundnum;
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
	//ϵͳ����Ӧ�����
	private Double syspayamount;
	//ʵ��Ӧ�����
	private Double payamount;
	//����Ʊ��
	private Long adjustnum;
	//�������
	private Double adjustamount;
	//����˵��
	private String adjustdesc;
	//״̬
	private String status;
	//�Ƿ����
	private String reqmoney;
	//����id
	private Long configid;
	//�Ƿ������һ�ڽ��㵥
	private String lastbill;
	//gsp����
	private Timestamp gspupdatetime;
	
	public DramaSettleBill() {
	}
	
	public DramaSettleBill(Long dramaid, String suppliercode, Timestamp starttime,
			Timestamp endtime, Long configid, String lastbill) {
		this.dramaid = dramaid;
		this.suppliercode = suppliercode;
		this.starttime = starttime;
		this.endtime = endtime;
		this.ticketnum = 0l;
		this.ticketamount = 0.0;
		this.refundnum = 0l;
		this.refundamount = 0.0;
		this.settleamount = 0.0;
		this.madisamount = 0.0;
		this.mbddisamount = 0.0;
		this.butieamount = 0.0;
		this.syspayamount = 0.0;
		this.payamount = 0.0;
		this.adjustnum = 0l;
		this.adjustamount = 0.0;
		this.status = DramaConst.NEW;
		this.configid = configid;
		this.reqmoney = DramaConst.N;
		this.lastbill = lastbill;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
	}
	

	public Double getButieamount() {
		return butieamount;
	}

	public void setButieamount(Double butieamount) {
		this.butieamount = butieamount;
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

	public Double getSyspayamount() {
		return syspayamount;
	}

	public void setSyspayamount(Double syspayamount) {
		this.syspayamount = syspayamount;
	}

	public String getAdjustdesc() {
		return adjustdesc;
	}

	public void setAdjustdesc(String adjustdesc) {
		this.adjustdesc = adjustdesc;
	}

	public Timestamp getGspupdatetime() {
		return gspupdatetime;
	}

	public void setGspupdatetime(Timestamp gspupdatetime) {
		this.gspupdatetime = gspupdatetime;
	}

	public Long getRecordid() {
		return recordid;
	}



	public String getLastbill() {
		return lastbill;
	}

	public void setLastbill(String lastbill) {
		this.lastbill = lastbill;
	}

	public Long getAdjustnum() {
		return adjustnum;
	}

	public void setAdjustnum(Long adjustnum) {
		this.adjustnum = adjustnum;
	}

	public Double getAdjustamount() {
		return adjustamount;
	}

	public void setAdjustamount(Double adjustamount) {
		this.adjustamount = adjustamount;
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



	public Long getTicketnum() {
		return ticketnum;
	}



	public void setTicketnum(Long ticketnum) {
		this.ticketnum = ticketnum;
	}



	public Double getTicketamount() {
		return ticketamount;
	}



	public void setTicketamount(Double ticketamount) {
		this.ticketamount = ticketamount;
	}



	public Long getRefundnum() {
		return refundnum;
	}



	public void setRefundnum(Long refundnum) {
		this.refundnum = refundnum;
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


	public Double getPayamount() {
		return payamount;
	}



	public void setPayamount(Double payamount) {
		this.payamount = payamount;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}

	

	public String getReqmoney() {
		return reqmoney;
	}

	public void setReqmoney(String reqmoney) {
		this.reqmoney = reqmoney;
	}

	public Long getConfigid() {
		return configid;
	}



	public void setConfigid(Long configid) {
		this.configid = configid;
	}

	@Override
	public Serializable realId() {
		return recordid;
	}

}
