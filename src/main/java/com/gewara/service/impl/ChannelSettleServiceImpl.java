package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.AdjustmentStatusEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.ChannelSettleService;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.SettleJitiService;
import com.gewara.service.SettlementBillService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;
import com.gewara.web.util.PageUtil;

/**  
 * @ClassName: ChannelSettleServiceImpl  
 * @Description: 通道费结算业务逻辑实现 
 * @author yujun.su@gewara.com
 * @date 2014-10-14 上午10:26:16    
 */
@Service("channelSettleService")
public class ChannelSettleServiceImpl implements ChannelSettleService {
	private final GewaLogger logger = WebLogger.getLogger(getClass());
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("config")
	private Config config;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;

	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	
	@Autowired
	@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	@Autowired
	@Qualifier("settleJitiServiceImpl")
	private SettleJitiService settleJitiService;
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinemas  
	 * @Description:加载所有影院信息
	 * @param model
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap loadAllCinemas(ModelMap model,String recordId) {
		if(StringUtils.isNotEmpty(recordId)){
			Place place = daoService.getObject(Place.class, recordId);
			model.put("place", place);			
		}
		return model;
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: saveChannelSettleConfig  
	 * @Description:保存通道费结算配置信息
	 * @throws
	 */
	@Override
	public ModelMap saveChannelSettleConfig(ModelMap model, ChannelSettleConfig settleConfig, String[] cinema, User user) {
		settleConfig.setSettleCycle("MONTH");
		settleConfig.setNextExeTime(settleConfig.getFirstSettleDate());
		settleConfig.setCreateTime(DateUtil.getCurFullTimestamp());
		settleConfig.setCreateUser(user.getNickname());
		settleConfig.setUpdateTime(DateUtil.getCurFullTimestamp());
		settleConfig.setUpdateUser(user.getNickname());
		settleConfig.setStatus(SettleConstant.STATUS_VALID);
		//待审核
		settleConfig.setVerifyStatus(SettleConstant.STATUS_INVALID);
		daoService.addObject(settleConfig);
		if(cinema!=null){
			VendorCinemaRelation vcr = null;
			List<VendorCinemaRelation> vcrs = new ArrayList<VendorCinemaRelation>();
			for(int i = 0 ; i<cinema.length; i++){
				vcr = new VendorCinemaRelation();
				vcr.setVendorRecordId(settleConfig.getRecordId());
				vcr.setCinemaRecordId(cinema[i]);
				vcrs.add(vcr);
			}
			daoService.saveObjectList(vcrs);
		}
		return model;
	}
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadChannelConfig  
	 * @Description:加载所有的通道费结算配置信息
	 * @param  model
	 * @return void
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap loadChannelConfig(ModelMap model,Integer pageNo, Integer pageSize,Long vendorId, String placeid, String vendorType) {
		pageNo = (pageNo == null ? 0 : pageNo);
		pageSize = (pageSize == null ? 15 : pageSize);
		
		List<ChannelSettleConfig> clist = new ArrayList<ChannelSettleConfig>();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(ChannelSettleConfig.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(ChannelSettleConfig.class);
		if(vendorId != null) {
			queryCount.add(Restrictions.eq("recordId", vendorId));
			queryList.add(Restrictions.eq("recordId", vendorId));
			ChannelSettleConfig cfg = daoService.getObject(ChannelSettleConfig.class, vendorId);
			model.put("config", cfg);
		}
		if(StringUtils.isNotBlank(placeid)) {
			List<VendorCinemaRelation> vrlist = daoService.getObjectListByField(VendorCinemaRelation.class, "cinemaRecordId", placeid);
			if(CollectionUtils.isNotEmpty(vrlist)) {
				List<Long> vendorRecordIds = BeanUtil.getBeanPropertyList(vrlist, "vendorRecordId", true);
				queryCount.add(Restrictions.in("recordId", vendorRecordIds));
				queryList.add(Restrictions.in("recordId", vendorRecordIds));
			} else {
				queryCount.add(Restrictions.eq("recordId", 0l));
				queryList.add(Restrictions.eq("recordId", 0l));
			}
		}
		if(StringUtils.isNotBlank(vendorType)) {
			queryCount.add(Restrictions.eq("vendorType", vendorType));
			queryList.add(Restrictions.eq("vendorType", vendorType));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("status"));
		queryList.addOrder(Order.desc("recordId"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			clist = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "platform/channelsettle/loadChannelConfigPage.xhtml", true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("vendorId", vendorId);
			m.put("placeId", placeid);
			m.put("vendorType", vendorType);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			
		}
		model.put("clist", clist);
		
		return model;
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: getChannelDetachedCriteria  
	 * @Description:生成查询query
	 * @return DetachedCriteria
	 * @throws
	 */
	private DetachedCriteria getChannelDetachedCriteria(){
		DetachedCriteria query = DetachedCriteria.forClass(ChannelSettleConfig.class);
		query.add(Restrictions.eq("status", SettleConstant.STATUS_VALID));
		query.add(Restrictions.eq("verifyStatus", SettleConstant.STATUS_VALID));
		return query;
	}

	/**
	 * @author yujun.su@gewara.com
	 * 删除通道费结算配置，根据ID删除
	 */
	@Override
	public void deleteChannelSettleConfig(ModelMap model, Long recordId) {
		ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class,recordId);
		csc.setStatus(SettleConstant.STATUS_INVALID);
		daoService.updateObject(csc);
		String delChannelCheckbill = "delete from check_bill where place_type=? and place_id=? and status = ?";
		String delChannelSettlebill = "delete from settlement_bill where tag=? and relate_id=? and status = ?";
		int i = jdbcTemplate.update(delChannelCheckbill, new Object[]{SettleConstant.TAG_SETTLEMENTBILL_CHANNEL,recordId,CheckBillStatusEnums.NEW.getStatus()});
		logger.warn("delete channnel checkbill,channelsettleconfig recordId:" + recordId + ",delete number:" + i);
		int j = jdbcTemplate.update(delChannelSettlebill, new Object[]{SettleConstant.TAG_SETTLEMENTBILL_CHANNEL,recordId,CheckBillStatusEnums.NEW.getStatus()});
		logger.warn("delete channnel settlementbill, channelsettleconfigconfig recordId:" + recordId + ",delete number:" + j);
	}
	



	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryChannelSettlebills  
	 * @Description:查询通道费结算单
	 * @param vendorName
	 * @param start
	 * @param end
	 * @return void
	 * @throws
	 */
	@Override
	public void queryChannelSettlebills(ModelMap model, Integer vendorId,
			Timestamp start, Timestamp end,String reqMoneyStatus, Integer pageNo, Integer pageSize) {
		pageNo = (pageNo == null ? 0 : pageNo);
		pageSize = (pageSize == null ? 20 : pageSize);
		List<SettlementBill> list = queryChannelSettlebillsByPage(vendorId,start,end,reqMoneyStatus,pageNo,pageSize);
		int count = countChannelSettlebillsByPage(vendorId,start,end,reqMoneyStatus);
		
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath()+ "/platform/channelsettle/queryChannelSettlebills.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("start", start);
		m.put("end", end);
		m.put("reqMoneyStatus", reqMoneyStatus);
		pageUtil.initPageInfo(m);
		
		DetachedCriteria query = getChannelDetachedCriteria();
		List<ChannelSettleConfig> configs = daoService.findByCriteria(query);
		
		List<String> recordIds = BeanUtil.getBeanPropertyList(list, "configId", true);
		List<Long> ids = new ArrayList<Long>();
		for(int i = 0 ; i < recordIds.size();i++){
			ids.add(Long.valueOf(recordIds.get(i).split(",")[0].toString()));
		}
		Map<Long, ChannelSettleConfig> configMap = daoService.getObjectMap(ChannelSettleConfig.class, ids);
		model.put("configMap", configMap);
		model.put("sblist", list);
		model.put("pageUtil", pageUtil);
		model.put("configs", configs);
		if(null != vendorId){
			ChannelSettleConfig channelSettleConfig = daoService.getObject(ChannelSettleConfig.class, vendorId.longValue());
			model.put("config", channelSettleConfig);
		}
	}

	/**
	 * 
	 * @param vendorId
	 * @param start
	 * @param end
	 * @param reqMoneyStatus
	 * @return
	 */
	private int countChannelSettlebillsByPage(Integer vendorId,Timestamp start, Timestamp end,String reqMoneyStatus) {
		DetachedCriteria query = getChannelSettlebillsQuery(vendorId,start,end,reqMoneyStatus);
		query.setProjection(Projections.rowCount());
		Object o = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(o.toString());
	}

	/**
	 * 
	 * @param vendorId
	 * @param start
	 * @param end
	 * @param reqMoneyStatus
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	private List<SettlementBill> queryChannelSettlebillsByPage(Integer vendorId,Timestamp start,Timestamp end,String reqMoneyStatus,Integer pageNo,Integer pageSize){
		DetachedCriteria query = getChannelSettlebillsQuery(vendorId,start,end,reqMoneyStatus);
		query.addOrder(Order.desc("startTime"));
		List<SettlementBill> bills = daoService.findByCriteria(query, pageNo * pageSize, pageSize);
		return bills;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getChannelSettlebillsQuery  
	 * @Description:
	 * @param @param vendorId
	 * @param @param start
	 * @param @param end
	 * @return void
	 * @throws  
	 */
	private DetachedCriteria getChannelSettlebillsQuery(Integer vendorId, Timestamp start,Timestamp end,String reqMoneyStatus) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("tag", "CHANNEL"));
		if (vendorId != null) {
			query.add(Restrictions.eq("relateId", Long.valueOf(vendorId.toString())));
		}
		if (start != null) {
			query.add(Restrictions.ge("startTime", start));
		}
		if (end != null) {
			query.add(Restrictions.le("endTime", end));
		}
		if (reqMoneyStatus != null) {
			query.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		return query;
	}

	

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryOrderDetailForChannel  
	 * @Description: 通道费结算订单明细查询
	 * @param model
	 * @param settleBillId
	 * @param pageNo
	 * @param pageSize
	 * @param downLoadXls
	 * @return void
	 * @throws
	 **/
	@Override
	public void queryOrderDetailForChannel(ModelMap model,
			Long settleBillId, Integer pageNo, Integer pageSize,
			String downLoadXls) {
		pageNo = StringUtils.isNotEmpty(downLoadXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotEmpty(downLoadXls) ? 10000 : pageSize == null ? 20 : pageNo;
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if(settleBillId != null){
			query.add(Restrictions.eq("recordId", settleBillId));			
		}
		SettlementBill channelSettleBill= daoService.getObject(SettlementBill.class, settleBillId);
		//如果结算单位通道费结算单，进行后续处理
		if(channelSettleBill != null && SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(channelSettleBill.getTag())){
			DetachedCriteria orderQuery = getQueryForChannleOrders(settleBillId);
			List<GewaOrder> orders = daoService.findByCriteria(orderQuery, pageNo * pageSize, pageSize);
			model.put("orders", orders);
			
			orderQuery = getQueryForChannleOrders(settleBillId);
			orderQuery.setProjection(Projections.rowCount());
			int count = Integer.valueOf(daoService.findByCriteria(orderQuery).get(0).toString());
			
			Object[] aggres = {count,channelSettleBill.getOrderTotalNumber(),channelSettleBill.getOrderTotalAmount()};
			model.put("aggres", aggres);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "/platform/channelsettle/queryOrderDetailForChannel.xhtml", true, true);
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("settleBillId", settleBillId);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getQueryForChannleOrders  
	 * @Description:
	 * @param @param settleBillId
	 * @param @return
	 * @return DetachedCriteria
	 * @throws  
	 */
	private DetachedCriteria getQueryForChannleOrders(Long settleBillId) {
		DetachedCriteria orderQuery = DetachedCriteria.forClass(GewaOrder.class);
		List<CheckBill> cks = daoService.getObjectListByField(CheckBill.class, "settlementId", settleBillId);
		List<Long> cksIds = BeanUtil.getBeanPropertyList(cks, "recordId", true);
		orderQuery.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		orderQuery.add(Restrictions.in("channelCheckBillId", cksIds));
		return orderQuery;
	}


	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getSettleTimeForSingle  
	 * @Description:
	 * @param @param c
	 * @param @return
	 * @return String
	 * @throws  
	 */
	private String getSettleTimeForSingle(ChannelSettleConfig c) {
		String timeField = "06:00:00";
		if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(c.getVendorType())){
			List<VendorCinemaRelation> vcrList = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", c.getRecordId());
			if(CollectionUtils.isEmpty(vcrList)) {
				return timeField;
			}
			VendorCinemaRelation vcr = vcrList.get(0);
			SettleConfig sc = daoService.getObject(SettleConfig.class, vcr.getCinemaRecordId());
			timeField = sc.getSettleTime() + ":00";
		} else {
			String timecut = c.getTimecut();
			if(StringUtils.isNotBlank(timecut)) {
				timeField = timecut;
			}
		}
		return timeField;
	}

	/**
	 * 通道费结算单生成
	 */
	@Override
	public void generateChannelSettleBill(List<ChannelSettleConfig> configs,String tag) {
		Map<String, Timestamp> lastSettleBillsTime = settlementBillService.getLastSettleBillsEndTimeByConfigs(tag);
		Timestamp start = null;
		Timestamp end = null;
		for (ChannelSettleConfig cfg : configs) {
			String timeFile = getSettleTimeForSingle(cfg);
			String settleCycle = cfg.getSettleCycle();
			String configId = RecordIdUtils.contactRecordId(tag, cfg.getRecordId());
			Timestamp lastEndTime = lastSettleBillsTime.get(configId) == null ? Timestamp.valueOf(DateUtil.format(cfg.getFirstSettleDate(),"yyyy-MM-dd").toString() + " " + timeFile) : lastSettleBillsTime.get(configId);
			if (lastEndTime != null) {
				start = lastEndTime;
			} else {
				start = Timestamp.valueOf(DateUtil.format(cfg.getFirstSettleDate(),"yyyy-MM-dd").toString() + " " + timeFile);
			}
			if (SettleConstant.CHANNEL_CYCLE_SETTLE_MONTH.equals(settleCycle)) {
				end = Timestamp.valueOf(DateUtil.format( DateUtil.getNextMonthFirstDay(start),"yyyy-MM-dd").toString() + " " + timeFile);
			} else if (SettleConstant.CHANNEL_CYCLE_SETTLE_DAYS.equals(settleCycle)) {
				end = DateUtil.addDay(start, cfg.getSettleDays());
			}
			Timestamp now = DateUtil.getCurTruncTimestamp();
			if(end.after(now)){
				continue;
			}
			SettlementBill initedBill = new SettlementBill(start, end, tag, cfg.getRecordId(), configId);
			initedBill.setPlayType(cfg.getVendorType());
			daoService.saveObject(initedBill);
			if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(cfg.getVendorType())){
				checkBillService.createPlaceCheckBillBySettleBill(initedBill);
			}
		}
	}

	
	/**
	 * 计算通达费结算单
	 */
	@Override
	public void autoToMerchant(SettlementBill settleBill) {
		ChangeEntry ce = new ChangeEntry(settleBill);
		doFullingChannelBill(settleBill);
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), "System","", "INIT", settleBill.getStatus(), "GEWA");
		monitorService.saveChangeLog( 0L, SettlementBill.class, settleBill.getRecordId(), ce.getChangeMap(settleBill));
		daoService.saveObject(st);
	}

