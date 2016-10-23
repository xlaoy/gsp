package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.OrderTypeEnums;
import com.gewara.job.JobService;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.GoodsSettleService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

/**
 * 
 * @ClassName: GoodsSettleJob  
 * @Description:卖品对账单、结算单自动任务生成  
 * @author yujun.su@gewara.com
 * @date 2014-10-9 下午5:23:54
 */
public class GoodsSettleJob extends JobService {
	private final GewaLogger logger = WebLogger.getLogger(getClass());

	@Autowired
	@Qualifier("goodsSettleService")
	private GoodsSettleService goodsSettleService;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	

	public void generateGoodsSettleBill(){
		for(int i = 0 ; i < 2 ; i ++ ){
			goodsSettleService.generateGoodsSettleBill();
		}
	}
	
	/**
	 * 卖品结算单计算
	 */
	public void doGoodsSettleFilling(){
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("status", CheckBillStatusEnums.NEW.getStatus()));
		query.add(Restrictions.eq("tag", OrderTypeEnums.GOODS.getType()));
		List<SettlementBill> settlementBills = daoService.findByCriteria(query);
		logger.warn("ExecutorService卖品对账单填充---ExecutorService start.");
		Timestamp now = DateUtil.getCurFullTimestamp();
		for(int i = 0; i < settlementBills.size(); i ++){
			SettlementBill bill = settlementBills.get(i);
			int diff = DateUtil.getDiffDay(now, bill.getEndTime());
			if(diff < 2){
				continue;
			}
			
			if (!checkCheckbillCount(bill)){
				continue;
			}
			
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.updateObject(bill);
			goodsSettleService.autoToMerchant(bill);
		}
		logger.warn("ExecutorService卖品对账单填充---ExecutorService end.");
	}
	
	private boolean checkCheckbillCount(SettlementBill bill){
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		if(CollectionUtils.isEmpty(checkBills)) {
			checkBills = checkBillService.createCheckBillBySettleBill(bill);
		}
		int checkSize = checkBills.size();
		int diffDay = DateUtil.getDiffDay(bill.getStartTime(), bill.getEndTime());
		if (checkSize != diffDay){
			String content = "卖品对账单数量异常， 结算单单号：" + bill.getRecordId() + ",check size:" + checkSize + "-->diffDay:" + diffDay;
			logger.error(content);
			GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
			return false;
		}
		return true;
	}
	
	/**
	 * 更新卖品取票时间
	 */
	public void updateGoodsTakeTime(){
		
		Timestamp startTime =  DateUtil.getMonthFirstDay(DateUtil.getCurFullTimestamp());
		Calendar cd = Calendar.getInstance();
		cd.setTimeInMillis(startTime.getTime());
		cd.add(Calendar.MONTH, -1);
		startTime =  new Timestamp(cd.getTimeInMillis());
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_GOODS));
		query.add(Restrictions.isNull("takeTime"));
		query.add(Restrictions.ge("updateTime",startTime));
		query.add(Restrictions.eq("category", "goods"));
		query.add(Restrictions.isNotNull("relateId"));
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.property("tradeno"));
		query.setProjection(pl);
		List<String> tradeNos = daoService.findByCriteria(query);
		
		logger.warn("updateGoodsTakeTime：开始时间：" + startTime + ",当前未打票的卖品订单" + tradeNos.size());
		List<List<String>> partitions = BeanUtil.partition(tradeNos, 50);
		for (List<String> p : partitions){
			goodsSettleService.updateGoodsTakeTimeByHttpRequest(p);
		}
		logger.warn("updateGoodsTakeTime：取票时间更新结束");
	}
}
