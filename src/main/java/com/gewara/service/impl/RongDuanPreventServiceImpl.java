package com.gewara.service.impl;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.GspJob;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.RDBuckle;
import com.gewara.model.gsp.RDDayCount;
import com.gewara.model.gsp.RDPay;
import com.gewara.model.gsp.RDPrevent;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.RongDuanPreventService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;
import com.gewara.web.util.PageUtil;

@Service("rongDuanPreventService")
public class RongDuanPreventServiceImpl implements RongDuanPreventService {

	private final GewaLogger logger = WebLogger.getLogger(getClass());
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
	
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
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;

	/**
	 * 熔断汇总查询
	 */
	@Override
	public ModelMap queryRongduanHuizong(String vendorName, Integer pageNo, Integer pageSize, String url, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<RDPrevent> rdpList = new ArrayList<RDPrevent>();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(RDPrevent.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(RDPrevent.class);
		
		if(StringUtils.isNotBlank(vendorName)) {
			queryCount.add(Restrictions.like("vendorName", vendorName, MatchMode.ANYWHERE));
			queryList.add(Restrictions.like("vendorName", vendorName, MatchMode.ANYWHERE));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("vendorCode"));
		
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			rdpList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("vendorName", vendorName);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("rdpList", rdpList);
		return model;
	}
	
	/**
	 * 每天系统订单金额统计
	 */
	@Override
	public ModelMap queryRongdunDayCount(RDDayCount dayc, ModelMap model) {
		
		RDPrevent rdp = daoService.getObject(RDPrevent.class, dayc.getVendorCode());
		if(rdp == null) {
			return model;
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(RDDayCount.class);
		query.add(Restrictions.eq("vendorCode", dayc.getVendorCode()));
		query.add(Restrictions.ge("startTime", rdp.getStartTime()));
		query.add(Restrictions.le("endTime", rdp.getEndTime()));
		query.addOrder(Order.asc("startTime"));
		List<RDDayCount> dcList = daoService.findByCriteria(query);
		model.put("dcList", dcList);
		model.put("rdp", rdp);
		return model;
	}
	
	/**
	 * 预付详情
	 */
	@Override
	public ModelMap queryRongdunPayRecord(RDPay pay, Integer pageNo,
			Integer pageSize, String url, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 20 : pageSize;
		
		List<RDPay> paylist = new ArrayList<RDPay>();
		
		Long recordId = pay.getRecordId();
		String vendorCode = pay.getVendorCode();
		String optUser = pay.getOptUser();
		Timestamp startTime = pay.getStartTime();
		Timestamp endTime = pay.getEndTime();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(RDPay.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(RDPay.class);
		
		if(recordId != null) {
			queryCount.add(Restrictions.eq("recordId", recordId));
			queryList.add(Restrictions.eq("recordId", recordId));
		}
		if(StringUtils.isNotBlank(vendorCode)) {
			queryCount.add(Restrictions.eq("vendorCode", vendorCode));
			queryList.add(Restrictions.eq("vendorCode", vendorCode));
		}
		if(StringUtils.isNotBlank(optUser)) {
			queryCount.add(Restrictions.eq("optUser", optUser));
			queryList.add(Restrictions.eq("optUser", optUser));
		}
		if(startTime != null) {
			queryCount.add(Restrictions.ge("payTime", startTime));
			queryList.add(Restrictions.ge("payTime", startTime));
		}
		if(endTime != null) {
			queryCount.add(Restrictions.le("payTime", startTime));
			queryList.add(Restrictions.le("payTime", startTime));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("payTime"));

		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			paylist = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("vendorCode", vendorCode);
			m.put("optUser", optUser);
			m.put("startTime", startTime);
			m.put("endTime", endTime);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("paylist", paylist);
		model.put("vendorCode", vendorCode);
		model.put("optUser", optUser);
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		
		return model;
	}
	
	/**
	 * 扣款详情
	 */
	@Override
	public ModelMap queryRongdunBuckleRecord(RDBuckle buckle, Integer pageNo,
			Integer pageSize, String url, String isXls, ModelMap model) {
		
		if(StringUtils.isNotBlank(isXls)){
			pageNo = 0;
			pageSize = 10000;
		} else {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 20 : pageSize;
		}
		
		List<RDPay> bucklelist = new ArrayList<RDPay>();
		
		String vendorCode = buckle.getVendorCode();
		Long relateId = buckle.getRelateId();
		String placeId = buckle.getPlaceId();
		Timestamp startTime = buckle.getStartTime();
		Timestamp endTime = buckle.getEndTime();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(RDBuckle.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(RDBuckle.class);
		
		if(StringUtils.isNotBlank(vendorCode)) {
			queryCount.add(Restrictions.eq("vendorCode", vendorCode));
			queryList.add(Restrictions.eq("vendorCode", vendorCode));
		}
		if(relateId != null) {
			queryCount.add(Restrictions.eq("relateId", relateId));
			queryList.add(Restrictions.eq("relateId", relateId));
		}
		if(StringUtils.isNotBlank(placeId)) {
			queryCount.add(Restrictions.eq("placeId", placeId));
			queryList.add(Restrictions.eq("placeId", placeId));
		}
		if(startTime != null) {
			queryCount.add(Restrictions.ge("buckleTime", startTime));
			queryList.add(Restrictions.ge("buckleTime", startTime));
		}
		if(endTime != null) {
			queryCount.add(Restrictions.le("buckleTime", endTime));
			queryList.add(Restrictions.le("buckleTime", endTime));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("cycle"));

		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			bucklelist = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("vendorCode", vendorCode);
			m.put("relateId", relateId);
			m.put("placeId", placeId);
			m.put("startTime", startTime);
			m.put("endTime", endTime);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("bucklelist", bucklelist);
		model.put("vendorCode", vendorCode);
		model.put("relateId", relateId);
		model.put("placeId", placeId);
		
		return model;
	}
	
	/**
	 * 预付款
	 */
	@Override
	public String rongdunPay(RDPrevent rdp, RDPay pay, String optUser) {
		
		GspJob job = daoService.getObject(GspJob.class, "rd-autoTomerchant");
		if(SettleConstant.RUNNING.equals(job.getStatus())) {
			return "jobrunning";
		}
		
		pay.setVendorCode(rdp.getVendorCode());
		pay.setVendorName(rdp.getVendorName());
		pay.setPayBefore(rdp.getTotalAmount());
		pay.setPayAfter(rdp.getTotalAmount() + pay.getPayAmount());
		pay.setPayTime(DateUtil.getCurFullTimestamp());
		pay.setOptUser(optUser);
		pay.setReqMoneyId(SettleConstant.COMM_N);
		daoService.addObject(pay);
		
		Double amount = pay.getPayAmount();
		rdp.setTotalAmount(rdp.getTotalAmount() + amount);
		rdp.setCanUseAmount(rdp.getCanUseAmount() + amount);
		rdp.setSurplusAmount(rdp.getSurplusAmount() + amount);
		if(rdp.getSurplusAmount() >= rdp.getWaringAmount()) {
			rdp.setStatus(SettleConstant.COMM_Y);
		} else {
			rdp.setStatus(SettleConstant.COMM_N);
			/*String content = "<h1>【" + rdp.getVendorName() + "】的可用余额" + rdp.getCanUseAmount() + "已经低于当前预警金额" + rdp.getWaringAmount() + 
					"，请及时进行预付充值以免影响影院付款！<h1><br>" + "发送时间：" + DateUtil.getCurFullTimestampStr();
			sendWaringMail(content, true);*/
		}
		daoService.updateObject(rdp);
		
		//线上付款
		if(SettleConstant.COMM_Y.equals(pay.getPayWay())) {
			//插入预付记录
			DownloadRecorder re = new DownloadRecorder(pay.getRecordId(), BillTypeEnums.PAYBILL);
			re.setSpecial(SettleConstant.RONGDUAN);
			re.setVendorNo(rdp.getVendorCode());
			re.setNativeMoney("" + DECIMAL_FORMAT.format(amount));
			re.setOptUser(optUser);
			re.setStatus(SettleConstant.JSSUBMIT);
			daoService.saveObject(re);
		}
		
		return "ok";
	}
	
	/**
	 * 修改预警金额
	 */
	@Override
	public String waringMfy(RDPrevent rdp, Double amount) {
		
		GspJob job = daoService.getObject(GspJob.class, "rd-autoTomerchant");
		if(SettleConstant.RUNNING.equals(job.getStatus())) {
			return "jobrunning";
		}
		
		rdp.setWaringAmount(amount);
		if(rdp.getSurplusAmount() >= rdp.getWaringAmount()) {
			rdp.setStatus(SettleConstant.COMM_Y);
		} else {
			rdp.setStatus(SettleConstant.COMM_N);
			/*String content = "【" + rdp.getVendorName() + "】的可用余额" + rdp.getCanUseAmount() + "已经低于当前预警金额" + rdp.getWaringAmount() + 
					"，请及时进行预付充值以免影响影院付款！<br>" + "发送时间：" + DateUtil.getCurFullTimestampStr();
			sendWaringMail(content, true);*/
		}
		daoService.updateObject(rdp);
		
		return "ok";
	}
	
	//发送预警邮件
	private void sendWaringMail(String content) {
		int day = DateUtil.getWeek(DateUtil.getCurDate());
		if(day != 1 && day != 5) {
			return;
		}
		GSPMaill.sendMaill(content, GSPSendMaill.RONGDUANMAIL);
	}
	
	/**
	 * 每天防熔断供应商影票统计
	 */
	@Override
	public void everyDayCollection() {
		logger.warn("RongDuanJob->everyDayCollection: started!");
		
		Date date = DateUtil.getCurDate();
		Timestamp end = new Timestamp(date.getTime());
		Timestamp start = DateUtil.addDay(end, -1);
		
		List<RDPrevent> rdpList = daoService.getAllObjects(RDPrevent.class);
		for(RDPrevent rdp : rdpList) {
			if(rdp.getWaringAmount() <= 0) {
				logger.warn("RongDuanJob->everyDayCollection: 供应商编号为" + rdp.getVendorCode() + " 预警金额小于0不计算0！");
				continue;
			}
			RDDayCount rdc;
			/*
			 * 查询每日单据
			 */
			DetachedCriteria queryrdc = DetachedCriteria.forClass(RDDayCount.class);
			queryrdc.add(Restrictions.eq("vendorCode", rdp.getVendorCode()));
			queryrdc.add(Restrictions.eq("startTime", start));
			queryrdc.add(Restrictions.eq("endTime", end));
			List<RDDayCount> rdcList = daoService.findByCriteria(queryrdc);
			if(rdcList.size() == 0) {
				rdc = new RDDayCount();
				rdc.setVendorCode(rdp.getVendorCode());
				rdc.setVendorName(rdp.getVendorName());
				rdc.setStartTime(start);
				rdc.setEndTime(end);
			} else if(rdcList.size() == 1) {
				rdc = rdcList.get(0);
			} else {
				logger.error("RongDuanJob->everyDayCollection: 供应商编号为" + rdp.getVendorCode() + "出现多条日统计订单记录！");
				continue;
			}
			/*
			 * 查询使用该供应商编号的影院配置
			 */
			DetachedCriteria qrysc = DetachedCriteria.forClass(SettleConfig.class);
			qrysc.add(Restrictions.eq("payVenderNo", rdp.getVendorCode()));
			List<SettleConfig> scList = daoService.findByCriteria(qrysc);
			if(CollectionUtils.isEmpty(scList)) {
				logger.warn("RongDuanJob->everyDayCollection: 供应商编号为" + rdp.getVendorCode() + "对应的影院配置为空！");
				rdc.setNum(0l);
				rdc.setAmount(0.0);
				daoService.saveObject(rdc);
				continue;
			}
			List<Long> placeIds = BeanUtil.getBeanPropertyList(scList, "relateId", true);
			/*
			 * 查询订单
			 */
			DetachedCriteria queryorder = DetachedCriteria.forClass(GewaOrder.class);
			queryorder.add(Restrictions.ge("dealTime", start));
			queryorder.add(Restrictions.lt("dealTime", end));
			queryorder.add(Restrictions.in("relateId", placeIds));
			queryorder.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
			MiscUtil.appendCategoryQueryCondition(queryorder);
			//万达非活动场
			if("1582".equals(rdp.getVendorCode())) {
				queryorder.add(Restrictions.or(Restrictions.isNull("special"), Restrictions.ne("special", SettleConstant.WANDA_ACTIVITY)));
			}
			List<GewaOrder> orders = daoService.findByCriteria(queryorder);
			
			Long num = 0l;
			Double amount = 0.0;
			for(GewaOrder o : orders) {
				num += o.getQuantity();
				amount += o.getTotalCost();
			}
			
			rdc.setNum(num);
			rdc.setAmount(amount);
			
			daoService.saveObject(rdc);
		}
		
		logger.warn("RongDuanJob->everyDayCollection: end!");
	}
	
	/**
	 * 熔断统计
	 */
	@Override
	public void rongduanCollection() {
		logger.warn("RongDuanJob->rongduanCollection started!");
		
		Timestamp start;
		Date curr = DateUtil.getCurDate();
		Date monthHeader = DateUtil.getMonthFirstDay(curr);
		if(1 < DateUtil.getDiffDay(monthHeader, curr)) {
			//当月1号
			start = new Timestamp(monthHeader.getTime());
		} else {
			//上月1号
			start = MiscUtil.addMonth(new Timestamp(monthHeader.getTime()), -1);
		}
		Timestamp end = new Timestamp(curr.getTime());
		
		StringBuilder mailContent = new StringBuilder("");
		List<RDPrevent> rdpList = daoService.getAllObjects(RDPrevent.class);
		for(RDPrevent rdp : rdpList) {
			if(rdp.getWaringAmount() <= 0) {
				logger.warn("RongDuanJob->rongduanCollection: 预警金额小于0不计算0！");
				continue;
			}
			/*
			 * 查询使用该供应商编号的影院配置
			 */
			DetachedCriteria qrysc = DetachedCriteria.forClass(SettleConfig.class);
			qrysc.add(Restrictions.eq("payVenderNo", rdp.getVendorCode()));
			List<SettleConfig> scList = daoService.findByCriteria(qrysc);
			if(CollectionUtils.isEmpty(scList)) {
				logger.warn("RongDuanJob->rongduanCollection: 供应商编号为" + rdp.getVendorCode() + "对应的影院配置为空！");
				continue;
			}
			List<Long> placeIds = BeanUtil.getBeanPropertyList(scList, "relateId", true);
			
			Map<String, Object> unmap = unconfirmAndBuckle(rdp, start, placeIds);
			
			Map<String, Object> daymap = dayCount(rdp, start, end);
			
			/*
			 * 汇总
			 */
			rdp.setWaitingPayAmount((Double)unmap.get("unconfirmAmount"));
			rdp.setWaitingPayRelation(unmap.get("unconfirmRelation").toString());
			rdp.setUncountAmount((Double)daymap.get("dayCountAmount"));
			rdp.setUncountRelation(daymap.get("dayCountRelation").toString());
			rdp.setStartTime(start);
			rdp.setEndTime(end);
			rdp.setSurplusAmount(rdp.getCanUseAmount() - rdp.getWaitingPayAmount() - rdp.getUncountAmount());
			if(rdp.getSurplusAmount() >= rdp.getWaringAmount()) {
				rdp.setStatus(SettleConstant.COMM_Y);
			} else {
				rdp.setStatus(SettleConstant.COMM_N);
				mailContent.append("【" + rdp.getVendorName() + "】的可用余额" + rdp.getSurplusAmount() + 
						"已经低于当前预警金额" + rdp.getWaringAmount() + "，请及时进行预付充值以免影响影院付款！<br>");
				
			}
			rdp.setUpdateTime(DateUtil.getCurFullTimestamp());
			
			daoService.updateObject(rdp);
		}
		
		if(!"".equals(mailContent.toString())) {
			String content = mailContent.toString() + "发送时间：" + DateUtil.getCurFullTimestampStr();
			sendWaringMail(content);
		}
		
		logger.warn("RongDuanJob->rongduanCollection end!");
	}
	
	/**
	 * 统计扣款和未确认结算单
	 * @param rdp
	 * @param start
	 * @param placeIds
	 * @return
	 */
	private Map<String, Object> unconfirmAndBuckle(RDPrevent rdp, Timestamp start, List<Long> placeIds) {
		/*
		 * 查询结算单
		 */
		Double unconfirmAmount = 0.0;
		StringBuilder unconfirmRelation = new StringBuilder("");
		DetachedCriteria querySt = DetachedCriteria.forClass(SettlementBill.class);
		querySt.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		querySt.add(Restrictions.in("relateId", placeIds));
		querySt.add(Restrictions.or(Restrictions.or(
				Restrictions.eq("status", CheckBillStatusEnums.WAITINGPAY.getStatus()),
				Restrictions.eq("status", CheckBillStatusEnums.GEWACONFIRMED.getStatus())), 
				Restrictions.eq("status", CheckBillStatusEnums.READJUST.getStatus())));
		//万达的要非活动场
		querySt.add(Restrictions.or(Restrictions.or(Restrictions.isNull("special"),Restrictions.eq("special", "")), 
				Restrictions.eq("special", SettleConstant.WANDA_NOT_ACTIVITY)));
		Timestamp tmep = DateUtil.parseTimestamp("2016-03-01", "yyyy-MM-dd");
		Timestamp miniTime = MiscUtil.addMonth(start, -2);
		if(miniTime.before(tmep)) {
			miniTime = tmep;
		}
		querySt.add(Restrictions.ge("startTime", miniTime));
		
		List<SettlementBill> stList = daoService.findByCriteria(querySt);
		for(SettlementBill sb : stList) {
			if(CheckBillStatusEnums.WAITINGPAY.getStatus().equals(sb.getStatus())) {
				RDBuckle buckle = new RDBuckle();
				buckle.setRelateId(sb.getRecordId());
				buckle.setVendorCode(rdp.getVendorCode());
				buckle.setVendorName(rdp.getVendorName());
				Place p = daoService.getObject(Place.class, sb.getConfigId());
				if(p != null) {
					buckle.setPlaceId(p.getRecordId());
					buckle.setPlaceName(p.getName());
				}
				buckle.setConfirmTime(sb.getUpdateTime());
				buckle.setSpecial(sb.getSpecial());
				buckle.setCycle(sb.getStartTime().toString() + " ~ " + sb.getEndTime());
				buckle.setBuckleTime(DateUtil.getCurFullTimestamp());
				buckle.setBuckleBefore(rdp.getCanUseAmount());
				buckle.setBuckleAmount(sb.getOrderTotalAmount());
				//扣款
				rdp.setCanUseAmount(rdp.getCanUseAmount() - sb.getOrderTotalAmount());
				buckle.setBuckleAfter(rdp.getCanUseAmount());
				daoService.addObject(buckle);
				//结算单更新
				sb.setStatus(CheckBillStatusEnums.SETTLED.getStatus());
				sb.setUpdateTime(DateUtil.getCurFullTimestamp());
				sb.setLastOperator("sys-rongduan");
				daoService.updateObject(sb);
			} else {
				unconfirmAmount += sb.getOrderTotalAmount();
				unconfirmRelation.append(sb.getRecordId() + ",");
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("unconfirmAmount", unconfirmAmount);
		map.put("unconfirmRelation", unconfirmRelation.toString());
		return map;
	}
	
	/**
	 * 统计每天订单金额
	 * @param rdp
	 * @param start
	 * @param end
	 * @return
	 */
	private Map<String, Object> dayCount(RDPrevent rdp, Timestamp start, Timestamp end) {
		/*
		 * 查询每日统计账单
		 */
		Double dayCountAmount = 0.0;
		StringBuilder dayCountRelation = new StringBuilder("");
		DetachedCriteria querydc = DetachedCriteria.forClass(RDDayCount.class);
		querydc.add(Restrictions.eq("vendorCode", rdp.getVendorCode()));
		querydc.add(Restrictions.ge("startTime", start));
		querydc.add(Restrictions.le("endTime", end));
		List<RDDayCount> rdcList = daoService.findByCriteria(querydc);
		for(RDDayCount rdc : rdcList) {
			dayCountAmount += rdc.getAmount();
			dayCountRelation.append(rdc.getRecordId() + ",");
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dayCountAmount", dayCountAmount);
		map.put("dayCountRelation", dayCountRelation.toString());
		return map;
	}
	
	/**
	 * 请款
	 */
	@Override
	public boolean reqMoney(RDPay pay, RDPrevent rdp, Long userId) {
		ReqMoneyBill reqbill = new ReqMoneyBill();
		reqbill.setVendorType(SettleConstant.RONGDUAN);
		reqbill.setVendorName(rdp.getVendorName());
		reqbill.setVendorCode(rdp.getVendorCode());
		reqbill.setPayTime(pay.getPayTime());
		reqbill.setTicketAmount(pay.getPayAmount());
		reqbill.setGoodsAmount(rdp.getSurplusAmount());
		reqbill.setChannelAmount(rdp.getWaringAmount());
		reqbill.setRelateSettleId(pay.getRecordId() + "");
		reqbill.setPayUserId(userId);
		reqbill = daoService.addObject(reqbill);
		pay.setReqMoneyId(reqbill.getRecordId() + "");
		daoService.updateObject(pay);
		return true;
	}
}
