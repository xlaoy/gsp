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
		//检验
		boolean flag = WPServiceImpl.checkWP();
		if(!flag) {
			return;
		}
		//重置
		SettlementBill bill = WPServiceImpl.resetWP();
		if(bill == null) {
			return;
		}
		//生成影院月单据
		flag = WPServiceImpl.createWPPlaceMonthBill(bill);
		if(!flag) {
			return;
		}
		//生成影院日单据
		flag = WPServiceImpl.createWPPlaceDayBill(bill);
		if(!flag) {
			return;
		}
		//计算影院日账单
		WPServiceImpl.calculateWPPlaceDayBill(bill);
	}
	
	public void wpMonthFull() {
		SettlementBill bill = WPServiceImpl.queryWatingWP();
		if(bill == null) {
			return;
		}
		//计算影院月账单
		WPServiceImpl.calculateWPPlaceMonthBill(bill);
		//计算微票月账单
		WPServiceImpl.calculateWPBill(bill);
		//计算微票日账单
		WPServiceImpl.calculateWPDayBill(bill);
	}
	
	/**
	 * 同步点卡订单
	 *  每天2点25执行一次
	 */
	public void sysPointCardOrder() {
		pointCardService.sysPointCardOrder();
	}
	
	/**
	 * 生成点卡账单
	 *  每月4号2点45分执行一次
	 */
	public void createPointCardBill() {
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.getMonthFirstDay(now);
		Timestamp start = MiscUtil.addMonth(now, -1);
		//生成系统月账单
		PointCardSettleBill bill = pointCardService.createPointCardMonthSettleBill(SettleConstant.WPPARTNERID, start, true);
		//生成系统日账单
		List<PointCardSettleBill> daybilllist = null;
		if(bill != null) {
			daybilllist = pointCardService.createPointCardDaySettleBill(bill);
		} else {
			return;
		}
		//生成影院日账单
		List<Map<String, Object>> placelist = null;
		if(CollectionUtils.isNotEmpty(daybilllist)) {
			placelist = pointCardService.createPointCardDayPlaceBill(daybilllist, bill);
		} else {
			return;
		}
		//生成影院月账单
		if(CollectionUtils.isNotEmpty(placelist)) {
			pointCardService.createPointCardMonthPlaceBill(bill, placelist);
		}
	}
	
	/**
	 * 每月4号5点10分执行一次
	 */
	public void calculatePointCardBill() {
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.getMonthFirstDay(now);
		Timestamp start = MiscUtil.addMonth(now, -1);
		PointCardSettleBill bill = pointCardService.getPointCardMonthSettleBill(SettleConstant.WPPARTNERID, start);
		//计算影院日账单
		pointCardService.calculatePointCardDayPlaceBill(bill);
		//计算系统日账单
		pointCardService.calculatePointCardDaySettleBill(bill);
		//计算系统月账单
		pointCardService.calculatePointCardMonthSettleBill(bill);
		//计算影院月账单
		pointCardService.calculatePointCardPlacePlaceBill(bill);
	}
}
