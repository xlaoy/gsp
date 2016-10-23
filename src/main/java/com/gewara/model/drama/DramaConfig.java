package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.DramaConst;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DramaConfig extends BaseObject {

	private static final long serialVersionUID = -246020510520080678L;

	private Long recordid;
	//�ݳ��汾id
	private Long dramaversionid;
	//��Ӧ�����
	private String suppliercode;
	//���㷽ʽ
	private String settlebase;
	//��������
	private String settlecycle;
	//״̬
	private String status;
	//���ʱ��
	private Timestamp addtime;
	//��Ŀ��ʼʱ��
	private Timestamp starttime;
	//��Ŀ����ʱ��
	private Timestamp endtime;
	//gsp���ʱ��
	private Timestamp gspupdatetime;
	//�������ʱ�䣨����ͨ����ʱ����ֵ��
	private Timestamp lastendtime;
	//���ݼ��ᵥʱ�䣨����ͨ����ʱ����ֵ��
	private Timestamp lastjitiendtime;
	//�ܷ����ɱ�
	private String canjiti;
	//�ۿ�����
	private String distype;
	//�ݳ�id
	private Long dramaid;
	
	public DramaConfig() {
	}
	
	public DramaConfig(Long dramaversionid, String suppliercode, Timestamp starttime, Timestamp endtime, Long dramaid, String distype) {
		this.dramaversionid = dramaversionid;
		this.suppliercode = suppliercode;
		this.starttime = starttime;
		this.endtime = endtime;
		this.dramaid = dramaid;
		this.distype = distype;
		this.gspupdatetime = DateUtil.getCurFullTimestamp();
		if(DramaConst.PERCENT.equals(distype)) {
			this.status = DramaConst.WAITFINISH;
		} else {
			this.status = DramaConst.DISCARD;
		}
	}

	public String getDistype() {
		return distype;
	}

	public void setDistype(String distype) {
		this.distype = distype;
	}

	public Long getDramaversionid() {
		return dramaversionid;
	}



	public void setDramaversionid(Long dramaversionid) {
		this.dramaversionid = dramaversionid;
	}



	public String getCanjiti() {
		return canjiti;
	}



	public void setCanjiti(String canjiti) {
		this.canjiti = canjiti;
	}



	public Timestamp getLastjitiendtime() {
		return lastjitiendtime;
	}



	public void setLastjitiendtime(Timestamp lastjitiendtime) {
		this.lastjitiendtime = lastjitiendtime;
	}



	public Timestamp getLastendtime() {
		return lastendtime;
	}



	public void setLastendtime(Timestamp lastendtime) {
		this.lastendtime = lastendtime;
	}



	public String getSuppliercode() {
		return suppliercode;
	}



	public void setSuppliercode(String suppliercode) {
		this.suppliercode = suppliercode;
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



	public String getSettlebase() {
		return settlebase;
	}



	public void setSettlebase(String settlebase) {
		this.settlebase = settlebase;
	}



	public String getSettlecycle() {
		return settlecycle;
	}



	public void setSettlecycle(String settlecycle) {
		this.settlecycle = settlecycle;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}


	public Timestamp getAddtime() {
		return addtime;
	}



	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
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