	/**
	 * 
	 */
	private void doFullingChannelBill(SettlementBill bill) {
		
		ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, bill.getRelateId());
		
		if(csc == null) {
			logger.error("通道费计算异常， 结算单单号：" + bill.getRecordId() + "没有系统配置ChannelSettleConfig");
			bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
			daoService.updateObject(bill);
			return;
		}
		
		List<CheckBill> ckList = new ArrayList<CheckBill>();
		
		if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(csc.getVendorType())){//单家影院，按影票结算单提取通道费
			ckList = doCollectionForSingleCinema(bill,csc);
		} else if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(csc.getVendorType())) {//院线
			ckList = checkBillService.channelCheckBillCollection(bill);
		} else if(SettleConstant.CHANNEL_VENDOR_SYS.equals(csc.getVendorType())) {//系统
			ckList = checkBillService.channelSysCheckBillCollection(bill, csc);
		}
		
		if(CollectionUtils.isEmpty(ckList)) {
			bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
			daoService.updateObject(bill);
			return;
		}
		
		hibernateTemplate.flush();
		
		Double totalNum = 0.0;
		Double totalAmount = 0.0;
		
		for(CheckBill ck : ckList) {
			bill.setSuccTicketNumber(bill.getSuccTicketNumber() + ck.getSuccTicketNumber());
			bill.setSuccTicketAmount(bill.getSuccTicketAmount() + ck.getSuccTicketAmount());
			bill.setRefundTotalNumber(bill.getRefundTotalNumber() + ck.getRefundTotalNumber());
			bill.setRefundTotalAmount(bill.getRefundTotalAmount() + ck.getRefundTotalAmount());
			totalNum += ck.getOrderTotalNumber();
			totalAmount += ck.getOrderTotalAmount();
			
		}
		
		Double totalSettleMoney = 0d;
		Double totalPencentMoney = 0d;
		if(null != csc.getSettleMoney()){
			totalSettleMoney = totalNum * csc.getSettleMoney();
		}
		if(null != csc.getOrderPercent()){
			totalPencentMoney = totalAmount * csc.getOrderPercent()/100;
		}
		bill.setOrderTotalNumber(totalNum);
		bill.setOrderTotalAmount(Double.valueOf(String.format("%.2f", totalSettleMoney + totalPencentMoney)));
		
		if(bill.getOrderTotalAmount() == 0) {
			bill.setStatus(CheckBillStatusEnums.INVALID.getStatus());//无效
		} else {
			bill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
		}
		bill.setUpdateTime(DateUtil.getCurFullTimestamp());
		bill.setLastOperator("SYSTEM");
		daoService.updateObject(bill);
		
		//扩展字段
		SettlementBillExtend billextend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(billextend == null) {
			billextend = new SettlementBillExtend(bill.getRecordId());
		}
		billextend.setBillingamount(bill.getOrderTotalAmount());
		daoService.saveObject(billextend);
		
		settleJitiService.updateJiti(bill);
	}
	
	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: doCollectionForSingleCinema  
	 * @Description:
	 * @param bill
	 * @return void
	 * @throws  
	 */
	private List<CheckBill> doCollectionForSingleCinema(SettlementBill bill,ChannelSettleConfig channelSettleconfig) {
		List<CheckBill> checkList = new ArrayList<CheckBill>();
		VendorCinemaRelation r = null;
		List<VendorCinemaRelation> relations = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", bill.getRelateId());
		if(CollectionUtils.isNotEmpty(relations)){
			r = relations.get(0);
		}else{
			logger.error("未找到影票结算单，通道费配置 vendorRecordId：" + bill.getRelateId());
			return checkList;
		}
		//查询出来的结算单是否全部为待付款
		Timestamp startTime = bill.getStartTime();
		Timestamp endTime = bill.getEndTime();
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		query.add(Restrictions.eq("relateId", Long.valueOf(r.getCinemaRecordId().split(",")[0])));
		query.add(Restrictions.eq("playType", SettleConstant.ZL));
		query.add(Restrictions.or(Restrictions.eq("status", CheckBillStatusEnums.SETTLED.getStatus()), Restrictions.eq("status", CheckBillStatusEnums.WAITINGPAY.getStatus())));
		query.add(Restrictions.ge("startTime", startTime));
		query.add(Restrictions.le("endTime", endTime));
		query.addOrder(Order.desc("endTime"));
		List<SettlementBill> ticketBills = daoService.findByCriteria(query);
		
		if(CollectionUtils.isNotEmpty(ticketBills)){
			//如果结束时间不一致，则不进行计算
			if((endTime.compareTo(ticketBills.get(0).getEndTime())) != 0) {
				logger.warn("结算单号:" + bill.getRecordId() + "->通道费的结算单结束时间为：" + endTime + ",影票结算单的结算结束为：" + ticketBills.get(0).getEndTime() + "，不一致计算回滚！");
				return checkList;
			}
			
			List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
			Map<Timestamp, CheckBill> map = new HashMap<Timestamp, CheckBill>();
			for(CheckBill ck : checkBills) {
				map.put(ck.getStart(), ck);
			}
			
			for(SettlementBill ticketSettlebill : ticketBills){
				CheckBill checkbill = map.get(ticketSettlebill.getStartTime());
				if(checkbill == null) {
					checkbill = new CheckBill();
				}
				
				checkbill.setConfigId(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL, ticketSettlebill.getRelateId()));
				checkbill.setEnd(ticketSettlebill.getEndTime());
				checkbill.setStart(ticketSettlebill.getStartTime());
				checkbill.setRelateId(ticketSettlebill.getRelateId());
				checkbill.setSettlementId(bill.getRecordId());
				checkbill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
				checkbill.setTag(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL);
				checkbill.setPlayType(SettleConstant.CHANNEL_VENDOR_CINEMA);
				
				checkbill.setSuccTicketNumber(ticketSettlebill.getOrderTotalAmount());
				checkbill.setSuccTicketAmount(ticketSettlebill.getOrderTotalAmount());
				checkbill.setRefundTotalNumber(0.0);
				checkbill.setRefundTotalAmount(0.0);
				checkbill.setOrderTotalAmount(ticketSettlebill.getOrderTotalAmount());
				checkbill.setOrderTotalNumber(ticketSettlebill.getOrderTotalNumber());
				
				daoService.saveObject(checkbill);
				checkList.add(checkbill);
			}
		}
		return checkList;
	}

	
	
	/**
	 * 
	 */
	@Override
	public ModelMap queryCheckBillBySettleId(ModelMap model, Long settleId, Long vendorId, Integer pageNo, Integer pageSize){
		DetachedCriteria query = getCheckBillQuery(settleId,vendorId);
		List<CheckBill> cks = daoService.findByCriteria(query,pageNo * pageSize,pageSize);
		query = getCheckBillQuery(settleId,vendorId);
		query.setProjection(Projections.rowCount());
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
		model.put("channelSettleCheckBills", cks);
		model.put("count", count);
		List<Long> vendorRecordId = BeanUtil.getBeanPropertyList(cks, "relateId", true);
		Map<Long,ChannelSettleConfig> configMap = daoService.getObjectMap(ChannelSettleConfig.class, vendorRecordId);
		model.put("configMap", configMap);
		if(null != vendorId){
			ChannelSettleConfig channelSettleConfig = daoService.getObject(ChannelSettleConfig.class, vendorId);
			model.put("config",channelSettleConfig);
		}
		return model;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getCheckBillQuery  
	 * @Description:
	 * @param @return
	 * @return DetachedCriteria
	 * @throws  
	 */
	private DetachedCriteria getCheckBillQuery(Long settleId,Long vendorId) {
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_CHANNEL));
		if(null != settleId){
			query.add(Restrictions.eq("settlementId", settleId));
		}
		if(null != vendorId){
			query.add(Restrictions.eq("relateId", vendorId));
		}
		return query;
	}


	/**
	 * 更新审核状态
	 */
	@Override
	public ChannelSettleConfig verifyChannelConfig(Long recordId,String status, User user) {
		ChannelSettleConfig channelSettleConfig = daoService.getObject(ChannelSettleConfig.class, recordId);
		if(null == channelSettleConfig){
			return null;
		}
		ChangeEntry ce = new ChangeEntry(channelSettleConfig);
		channelSettleConfig.setUpdateTime(DateUtil.getCurFullTimestamp());
		channelSettleConfig.setUpdateUser(user.getNickname());
		//有效
		channelSettleConfig.setVerifyStatus(status);
		daoService.updateObject(channelSettleConfig);
		monitorService.saveChangeLog(user.getId(), ChannelSettleConfig.class, recordId, ce.getChangeMap(channelSettleConfig));
		return channelSettleConfig;
	}

	/**
	 * 
	 */
	@Override
	public Map<String, Object> loadUnVerifiedConfig(String vendorName , Integer pageSize ,	Integer pageNo) {
		DetachedCriteria query = extraUnVeriedConfigQuery(vendorName);
		List<ChannelSettleConfig> configs = daoService.findByCriteria(query, pageNo * pageSize, pageSize);
		query = extraUnVeriedConfigQuery(vendorName);
		query.setProjection(Projections.rowCount());
		Integer count = Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("configs", configs);
		result.put("count", count);
		return result;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: extraUnVeriedConfigQuery  
	 * @Description:
	 * @param @return
	 * @return DetachedCriteria
	 * @throws  
	 */
	private DetachedCriteria extraUnVeriedConfigQuery(String vendorName) {
		DetachedCriteria query = DetachedCriteria.forClass(ChannelSettleConfig.class);
		query.add(Restrictions.eq("status",SettleConstant.STATUS_VALID));
		query.add((Restrictions.eq("verifyStatus", SettleConstant.STATUS_INVALID)));
		if(StringUtils.isNotEmpty(vendorName)){
			query.add(Restrictions.like("vendorName", vendorName.trim(), MatchMode.ANYWHERE));
		}
		return query;
	}

	/**
	 * 
	 */
	@Override
	public Integer checkVendorNameIsRepeat(String vendorCode,Long recordId) {
		DetachedCriteria query = DetachedCriteria.forClass(ChannelSettleConfig.class);
		query.add(Restrictions.eq("status", SettleConstant.STATUS_VALID));
		query.add(Restrictions.eq("vendorCode", vendorCode.trim()));
		if(null != recordId){
			query.add(Restrictions.ne("recordId", recordId));
		}
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
	}

	/**
	 * 
	 */
	@Override
	public List<ChannelSettleConfig> queryVendorByName(String inputValue) {
		DetachedCriteria query = DetachedCriteria.forClass(ChannelSettleConfig.class);
		if(StringUtils.isNotEmpty(inputValue)){
			query.add(Restrictions.like("vendorName", inputValue.trim(),MatchMode.ANYWHERE));			
		}
		return daoService.findByCriteria(query);
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadConfigAndCinemaById  
	 * @Description:根据通道费结算配置 加载 配置以及 关联的影院
	 * @param recordId
	 * @return Map<String,Object>
	 */
	@Override
	public Map<String, Object> loadConfigAndCinemaById(Long recordId) {
		Map<String,Object> result = new HashMap<String, Object>();
		if(null != recordId){
			ChannelSettleConfig channelConfig = daoService.getObject(ChannelSettleConfig.class, recordId);
			List<String> cinemaIds = new ArrayList<String>();
			List<Place> places = new ArrayList<Place>();
			if(null != channelConfig){
				List<VendorCinemaRelation> rls = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", channelConfig.getRecordId());
				if(CollectionUtils.isNotEmpty(rls)){
					cinemaIds = BeanUtil.getBeanPropertyList(rls, "cinemaRecordId", true);
					places = daoService.getObjectList(Place.class, cinemaIds);
				}
			}
			result.put("config", channelConfig);
			result.put("places", places);
		}
		return result;
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: modifyChannelSettleConfig  
	 * @Description:通道费结算配置修改
	 * @param  recordId
	 * @param  vendorName
	 * @param  vendorCode
	 * @return void
	 */
	@Override
	public void modifyChannelSettleConfig(ChannelSettleConfig newconfig, ChannelSettleConfig oldconfig, String[] cinema, String username) {
		
		Long recordId = oldconfig.getRecordId();
		String vendertype = oldconfig.getVendorType();
		
		oldconfig.setVendorCode(newconfig.getVendorCode());
		oldconfig.setVendorName(newconfig.getVendorName());
		oldconfig.setPjtcode(newconfig.getPjtcode());
		oldconfig.setSettleMoney(newconfig.getSettleMoney());
		oldconfig.setOrderPercent(newconfig.getOrderPercent());
		oldconfig.setIsSettleRefund(newconfig.getIsSettleRefund());
		oldconfig.setSettletime(newconfig.getSettletime());
		oldconfig.setVerifyStatus(SettleConstant.STATUS_INVALID);
		
		if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(vendertype)) {
			oldconfig.setTimecut(newconfig.getTimecut());
		} else if(SettleConstant.CHANNEL_VENDOR_SYS.equals(vendertype)) {
			oldconfig.setTimecut(newconfig.getTimecut());
			oldconfig.setSyscode(newconfig.getSyscode());
			String update = "delete from vendor_cinema_relation where vendor_recordid = ? ";
			int k = jdbcTemplate.update(update, recordId);
			logger.warn("删除通道配置 [" + oldconfig.getVendorName() + "],删除映射关系" + k + "条");
			if(cinema!=null){
				VendorCinemaRelation vcr = null;
				List<VendorCinemaRelation> vcrs = new ArrayList<VendorCinemaRelation>();
				for(int i = 0 ; i<cinema.length; i++){
					vcr = new VendorCinemaRelation();
					vcr.setVendorRecordId(recordId);
					vcr.setCinemaRecordId(cinema[i]);
					vcrs.add(vcr);
				}
				daoService.saveObjectList(vcrs);
			}
		}
		daoService.updateObject(oldconfig);	
		
		logger.warn("结算配置修改,recordId="+recordId);
		String delChannelCheckbill = "delete from check_bill where place_type=? and place_id=? and status = ?";
		String delChannelSettlebill = "delete from settlement_bill where tag=? and relate_id=? and status = ?";
		int i = jdbcTemplate.update(delChannelCheckbill, new Object[]{SettleConstant.TAG_SETTLEMENTBILL_CHANNEL,recordId,CheckBillStatusEnums.NEW.getStatus()});
		logger.warn("delete channnel checkbill,channelsettleconfig recordId:" + recordId + ",delete number:" + i);
		int j = jdbcTemplate.update(delChannelSettlebill, new Object[]{SettleConstant.TAG_SETTLEMENTBILL_CHANNEL,recordId,CheckBillStatusEnums.NEW.getStatus()});
		logger.warn("delete channnel settlementbill, channelsettleconfigconfig recordId:" + recordId + ",delete number:" + j);
	}

	/**
	 * 
	 */
	@Override
	public ResultCode reverseChannelSettle(SettlementBill bill) {
		if (bill == null){
			return ResultCode.getFailure("null settle bill");			
		}
		Long settleId = bill.getRecordId();
		
		ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, bill.getRelateId());
		if(csc == null) {
			return ResultCode.getFailure("null ChannelSettleConfig");	
		}
		
		if(SettleConstant.CHANNEL_VENDOR_SYS.equals(csc.getVendorType())) {
			//清楚影院，以防影院发生变动
			String delpalce = "delete from check_bill where place_type = 'CHANNEL' and settlement_id = ? ";
			jdbcTemplate.update(delpalce,  settleId);
		} else {
			List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", settleId);
			for (CheckBill ck : checkBills){
				ck.initAggreDataExt();
				daoService.saveObject(ck);
			}
		}
		
		bill.initAggData();
		daoService.saveObject(bill);
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(extend != null) {
			extend.initData();
			daoService.updateObject(extend);
		}
		return ResultCode.SUCCESS;
	}

	/**
	 * 
	 */
	@Override
	public List<Place> loadPlacesByVendorId(Long vendorId) {
    	List<VendorCinemaRelation> relations = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", vendorId);
    	List<String> cinemaRecordIds = BeanUtil.getBeanPropertyList(relations, "cinemaRecordId", true);
    	return daoService.getObjectList(Place.class, cinemaRecordIds);
	}

	/**
	 * 
	 */
	@Override
	public void abandonChannelConfig(ChannelSettleConfig cfg, User user) {
		daoService.removeObject(cfg);
		int i = jdbcTemplate.update("delete from vendor_cinema_relation where vendor_recordid=?", new Object[]{cfg.getRecordId()});
		logger.warn("废弃通道配置 [" + cfg.getVendorName() + "],删除映射关系" + i + "条");
	}

	/**
	 * 
	 */
	@Override
	public int countChannelSettleBills(Long vendorId, Timestamp start,
			Timestamp end, String status,String reqMoneyStatus) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if(null != vendorId){
			query.add(Restrictions.eq("relateId", vendorId));
		}
		if(null != start){
			query.add(Restrictions.ge("startTime", start));
		}
		if(null != end){
			query.add(Restrictions.le("endTime", end));
		}
		if(StringUtils.isNotEmpty(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotEmpty(reqMoneyStatus)){
			query.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_CHANNEL));
		query.setProjection(Projections.rowCount());
		Object o = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(o.toString());
	}

	/**
	 * 
	 */
	@Override
	public List<SettlementBill> querySettleMents(Long vendorId, Timestamp start, Timestamp end, String status, Integer pageNo,
			Integer pageSize,String reqMoneyStatus) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if(null != vendorId){
			query.add(Restrictions.eq("relateId", vendorId));
		}
		if(null != start){
			query.add(Restrictions.ge("startTime", start));
		}
		if(null != end){
			query.add(Restrictions.le("endTime", end));
		}
		if(StringUtils.isNotEmpty(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotEmpty(reqMoneyStatus)){
			query.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		query.addOrder(Order.desc("recordId"));
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_CHANNEL));		
		return daoService.findByCriteria(query, pageNo * pageSize, pageSize);
	}
}