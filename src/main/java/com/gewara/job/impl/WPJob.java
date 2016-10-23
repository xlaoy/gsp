package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.SettleConstant;
import com.gewara.job.JobService;
import com.gewara.model.gsp.PointCardSettleBill;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.PointCardService;
import com.gewara.service.WPService;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;

public class WPJob extends JobService {

	@Autowired
	@Qualifier("WPServiceImpl")
	private WPService WPServiceImpl;
	
	@Autowired
	@Qualifier("pointCardService")
	private PointCardService pointCardService;
	
	public void autoTomerchant() {
		//����
		boolean flag = WPServiceImpl.checkWP();
		if(!flag) {
			return;
		}
		//����
		SettlementBill bill = WPServiceImpl.resetWP();
		if(bill == null) {
			return;
		}
		//����ӰԺ�µ���
		flag = WPServiceImpl.createWPPlaceMonthBill(bill);
		if(!flag) {
			return;
		}
		//����ӰԺ�յ���
		flag = WPServiceImpl.createWPPlaceDayBill(bill);
		if(!flag) {
			return;
		}
		//����ӰԺ���˵�
		WPServiceImpl.calculateWPPlaceDayBill(bill);
	}
	
	public void wpMonthFull() {
		SettlementBill bill = WPServiceImpl.queryWatingWP();
		if(bill == null) {
			return;
		}
		//����ӰԺ���˵�
		WPServiceImpl.calculateWPPlaceMonthBill(bill);
		//����΢Ʊ���˵�
		WPServiceImpl.calculateWPBill(bill);
		//����΢Ʊ���˵�
		WPServiceImpl.calculateWPDayBill(bill);
	}
	
	/**
	 * ͬ���㿨����
	 *  ÿ��2��25ִ��һ��
	 */
	public void sysPointCardOrder() {
		pointCardService.sysPointCardOrder();
	}
	
	/**
	 * ���ɵ㿨�˵�
	 *  ÿ��4��2��45��ִ��һ��
	 */
	public void createPointCardBill() {
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.getMonthFirstDay(now);
		Timestamp start = MiscUtil.addMonth(now, -1);
		//����ϵͳ���˵�
		PointCardSettleBill bill = pointCardService.createPointCardMonthSettleBill(SettleConstant.WPPARTNERID, start, true);
		//����ϵͳ���˵�
		List<PointCardSettleBill> daybilllist = null;
		if(bill != null) {
			daybilllist = pointCardService.createPointCardDaySettleBill(bill);
		} else {
			return;
		}
		//����ӰԺ���˵�
		List<Map<String, Object>> placelist = null;
		if(CollectionUtils.isNotEmpty(daybilllist)) {
			placelist = pointCardService.createPointCardDayPlaceBill(daybilllist, bill);
		} else {
			return;
		}
		//����ӰԺ���˵�
		if(CollectionUtils.isNotEmpty(placelist)) {
			pointCardService.createPointCardMonthPlaceBill(bill, placelist);
		}
	}
	
	/**
	 * ÿ��4��5��10��ִ��һ��
	 */
	public void calculatePointCardBill() {
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.getMonthFirstDay(now);
		Timestamp start = MiscUtil.addMonth(now, -1);
		PointCardSettleBill bill = pointCardService.getPointCardMonthSettleBill(SettleConstant.WPPARTNERID, start);
		//����ӰԺ���˵�
		pointCardService.calculatePointCardDayPlaceBill(bill);
		//����ϵͳ���˵�
		pointCardService.calculatePointCardDaySettleBill(bill);
		//����ϵͳ���˵�
		pointCardService.calculatePointCardMonthSettleBill(bill);
		//����ӰԺ���˵�
		pointCardService.calculatePointCardPlacePlaceBill(bill);
	}
}
