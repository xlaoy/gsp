/** 
 */
package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.SettleBaseEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.DiffPriceBill;
import com.gewara.model.gsp.DiffPriceOrder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.GewaOrderService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MapRow;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 5, 2013  4:32:08 PM
 */
@Service("checkBillService")
public class CheckBillServiceImpl implements CheckBillService{
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Autowired
	@Qualifier("config")
	private Config config;
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("gewaOrderService")
	private GewaOrderService gewaOrderService;
	
	
	/**
	 * shit
	 */
	@Override
	public List<CheckBill> queryCheckBill(String placeId, String status,String tag, Timestamp start, Timestamp end, int maxSize) {
		return queryCheckBill(placeId, status, start, end, maxSize, false,tag);
	}
	
	/**
	 * shit
	 */
	@Override
	public List<CheckBill> queryCheckBill(List<String> placeIds, String status,String tag, Timestamp start, Timestamp end, int maxSize){
		return queryCheckBill(placeIds, status, tag, start, end, maxSize, false);
	}
	
	/**
	 * 
	 * @param placeIds
	 * @param status
	 * @param tag
	 * @param start
	 * @param end
	 * @param maxSize
	 * @param isStatusNotEq
	 * @return
	 */
	public List<CheckBill> queryCheckBill(List<String> placeIds, String status, String tag, Timestamp start, Timestamp end, int maxSize, boolean isStatusNotEq){
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		if (CollectionUtils.isNotEmpty(placeIds))
			query.add(Restrictions.in("configId", placeIds));
		if (StringUtils.isNotBlank(status)){
			if (!isStatusNotEq)
				query.add(Restrictions.eq("status", status));
			else
				query.add(Restrictions.ne("status", status));
		}
		if (start != null)
			query.add(Restrictions.ge("start", start));
		if (end != null)
			query.add(Restrictions.le("end", end));
		if(StringUtils.isNotEmpty(tag)){
			query.add(Restrictions.eq("tag", tag));
		}
		query.addOrder(Order.asc("end"));
		return daoService.findByCriteria(query, 0, maxSize);
	}
	
	
	/**
	 * 
	 */
	@Override
	public int countingCheckBill(String placeId, String status, Timestamp start, Timestamp end,String tag) {
		return countingCheckBill(placeId, status, start, end, false,tag);
	}
	
	
	/**
	 * @param orders
	 * @return
	 */
	private List<Refundment> getRefundedOrderRefundment(List<GewaOrder> orders) {
		if (CollectionUtils.isEmpty(orders))
			return new ArrayList<Refundment>();
		List<String> tradenos = BeanUtil.getBeanPropertyList(orders, "tradeno", true);
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.eq("isSettled", "N"));
		query.add(Restrictions.in("tradeno", tradenos));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * 
	 * @param orders
	 * @return
	 */
	private List<Refundment> getChannelRefundedOrderRefundment(List<GewaOrder> orders) {
		List<Refundment> result = new ArrayList<Refundment>();
		if (CollectionUtils.isEmpty(orders)){
			return result;
		}		
		List<String> tradenos = BeanUtil.getBeanPropertyList(orders, "tradeno", true);
		List<List<String>> groupList = BeanUtil.partition(tradenos, 10000);
		for(List<String> subList : groupList){
			DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
			query.add(Restrictions.eq("isChannelSettled", "N"));
			query.add(Restrictions.in("tradeno", subList));
			List<Refundment> ref = daoService.findByCriteria(query);
			result.addAll(ref);
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param settleBill
	 * @param checkBillId
	 * @param orders
	 * @param refundments
	 */
	private void batchUpdate(SettlementBill settleBill, Long checkBillId, List<GewaOrder> orders, List<Refundment> refundments){
		
		String order = "";
		String refundment = "";
		
		if (CollectionUtils.isNotEmpty(orders)){
			List<String> orderIds = BeanUtil.getBeanPropertyList(orders, "tradeno", true);
			List<List<String>> groupIds = BeanUtil.partition(orderIds, 800);
			for(List<String> groupId : groupIds){
				if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(settleBill.getTag())){
					order = "update gewa_order set is_channel_settled = 'Y', channel_check_bill_id = ? where tradeno in " + DramaUtil.sqlinList(groupId, String.class);
				} else {
					order = "update gewa_order set is_settled = 'Y', check_bill_id = ? where tradeno in " + DramaUtil.sqlinList(groupId, String.class);
				}
				jdbcTemplate.update(order, checkBillId);
			}
		}
		
		if (CollectionUtils.isNotEmpty(refundments)){
			List<String> refundIds = BeanUtil.getBeanPropertyList(refundments, "tradeno", true);
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(settleBill.getTag())){
				refundment = "update refundment set is_channel_settled = 'Y', channel_check_bill_id = ?, channel_settle_bill_id = ? where tradeno in " + DramaUtil.sqlinList(refundIds, String.class);
			} else {
				refundment = "update refundment set is_settled = 'Y', check_bill_id = ?, settle_bill_id = ? where tradeno in " + DramaUtil.sqlinList(refundIds, String.class);
			}
			jdbcTemplate.update(refundment, checkBillId, settleBill.getRecordId());
		}
		
	}
	
	
	/**
	 * 
	 */
	@Override
	public int countingCheckBill(String placeId, String status,
			Timestamp start, Timestamp end, boolean isStatusNotEq,String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		
		if (StringUtils.isNotBlank(placeId)) {
			query.add(Restrictions.eq("configId", placeId));
		}
		
		if (StringUtils.isNotBlank(status)){
			if (!isStatusNotEq){
				query.add(Restrictions.eq("status", status));
			} else {
				query.add(Restrictions.ne("status", status));
			}
		}
		if (start != null) {
			query.add(Restrictions.ge("start", start));
		}
		if (end != null) {
			query.add(Restrictions.le("end", end));
		}
		if(StringUtils.isNotEmpty(tag)){ 
			query.add(Restrictions.eq("tag", tag));
		}
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
	}
	
