package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.job.JobService;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.BaoChangService;
import com.gewara.service.DaoService;
import com.gewara.service.DataExtractionService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;

public class BaoChangJob extends JobService {

	private GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("dataExtractionService")
	private DataExtractionService dataExtractionService;
	
	@Autowired
	@Qualifier("baoChangServiceImpl")
	private BaoChangService baoChangService;
	
	/**
	 * zyj
	 * 同步包场列表
	 */
	public void sysBaoChang() {
		dataExtractionService.sysBaoChang();
	}
	
	/**
	 * 包场数据计算
	 * 
	 */
	public void bcCalculate() {
		baoChangService.bcCalculate();
	}
	
	/**
	 * 包场结算单
	 * 
	 */
	public void bcSettleBill() {
		Timestamp curr = DateUtil.getCurFullTimestamp();
		Timestamp end = DateUtil.getMonthFirstDay(curr);
		Timestamp start = MiscUtil.addMonth(end, -1);
		final Map<String, Object> map = baoChangService.createBcSettleBill(start, end);
		
		
		new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				dbLogger.warn("bcSettleBill->包场影票结算单开始计算！ ");
				List<SettlementBill> bcbilllist = (ArrayList<SettlementBill>)map.get("bcbilllist");
				for(SettlementBill bill : bcbilllist) {
					baoChangService.fullBcSettleBill(bill);
				}
				dbLogger.warn("bcSettleBill->包场影票结算单计算结束！ ");
			}
		}).start();
		
		new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				dbLogger.warn("bcSettleBill->包场卖品结算单开始计算！ ");
				List<SettlementBill> goodsbilllist = (ArrayList<SettlementBill>)map.get("goodsbilllist");
				for(SettlementBill bill : goodsbilllist) {
					baoChangService.fullBcGoodsSettleBill(bill);
				}
				dbLogger.warn("bcSettleBill->包场卖品结算单计算结束！ ");
			}
		}).start();
		
		dbLogger.warn("bcSettleBill->包场结算单计算中！ ");
	}
	
}
