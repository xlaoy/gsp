package com.gewara.service;

import java.sql.Timestamp;
import java.util.Map;

import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;

public interface BaoChangService {

	//预付款
	boolean prePay(BaoChang bc, SettleConfig sc, Place p, String optUser, String type);
	
	//重新预付款
	boolean rePayBaochang(BaoChang bc, String optUser);
	
	// 请款
	boolean bcReqMoney(BaoChang bc, SettleConfig sc, Place p, String optUser);
	
	//包场计算
	void bcCalculate();
	
	//生成包场结算单
	Map<String, Object> createBcSettleBill(Timestamp start, Timestamp end);
	
	//填充包场结算单
	void fullBcSettleBill(SettlementBill bill);
	
	//计算包场卖品结算单
	void fullBcGoodsSettleBill(SettlementBill bill);
}