	/**
	 * 
	 */
	@Override
	public List<CheckBill> queryCheckBill(String placeId, String status,
			Timestamp start, Timestamp end, int maxSize, boolean isStatusNotEq,String tag) {
		List<String> placeIds = null;
		if (StringUtils.isNotBlank(placeId)){
			placeIds = new ArrayList<String>();
			placeIds.add(placeId);
		}
		return queryCheckBill(placeIds, status,tag, start, end, maxSize);
	}
	
	/**
	 * 
	 */
	@Override
	public void settleBillStatusChanged(SettlementBill settleBill, String status) {
		 Long settlementId = settleBill.getRecordId();
		 List<CheckBill> cks = daoService.getObjectListByField(CheckBill.class, "settlementId", settlementId);
		 for (CheckBill ck : cks ) {
			 ck.setStatus(status);
		 }
		daoService.saveObjectList(cks);
	}
	
	/**
	 * 
	 */
	@Override
	public ResultCode afterAdjust(CheckBill ck, Adjustment ad) {
		if (CheckBillStatusEnums.NEW.getStatus().equals(ck.getStatus())) {
			throw new IllegalArgumentException("checkbill status:" + ck.getStatus());
		}
		Long settleId = ck.getSettlementId();
		
		ck.setAdjustTotalAmount(ck.getAdjustTotalAmount() + ad.getAmount());
		ck.setOrderTotalAmount(ck.getOrderTotalAmount() + ad.getAmount());
		String adjustReason = StringUtils.isNotBlank(ck.getAdjustReason()) ? ck.getAdjustReason() + "," + ad.getReason() : ad.getReason();
		ck.setAdjustReason(adjustReason );
		
		SettlementBill settlement = daoService.getObject(SettlementBill.class, settleId);
		
		settlement.setAdjustTotalAmount(settlement.getAdjustTotalAmount() + ad.getAmount());
		settlement.setOrderTotalAmount(settlement.getOrderTotalAmount() + ad.getAmount());
		
		daoService.saveObject(ck);
		daoService.saveObject(settlement);
		
		return ResultCode.SUCCESS;
		
		
	}
	
