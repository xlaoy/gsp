package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.GewaUserMappingU8;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.WPService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebLogger;
import com.gewara.web.util.PageUtil;

@Service("WPServiceImpl")
public class WPServiceImpl implements WPService {
	
	private final GewaLogger logger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("daoService")
	protected DaoService daoService;
	
	@Autowired
	@Qualifier("config")
	protected Config config;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	private final LinkedBlockingQueue<Runnable> fulfillmentQueue;
	private final ThreadPoolExecutor fulfillmentExecutor;
	
	public WPServiceImpl() {
		fulfillmentQueue = new LinkedBlockingQueue<Runnable>();
		fulfillmentExecutor = new ThreadPoolExecutor(10, 10, 300, TimeUnit.SECONDS, fulfillmentQueue);
		fulfillmentExecutor.allowCoreThreadTimeOut(false);
	}
	

	/**
	 * 查询微票月账单
	 */
	@Override
	public ModelMap queryWPMonthBill(SettlementBill sb, Integer pageNo, Integer pageSize, String url, String optUser, ModelMap model) {
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<SettlementBill> sbList = new ArrayList<SettlementBill>();
		Long recordId = sb.getRecordId();
		String status = sb.getStatus();
		Timestamp startTime = sb.getStartTime();
		Timestamp endTime = sb.getEndTime();
		String configId = "5369,TICKET";
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(SettlementBill.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(SettlementBill.class);
		
		if(recordId != null) {
			queryCount.add(Restrictions.eq("recordId", recordId));
			queryList.add(Restrictions.eq("recordId", recordId));
		}
		if(StringUtils.isNotBlank(status)) {
			queryCount.add(Restrictions.eq("status", status));
			queryList.add(Restrictions.eq("status", status));
		}
		if(startTime != null) {
			queryCount.add(Restrictions.ge("startTime", startTime));
			queryList.add(Restrictions.ge("startTime", startTime));
		}
		if(endTime != null) {
			queryCount.add(Restrictions.le("endTime", endTime));
			queryList.add(Restrictions.le("endTime", endTime));
		}
		queryCount.add(Restrictions.eq("configId", configId));
		queryList.add(Restrictions.eq("configId", configId));
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("startTime"));
		
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			sbList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("status", status);
			m.put("startTime", startTime);
			m.put("endTime", endTime);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			
			List<GewaUserMappingU8> mappingList = daoService.getObjectListByField(GewaUserMappingU8.class, "gewaLoginName", optUser);
			if(CollectionUtils.isEmpty(mappingList)){
				//外部人员登录
				model.put("outer", "Y");
			}
			
			Place place = daoService.getObject(Place.class, configId);
			model.put("place", place);
		}
		
		model.put("sbList", sbList);
		
