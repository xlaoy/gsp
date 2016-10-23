package com.gewara.service;

import org.springframework.ui.ModelMap;

import com.gewara.model.gsp.RDBuckle;
import com.gewara.model.gsp.RDDayCount;
import com.gewara.model.gsp.RDPay;
import com.gewara.model.gsp.RDPrevent;

public interface RongDuanPreventService {

	//�۶ϻ��ܲ�ѯ
	ModelMap queryRongduanHuizong(String vendorName, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//ÿ��ϵͳ�������ͳ��
	ModelMap queryRongdunDayCount(RDDayCount dayc, ModelMap model);
	
	//ÿ��ϵͳ�������ͳ��
	ModelMap queryRongdunPayRecord(RDPay pay, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//�ۿ�����
	ModelMap queryRongdunBuckleRecord(RDBuckle buckle, Integer pageNo, Integer pageSize, String url, String isXls, ModelMap model);
	
	//Ԥ����
	String rongdunPay(RDPrevent rdp, RDPay pay, String optUser);
	
	//�޸�Ԥ�����
	String waringMfy(RDPrevent rdp, Double amount);
	
	//���
	boolean reqMoney(RDPay pay, RDPrevent rdp, Long userId);
	
	//ÿ����۶Ϲ�Ӧ��ӰƱͳ��
	void everyDayCollection();
	
	//�۶�ͳ��
	void rongduanCollection();
	
}
