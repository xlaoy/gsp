package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.gsp.PointCardOrder;
import com.gewara.model.gsp.PointCardSettleBill;

public interface PointCardService {

	List<PointCardOrder> getPointCardOrders(Long partnerid, Long cinemaid, Timestamp starttime, Timestamp endtime, String status);
	
	//ͬ���㿨����
	void sysPointCardOrder();
	
	//����ϵͳ���˵�
	PointCardSettleBill createPointCardMonthSettleBill(Long partnerid, Timestamp start, boolean recreate);
	
	//����ϵͳ���˵�
	List<PointCardSettleBill> createPointCardDaySettleBill(PointCardSettleBill bill);
	
	//����ӰԺ���˵�
	List<Map<String, Object>> createPointCardDayPlaceBill(List<PointCardSettleBill> daybilllist, PointCardSettleBill bill);
	
	//����ӰԺ���˵�
	void createPointCardMonthPlaceBill(PointCardSettleBill bill, List<Map<String, Object>> placelist);
	
	//��ȡϵͳ���㵥
	PointCardSettleBill getPointCardMonthSettleBill(Long partnerid, Timestamp start);
	
	//����ӰԺ���˵�
	void calculatePointCardDayPlaceBill(PointCardSettleBill bill);
	
	//����ϵͳ���˵�
	void calculatePointCardDaySettleBill(PointCardSettleBill bill);
	
	//����ϵͳ���˵�
	void calculatePointCardMonthSettleBill(PointCardSettleBill bill);
	
	//����ӰԺ���˵�
	void calculatePointCardPlacePlaceBill(PointCardSettleBill bill);
}