		return model;
	}
	
	/**
	 * 保存文件
	 */
	@Override
	public ResultCode saveToRemote(byte[] fileBuff, String fileName, String optUser) {
		String tempUrl = config.getString("uploadTempURL");
		String remoteUrl = config.getString("uploadComit");
		Map<String, String> params = new HashMap<String, String>();
		params.put("callbackUrl", "no call back");
		
		HttpResult result = HttpUtils.uploadFile(tempUrl, params, fileBuff, "noinput", fileName);
		Map tempResult = JsonUtils.readJsonToMap(result.getResponse());
		String successFile = (String) tempResult.get("successFile");
		if (StringUtils.isBlank(successFile)){
			return ResultCode.getFailure("上传到临时文件时发生错误");
		}
			
		Map<String, String> commitParams = new HashMap<String, String>();
		commitParams.put("userid", optUser);
		commitParams.put("systemid", "GSP");
		String dateStr = DateUtil.format(new Date(), "yyyyMM");
		String remoteFold = "/upload/gsp/" + dateStr + "/";
		commitParams.put("path", remoteFold);
		commitParams.put("files", successFile);
		commitParams.put("check", StringUtil.md5(successFile + remoteFold + "GSP" + optUser + "GewaUploadFile"));
		HttpResult r = HttpUtils.postUrlAsString(remoteUrl, commitParams);
		if (!"SUCCESS".equals(r.getResponse())){
			return ResultCode.getFailure(r.getResponse());
		}
			
		return ResultCode.getSuccessReturn(remoteFold + successFile);
	}
	
	/**
	 * 影院月账单
	 */
	@Override
	public ModelMap queryWPPlaceBill(CheckBill ck, String isXls, Integer pageNo, Integer pageSize, String url,
			ModelMap model) {
		
		if(StringUtils.isNotBlank(isXls)){
			pageNo = 0;
			pageSize = 10000;
		} else {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 20 : pageSize;
		}
		
		List<CheckBill> ckList = new ArrayList<CheckBill>();
		Long settleId = ck.getSettlementId();
		String configId = ck.getConfigId();
		Timestamp startTime = ck.getStart();
		Timestamp endTime = ck.getEnd();
		String special = ck.getSpecial();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(CheckBill.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(CheckBill.class);
		
		if(settleId != null) {
			queryCount.add(Restrictions.eq("settlementId", settleId));
			queryList.add(Restrictions.eq("settlementId", settleId));
		}
		if(configId != null && !"".equals(configId)) {
			Long placeId = Long.valueOf(configId.split(",")[0]);
			queryCount.add(Restrictions.eq("relateId", placeId));
			queryList.add(Restrictions.eq("relateId", placeId));
		}
		if(startTime != null) {
			queryCount.add(Restrictions.ge("start", startTime));
			queryList.add(Restrictions.ge("start", startTime));
		}
		if(endTime != null) {
			queryCount.add(Restrictions.le("end", endTime));
			queryList.add(Restrictions.le("end", endTime));
		}
		queryCount.add(Restrictions.eq("special", special));
		queryList.add(Restrictions.eq("special", special));
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.asc("start"));
		queryList.addOrder(Order.asc("relateId"));
		
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			ckList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("settlementId", settleId);
			m.put("configId", configId);
			m.put("start", startTime);
			m.put("end", endTime);
			m.put("special", special);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			
			List<String> placeIds = BeanUtil.getBeanPropertyList(ckList, "configId", true);
			Map<String, Place> pmap = daoService.getObjectMap(Place.class, placeIds);
			model.put("pmap", pmap);
		}
		
		model.put("ckList", ckList);
		
		return model;
		
	}
	
	/**
	 * 导出订单
	 */
	@Override
	public ModelMap exportWPOrder(String type, Long recordId, String duan,  Timestamp startTime, Timestamp endTime, ModelMap model) {
		SettlementBill sb = null;
		CheckBill ck = null;
		Timestamp start = null;
		Timestamp end = null;
		Long placeid = null;
		if(SettleConstant.WP.equals(type)) {
			sb = daoService.getObject(SettlementBill.class, recordId);
			if(sb == null) {
				return model;
			}
			start = sb.getStartTime();
			if("a".equals(duan)) {
				end = DateUtil.addDay(start, 10);
			} else if("b".equals(duan)) {
				start = DateUtil.addDay(start, 10);
				end = DateUtil.addDay(start, 10);
			} else if("c".equals(duan)) {
				start = DateUtil.addDay(start, 20);
				end = sb.getEndTime();
			} else {
				return model;
			}
		} else if(SettleConstant.WPDAY.equals(type)) {
			start = startTime;
			end = endTime;
		} else {
			ck = daoService.getObject(CheckBill.class, recordId);
			if(ck == null) {
				return model;
			}
			start = ck.getStart();
			end = ck.getEnd();
			placeid = ck.getRelateId();
		}
		
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select p.name, o.* from gewa_order o LEFT OUTER JOIN place p on p.place_id = o.place_id ");
		sql.append("where o.order_type = 'TICKET' ");
		sql.append("and o.category = 'WP' ");
		sql.append("and o.deal_time >= ? ");
		sql.append("and o.deal_time < ? ");
		if(SettleConstant.WP.equals(type)) {
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end);
		} else if(SettleConstant.WPDAY.equals(type)) {
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end);
		} else {
			sql.append("and o.place_id = ? ");
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end, placeid);
		}
		model.put("mapList", mapList);
		
		return model;
	}
	
	/**
	 * 导出退单
	 */
	@Override
	public ModelMap exportWPRefund(String type, Long recordId, Timestamp startTime, Timestamp endTime, ModelMap model) {
		SettlementBill sb = null;
		CheckBill ck = null;
		Timestamp start = null;
		Timestamp end = null;
		Long placeid = null;
		if(SettleConstant.WP.equals(type)) {
			sb = daoService.getObject(SettlementBill.class, recordId);
			if(sb == null) {
				return model;
			}
			start = sb.getStartTime();
			end = sb.getEndTime();
		} else if(SettleConstant.WPDAY.equals(type)) {
			start = startTime;
			end = endTime;
		} else {
			ck = daoService.getObject(CheckBill.class, recordId);
			if(ck == null) {
				return model;
			}
			start = ck.getStart();
			end = ck.getEnd();
			placeid = ck.getRelateId();
		}
		
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select p.name, r.*, o.outer_id from refundment r LEFT OUTER JOIN place p on r.relate_id = p.place_id ");
		sql.append("LEFT OUTER JOIN gewa_order o on o.tradeno = r.tradeno ");
		sql.append("where r.order_type = 'TICKET' ");
		sql.append("and r.refund_category = 'WP' ");
		sql.append("and r.refund_time >= ? ");
		sql.append("and r.refund_time < ? ");
		if(SettleConstant.WP.equals(type)) {
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end);
		} else if(SettleConstant.WPDAY.equals(type)) {
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end);
		}  else {
			sql.append("and r.relate_id = ? ");
			mapList = jdbcTemplate.queryForList(sql.toString(), start, end, placeid);
		}
		model.put("mapList", mapList);
		
		return model;
	}
	
	/**
	 * 检验
	 */
	@Override
	public boolean checkWP() {
		logger.warn("WPJob->checkWP:开始检验！");
		String configId = "5369,TICKET";
		Place p = daoService.getObject(Place.class, configId);
		if(p == null) {
			logger.warn("WPJob->checkWP:'5369,TICKET'影院不存在！");
			return false;
		}
		SettleConfig sc = daoService.getObject(SettleConfig.class, configId);
		if(sc == null) {
			logger.warn("WPJob->checkWP:'5369,TICKET'影院配置不存在！");
			return false;
		}
		if(!SettleConstant.COMM_N.equals(sc.getStatus())) {
			logger.warn("WPJob->checkWP:'5369,TICKET'影院配置不能审核！");
			return false;
		}
		logger.warn("WPJob->checkWP:检验完成！");
		return true;
	}
	
	/**
	 * 重置
	 */
	@Override
	public SettlementBill resetWP() {
		logger.warn("WPJob->resetWP:开始重置！");
		
		Timestamp endTime = DateUtil.getMonthFirstDay(DateUtil.getCurFullTimestamp());
		Timestamp startTime = MiscUtil.addMonth(endTime, -1);
		String configId = "5369,TICKET";
		SettlementBill bill = null;
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		query.add(Restrictions.eq("configId", configId));
		query.add(Restrictions.eq("startTime", startTime));
		List<SettlementBill> sbList = daoService.findByCriteria(query);
		
		//生成单据
		if(sbList.size() == 0) {
			bill = new SettlementBill(startTime, endTime, SettleConstant.TAG_SETTLEMENTBILL_TICKET, 5369l, configId);
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());//防止被影票定时任务计算
			bill.setPlayType(SettleConstant.ZL);
			daoService.addObject(bill);
			return bill;
		} else if(sbList.size() == 1) {
			logger.warn("WPJob->resetWP:startTime为" + startTime + "的单据已经有了，将要清除对应所有单据！");
			
			bill = sbList.get(0);
			bill.initAggData();
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.updateObject(bill);
			
			String delete = "delete from check_bill where settlement_id = ? ";
			jdbcTemplate.update(delete, bill.getRecordId());
			
			List<Adjustment> ads = daoService.getObjectListByField(Adjustment.class, "settleBillId", bill.getRecordId());
			for (Adjustment ad : ads){
				ad.setIsSettled("N");
				ad.setSettleBillId(null);
			}
			daoService.saveObjectList(ads);
			
			logger.warn("WPJob->resetWP:重置完成！");
			return bill;
		} else {
			//有异常单据
			return bill;
		}
	}
	
	/**
	 * 生成影院月单据
	 */
	@Override
	public boolean createWPPlaceMonthBill(SettlementBill bill) {
		logger.warn("WPJob->createWPPlaceMonthBill:开始生成影院月单据！");
		
		List<CheckBill> checkList = new ArrayList<CheckBill>();
		String sql = "select DISTINCT place_id from gewa_order where order_type = 'TICKET' and category = 'WP' and deal_time >= ? and deal_time < ?";
		List<Long> placeIds = jdbcTemplate.queryForList(sql, Long.class, bill.getStartTime(), bill.getEndTime());
		String refund = "select DISTINCT relate_id from refundment where order_type = 'TICKET' and refund_category = 'WP' and refund_time >= ? and refund_time < ? ";
		List<Long> refundplaceIds = jdbcTemplate.queryForList(refund, Long.class, bill.getStartTime(), bill.getEndTime());
		for(Long palceid : refundplaceIds) {
			if(!placeIds.contains(palceid)) {
				placeIds.add(palceid);
			}
		}
		logger.warn("WPJob->createWPPlaceMonthBill:影院数量" + placeIds.size());
		for(Long relateId : placeIds) {
			CheckBill ck = new CheckBill();
			ck.setStart(bill.getStartTime());
			ck.setEnd(bill.getEndTime());
			ck.setTag(bill.getTag());
			ck.setRelateId(relateId);
			ck.setConfigId(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, relateId));
			ck.setSettlementId(bill.getRecordId());
			ck.setSpecial(SettleConstant.MONTH);
			
			ck.setSuccTicketNumber(0.0);
			ck.setSuccTicketAmount(0.0);
			ck.setRefundTotalNumber(0.0);
			ck.setRefundTotalAmount(0.0);
			ck.setBillingamount(0.0);
			
			checkList.add(ck);
		}
		daoService.saveObjectList(checkList);
		
		logger.warn("WPJob->createWPPlaceMonthBill:完成生成影院月单据！");
		return true;
	}
	
	/**
	 * 生成影院日单据
	 */
	@Override
	public boolean createWPPlaceDayBill(SettlementBill bill) {
		logger.warn("WPJob->createWPPlaceDayBill:开始生成影院日单据！");
		
		List<CheckBill> places = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		for(CheckBill place : places) {
			List<CheckBill> checkList = new ArrayList<CheckBill>();
			Timestamp startTime = place.getStart();
			Timestamp endTime = DateUtil.addDay(startTime, 1);
			while(startTime.before(place.getEnd())) {
				CheckBill ck = new CheckBill();
				ck.setStart(startTime);
				ck.setEnd(endTime);
				ck.setTag(place.getTag());
				ck.setRelateId(place.getRelateId());
				ck.setConfigId(place.getConfigId());
				ck.setSettlementId(bill.getRecordId());
				ck.setSpecial(SettleConstant.DAY);
				ck.setStatus(CheckBillStatusEnums.NEW.getStatus());
				checkList.add(ck);
				
				ck.setSuccTicketNumber(0.0);
				ck.setSuccTicketAmount(0.0);
				ck.setRefundTotalNumber(0.0);
				ck.setRefundTotalAmount(0.0);
				ck.setBillingamount(0.0);
				
				startTime = DateUtil.addDay(startTime, 1);
				endTime = DateUtil.addDay(endTime, 1);
			}
			daoService.saveObjectList(checkList);
		}
		
		logger.warn("WPJob->createWPPlaceDayBill:完成生成影院日单据！");
		return true;
	}
	
	/**
	 * 计算影院日账单
	 */
	@Override
	public void calculateWPPlaceDayBill(final SettlementBill bill) {
		logger.warn("WPJob->calculateWPPlaceDayBill:开始计算影院日账单！");
		
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		query.add(Restrictions.eq("settlementId", bill.getRecordId()));
		query.add(Restrictions.eq("special", SettleConstant.DAY));
		final List<CheckBill> dayList = daoService.findByCriteria(query);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(CheckBill day : dayList) {
					calculate(bill, day);
				}
			}
		}).start();
		
		logger.warn("WPJob->calculateWPPlaceDayBill:影院日账单计算中...");
	}
	
	/**
	 * 
	 * @param bill
	 * @param day
	 */
	private void calculate(SettlementBill bill, CheckBill day) {
		DetachedCriteria queryorder = DetachedCriteria.forClass(GewaOrder.class);
		queryorder.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		queryorder.add(Restrictions.eq("category", "WP"));
		queryorder.add(Restrictions.eq("relateId", day.getRelateId()));
		queryorder.add(Restrictions.ge("dealTime", day.getStart()));
		queryorder.add(Restrictions.lt("dealTime", day.getEnd()));
		List<GewaOrder> orderList = daoService.findByCriteria(queryorder);
		
		Double billingamount = 0.0;
		for(GewaOrder o : orderList) {
			day.setSuccTicketNumber(day.getSuccTicketNumber() + o.getQuantity());
			day.setSuccTicketAmount(day.getSuccTicketAmount() + o.getTotalCost());
			double discount = o.getDiscount() == null ? 0 : o.getDiscount().doubleValue();
			if("paid_success".equals(o.getOrderStatus())) {
				billingamount += discount;
			} else if("paid_return".equals(o.getOrderStatus())) {
				billingamount += o.getTotalCost();
			}
		}
		
		if(CollectionUtils.isNotEmpty(orderList)) {
			List<String> tradenos = BeanUtil.getBeanPropertyList(orderList, "tradeno", true);
			String updateOrder = "update gewa_order set check_bill_id = ? where tradeno in " + sqlinList(tradenos);
			jdbcTemplate.update(updateOrder, day.getRecordId());
		}
		
		DetachedCriteria queryrefund = DetachedCriteria.forClass(Refundment.class);
		queryrefund.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		queryrefund.add(Restrictions.eq("category", "WP"));
		queryrefund.add(Restrictions.eq("relateId", day.getRelateId()));
		queryrefund.add(Restrictions.ge("refundTime", day.getStart()));
		queryrefund.add(Restrictions.lt("refundTime", day.getEnd()));
		List<Refundment> refundList = daoService.findByCriteria(queryrefund);
		
		for(Refundment r : refundList) {
			day.setRefundTotalNumber(day.getRefundTotalNumber() + r.getQuantity());
			day.setRefundTotalAmount(day.getRefundTotalAmount() + r.getOldSettle() - r.getNewSettle());
		}
		
		if(CollectionUtils.isNotEmpty(refundList)) {
			List<String> refundnos = BeanUtil.getBeanPropertyList(refundList, "tradeno", true);
			String updateRefundr = "update refundment set check_bill_id = ?, settle_bill_id = ? where tradeno in " + sqlinList(refundnos);
			jdbcTemplate.update(updateRefundr, day.getRecordId(), bill.getRecordId());
		}
		
		day.setBillingamount(billingamount - day.getRefundTotalAmount());
		day.setStatus(CheckBillStatusEnums.SETTLED.getStatus());
		daoService.updateObject(day);
	}
	
	
	/**
	 * 查询要计算的微票账单
	 */
	@Override
	public SettlementBill queryWatingWP() {
		logger.warn("WPJob->queryWatingWP:开始查询要计算的微票账单！");
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		query.add(Restrictions.eq("configId", "5369,TICKET"));
		query.add(Restrictions.eq("status", CheckBillStatusEnums.FULFILLING.getStatus()));
		List<SettlementBill> sbList = daoService.findByCriteria(query);
		
		if(CollectionUtils.isEmpty(sbList)) {
			logger.warn("WPJob->queryWatingWP:无要计算的微票账单！");
			return null;
		}
		
		SettlementBill bill = sbList.get(0);
		
		String all = "select count(1) as cunm from check_bill where special = 'DAY' and settlement_id = ?";
		String already = "select count(1) as cunm from check_bill where special = 'DAY' and status = 'SETTLED' and settlement_id = ?";
		
		Map<String, Object> allMap = jdbcTemplate.queryForMap(all, bill.getRecordId());
		Map<String, Object> alreadyMap = jdbcTemplate.queryForMap(already, bill.getRecordId());
		
		//数量不一样说明还没计算完
		Long alllong = Long.valueOf(allMap.get("cunm").toString());
		Long alreadylong = Long.valueOf(alreadyMap.get("cunm").toString());
		if(alreadylong.longValue() != alllong.longValue()) {
			logger.warn("WPJob->queryWatingWP:微票影院日账单共" + alllong + ",已完成" + alreadylong + ",影院日账单还没计算完成！");
			return null;
		}
		
		logger.warn("WPJob->queryWatingWP:完成查询要计算的微票账单！");
		
		return bill;
	}
	
	/**
	 * 计算影院月账单
	 */
	@Override
	public void calculateWPPlaceMonthBill(SettlementBill bill) {
		logger.warn("WPJob->calculateWPPlaceMonthBill:开始计算影院月账单！");
		
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		query.add(Restrictions.eq("settlementId", bill.getRecordId()));
		query.add(Restrictions.eq("special", SettleConstant.MONTH));
		List<CheckBill> monthList = daoService.findByCriteria(query);
		
		for(CheckBill month : monthList) {
			DetachedCriteria queryday = DetachedCriteria.forClass(CheckBill.class);
			queryday.add(Restrictions.eq("settlementId", bill.getRecordId()));
			queryday.add(Restrictions.eq("relateId", month.getRelateId()));
			queryday.add(Restrictions.eq("special", SettleConstant.DAY));
			List<CheckBill> dayList = daoService.findByCriteria(queryday);
			
			for(CheckBill day : dayList) {
				month.setSuccTicketNumber(month.getSuccTicketNumber() + day.getSuccTicketNumber());
				month.setSuccTicketAmount(month.getSuccTicketAmount() + day.getSuccTicketAmount());
				month.setRefundTotalNumber(month.getRefundTotalNumber() + day.getRefundTotalNumber());
				month.setRefundTotalAmount(month.getRefundTotalAmount() + day.getRefundTotalAmount());
				month.setBillingamount(month.getBillingamount() + day.getBillingamount());
			}
			
			daoService.updateObject(month);
		}
		
		logger.warn("WPJob->calculateWPPlaceMonthBill:完成计算影院月账单！");
		
	}
	
	/**
	 * 计算微票月账单
	 */
	@Override
	public void calculateWPBill(SettlementBill bill) {
		logger.warn("WPJob->calculateWPBill:开始计算微票月账单！");
		
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		query.add(Restrictions.eq("settlementId", bill.getRecordId()));
		query.add(Restrictions.eq("special", SettleConstant.MONTH));
		List<CheckBill> monthList = daoService.findByCriteria(query);
		
		Double billingamount = 0.0;
		for(CheckBill ck : monthList) {
			bill.setSuccTicketNumber(bill.getSuccTicketNumber() + ck.getSuccTicketNumber());
			bill.setSuccTicketAmount(bill.getSuccTicketAmount() + ck.getSuccTicketAmount());
			bill.setRefundTotalNumber(bill.getRefundTotalNumber() + ck.getRefundTotalNumber());
			bill.setRefundTotalAmount(bill.getRefundTotalAmount() + ck.getRefundTotalAmount());
			billingamount += ck.getBillingamount();
		}
		
		bill.setOrderTotalNumber(bill.getSuccTicketNumber() - bill.getRefundTotalNumber());
		bill.setOrderTotalAmount(bill.getSuccTicketAmount() - bill.getRefundTotalAmount());
		//借用这个字段
		bill.setApplySettleAmount(billingamount);
		
		bill.setUpdateTime(DateUtil.getCurFullTimestamp());
		bill.setLastOperator("WPJob");
		bill.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		daoService.updateObject(bill);
		
		logger.warn("WPJob->calculateWPBill:完成计算微票月账单！");
	}
	
	/**
	 * 计算微票日账单
	 */
	@Override
	public void calculateWPDayBill(SettlementBill bill) {
		logger.warn("WPJob->calculateWPDayBill:开始计算微票日账单！");
		
		List<CheckBill> checkList = new ArrayList<CheckBill>();
		Timestamp startTime = bill.getStartTime();
		Timestamp endTime = DateUtil.addDay(startTime, 1);
		while(startTime.before(bill.getEndTime())) {
			CheckBill ck = new CheckBill();
			ck.setStart(startTime);
			ck.setEnd(endTime);
			ck.setTag(bill.getTag());
			ck.setRelateId(bill.getRelateId());
			ck.setConfigId(bill.getConfigId());
			ck.setSettlementId(bill.getRecordId());
			ck.setSpecial(SettleConstant.WPDAY);
			
			DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
			query.add(Restrictions.eq("settlementId", bill.getRecordId()));
			query.add(Restrictions.eq("special", SettleConstant.DAY));
			query.add(Restrictions.eq("start", startTime));
			query.add(Restrictions.eq("end", endTime));
			List<CheckBill> placedayList = daoService.findByCriteria(query);
			
			Double sucnum = 0.0;
			Double sucamot = 0.0;
			Double rednum = 0.0;
			Double redamot = 0.0;
			Double billingamount = 0.0;
			for(CheckBill placeday : placedayList) {
				sucnum += placeday.getSuccTicketNumber();
				sucamot += placeday.getSuccTicketAmount();
				rednum += placeday.getRefundTotalNumber();
				redamot += placeday.getRefundTotalAmount();
				billingamount += placeday.getBillingamount();
			}
			
			ck.setSuccTicketNumber(sucnum);
			ck.setSuccTicketAmount(sucamot);
			ck.setRefundTotalNumber(rednum);
			ck.setRefundTotalAmount(redamot);
			ck.setOrderTotalNumber(sucnum - rednum);
			ck.setOrderTotalAmount(sucamot - redamot);
			ck.setBillingamount(billingamount);
			
			checkList.add(ck);
			
			startTime = DateUtil.addDay(startTime, 1);
			endTime = DateUtil.addDay(endTime, 1);
		}
		daoService.saveObjectList(checkList);
		
		logger.warn("WPJob->calculateWPDayBill:完成计算微票日账单！");
	}
	
	//订单号凭借
	private String sqlinList(List<String> trandos) {
		StringBuilder sb = new StringBuilder(" (");
		for(int i = 0; i < trandos.size(); i++) {
			String trando = trandos.get(i);
			if(i == (trandos.size() - 1)) {
				sb.append("'" + trando + "'");
			} else {
				sb.append("'" + trando + "',");
			}
		}
		sb.append(") ");
		return sb.toString();
	}
	
}
