package com.gewara.service;

import java.sql.Timestamp;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.SettlementBill;

public interface WPService {

	//��ѯ΢Ʊ���˵�
	ModelMap queryWPMonthBill(SettlementBill sb, Integer pageNo, Integer pageSize, String url, String optUser, ModelMap model);
	
	//ӰԺ���˵�
	ModelMap queryWPPlaceBill(CheckBill ck, String isXls, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//��������
	ModelMap exportWPOrder(String type, Long recordId, String duan,  Timestamp start, Timestamp end, ModelMap model);
	
	//�����˵�
	ModelMap exportWPRefund(String type, Long recordId, Timestamp start, Timestamp end, ModelMap model);
	
	//�����ļ�
	ResultCode saveToRemote(byte[] fileBuff, String fileName, String optUser);
	
	//����
	boolean checkWP();
	
	//����
	SettlementBill resetWP();
	
	//����ӰԺ�µ���
	boolean createWPPlaceMonthBill(SettlementBill bill);
	
	//����ӰԺ�յ���
	boolean createWPPlaceDayBill(SettlementBill bill);
	
	//����ӰԺ���˵�
	void calculateWPPlaceDayBill(SettlementBill bill);
	
	//��ѯҪ�����΢Ʊ�˵�
	SettlementBill queryWatingWP();
	
	//����ӰԺ���˵�
	void calculateWPPlaceMonthBill(SettlementBill bill);
	
	//����΢Ʊ���˵�
	void calculateWPBill(SettlementBill bill);
	
	//����΢Ʊ���˵�
	void calculateWPDayBill(SettlementBill bill);
}
