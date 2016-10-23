/** 
 */
package com.gewara.service.impl;

import java.sql.Timestamp;
import java.text.DecimalFormat;
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
import org.jdom.IllegalDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.dao.DataExtractionDao;
import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.SettleBaseEnums;
import com.gewara.enums.SettleCycleEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.RDPrevent;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.model.gsp.SysData;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.ReqMoneyBillService;
import com.gewara.service.SettleJitiService;
import com.gewara.service.SettlementBillService;
import com.gewara.service.SysDataService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;
import com.gewara.vo.SettleBillVo;
import com.gewara.web.util.PageUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 8, 2013  3:33:29 PM
 */
@Service("settlementBillService")
public class SettlementBillServiceImpl implements SettlementBillService {
	private final GewaLogger dbLogger = WebLogger.getLogger(getClass());
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("dataExtractionDao")
	private DataExtractionDao dataExtractionDao;
	
	@Autowired
	@Qualifier("reqMoneyBillService")
	private ReqMoneyBillService reqMoneyBillService;
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	
	@Autowired
	@Qualifier("config")
	private Config config;
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	

	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	@Autowired
	@Qualifier("settleJitiServiceImpl")
	private SettleJitiService settleJitiService;
	
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap settlementbills(SettleBillVo billvo, Integer pageNo, Integer pageSize, ModelMap model) {
		
		List<SettlementBill>  settleBills = new ArrayList<SettlementBill>();
		
		String isXls = billvo.getIsXls();
		if(StringUtils.isNotBlank(isXls)) {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 5000 : pageSize;
		} else {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 10 : pageSize;
		}
		
		Long recordId = billvo.getRecordId();
		String placeId = billvo.getPlaceId();
		String billType = billvo.getBillType();
		String status = billvo.getStatus();
		String reqMoneyStatus = billvo.getReqMoneyStatus();
		String playType = billvo.getPlayType();
		Long vendorId = billvo.getVendorId();
		Timestamp starttime = billvo.getStart();
		Timestamp endtime = billvo.getEnd();
		String channeltype = billvo.getChannelType();
		String goodsType = billvo.getGoodsType();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(SettlementBill.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(SettlementBill.class);
		
		if(recordId != null) {
			queryCount.add(Restrictions.eq("recordId", recordId));
			queryList.add(Restrictions.eq("recordId", recordId));
		}
		if(StringUtils.isNotBlank(placeId)) {
			List<String> placeIds = new ArrayList<String>();
			placeIds.add(placeId);
			//查询影院对应的通道费结算配置，只查询单家
			List<VendorCinemaRelation> relations = daoService.getObjectListByField(VendorCinemaRelation.class, "cinemaRecordId", placeId);
			for(VendorCinemaRelation vcr : relations){
				Long csconfigid = vcr.getVendorRecordId();
				ChannelSettleConfig scs = daoService.getObject(ChannelSettleConfig.class, csconfigid);
				if(scs != null) {
					if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(scs.getVendorType())) {
						placeIds.add(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL, vcr.getVendorRecordId()));
					}
				}
			}
			queryCount.add(Restrictions.in("configId", placeIds));
			queryList.add(Restrictions.in("configId", placeIds));
			Place place = daoService.getObject(Place.class, placeId);
			if(place != null) {
				model.put("placeFirstLetter", place.getName());
			}
		}
		if(StringUtils.isNotBlank(billType)) {
			if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(billType)) {
				if(StringUtils.isNotBlank(playType)) {
					queryCount.add(Restrictions.eq("playType", playType));
					queryList.add(Restrictions.eq("playType", playType));
				}
			}
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(billType)) {
				if(vendorId != null) {
					String channelid = RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL, vendorId);
					queryCount.add(Restrictions.eq("configId", channelid));
					queryList.add(Restrictions.eq("configId", channelid));
					
					ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, vendorId);
					if(csc != null) {
						model.put("vendorName", csc.getVendorName());
					}
				}
				if(StringUtils.isNotBlank(channeltype)) {
					queryCount.add(Restrictions.eq("playType", channeltype));
					queryList.add(Restrictions.eq("playType", channeltype));
				}
			}
			if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(billType)) {
				if(StringUtils.isNotBlank(goodsType)) {
					queryCount.add(Restrictions.eq("playType", goodsType));
					queryList.add(Restrictions.eq("playType", goodsType));
				}
			}
			queryCount.add(Restrictions.eq("tag", billType));
			queryList.add(Restrictions.eq("tag", billType));
		}
		if(StringUtils.isNotBlank(status)) {
			queryCount.add(Restrictions.eq("status", status));
			queryList.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(reqMoneyStatus)) {
			queryCount.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
			queryList.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		if(starttime != null) {
			queryCount.add(Restrictions.ge("startTime", starttime));
			queryList.add(Restrictions.ge("startTime", starttime));
		}
		if(endtime != null) {
			queryCount.add(Restrictions.lt("endTime", endtime));
			queryList.add(Restrictions.lt("endTime", endtime));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("startTime"));
		queryList.addOrder(Order.desc("updateTime"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			settleBills = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			model = setupSettleBills(settleBills, model);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + billvo.getUrl(), true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("recordId", recordId);
			m.put("placeId", placeId);
			m.put("billType", billType);
			m.put("status", status);
			m.put("reqMoneyStatus", reqMoneyStatus);
			m.put("playType", playType);
			m.put("goodsType", goodsType);
			m.put("vendorId", vendorId);
			m.put("starttime", starttime);
			m.put("endtime", endtime);
			m.put("channeltype", channeltype);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		model.put("recordId", recordId);
		model.put("placeId", placeId);
		model.put("billType", billType);
		model.put("status", status);
		model.put("reqMoneyStatus", reqMoneyStatus);
		model.put("playType", playType);
		model.put("vendorId", vendorId);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		model.put("channeltype", channeltype);
		model.put("goodsType", goodsType);
		return model;
	}
	
	
	private ModelMap setupSettleBills(List<SettlementBill>  settleBills, ModelMap model){
		Map<String, Place> placeMap = new HashMap<String, Place>();
		Map<String, SettleConfig> scm = new HashMap<String, SettleConfig>();
		Map<Long,ChannelSettleConfig> channelCfgMap = new HashMap<Long,ChannelSettleConfig>();
		Map<Long, SettlementBillExtend>  extendBillMap = new HashMap<Long, SettlementBillExtend>();
		for(SettlementBill bill : settleBills){
			//判断时候可付款勾选，用lastOperator字段代用一下
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(bill.getTag())) {
				Long channelid = bill.getRelateId();
				channelCfgMap.put(channelid, daoService.getObject(ChannelSettleConfig.class, channelid));
				bill.setLastOperator("Y");
			} else if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag())){
				String configid = bill.getConfigId();
				SettleConfig sc = scm.get(configid);
				if(sc == null) {
					bill.setLastOperator("Y");
				} else {
					String payVendorCode = sc.getPayVenderNo();
					SysData sys = sysDataService.getSysData(payVendorCode, SettleConstant.MEGERPAYVENDER, String.class);
					if(!SettleConstant.ZL.equals(bill.getPlayType())) {
						bill.setLastOperator("Y");
					} else if(SettleConstant.WANDA_ACTIVITY.equals(bill.getSpecial())) {
						bill.setLastOperator("Y");
					} else if(sys == null) {
						RDPrevent rdp = daoService.getObject(RDPrevent.class, payVendorCode);
						if(rdp == null) {
							bill.setLastOperator("Y");
						} else {
							bill.setLastOperator("N");
						}
					} else {
						bill.setLastOperator("N");
					}
				}
				placeMap.put(configid, daoService.getObject(Place.class, configid));
				scm.put(configid, sc);
			} else {
				bill.setLastOperator("Y");
				String configid = bill.getConfigId();
				SettleConfig sc = scm.get(configid);
				placeMap.put(configid, daoService.getObject(Place.class, configid));
				scm.put(configid, sc);
			}
			//查询扩展
			SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
			extendBillMap.put(bill.getRecordId(), extend);
		}
		model.put("sb", settleBills);
		model.put("pm", placeMap);
		model.put("scm", scm);
		model.put("channelCfgMap", channelCfgMap);
		model.put("extendBillMap", extendBillMap);
		return model;
	}
	
	/**
	 * 生成结算单
	 */
	@Override
	public List<SettlementBill> generateSettlementBill(List<SettleConfig> settleConfigs, String tag) {
		
		List<SettlementBill> bills = new ArrayList<SettlementBill>();
		Map<String, Timestamp> lastSettleBillsTime = getLastSettleBillsEndTimeByConfigs(tag);
		
		for (SettleConfig settleConfig : settleConfigs){
			String configId = settleConfig.getRecordId();
			Timestamp lastEndTime = lastSettleBillsTime.get(configId) == null ?  defaultCheckBillTime(settleConfig) : lastSettleBillsTime.get(configId);
			lastEndTime = DateUtil.parseTimestamp(DateUtil.format(lastEndTime, "yyyy-MM-dd") + " " + settleConfig.getSettleTime() + ":00", "yyyy-MM-dd HH:mm:ss");
			if(null == lastEndTime){
				dbLogger.error("lastEndTime is null :: settleConfig.getRecordId() = " + configId);
				continue;
			}
			Timestamp endTime = settleConfig.calculateEndTime(lastEndTime);
			if(endTime == null) {
				dbLogger.error("endTime is null :: settleConfig.getRecordId() = " + configId);
				continue;
			}
			if (!settleConfig.isTimeToSettle(endTime)) {
				continue;
			}
			
			Place place = daoService.getObject(Place.class, configId);
			if(place == null) {
				dbLogger.error("place is null :: settleConfig.getRecordId() = " + configId);
				continue;
			}
			//万达有活动场
			if(SettleConstant.WDCATE.contains(place.getOpenType())) {
				bills = createWDBill(lastEndTime, endTime, tag, settleConfig, bills);
			} else {
				bills = createNomalBill(lastEndTime, endTime, tag, settleConfig, bills);
			}
		}
		
		if (CollectionUtils.isEmpty(bills)) {
			return bills;
		}
		
		daoService.saveObjectList(bills);
		//生产对账单
		for(SettlementBill bill : bills) {
			checkBillService.createCheckBillBySettleBill(bill);
		}
		
		return bills;
	}
	
	/**
	 * 
	 * @param lastEndTime
	 * @param endTime
	 * @param relateId
	 * @param configId
	 * @param settleConfig
	 * @param bills
	 * @return
	 */
	private List<SettlementBill> createWDBill(Timestamp lastEndTime, Timestamp endTime, String tag,
			SettleConfig settleConfig, List<SettlementBill> bills) {
		Long relateId = settleConfig.getRelateId();
		String configId = settleConfig.getRecordId();
		//直连非活动场
		SettlementBill wdn2 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
		wdn2.setSpecial(SettleConstant.WANDA_NOT_ACTIVITY);
		wdn2.setPlayType(SettleConstant.ZL);
		bills.add(wdn2);
		//直连活动场
		if(SettleConstant.COMM_Y.equals(settleConfig.getPlayType()) &&
				 hasOrder(SettleConstant.WANDA_ACTIVITY, relateId, lastEndTime, endTime, settleConfig)) {
			SettlementBill wda1 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
			wda1.setSpecial(SettleConstant.WANDA_ACTIVITY);
			wda1.setPlayType(SettleConstant.ZL);
			bills.add(wda1);
		}
		//虚拟
		if(hasOrder("GEWA", relateId, lastEndTime, endTime, settleConfig)) {
			SettlementBill wd3 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
			wd3.setPlayType(SettleConstant.XN);
			bills.add(wd3);
		}
		
		//格瓦
		if(hasOrder("GPTBS", relateId, lastEndTime, endTime, settleConfig)) {
			SettlementBill wd4 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
			wd4.setPlayType(SettleConstant.GW);
			bills.add(wd4);
		}
		return bills;
	}
	
	
	/**
	 * 
	 * @param lastEndTime
	 * @param endTime
	 * @param relateId
	 * @param configId
	 * @param tag
	 * @param settleConfig
	 * @param bills
	 * @return
	 */
	private List<SettlementBill> createNomalBill(Timestamp lastEndTime, Timestamp endTime, String tag,
			SettleConfig settleConfig, List<SettlementBill> bills) {
		Long relateId = settleConfig.getRelateId();
		String configId = settleConfig.getRecordId();
		//直连
		SettlementBill initedBill1 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
		initedBill1.setPlayType(SettleConstant.ZL);
		bills.add(initedBill1);
		//虚拟
		if(hasOrder("GEWA", relateId, lastEndTime, endTime, settleConfig)) {
			SettlementBill initedBill2 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
			initedBill2.setPlayType(SettleConstant.XN);
			bills.add(initedBill2);
		}
		//格瓦
		if(hasOrder("GPTBS", relateId, lastEndTime, endTime, settleConfig)) {
			SettlementBill initedBill3 = new SettlementBill(lastEndTime, endTime, tag, relateId, configId);
			initedBill3.setPlayType(SettleConstant.GW);
			bills.add(initedBill3);
		}
		return bills;
	}
	
	/**
	 * 判断是否有订单
	 * @param cate
	 * @param placeId
	 * @param start
	 * @param end
	 * @param sc
	 * @return
	 */
	private boolean hasOrder(String cate, Long placeId, Timestamp start, Timestamp end, SettleConfig sc) {
		String timeField = sc.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		if(SettleConstant.WANDA_ACTIVITY.equals(cate)) {
			MiscUtil.appendCategoryQueryCondition(query);
			query.add(Restrictions.eq("special", SettleConstant.WANDA_ACTIVITY));
		} else {
			query.add(Restrictions.eq("category", cate));
			query.add(Restrictions.or(Restrictions.isNull("special"), Restrictions.ne("special", SettleConstant.WANDA_ACTIVITY)));
		}
		query.add(Restrictions.eq("relateId", placeId));
		query.add(Restrictions.ge(timeField, start));
		query.add(Restrictions.lt(timeField, end));
		//排除包场场次
		String sql = "select play_id from baochang where place_id = ? and play_time >= ? ";
		List<Long> playids = jdbcTemplate.queryForList(sql, Long.class, placeId + ",TICKET", start);
		for(Long playid : playids) {
			query.add(Restrictions.ne("playId", playid));
		}
		
		query.setProjection(Projections.rowCount());
		int count = Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
		if(count >0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 影票结算单计算
	 */
	@Override
	public void fulFillSettlementBill(SettlementBill bill){
		
		List<CheckBill> ckList = checkBillService.ticketCheckBillCollection(bill);
		if(CollectionUtils.isEmpty(ckList)) {
			dbLogger.error("fulFillSettlementBill结算单号：" + bill.getRecordId() + "ckList为空！");
			bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
			daoService.updateObject(bill);
			return;
		}
		
		hibernateTemplate.flush();
		
		SettlementBillExtend billextend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(billextend == null) {
			billextend = new SettlementBillExtend(bill.getRecordId());
		}
		
		Double totalNum = 0.0;
		Double totalAmount = 0.0;
		
		for(CheckBill ck : ckList) {
			bill.setSuccTicketNumber(bill.getSuccTicketNumber() + ck.getSuccTicketNumber());
			bill.setSuccTicketAmount(bill.getSuccTicketAmount() + ck.getSuccTicketAmount());
			bill.setRefundTotalNumber(bill.getRefundTotalNumber() + ck.getRefundTotalNumber());
			bill.setRefundTotalAmount(bill.getRefundTotalAmount() + ck.getRefundTotalAmount());
			bill.setDiffPriceNumber(bill.getDiffPriceNumber() + ck.getDiffPriceNumber());
			bill.setDiffPriceAmount(bill.getDiffPriceAmount() + ck.getDiffPriceAmount());
			billextend.setBillingamount(billextend.getBillingamount() + ck.getBillingamount());
			billextend.setPlaceallowance(billextend.getPlaceallowance() + ck.getPlaceallowance());
			
			totalNum += ck.getOrderTotalNumber();
			totalAmount += ck.getOrderTotalAmount();
		}
		
		bill.setAdjustTotalNumber(0.0);
		bill.setAdjustTotalAmount(0.0);
		bill.setPreviousRefundNumber(0.0);
		bill.setPreviousRefundAmount(0.0);
		bill.setOrderTotalNumber(totalNum);
		bill.setOrderTotalAmount(Double.valueOf(DECIMAL_FORMAT.format(totalAmount)));
		
		bill.setUpdateTime(DateUtil.getCurFullTimestamp());
		bill.setLastOperator("System");
		if(totalAmount == 0) {
			bill.setStatus(CheckBillStatusEnums.INVALID.getStatus());//无效
		} else {
			bill.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		}
		
		daoService.updateObject(bill);
		daoService.saveObject(billextend);
		settleJitiService.updateJiti(bill);
	}


	/**
	 * 
	 */
	@Override
	public Map<String, Timestamp> getLastSettleBillsEndTimeByConfigs(String tag) {
		Map<String, Timestamp> lastSettleBillsMapping = new HashMap<String, Timestamp>();
		String sql = " SELECT MAX(END_TIME), CONFIG_ID FROM SETTLEMENT_BILL where tag= ? GROUP BY CONFIG_ID";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,tag);
		for (Map<String, Object> m : result) {
			lastSettleBillsMapping.put(m.get("CONFIG_ID").toString(), (Timestamp)(m.get("MAX")));
		}
		return lastSettleBillsMapping;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	private Timestamp defaultCheckBillTime(SettleConfig c) {
		Timestamp end = c.getFirstSettle();
		if (end == null){
			end = DateUtil.parseTimestamp(config.getString("sysInitTime"));
			end = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(end), 1);
			if (SettleCycleEnums.MIDDLE.getType().equals(c.getSettleCycle())) {
				end = DateUtil.getNextMonthFirstDay(end);
			}
		}
		String settleTime = c.getSettleTime();
		end = DateUtil.parseTimestamp(DateUtil.format(end, "yyyy-MM-dd") + " " + settleTime + ":00", "yyyy-MM-dd HH:mm:ss");
		return end;
	}
	
	
	
	/**
	 * shit
	 */
	@Override
	public List<SettlementBill> querySettleMents(List<String> placeIds,
			Timestamp start, Timestamp end, String status, String orderField, Integer pageNo,
			Integer pageSize, boolean isStatusNotEq,String billType,String reqMoneyStatus,String playType) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if (CollectionUtils.isNotEmpty(placeIds)){
			query.add(Restrictions.in("configId", placeIds));
		}
		if (start != null) {
			query.add(Restrictions.ge("startTime", start));
		}
		if (end != null) {
			query.add(Restrictions.le("endTime", end));
		}
		if (StringUtils.isNotBlank(status)){
			if (!isStatusNotEq) {
				query.add(Restrictions.eq("status", status));
			} else {
				query.add(Restrictions.ne("status", status));
				query.add(Restrictions.ne("status", CheckBillStatusEnums.INVALID.getStatus()));
			}
		}
		if(StringUtils.isNotEmpty(billType)){
			query.add(Restrictions.eq("tag", billType));
		}
		if(StringUtils.isNotEmpty(reqMoneyStatus)){
			query.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		if(StringUtils.isNotEmpty(playType)){
			query.add(Restrictions.eq("playType", playType));
		}
		query.addOrder(Order.desc(orderField));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	/**
	 * shit
	 */
	@Override
	public int countingSettleBills(List<String> placeIds, Timestamp start,
			Timestamp end, String status, boolean isStatusNotEq,String billType,String reqMoneyStatus,String playType) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if (CollectionUtils.isNotEmpty(placeIds)){
			query.add(Restrictions.in("configId", placeIds));
		}
		if (start != null) {
			query.add(Restrictions.ge("startTime", start));
		}
		if (end != null) {
			query.add(Restrictions.le("endTime", end));
		}
		if (StringUtils.isNotBlank(status)){
			if (!isStatusNotEq) {
				query.add(Restrictions.eq("status", status));
			} else {
				query.add(Restrictions.ne("status", status));
				query.add(Restrictions.ne("status", CheckBillStatusEnums.INVALID.getStatus()));
			}
		}
		if(StringUtils.isNotEmpty(billType)){
			query.add(Restrictions.eq("tag",billType));
		}
		if(StringUtils.isNotEmpty(reqMoneyStatus)){
			query.add(Restrictions.eq("reqMoneyStatus", reqMoneyStatus));
		}
		if(StringUtils.isNotEmpty(playType)){
			query.add(Restrictions.eq("playType", playType));
		}
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
	}

	/**
	 * 
	 */
	@Override
	public ResultCode saveAdjustment(SettlementBill bill, String placeId, String comments, 
										String attachePath, Long operator, String mname,  String operatorCategory) {
		String oldStatus = bill.getStatus();
		bill.setStatus(CheckBillStatusEnums.READJUST.getStatus());
		bill.setLastOperator( mname);
		bill.setUpdateTime(DateUtil.getCurFullTimestamp());
		
		Map<String, String> otherMap = new HashMap<String, String>();
		otherMap.put("attachePath", attachePath);
		
		//String tag, Serializable relateId, String operator, String comments, String oldStatus, String newStatus
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), bill.getRecordId(), mname,
												 comments, oldStatus, bill.getStatus(), operatorCategory);
		
		st.setOtherInfo(JsonUtils.writeMapToJson(otherMap));
		daoService.saveObject(bill);
		daoService.saveObject(st);
		checkBillService.settleBillStatusChanged(bill, CheckBillStatusEnums.READJUST.getStatus());
		return ResultCode.getSuccess("");
	}

	/**
	 * 影票结算单计算入口
	 */
	@Override
	public void autoToMerchant(SettlementBill settleBill){
		ChangeEntry ce = new ChangeEntry(settleBill);
		fulFillSettlementBill(settleBill);
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), "System",
				 "", "INIT", settleBill.getStatus(), "GEWA");
		
		monitorService.saveChangeLog( 0L,
				 SettlementBill.class, settleBill.getRecordId(), ce.getChangeMap(settleBill));
		daoService.saveObject(st);
	}

	/**
	 * 
	 */
	@Override
	public ResultCode merchantBatchConfirn(List<SettlementBill> bills, Long uid, String uname) {
		for (SettlementBill bill : bills){
			String oldStatus = bill.getStatus();
			ChangeEntry ce = new ChangeEntry(bill);
			bill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
			bill.setLastOperator(uname);
			monitorService.saveChangeLog( uid,
										 SettlementBill.class, bill.getRecordId(), ce.getChangeMap(bill));
			checkBillService.settleBillStatusChanged(bill, CheckBillStatusEnums.WAITINGPAY.getStatus());
			StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), bill.getRecordId(), 
												uname, "", oldStatus, bill.getStatus(), "MERCHANT");
			daoService.saveObject(st);
		}
		//insertDownloadRecord(bills, BillTypeEnums.PAYABLEBILL);
		daoService.saveObjectList(bills);
		return ResultCode.getSuccess("");
	}

	
	/**
	 * @param bills
	 */
	private void insertDownloadRecord(List<SettlementBill> bills, BillTypeEnums billType) {
		for (SettlementBill bill : bills){
			DownloadRecorder re = new DownloadRecorder(bill.getRecordId(), billType);
			daoService.saveObject(re);
		}
	}

	/**
	 * 
	 */
	@Override
	public ResultCode paySettle(final SettlementBill settleBill, final User user, String isOnline) {
		String oldStatus = settleBill.getStatus();
		settleBill.setStatus(CheckBillStatusEnums.SETTLED.getStatus());
		settleBill.setLastOperator(user.getUsername());
		settleBill.setUpdateTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(settleBill);
		checkBillService.settleBillStatusChanged(settleBill, CheckBillStatusEnums.SETTLED.getStatus());
		if ("true".equals(isOnline)){
			String vendorNo = "";
			String mNativeMoney = "";
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(settleBill.getTag())){//通道费
				ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, settleBill.getRelateId());
				vendorNo = csc.getVendorCode();
				mNativeMoney = settleBill.getOrderTotalAmount().toString();
			}else if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(settleBill.getTag())) {//影票
				SettleConfig sc = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
				if(SettleConstant.ZL.equals(settleBill.getPlayType())) {
					mNativeMoney = settleBill.getOrderTotalAmount().toString();
					//万达活动场
					if(SettleConstant.WANDA_ACTIVITY.equals(settleBill.getSpecial())){
						vendorNo = sc.getVenderNo();
					} else {
						vendorNo = sc.getPayVenderNo();
					}
				} else {
					mNativeMoney = settleBill.getOrderTotalAmount().toString();
					vendorNo = sc.getVenderNo();
				}
			} else {//卖品
				SettleConfig sc = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
				vendorNo = sc.getVenderNo();
				mNativeMoney = settleBill.getOrderTotalAmount().toString();
			}
			
			DownloadRecorder dlRecorder = new DownloadRecorder(settleBill.getRecordId(), BillTypeEnums.PAYBILL);
			dlRecorder.setVendorNo(vendorNo);
			dlRecorder.setNativeMoney(mNativeMoney);
			dlRecorder.setSpecial(SettleConstant.SETTLE);
			dlRecorder.setOptUser(user.getUsername());
			daoService.saveObject(dlRecorder);
		}
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), user.getUsername(),
				 "", oldStatus, settleBill.getStatus(), "GEWA");
		daoService.saveObject(st);
		return ResultCode.getSuccess("");
	}

	/**
	 * shit
	 */
	@Override
	public int countingPayBills(String placeId) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		
		if (StringUtils.isNotBlank(placeId)) {
			query.add(Restrictions.eq("configId", placeId));
		}
		
		query.add(Restrictions.eq("status", CheckBillStatusEnums.SETTLED.getStatus()));
		
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
		
		
	}

	/**
	 * shit
	 */
	@Override
	public List<SettlementBill> queryPayBills(String placeId, Integer pageNo,
			Integer pageSize) {
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if (StringUtils.isNotBlank(placeId)) {
			query.add(Restrictions.eq("configId", placeId));
		}
		
		query.add(Restrictions.eq("status", CheckBillStatusEnums.SETTLED.getStatus()));
		
		query.addOrder(Order.desc("updateTime"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	/**
	 * 
	 */
	@Override
	public Map<Long, StatusTracker> queryMerchantConfirm(List<Long> settleIds) {
		DetachedCriteria query = DetachedCriteria.forClass(StatusTracker.class);
		query.add(Restrictions.eq("newStatus", CheckBillStatusEnums.MERCHANTCONFIRMED.getStatus()));
		query.add(Restrictions.in("relateId", settleIds));
		List<StatusTracker> trackers = daoService.findByCriteria(query);
		
		Map<Long, StatusTracker> trackerMap = new HashMap<Long, StatusTracker>();
		for (StatusTracker st : trackers)
			trackerMap.put(st.getRelateId(), st);
		return trackerMap;
	}

	/**
	 * shit
	 */
	@Override
	public Map<Long, StatusTracker> queryGewaPayBill(List<Long> settleIds) {
		DetachedCriteria query = DetachedCriteria.forClass(StatusTracker.class);
		query.add(Restrictions.eq("newStatus", CheckBillStatusEnums.SETTLED.getStatus()));
		query.add(Restrictions.in("relateId", settleIds));
		List<StatusTracker> trackers = daoService.findByCriteria(query);
		
		Map<Long, StatusTracker> trackerMap = new HashMap<Long, StatusTracker>();
		for (StatusTracker st : trackers) {
			trackerMap.put(st.getRelateId(), st);
		}
		return trackerMap;
	}

	/**
	 * 
	 */
	@Override
	public void afterAdjust(SettlementBill bill, Adjustment ad) {
		if (!CheckBillStatusEnums.READJUST.getStatus().equals(bill.getStatus()))
			throw new IllegalDataException("settlement bill's status is incorrect. should be :" + CheckBillStatusEnums.READJUST.getStatus() + " or:" + CheckBillStatusEnums.GEWAREADJUST.getStatus());
		
		Double number = ad.getAdjustNumber();
		Double amount = ad.getAmount();
		
		bill.setAdjustTotalNumber(bill.getAdjustTotalNumber() + number);
		bill.setAdjustTotalAmount(bill.getAdjustTotalAmount() + amount);
		bill.setOrderTotalNumber(bill.getOrderTotalNumber() + number);
		bill.setOrderTotalAmount(bill.getOrderTotalAmount() + amount);
		
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(extend != null) {
			extend.setBillingamount(extend.getBillingamount() + amount);
			daoService.updateObject(extend);
		}
		
		if(!isAllAdjustPassed(ad.getRelateId(),ad.getRecordId())){
			bill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
			checkBillService.settleBillStatusChanged(bill, CheckBillStatusEnums.WAITINGPAY.getStatus());
		}
		daoService.saveObject(bill);
		settleJitiService.updateJiti(bill);
	}

	/**
	 * 
	 * @param settlebillId
	 * @param adjustId
	 * @return
	 */
	private boolean isAllAdjustPassed(String settlebillId,Long adjustId){
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("relateId", settlebillId));
		query.add(Restrictions.eq("status", SettleConstant.ADJUST_STATUS_NEW));
		query.add(Restrictions.ne("recordId", adjustId));
		List<Adjustment> ads = daoService.findByCriteria(query);
		return ads.size() > 0 ? true : false;
	}
	
	/**
	 * 
	 */
	@Override
	public ResultCode gewaReadjust(SettlementBill settleBill, User user) {
		String oldStatus = settleBill.getStatus();
		settleBill.setStatus(CheckBillStatusEnums.GEWAREADJUST.getStatus());
		settleBill.setLastOperator(user.getUsername());
		daoService.updateObject(settleBill);
		checkBillService.settleBillStatusChanged(settleBill, CheckBillStatusEnums.GEWAREADJUST.getStatus());
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), user.getUsername(),
				 "", oldStatus, settleBill.getStatus(), "GEWA");
		daoService.saveObject(st);
		return ResultCode.getSuccess("");
	}

	/**
	 * 
	 */
	@Override
	public ResultCode confirmPay(SettlementBill settleBill, User user) {
		String oldStatus = settleBill.getStatus();
		settleBill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
		settleBill.setLastOperator(user.getUsername());
		daoService.updateObject(settleBill);
		checkBillService.settleBillStatusChanged(settleBill, CheckBillStatusEnums.WAITINGPAY.getStatus());
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), user.getUsername(),
				 "", oldStatus, settleBill.getStatus(), "GEWA");
		daoService.saveObject(st);
		return ResultCode.getSuccess("");
	}

	/**
	 * 
	 */
	@Override
	public ResultCode toMerchant(SettlementBill settleBill, User logonUser) {
		if (!CheckBillStatusEnums.READJUST.getStatus().equals(settleBill.getStatus()))
			return ResultCode.getFailure("当前状态：" + settleBill.getStatus() + " 不允许提交");
		
		settleBill.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		settleBill.setLastOperator(logonUser.getUsername());
		daoService.updateObject(settleBill);
		checkBillService.settleBillStatusChanged(settleBill, CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), settleBill.getRecordId(), logonUser.getUsername(),
				 "", CheckBillStatusEnums.READJUST.getStatus(), settleBill.getStatus(), "GEWA");
		
		daoService.saveObject(st);
		return ResultCode.SUCCESS;
	} 
	
	/**
	 * 1. update gewaorder is_settled = 'N' check_bill_id = ''
	 * 2. update refundment is_settled = 'N', check_bill_id = ''
	 * 3. update Adjustment ad.setIsSettled("N")， ad.setSettleBillId(null);
	 * 4. update refund.setIsSettled("N"), refund.setSettleBillId(null);
	 * 5. init to Zero checkbill
	 * 6. init to Zero settlement
	 */
	@Override
	public ResultCode reverseSettleBill(SettlementBill settleBill){
		if (settleBill == null)
			return ResultCode.getFailure("null settle bill");
		
		Long settleId = settleBill.getRecordId();
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", settleId);
		if (!CollectionUtils.isEmpty(checkBills)){
			final List<Long> ckIds = BeanUtil.getBeanPropertyList(checkBills, "recordId", true);
			final String order = " UPDATE gewa_order SET is_settled = 'N', check_bill_id = null WHERE " +
					"check_bill_id in :inList";
			final String refundment = " UPDATE refundment SET is_settled = 'N', check_bill_id = null, settle_bill_id = null  WHERE " +
					"check_bill_id in :inList or settle_bill_id = " + settleId;
			//reverse gewaorders
			hibernateTemplate.execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) {
					Query query = session.createSQLQuery(order).setParameterList("inList", ckIds);
					return query.executeUpdate();
				}
			});
			
			//reverse refundments
			hibernateTemplate.execute(new HibernateCallback() {
				@Override
				public Object doInHibernate(Session session) {
					Query query = session.createSQLQuery(refundment).setParameterList("inList", ckIds);
					return query.executeUpdate();
				}
			});
			
			for (CheckBill ck : checkBills){
				ck.initAggreDataExt();
				daoService.saveObject(ck);
			}
			
		}
		List<Adjustment> ads = daoService.getObjectListByField(Adjustment.class, "settleBillId", settleBill.getRecordId());
		
		for (Adjustment ad : ads){
			ad.setIsSettled("N");
			ad.setSettleBillId(null);
		}
		daoService.saveObjectList(ads);
		
		List<Refundment> refunds = daoService.getObjectListByField(Refundment.class, "settleBillId", settleBill.getRecordId());
		
		for (Refundment refund : refunds){
			refund.setIsSettled("N");
			refund.setSettleBillId(null);
		}
		daoService.saveObjectList(refunds);
		
		settleBill.initAggData();
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, settleBill.getRecordId());
		if(extend != null) {
			extend.initData();
			daoService.updateObject(extend);
		}
		daoService.updateObject(settleBill);
		return ResultCode.SUCCESS;
	}

	/**
	 * 当卖品结算系统金额  >= 商家申请金额，卖品结算单状态变为等待付款状态，同时系统自动将结算系统金额按差额进行调减；
	 * 当卖品结算系统金额 < 商家申请金额时，卖品结算单状态商户申请调整，由结算人员按照最终确认付款额对结算单金额进行调整，
	 * 然后由财务主管人员审核，通过后方可付款
	 */
	@Override
	public ResultCode saveApply(Long recordId, Double applyNumber,Double applyAmount, String mname) {
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		Double billNumber = bill.getOrderTotalNumber();
		Double billAmount = bill.getOrderTotalAmount();
		
		bill.setApplySettleAmount(applyAmount);
		bill.setApplySettleNumber(applyNumber);
		bill.setLastOperator(mname);
		
		String status = "";
		if(bill.getOrderTotalAmount().doubleValue() >= applyAmount.doubleValue()){
			status = CheckBillStatusEnums.WAITINGPAY.getStatus();
			Double adjustAmount = applyAmount - billAmount;
			Double adjustNumber = applyNumber - billNumber;
			bill.setOrderTotalAmount(applyAmount);
			bill.setOrderTotalNumber(applyNumber);
			bill.setAdjustTotalNumber(adjustNumber);
			bill.setAdjustTotalAmount(adjustAmount);
			
			SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
			if(extend != null) {
				extend.setBillingamount(applyAmount);
				daoService.saveObject(extend);
			}
			
			Adjustment ad = new Adjustment(SettleConstant.ADJUST_TYPE_SETTLEMENT, recordId.toString(), adjustNumber, adjustAmount, "GOODS", "系统调整", "SYS",bill.getConfigId());
			ad.setIsSettled("Y");
			ad.setStatus(SettleConstant.ADJUST_STATUS_APPROVED);
			ad.setSettleBillId(recordId);
			daoService.saveObject(ad);
		}else{
			status = CheckBillStatusEnums.READJUST.getStatus();
		}
		bill.setStatus(status);		
		checkBillService.settleBillStatusChanged(bill, status);
		daoService.saveObject(bill);
		return ResultCode.SUCCESS;
	}
	
}
