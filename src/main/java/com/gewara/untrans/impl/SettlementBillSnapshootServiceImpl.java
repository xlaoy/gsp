/** 
 */
package com.gewara.untrans.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.SettlementBillService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.SettlementBillSnapshootService;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Dec 25, 2013  5:37:47 PM
 */
@Service("settlementBillSnapshootService")
public class SettlementBillSnapshootServiceImpl implements 
		SettlementBillSnapshootService {
	protected final GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	public SettlementBillSnapshootServiceImpl(){
	}
	
	@Override
	public void settleBillInited(SettlementBill bill) {
		/*if (!CheckBillStatusEnums.NEW.getStatus().equals(bill.getStatus())){  //only snapshoot for new bill snapshooting....
			dbLogger.warn(bill.getRecordId() + ",settle bill' status is not new :" + bill.getStatus());
		}
		bill.setStatus("sting"); //snapshooting...
		daoService.saveObject(bill);
		settleBillSnapshootExecutor.execute(new SettleBillSnapshootTask(bill));*/
	}
	
	/*private class SettleBillSnapshootTask implements Runnable{
		private SettlementBill bill;
		public SettleBillSnapshootTask(SettlementBill bill){
			this.bill = bill;
		}
		
		@Override
		public void run() {
			try {
				 SettlementBill conditionSettleBill = new SettlementBill();
				 BeanUtil.copyProperties(conditionSettleBill, bill);
				 Collection<Map<String, Object>> aggreSettlement = settlementBillService.doCollection(conditionSettleBill, true);
				 settlementBillService.doFulFilling(conditionSettleBill, aggreSettlement);
				 SettlementBillSnapshoot sbs = new SettlementBillSnapshoot();
				 sbs.setSettlementBillId(conditionSettleBill.getRecordId());
				 sbs.setOrderTotalNumber(conditionSettleBill.getOrderTotalNumber());
				 sbs.setOrderTotalAmount(conditionSettleBill.getOrderTotalAmount());
				 bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
				 daoService.saveObjectList(sbs, bill);
			} catch (Exception e) {
				String content = "µ•∫≈£∫" + bill.getRecordId() + ",“Ï≥££∫" + e.toString() ;
				GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
				bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
				daoService.saveObject(bill);
				dbLogger.error("", e);
			}
		}
	}*/
}