	protected final GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	/**
	 * zyj
	 * 通过订单号获取异价订单
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<DiffPriceOrder> getDiffPriceOrderList(List<GewaOrder> orders) {
		if (CollectionUtils.isEmpty(orders))
			return new ArrayList<DiffPriceOrder>();
		List<String> tradenos = BeanUtil.getBeanPropertyList(orders, "tradeno", true);
		DetachedCriteria query = DetachedCriteria.forClass(DiffPriceOrder.class);
		query.add(Restrictions.in("tradeno", tradenos));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * zyj
	 *  通过退票单号获取异价退票订单
	 * @param orders
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<DiffPriceOrder> getRefundDiffPriceOrderList(List<Refundment> refundments) {
		if (CollectionUtils.isEmpty(refundments))
			return new ArrayList<DiffPriceOrder>();
		List<String> tradenos = BeanUtil.getBeanPropertyList(refundments, "tradeno", true);
		DetachedCriteria query = DetachedCriteria.forClass(DiffPriceOrder.class);
		query.add(Restrictions.in("tradeno", tradenos));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * zyj
	 * 创建异价对账明细单
	 * @param settleBill
	 * @param check
	 * @param checkAgg
	 */
	private void createDiffPriceBill(SettlementBill settleBill, CheckBill check, Long diffOrderNum, Long diffRefundNum) {
		DiffPriceBill dpb = daoService.getObject(DiffPriceBill.class, check.getRecordId());
		if(dpb == null) {
			dpb = new DiffPriceBill();
		}
		dpb.setCheckBillId(check.getRecordId());
		dpb.setSettleBillId(settleBill.getRecordId());
		dpb.setStart(check.getStart());
		dpb.setEnd(check.getEnd());
		dpb.setDiffOrderNum(diffOrderNum);
		dpb.setDiffRefundNum(diffRefundNum);
		dpb.setDiffAmount(check.getDiffPriceAmount());
		daoService.saveObject(dpb);
	}
	
