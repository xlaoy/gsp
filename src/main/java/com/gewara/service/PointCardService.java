package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.gsp.PointCardOrder;
import com.gewara.model.gsp.PointCardSettleBill;

public interface PointCardService {

	List<PointCardOrder> getPointCardOrders(Long partnerid, Long cinemaid, Timestamp starttime, Timestamp endtime, String status);
	
	//同步点卡订单
	void sysPointCardOrder();
	
	//生成系统月账单
	PointCardSettleBill createPointCardMonthSettleBill(Long partnerid, Timestamp start, boolean recreate);
	
	//生成系统日账单
	List<PointCardSettleBill> createPointCardDaySettleBill(PointCardSettleBill bill);
	
	//生成影院日账单
	List<Map<String, Object>> createPointCardDayPlaceBill(List<PointCardSettleBill> daybilllist, PointCardSettleBill bill);
	
	//生成影院月账单
	void createPointCardMonthPlaceBill(PointCardSettleBill bill, List<Map<String, Object>> placelist);
	
	//获取系统结算单
	PointCardSettleBill getPointCardMonthSettleBill(Long partnerid, Timestamp start);
	
	//计算影院日账单
	void calculatePointCardDayPlaceBill(PointCardSettleBill bill);
	
	//计算系统日账单
	void calculatePointCardDaySettleBill(PointCardSettleBill bill);
	
	//计算系统月账单
	void calculatePointCardMonthSettleBill(PointCardSettleBill bill);
	
	//计算影院月账单
	void calculatePointCardPlacePlaceBill(PointCardSettleBill bill);
}
