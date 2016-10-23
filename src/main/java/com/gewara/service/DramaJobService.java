package com.gewara.service;

public interface DramaJobService {

	//ͬ���ݳ���Ŀ
	void syncDrama();
	
	//ͬ���ݳ�����
	void syncDramaConfig();
	
	//ͬ�����϶���
	void syncOnlineOrder();
	
	//ͬ�������˵�
	void syncOnlineRefund();
	
	//ͬ�����¶���
	void syncOfflineOrder();
	
	//ͬ�������˵�
	void syncOfflineRefund();
	
	//ͬ��Ʊ�񶩵�
	void syncPiaowuOrder();
	
	//ͬ��Ʊ���˵�
	void syncPiaowuRefund();
	
	//�Զ�����δ��˵�����
	void sysWaitApprovalDramaConfig();
	
	//�Զ����ͬ���������ݵ�job
	boolean checkSysDataJob();
	
	//ͬ���ݳ���Ӧ����Ϣ
	void syncDramaSupplier();
	
	//ͬ���ݳ�������Ϣ
	void syncDramaPlace();
	
	//���ɽ��㵥��
	void generateSettltBill();
	
	//ͬ���ݳ�������Ϣ
	void syncDramaPlayItem();
	
}
