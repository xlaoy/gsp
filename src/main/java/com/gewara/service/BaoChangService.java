package com.gewara.service;

import java.sql.Timestamp;
import java.util.Map;

import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;

public interface BaoChangService {

	//Ԥ����
	boolean prePay(BaoChang bc, SettleConfig sc, Place p, String optUser, String type);
	
	//����Ԥ����
	boolean rePayBaochang(BaoChang bc, String optUser);
	
	// ���
	boolean bcReqMoney(BaoChang bc, SettleConfig sc, Place p, String optUser);
	
	//��������
	void bcCalculate();
	
	//���ɰ������㵥
	Map<String, Object> createBcSettleBill(Timestamp start, Timestamp end);
	
	//���������㵥
	void fullBcSettleBill(SettlementBill bill);
	
	//���������Ʒ���㵥
	void fullBcGoodsSettleBill(SettlementBill bill);
}