	/**
	 * 跟新异价订单
	 */
	private void updateDiffPriceOrder(List<DiffPriceOrder> orderList, List<DiffPriceOrder> refundList, long checkBillId, long settleBillId) {
		if(CollectionUtils.isNotEmpty(orderList)) {
			List<String> tradenos = BeanUtil.getBeanPropertyList(orderList, "tradeno", true);
			String sql = "update diff_price_order  set check_bill_id = ?, settlt_bill_id = ? where trade_no in " + DramaUtil.sqlinList(tradenos, String.class);
			jdbcTemplate.update(sql, checkBillId, settleBillId);
		}
		if(CollectionUtils.isNotEmpty(refundList)) {
			List<String> tradenos = BeanUtil.getBeanPropertyList(refundList, "tradeno", true);
			String sql = "update diff_price_order  set refund = 'Y', refund_settleid = ? where trade_no in " + DramaUtil.sqlinList(tradenos, String.class);
			jdbcTemplate.update(sql, settleBillId);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public List<CheckBill> createCheckBillBySettleBill(SettlementBill bill) {
		Timestamp startTime = bill.getStartTime();
		Timestamp endTime = DateUtil.addDay(startTime, 1);
		List<CheckBill> checkBills = new ArrayList<CheckBill>();
		while(startTime.before(bill.getEndTime())) {
			CheckBill ck = new CheckBill();
			ck.setStart(startTime);
			ck.setEnd(endTime);
			ck.setTag(bill.getTag());
			ck.setStatus(CheckBillStatusEnums.NEW.getStatus());
			ck.setRelateId(bill.getRelateId());
			ck.setConfigId(RecordIdUtils.contactRecordId(bill.getTag(), bill.getRelateId()));
			ck.setSpecial(bill.getSpecial());
			ck.setPlayType(bill.getPlayType());
			ck.setSettlementId(bill.getRecordId());
			checkBills.add(ck);
			startTime = DateUtil.addDay(startTime, 1);
			endTime = DateUtil.addDay(endTime, 1);
		}
		daoService.saveObjectList(checkBills);
		return checkBills;
	}
	
	
	/**
	 * 影票日账单计算
	 * @param settleBill
	 * @return
	 */
	@Override
	public List<CheckBill> ticketCheckBillCollection(SettlementBill bill) {
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		
		SettleConfig sc = daoService.getObject(SettleConfig.class, bill.getConfigId());
		if(sc == null) {
			return null;
		}
		Map<String, Double> difkplist = new HashMap<String, Double>();
		for(CheckBill ck : checkBills) {
			List<GewaOrder> orders =  getTicketOrder(bill, ck, sc);
			List<DiffPriceOrder> diffPriceOrderList = getDiffPriceOrderList(orders);
			Map<String, Double> diffPriceOrderMap = new HashMap<String, Double>();
			for(DiffPriceOrder dpo : diffPriceOrderList) {
				diffPriceOrderMap.put(dpo.getTradeno(), (dpo.getQuantity() * dpo.getActualPrice()));
			}
			List<Refundment> refundments = getTicketRefundedOrderRefundment(bill, ck, sc);
			List<DiffPriceOrder> diffPriceRefundList = getRefundDiffPriceOrderList(refundments);
			Map<String, Double> diffPriceRefundMap = new HashMap<String, Double>();
			for(DiffPriceOrder dpo : diffPriceRefundList) {
				diffPriceRefundMap.put(dpo.getTradeno(), (dpo.getQuantity() * dpo.getActualPrice()));
			}
			
			Long successNum = 0L;
			Double successAmount = 0.0;
			Double orderActualAmount = 0.0;
			Long diffOrderNum = 0L;
			Double billingamount = 0.0;
			double placeallowance = 0;
			for (GewaOrder order : orders) {
				successNum += order.getQuantity();
				successAmount += order.getTotalCost();
				placeallowance += order.getPlaceallowance() == null ? 0 : order.getPlaceallowance();
				double kpamount = order.getDiscount() == null ? 0 : order.getDiscount().doubleValue();
				Double dd = diffPriceOrderMap.get(order.getTradeno());
				if(dd != null) {
					orderActualAmount +=  dd.doubleValue();
					diffOrderNum += order.getQuantity();
					//当有异价的时候，如果原来的开票金额大于了异价的结算金额，取异价的结算金额
					if(kpamount > dd.doubleValue()) {
						kpamount = dd.doubleValue();
						difkplist.put(order.getTradeno(), kpamount);
					}
				} else {
					orderActualAmount += order.getTotalCost();
				}
				billingamount += kpamount;
			}
			
			Long refundTotal = 0L; 
			Double refundAmount = 0.0;
			Double refundActualAmount = 0.0;
			Long diffRefundNum = 0L;
			for (Refundment refundment : refundments){
				refundTotal += refundment.getQuantity() == null ? 0 : refundment.getQuantity();
				refundAmount += refundment.getOldSettle() - refundment.getNewSettle();
				Double dd = diffPriceRefundMap.get(refundment.getTradeno());
				if(dd != null) {
					refundActualAmount += dd.doubleValue();
					diffRefundNum += refundment.getQuantity() == null ? 0 : refundment.getQuantity();
				} else {
					refundActualAmount += refundment.getOldSettle() - refundment.getNewSettle();
				}
				GewaOrder o = daoService.getObject(GewaOrder.class, refundment.getTradeno());
				placeallowance -= o.getPlaceallowance() == null ? 0 : o.getPlaceallowance();
			}
			
			ck.setSuccTicketNumber(Double.valueOf(successNum));
			ck.setSuccTicketAmount(successAmount);
			ck.setRefundTotalNumber(Double.valueOf(refundTotal));
			ck.setRefundTotalAmount(refundAmount);
			ck.setDiffPriceNumber(Double.valueOf(diffOrderNum));
			ck.setDiffPriceAmount((successAmount - orderActualAmount) - (refundAmount - refundActualAmount));
			ck.setOrderTotalNumber(Double.valueOf(successNum - refundTotal));
			ck.setOrderTotalAmount(orderActualAmount - refundActualAmount - placeallowance);
			ck.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
			ck.setBillingamount(billingamount);
			ck.setPlaceallowance(placeallowance);
			
			if(CollectionUtils.isNotEmpty(diffPriceOrderList)) {
				createDiffPriceBill(bill, ck, diffOrderNum, diffRefundNum);
			}
			updateDiffPriceOrder(diffPriceOrderList, diffPriceRefundList, ck.getRecordId(), bill.getRecordId());
			batchUpdate(bill, ck.getRecordId(), orders, refundments);
			daoService.updateObject(ck);
		}
		//把异价的订单开票金额更新回去
		String updatakpsql = "update gewa_order set discount = ? where tradeno = ? ";
		for(String tradeno : difkplist.keySet()) {
			jdbcTemplate.update(updatakpsql, difkplist.get(tradeno), tradeno);
		}
		return checkBills;
	}
	
	/**
	 * 卖品日账单计算
	 */
	@Override
	public List<CheckBill> goodsCheckBillCollection(SettlementBill bill) {
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		
		int checkSize = checkBills.size();
		int diffDay = DateUtil.getDiffDay(bill.getStartTime(), bill.getEndTime());
		if (checkSize != diffDay){
			dbLogger.error("ccc卖品对账单数量异常， 结算单单号：" + bill.getRecordId() + ",check size:" + checkSize + "-->diffDay:" + diffDay);
			return null;
		}
		
		for(CheckBill ck : checkBills) {
			List<GewaOrder> orders =  gewaOrderService.queryGoodsOrderByPlace(ck.getStart(), ck.getEnd(), 
					ck.getRelateId(), ck.getTag(), "goods");
			List<Refundment> refundments = getRefundedOrderRefundment(orders);
			
			Long successNum = 0L;
			Double successAmount = 0.0;
			for (GewaOrder order : orders) {
				successNum += order.getQuantity();
				successAmount += order.getTotalCost();
			}
			
			Long refundTotal = 0L; 
			Double refundAmount = 0.0;
			for (Refundment refundment : refundments){
				refundTotal += refundment.getQuantity() == null ? 0 : refundment.getQuantity();
				refundAmount += refundment.getOldSettle() - refundment.getNewSettle();
			}
			
			ck.setSuccTicketNumber(Double.valueOf(successNum));
			ck.setSuccTicketAmount(successAmount);
			ck.setRefundTotalNumber(Double.valueOf(refundTotal));
			ck.setRefundTotalAmount(refundAmount);
			ck.setOrderTotalNumber(Double.valueOf(successNum - refundTotal));
			ck.setOrderTotalAmount(successAmount - refundAmount);
			ck.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
			
			batchUpdate(bill, ck.getRecordId(), orders, refundments);
			daoService.updateObject(ck);
		}
		
		return checkBills;
	}
	
	/**
	 * 生成通道费非单家的影院账单
	 */
	@Override
	public List<CheckBill> createPlaceCheckBillBySettleBill(
			SettlementBill bill) {
		List<VendorCinemaRelation> rls = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", bill.getRelateId());
		List<CheckBill> checkBills = new ArrayList<CheckBill>();
		for(VendorCinemaRelation vcr : rls) {
			CheckBill ck = new CheckBill();
			ck.setStart(bill.getStartTime());
			ck.setEnd(bill.getEndTime());
			ck.setTag(bill.getTag());
			ck.setStatus(CheckBillStatusEnums.NEW.getStatus());
			ck.setRelateId(Long.valueOf(vcr.getCinemaRecordId().split(",")[0].toString()));
			ck.setConfigId(RecordIdUtils.contactRecordId(bill.getTag(), bill.getRelateId()));
			ck.setSettlementId(bill.getRecordId());
			ck.setPlayType(bill.getPlayType());
			checkBills.add(ck);
		}
		daoService.saveObjectList(checkBills);
		return checkBills;
	}
	
	/**
	 * 院线通道费结算单计算
	 */
	@Override
	public List<CheckBill> channelCheckBillCollection(SettlementBill bill) {
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		
		for(CheckBill ck : checkBills) {
			List<GewaOrder> orders =  gewaOrderService.queryGewaOrderByChannelConfigExt(ck.getStart(), ck.getEnd(), ck.getRelateId(), null);
			List<Refundment> refundments = gewaOrderService.queryRefundmentByChannelConfigExt(ck.getStart(), ck.getEnd(), ck.getRelateId(), null);
			
			Long successNum = 0L;
			Double successAmount = 0.0;
			for (GewaOrder order : orders) {
				successNum += order.getQuantity();
				successAmount += order.getTotalCost();
			}
			
			Long refundTotal = 0L; 
			Double refundAmount = 0.0;
			for (Refundment refundment : refundments){
				refundTotal += refundment.getQuantity() == null ? 0 : refundment.getQuantity();
				refundAmount += refundment.getOldSettle() - refundment.getNewSettle();
			}
			
			ck.setSuccTicketNumber(Double.valueOf(successNum));
			ck.setSuccTicketAmount(successAmount);
			ck.setRefundTotalNumber(Double.valueOf(refundTotal));
			ck.setRefundTotalAmount(refundAmount);
			ck.setOrderTotalNumber(Double.valueOf(successNum - refundTotal));
			ck.setOrderTotalAmount(successAmount - refundAmount);
			ck.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
			
			daoService.updateObject(ck);
		}
		
		return checkBills;
	}
	
	
	/**
	 * 系统方通道费计算
	 */
	@Override
	public List<CheckBill> channelSysCheckBillCollection(SettlementBill bill, ChannelSettleConfig csc) {
		
		List<CheckBill> cklist = new ArrayList<CheckBill>();
		
		String syscode = csc.getSyscode();
		String settletime = csc.getSettletime();
		String isSettleRefund = csc.getIsSettleRefund();
		
		if(StringUtils.isEmpty(syscode)) {
			dbLogger.error("ccc系统方通道费计算异常， 结算单单号：" + bill.getRecordId() + "没有系统category");
			return cklist;
		}
		if(StringUtils.isEmpty(settletime)) {
			settletime = SettleConstant.SETTLE_BASE_ORDERTIME;
		}
		
		StringBuilder placesql = new StringBuilder("");
		placesql.append("select distinct place_id from gewa_order where order_type = 'TICKET' ");
		if(SettleConstant.SETTLE_BASE_USETIME.equals(settletime)) {
			placesql.append("and use_time >= ? and use_time < ? ");
		} else {
			placesql.append("and deal_time >= ? and deal_time < ? ");
		}
		placesql.append("and category in (" + syscode + ") ");
		
		List<Long> allplace = jdbcTemplate.queryForList(placesql.toString(), Long.class, bill.getStartTime(), bill.getEndTime());
		if(CollectionUtils.isEmpty(allplace)) {
			dbLogger.error("ccc系统方通道费计算结算单单号：" + bill.getRecordId() + "没有要计算的影院1");
			return cklist;
		}
		
		//排除部分不计算的影院
		List<VendorCinemaRelation> rls = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", bill.getRelateId());
		List<Long> placeids = new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(rls)) {
			List<String> placerds = BeanUtil.getBeanPropertyList(rls, "cinemaRecordId", true);
			for(String str : placerds) {
				Long id = Long.valueOf(str.split(",")[0]);
				placeids.add(id);
			}
		}
		allplace.removeAll(placeids);
		
		if(CollectionUtils.isEmpty(allplace)) {
			dbLogger.error("ccc系统方通道费计算结算单单号：" + bill.getRecordId() + "没有要计算的影院2");
			return cklist;
		}
		
		StringBuilder ordersql = new StringBuilder("");
		ordersql.append("select sum(quantity) as quantity, sum(total_cost) as total_cost from gewa_order where order_type = 'TICKET' ");
		if(SettleConstant.SETTLE_BASE_USETIME.equals(settletime)) {
			ordersql.append("and use_time >= ? and use_time < ? ");
		} else {
			ordersql.append("and deal_time >= ? and deal_time < ? ");
		}
		ordersql.append("and category in (" + syscode + ") ");
		ordersql.append("and place_id = ? ");
		
		StringBuilder refundsql = new StringBuilder("");
		refundsql.append("select sum(quantity) as quantity, sum(old_settle - new_settle) as total_cost from refundment where order_type = 'TICKET' ");
		if(SettleConstant.SETTLE_BASE_USETIME.equals(settletime)) {
			refundsql.append("and use_time >= ? and use_time < ? ");
		} else {
			refundsql.append("and refund_time >= ? and refund_time < ? ");
		}
		refundsql.append("and refund_category in (" + syscode + ") ");
		refundsql.append("and relate_id = ? ");
		
		for(Long placeid : allplace) {
			CheckBill ck = new CheckBill();
			ck.setStart(bill.getStartTime());
			ck.setEnd(bill.getEndTime());
			ck.setTag(bill.getTag());
			ck.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
			ck.setRelateId(placeid);
			ck.setConfigId(bill.getConfigId());
			ck.setSettlementId(bill.getRecordId());
			ck.setPlayType(bill.getPlayType());
			ck.setSpecial(bill.getSpecial());
			
			Long succNum = 0l;
			Double succAmount = 0.0;
			List<Map<String, Object>> orderlist = jdbcTemplate.queryForList(ordersql.toString(), bill.getStartTime(), bill.getEndTime(), placeid);
			for(Map<String, Object> map : orderlist) {
				MapRow row = new MapRow(map);
				succNum += row.getLong("quantity");
				succAmount += row.getDouble("total_cost");
			}
			ck.setSuccTicketNumber(Double.valueOf(succNum));
			ck.setSuccTicketAmount(succAmount);
			
			Long refundNum = 0l;
			Double refundAmount = 0.0;
			if(SettleConstant.COMM_Y.equals(isSettleRefund)) {
				List<Map<String, Object>> refundlist = jdbcTemplate.queryForList(refundsql.toString(), bill.getStartTime(), bill.getEndTime(), placeid);
				for(Map<String, Object> map : refundlist) {
					MapRow row = new MapRow(map);
					refundNum += row.getLong("quantity");
					refundAmount += row.getDouble("total_cost");
				}
			}
			ck.setRefundTotalNumber(Double.valueOf(refundNum));
			ck.setRefundTotalAmount(refundAmount);
			
			
			ck.setOrderTotalNumber(Double.valueOf(succNum - refundNum));
			ck.setOrderTotalAmount(succAmount - refundAmount);
			
			cklist.add(ck);
			
			daoService.addObject(ck);
		}
		return cklist;
	}
	
	
	/**
	 * 获取订单
	 * @param start
	 * @param end
	 * @param placeId
	 * @param placeType
	 * @param settleConfig
	 * @param category
	 * @return
	 */
	private List<GewaOrder> getTicketOrder(SettlementBill settleBill, CheckBill check, SettleConfig settleConfig) {
		List<GewaOrder> orders = new ArrayList<GewaOrder>();
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		Place place = daoService.getObject(Place.class, settleConfig.getRecordId());
		if(place == null) {
			return orders;
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.ge(timeField, check.getStart()));
		query.add(Restrictions.lt(timeField, check.getEnd()));
		query.add(Restrictions.eq("relateId", check.getRelateId()));
		query.add(Restrictions.eq("tag", check.getTag()));
		query.add(Restrictions.ne("isSettled", "Y"));
		
		//排除包场场次
		String sql = "select play_id from baochang where place_id = ? and play_time >= ? ";
		List<Long> playids = jdbcTemplate.queryForList(sql, Long.class, settleConfig.getRecordId(), check.getStart());
		for(Long playid : playids) {
			query.add(Restrictions.ne("playId", playid));
		}
		
		query = ticketSpecialQueryDC(query, settleBill, check, place);
		
		orders = daoService.findByCriteria(query);
		return orders;
	}
	
	/**
	 * 影票查询退票
	 */
	private List<Refundment> getTicketRefundedOrderRefundment(SettlementBill settleBill, CheckBill check, SettleConfig settleConfig) {
		List<Refundment> refundments = new ArrayList<Refundment>();
		Place place = daoService.getObject(Place.class, settleConfig.getRecordId());
		if(place == null) {
			return refundments;
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.ge("refundTime", check.getStart()));
		query.add(Restrictions.lt("refundTime", check.getEnd()));
		query.add(Restrictions.eq("relateId", check.getRelateId()));
		query.add(Restrictions.eq("tag", check.getTag()));
		query.add(Restrictions.ne("isSettled", "Y"));
		
		//排除包场场次
		String sql = "select play_id from baochang where place_id = ? and play_time >= ? ";
		List<Long> playids = jdbcTemplate.queryForList(sql, Long.class, settleConfig.getRecordId(), check.getStart());
		for(Long playid : playids) {
			query.add(Restrictions.ne("playId", playid));
		}
		
		query = ticketSpecialQueryDC(query, settleBill, check, place);
		refundments = daoService.findByCriteria(query);
		
		return refundments;
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	private DetachedCriteria ticketSpecialQueryDC(DetachedCriteria query, SettlementBill settleBill, CheckBill check, Place place) {
		String playType = settleBill.getPlayType();
		String special = settleBill.getSpecial();
		//场次类别
		if(SettleConstant.ZL.equals(playType)) {
			MiscUtil.appendCategoryQueryCondition(query);
		} else if(SettleConstant.XN.equals(playType)) {
			query.add(Restrictions.eq("category", "GEWA"));
		} else if(SettleConstant.GW.equals(playType)) {
			query.add(Restrictions.eq("category", "GPTBS"));
		} else {
			//对接无区分场次类型结算单
			MiscUtil.appendCategoryQueryCondition(query);
		}
		//万达
		if(SettleConstant.WDCATE.contains(place.getOpenType())) {
			if(SettleConstant.WANDA_ACTIVITY.equals(special)) {
				query.add(Restrictions.eq("special", SettleConstant.WANDA_ACTIVITY));
			} else {
				query.add(Restrictions.or(Restrictions.isNull("special"), Restrictions.ne("special", SettleConstant.WANDA_ACTIVITY)));
			}
		}
		
		return query;
	}
}