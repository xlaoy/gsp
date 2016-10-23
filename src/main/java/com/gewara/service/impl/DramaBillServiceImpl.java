package com.gewara.service.impl;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.DramaConst;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.model.acl.User;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaBaseData;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaDiscountItem;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaOrderOffLineItem;
import com.gewara.model.drama.DramaOrderOffline;
import com.gewara.model.drama.DramaOrderOnLineItem;
import com.gewara.model.drama.DramaOrderOnline;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaPriceBill;
import com.gewara.model.drama.DramaPriceRate;
import com.gewara.model.drama.DramaRefundOffline;
import com.gewara.model.drama.DramaRefundOnline;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.model.drama.DramaVersion;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.service.DaoService;
import com.gewara.service.DramaBillService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;
import com.gewara.web.util.PageUtil;

@Service("dramaBillServiceImpl")
public class DramaBillServiceImpl implements DramaBillService {

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("config")
	protected Config config;
	
	ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 300l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	
	/**
	 * 演出结算单查询
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryDramaSettleBill(DramaSettleBill bill, Integer pageNo,
			Integer pageSize, String url, ModelMap model) {
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<DramaSettleBill> billList = new ArrayList<DramaSettleBill>();
		Long recordid = bill.getRecordid();
		Long dramaid = bill.getDramaid();
		String suppliercode = bill.getSuppliercode();
		String status = bill.getStatus();
		Long configid = bill.getConfigid();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(DramaSettleBill.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(DramaSettleBill.class);
		
		if(recordid != null) {
			queryCount.add(Restrictions.eq("recordid", recordid));
			queryList.add(Restrictions.eq("recordid", recordid));
		}
		if(dramaid != null) {
			queryCount.add(Restrictions.eq("dramaid", dramaid));
			queryList.add(Restrictions.eq("dramaid", dramaid));
		}
		if(StringUtils.isNotBlank(suppliercode)) {
			queryCount.add(Restrictions.eq("suppliercode", suppliercode));
			queryList.add(Restrictions.eq("suppliercode", suppliercode));
		}
		if(StringUtils.isNotBlank(status)) {
			queryCount.add(Restrictions.eq("status", status));
			queryList.add(Restrictions.eq("status", status));
		} else if(recordid == null && dramaid == null && configid == null && StringUtils.isEmpty(suppliercode)) {
			queryCount.add(Restrictions.eq("status", DramaConst.WAITINGPAY));
			queryList.add(Restrictions.eq("status", DramaConst.WAITINGPAY));
		}
		if(configid != null) {
			queryCount.add(Restrictions.eq("configid", configid));
			queryList.add(Restrictions.eq("configid", configid));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("starttime"));
		queryList.addOrder(Order.desc("suppliercode"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			billList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			
			List<Long> dramaids = BeanUtil.getBeanPropertyList(billList, "dramaid", true);
			Map<Long, Drama> dramamap = daoService.getObjectMap(Drama.class, dramaids);
			
			List<String> suppliercodes = BeanUtil.getBeanPropertyList(billList, "suppliercode", true);
			Map<String, DramaBaseData> suppliermap = getBaseData(suppliercodes, DramaConst.SUPPLIER, String.class);
			
			List<Long> configids = BeanUtil.getBeanPropertyList(billList, "configid", true);
			Map<Long, DramaConfig> configmap = daoService.getObjectMap(DramaConfig.class, configids);
			
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("recordid", recordid);
			m.put("dramaid", dramaid);
			m.put("suppliercode", suppliercode);
			m.put("status", status);
			m.put("configid", configid);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			model.put("dramamap", dramamap);
			model.put("suppliermap", suppliermap);
			model.put("configmap", configmap);
		}
		
		model.put("billList", billList);
		
		return model;
	}
	
	
	
	/**
	 * 演出查询
	 */
	@Override
	public ModelMap queryDrama(Drama drama, Long dramaversionid, Integer pageNo, Integer pageSize, String url, ModelMap model) {
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		List<Object> params = new ArrayList<Object>();
		Long recordid = drama.getRecordid();
		String dramaname = drama.getDramaname();
		String origin = drama.getOrigin();
		
		String querycount = "select count(1) as count ";
		String querylist = "select d.recordid, d.dramaname, d.pretype, d.origin, d.addtime, v.recordid as dramaversionid, v.starttime, v.endtime ";
		
		StringBuilder condition = new StringBuilder("");
		condition.append("from drama d, drama_version v where d.recordid = v.dramaid ");
		if(recordid != null) {
			condition.append("and d.recordid = ? ");
			params.add(recordid);
		}
		if(StringUtils.isNotEmpty(dramaname)) {
			condition.append("and d.dramaname like '%' || ? || '%' ");
			params.add(dramaname);
		}
		if(dramaversionid != null) {
			condition.append("and v.recordid = ? ");
			params.add(dramaversionid);
		}
		if(StringUtils.isNotEmpty(origin)) {
			condition.append("and d.origin = ? ");
			params.add(origin);
		}
		String page = " order by d.gspupdatetime limit ? offset ? ";
		
		List<Map<String, Object>> countlist = jdbcTemplate.queryForList(querycount + condition.toString(), params.toArray());
		
		if(CollectionUtils.isNotEmpty(countlist)) {
			int count = Integer.valueOf(countlist.get(0).get("count").toString());
			if(count > 0) {
				params.add(pageSize);
				params.add(pageNo * pageSize);
				mapList = jdbcTemplate.queryForList(querylist + condition.toString() + page, params.toArray());
				PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("recordid", recordid);
				m.put("dramaname", dramaname);
				m.put("dramaversionid", dramaversionid);
				m.put("origin", origin);
				pageUtil.initPageInfo(m);
				model.put("pageUtil", pageUtil);
			}
		}
		
		model.put("mapList", mapList);
		
		return model;
	}
	
	/**
	 * 演出配置查询
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryDramaConfig(DramaConfig dconfig, Integer pageNo,
			Integer pageSize, String url, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<DramaConfig> configList = new ArrayList<DramaConfig>();
		Long recordid = dconfig.getRecordid();
		Long dramaid = dconfig.getDramaid();
		String suppliercode = dconfig.getSuppliercode();
		String status = dconfig.getStatus();
		Long dramaversionid = dconfig.getDramaversionid();
		String settlebase = dconfig.getSettlebase();
		String distype = dconfig.getDistype();
		String settlecycle = dconfig.getSettlecycle();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(DramaConfig.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(DramaConfig.class);
		
		if(recordid != null) {
			queryCount.add(Restrictions.eq("recordid", recordid));
			queryList.add(Restrictions.eq("recordid", recordid));
		}
		if(dramaid != null) {
			queryCount.add(Restrictions.eq("dramaid", dramaid));
			queryList.add(Restrictions.eq("dramaid", dramaid));
		}
		if(StringUtils.isNotBlank(suppliercode)) {
			queryCount.add(Restrictions.eq("suppliercode", suppliercode));
			queryList.add(Restrictions.eq("suppliercode", suppliercode));
		}
		if(StringUtils.isNotBlank(status)) {
			queryCount.add(Restrictions.eq("status", status));
			queryList.add(Restrictions.eq("status", status));
		}
		if(dramaversionid != null) {
			queryCount.add(Restrictions.eq("dramaversionid", dramaversionid));
			queryList.add(Restrictions.eq("dramaversionid", dramaversionid));
		}
		if(StringUtils.isNotBlank(settlebase)) {
			queryCount.add(Restrictions.eq("settlebase", settlebase));
			queryList.add(Restrictions.eq("settlebase", settlebase));
		}
		if(StringUtils.isNotBlank(distype)) {
			queryCount.add(Restrictions.eq("distype", distype));
			queryList.add(Restrictions.eq("distype", distype));
		}
		if(StringUtils.isNotBlank(settlecycle)) {
			queryCount.add(Restrictions.eq("settlecycle", settlecycle));
			queryList.add(Restrictions.eq("settlecycle", settlecycle));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("status"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			configList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			
			List<Long> dramaids = BeanUtil.getBeanPropertyList(configList, "dramaid", true);
			Map<Long, Drama> dramamap = daoService.getObjectMap(Drama.class, dramaids);
			
			List<String> suppliercodes = BeanUtil.getBeanPropertyList(configList, "suppliercode", true);
			Map<String, DramaBaseData> suppliermap = new HashMap<String, DramaBaseData>();
			if(CollectionUtils.isNotEmpty(suppliercodes)) {
				suppliermap = getBaseData(suppliercodes, DramaConst.SUPPLIER, String.class);
			}
			
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("recordid", recordid);
			m.put("dramaid", dramaid);
			m.put("suppliercode", suppliercode);
			m.put("status", status);
			m.put("dramaversionid", dramaversionid);
			m.put("settlebase", settlebase);
			m.put("distype", distype);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			model.put("dramamap", dramamap);
			model.put("suppliermap", suppliermap);
		}
		
		model.put("configList", configList);
		
		return model;
	}
	
	/**
	 * 审批通过配置
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String passeDramConfig(List<DramaConfig> configList) {
		String result = "";
		for(DramaConfig dconfig : configList) {
			
			Long recordid = dconfig.getRecordid();
			
			if(!DramaConst.WAITAPPROVAL.equals(dconfig.getStatus())) {
				result = recordid + "不是未审批的配置不能审批通过！";
				continue;
			}
			Drama drama = daoService.getObject(Drama.class, dconfig.getDramaid());
			if(drama == null) {
				result = recordid + "没有查询到项目！";
				continue;
			}
			if(dconfig.getDramaversionid() == 0) {
				result = recordid + "没有关联到演出版本！";
				continue;
			}
			DetachedCriteria qry = DetachedCriteria.forClass(DramaPriceRate.class);
			qry.add(Restrictions.eq("configid", dconfig.getRecordid()));
			qry.add(Restrictions.eq("status", DramaConst.AVAILABLE));
			List<DramaPriceRate> prlist = daoService.findByCriteria(qry);
			if(CollectionUtils.isEmpty(prlist)) {
				result = recordid + "有效价格扣率配置为空！";
				continue;
			}
			DramaVersion dv = daoService.getObject(DramaVersion.class, dconfig.getDramaversionid());
			DetachedCriteria query = DramaUtil.getDramaPlayItemDetaByDV(dv);
			List<DramaPlayItem> playlist = daoService.findByCriteria(query);
			if(CollectionUtils.isEmpty(playlist)) {
				result = recordid + "演出版本没有查询到场次!";
				continue;
			}
			
			if(dconfig.getLastendtime() == null) {
				dconfig.setLastendtime(dconfig.getStarttime());
			}
			if(dconfig.getLastjitiendtime() == null) {
				dconfig.setLastjitiendtime(DateUtil.getMonthFirstDay(dconfig.getStarttime()));
			}
			if(dconfig.getStarttime().getTime() > dconfig.getEndtime().getTime()) {
				dconfig.setStatus(DramaConst.EXPIRED);
				dconfig.setCanjiti(DramaConst.N);
			} else {
				dconfig.setStatus(DramaConst.HASAPPROVAL);
				dconfig.setCanjiti(DramaConst.Y);
			}
			dconfig.setGspupdatetime(DateUtil.getCurFullTimestamp());
			daoService.updateObject(dconfig);
		}
		return result;
	}
	
	/**
	 * 基础数据查询
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryDramaBaseData(DramaBaseData dbData, Integer pageNo,
			Integer pageSize, String url, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 15 : pageSize;
		
		List<DramaBaseData> dataList = new ArrayList<DramaBaseData>();
		String type = dbData.getType();
		String code = dbData.getCode();
		String name = dbData.getName();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(DramaBaseData.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(DramaBaseData.class);
		
		if(StringUtils.isNotBlank(code)) {
			queryCount.add(Restrictions.eq("code", code));
			queryList.add(Restrictions.eq("code", code));
		}
		if(StringUtils.isNotBlank(name)) {
			queryCount.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
			queryList.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		if(StringUtils.isEmpty(type)) {
			type = DramaConst.PLACE;
		}
		queryCount.add(Restrictions.eq("type", type));
		queryList.add(Restrictions.eq("type", type));
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("code"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			dataList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("type", type);
			m.put("code", code);
			m.put("name", name);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("dataList", dataList);
		
		return model;
	}
	
	/**
	 * 演出结算单价格账单查询
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryDramaPriceBill(Long recordid, ModelMap model) {
		DramaSettleBill settlebill = daoService.getObject(DramaSettleBill.class, recordid);
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, settlebill.getConfigid());
		Drama drama = daoService.getObject(Drama.class, settlebill.getDramaid());
		DramaBaseData supplier = getBaseData(settlebill.getSuppliercode(), DramaConst.SUPPLIER, String.class);
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaPriceBill.class);
		query.add(Restrictions.eq("settlebillid", recordid));
		query.addOrder(Order.asc("price"));
		List<DramaPriceBill> pricebillList = daoService.findByCriteria(query);
		
		List<Long> placeids = BeanUtil.getBeanPropertyList(pricebillList, "placeid", true);
		Map<String, DramaBaseData> placemap = new HashMap<String, DramaBaseData>(); 
		if(CollectionUtils.isNotEmpty(placeids)) {
			placemap = getBaseData(placeids, DramaConst.PLACE, Long.class);
		}
		
		model.put("settlebill", settlebill);
		model.put("dconfig", dconfig);
		model.put("drama", drama);
		model.put("supplier", supplier);
		model.put("placemap", placemap);
		model.put("pricebillList", pricebillList);
		return model;
	}
	
	/**
	 * 查询计提单据
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryJitiBill(DramaJitiBill bill, Integer pageNo,
			Integer pageSize, String url, String isXls, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		if(StringUtils.isNotEmpty(isXls)) {
			pageSize = pageSize == null ? 10000 : pageSize;
		} else {
			pageSize = pageSize == null ? 10 : pageSize;
		}
		
		List<DramaJitiBill> jitiList = new ArrayList<DramaJitiBill>();
		Long dramaid = bill.getDramaid();
		String suppliercode = bill.getSuppliercode();
		String month = bill.getMonth();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(DramaJitiBill.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(DramaJitiBill.class);
		
		if(dramaid != null) {
			queryCount.add(Restrictions.eq("dramaid", dramaid));
			queryList.add(Restrictions.eq("dramaid", dramaid));
		}
		if(StringUtils.isNotBlank(suppliercode)) {
			queryCount.add(Restrictions.eq("suppliercode", suppliercode));
			queryList.add(Restrictions.eq("suppliercode", suppliercode));
		}
		if(StringUtils.isNotBlank(month)) {
			Timestamp starttime = DateUtil.parseTimestamp(month, "yyyy-MM");
			Timestamp endTime = DateUtil.getNextMonthFirstDay(starttime);
			queryCount.add(Restrictions.eq("starttime", starttime));
			queryCount.add(Restrictions.eq("endtime", endTime));
			queryList.add(Restrictions.eq("starttime", starttime));
			queryList.add(Restrictions.eq("endtime", endTime));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("starttime"));
		queryList.addOrder(Order.desc("suppliercode"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			jitiList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			
			List<Long> dramaids = BeanUtil.getBeanPropertyList(jitiList, "dramaid", true);
			Map<Long, Drama> dramamap = daoService.getObjectMap(Drama.class, dramaids);
			
			List<String> suppliercodes = BeanUtil.getBeanPropertyList(jitiList, "suppliercode", true);
			Map<String, DramaBaseData> suppliermap = getBaseData(suppliercodes, DramaConst.SUPPLIER, String.class);
			
			List<Long> configids = BeanUtil.getBeanPropertyList(jitiList, "configid", true);
			Map<Long, DramaConfig> configmap = daoService.getObjectMap(DramaConfig.class, configids);
			
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("dramaid", dramaid);
			m.put("suppliercode", suppliercode);
			m.put("month", month);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
			model.put("dramamap", dramamap);
			model.put("configmap", configmap);
			model.put("suppliermap", suppliermap);
		}
		
		model.put("jitiList", jitiList);
		
		return model;
	}
	
	/**
	 * 付款
	 */
	@Override
	public void payBill(List<DramaSettleBill> billList, String optUser) {
		for(DramaSettleBill settle : billList) {
			settle.setStatus(DramaConst.SETTLED);
			settle.setGspupdatetime(DateUtil.getCurFullTimestamp());
			daoService.updateObject(settle);
			
			//生成付款记录
			DownloadRecorder re = new DownloadRecorder(settle.getRecordid(), BillTypeEnums.PAYBILL);
			re.setSpecial(SettleConstant.DRAMA);
			re.setVendorNo(settle.getSuppliercode());
			re.setNativeMoney("" + new DecimalFormat("#.00").format(settle.getPayamount()));
			re.setOptUser(optUser);
			daoService.saveObject(re);
		}
	}
	
	/**
	 * 重新付款
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String rePayBill(Long recordid, String optUser) {
		DramaBaseData data = getBaseData(optUser, DramaConst.REPAYUSER, String.class);
		if(data == null) {
			return "重新付款失败，无权限！";
		}
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		if(bill == null) {
			return "重新付款失败，结算单" + recordid + "不存在！";
		}
		if(!DramaConst.SETTLED.equals(bill.getStatus())) {
			return "重新付款失败，结算单" + recordid + "的状态不是已付款状态！";
		}
		if(bill.getPayamount().doubleValue() <= 0) {
			return "重新付款失败，结算单" + recordid + "的付款金额不能小于或等于0！";
		}
		DownloadRecorder dlRecorder = null;
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("settlementId", recordid));
		query.add(Restrictions.eq("special", SettleConstant.DRAMA));
		query.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		List<DownloadRecorder> dls = daoService.findByCriteria(query);
		if (!dls.isEmpty()) {
			dlRecorder = dls.get(0);
			dlRecorder.setMaxDownCount(dlRecorder.getMaxDownCount() + 1);
			dlRecorder.setAddTime(DateUtil.getCurFullTimestamp());
			dlRecorder.setOptUser(optUser);
			dlRecorder.setNativeMoney("" + new DecimalFormat("#.00").format(bill.getPayamount()));
			dlRecorder.setStatus(SettleConstant.JSSUBMIT);
			daoService.updateObject(dlRecorder);
		} else {
			return "重新付款失败，付款记录不存在！";
		}
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(bill);
		return "重新付款成功！";
	}
	
	/**
	 * 查询多个DramaBaseData
	 * @param codeList
	 * @param type
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Object> Map<String, DramaBaseData> getBaseData(List<T> codeList, String type, Class<T> cls) {
		List<String> list = new ArrayList<String>();
		if("java.lang.String".equals(cls.getName())) {
			list = (List<String>)codeList;
		} else {
			for(T obj : codeList) {
				list.add(obj.toString());
			}
		}
		DetachedCriteria qry = DetachedCriteria.forClass(DramaBaseData.class);
		qry.add(Restrictions.eq("type", type));
		qry.add(Restrictions.in("code", list.toArray()));
		List<DramaBaseData> dataList = daoService.findByCriteria(qry);
		Map<String, DramaBaseData> map = new HashMap<String, DramaBaseData>();
		for(DramaBaseData data : dataList) {
			map.put(data.getCode(), data);
		}
		return map;
	}
	
	/**
	 * 查询单个DramaBaseData
	 * @param code
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Object> DramaBaseData getBaseData(T code, String type, Class<T> clz) {
		String c;
		if("java.lang.String".equals(clz.getName())) {
			c = (String)code;
		} else {
			c = code.toString();
		}
		DetachedCriteria qry = DetachedCriteria.forClass(DramaBaseData.class);
		qry.add(Restrictions.eq("type", type));
		qry.add(Restrictions.eq("code", c));
		List<DramaBaseData> dataList = daoService.findByCriteria(qry);
		if(CollectionUtils.isNotEmpty(dataList)) {
			return dataList.get(0);
		}
		return null;
	}
	
	
	/**
	 * 查询请款单
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryReqMoenyBill(DramaSettleBill bill, User user, ModelMap model) {
		Drama drama = daoService.getObject(Drama.class, bill.getDramaid());
		DramaBaseData supplier = getBaseData(bill.getSuppliercode(), DramaConst.SUPPLIER, String.class);
		Map<String, String> map = JsonUtils.readJsonToMap(supplier.getSpecial());
		model.put("drama", drama);
		model.put("user",user);
		model.put("bill", bill);
		model.put("supplier", supplier);
		model.put("reqDateStr", DateUtil.format(DateUtil.getCurDate(), "yyyy年MM月dd日"));
		model.put("cycle", DateUtil.format(bill.getStarttime(), "yyyy年MM月dd日") + " - " + DateUtil.format(bill.getEndtime(), "yyyy年MM月dd日") + " 票款");
		model.put("chsFmtMoney", MiscUtil.convertMoneyChineseFmt(bill.getPayamount()));
		model.put("bankAccount", map.get("bankAccount") == null ? "" : map.get("bankAccount").toString());
		model.put("bankName", map.get("bankName") == null ? "" : map.get("bankName").toString());
		return model;
	}
	
	/**
	 * 导出订单
	 */
	@Override
	public ModelMap exportOrder(Long recordid, ModelMap model) {
		DramaSettleBill settlebill = daoService.getObject(DramaSettleBill.class, recordid);
		if(settlebill == null) {
			return model;
		}
		List<DramaPriceBill> pricebilllist = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", recordid);
		if(CollectionUtils.isEmpty(pricebilllist)) {
			return model;
		}
		List<Long> pricebillids = BeanUtil.getBeanPropertyList(pricebilllist, "recordid", true);
		
		//线上
		String onlinesql = "select distinct i.tradeno from drama_orderonline_item i, drama_orderonline o where i.tradeno = o.tradeno and o.ordertime >= ? and o.ordertime < ? and i.pricebillid in " + DramaUtil.sqlinList(pricebillids, Long.class);
		List<String> onlinetrandenolist = jdbcTemplate.queryForList(onlinesql, String.class, settlebill.getStarttime(), settlebill.getEndtime());
		List<DramaOrderOnline> onlineorderlist = daoService.getObjectBatch(DramaOrderOnline.class, "tradeno", onlinetrandenolist);
		model.put("onlineorderlist", onlineorderlist);
		
		//线下
		String offlinesql = "select distinct i.tradeno from drama_orderoffline_item i, drama_orderoffline o where i.tradeno = o.tradeno and o.ordertime >= ? and o.ordertime < ? and i.pricebillid in " + DramaUtil.sqlinList(pricebillids, Long.class);
		List<String> offlinetrandenolist = jdbcTemplate.queryForList(offlinesql, String.class, settlebill.getStarttime(), settlebill.getEndtime());
		List<DramaOrderOffline> offlineorderlist = daoService.getObjectBatch(DramaOrderOffline.class, "tradeno", offlinetrandenolist);
		model.put("offlineorderlist", offlineorderlist);
		
		Drama drama = daoService.getObject(Drama.class, settlebill.getDramaid());
		model.put("drama", drama);
		
		return model;
	}
	
	/**
	 * 导出退单
	 */
	@Override
	public ModelMap exportRefund(Long recordid, ModelMap model) {
		DramaSettleBill settlebill = daoService.getObject(DramaSettleBill.class, recordid);
		if(settlebill == null) {
			return model;
		}
		List<DramaPriceBill> pricebilllist = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", recordid);
		if(CollectionUtils.isEmpty(pricebilllist)) {
			return model;
		}
		List<Long> pricebillids = BeanUtil.getBeanPropertyList(pricebilllist, "recordid", true);
		
		//线上
		String onlinesql = "select distinct i.tradeno from drama_orderonline_item i where i.hasrefund = 'Y' and i.refundpricebillid in " + DramaUtil.sqlinList(pricebillids, Long.class);
		List<String> onlinetrandenolist = jdbcTemplate.queryForList(onlinesql, String.class);
		List<DramaRefundOnline> onlinerefundlist = daoService.getObjectBatch(DramaRefundOnline.class, "tradeno", onlinetrandenolist);
		model.put("onlinerefundlist", onlinerefundlist);
		
		//线下
		String offlinesql = "select distinct i.tradeno from drama_orderoffline_item i where i.hasrefund = 'Y' and i.refundpricebillid in " + DramaUtil.sqlinList(pricebillids, Long.class);
		List<String> offlinetrandenolist = jdbcTemplate.queryForList(offlinesql, String.class);
		List<DramaRefundOffline> offlinerefundlist = daoService.getObjectBatch(DramaRefundOffline.class, "tradeno", offlinetrandenolist);
		model.put("offlinerefundlist", offlinerefundlist);
		
		Drama drama = daoService.getObject(Drama.class, settlebill.getDramaid());
		model.put("drama", drama);
		
		return model;
	}
	
	/**
	 * 导出线上优惠
	 */
	@Override
	public ModelMap exportDiscount(Long recordid, ModelMap model) {
		DramaSettleBill settlebill = daoService.getObject(DramaSettleBill.class, recordid);
		if(settlebill == null) {
			return model;
		}
		List<DramaPriceBill> pricebilllist = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", recordid);
		if(CollectionUtils.isEmpty(pricebilllist)) {
			return model;
		}
		List<Long> pricebillids = BeanUtil.getBeanPropertyList(pricebilllist, "recordid", true);
		
		String onlinesql = "select distinct i.tradeno from drama_orderonline_item i where i.pricebillid in " + DramaUtil.sqlinList(pricebillids, Long.class);
		List<String> onlinetrandenolist = jdbcTemplate.queryForList(onlinesql, String.class);
		List<DramaDiscountItem> discountlist = daoService.getObjectBatch(DramaDiscountItem.class, "relatedid", onlinetrandenolist);
		model.put("discountlist", discountlist);
		
		return model;
	}
	
	/**
	 * 导出订单
	 */
	@Override
	public ModelMap exportOrderItem(Long recordid, ModelMap model) {
		DramaPriceBill pricebill = daoService.getObject(DramaPriceBill.class, recordid);
		if(pricebill == null) {
			return model;
		}
		DramaSettleBill settlebill = daoService.getObject(DramaSettleBill.class, pricebill.getSettlebillid());
		if(settlebill == null) {
			return model;
		}
		
		Drama drama = daoService.getObject(Drama.class, settlebill.getDramaid());
		
		List<Map<String, Object>> orderList = getOrderItemByPriceBill(recordid);
		
		model.put("pricebill", pricebill);
		model.put("settlebill", settlebill);
		model.put("drama", drama);
		model.put("orderList", orderList);
		return model;
	}
	
	/**
	 * 根据价格对账单查询订单
	 * @param dconfig
	 * @param settlebill
	 * @param recordid
	 * @return
	 */
	private List<Map<String, Object>> getOrderItemByPriceBill(Long recordid) {
		
		List<Map<String, Object>> orderList = new ArrayList<Map<String, Object>>();
		
		//线下
		Future<List<Map<String, Object>>> offlinefuture = pool.submit(new Callable<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> call() throws Exception {
				StringBuilder offlinesql = new StringBuilder("");
				offlinesql.append("select o.username, o.status, o.paidtime, o.otherfee, o.ordertime, 'OFFLINE' as orderfrom, ");
				offlinesql.append("i.tradeno, i.quantity, (i.totalfee - i.disfee) as totalfee, i.hasrefund, i.taopiaonum ");
				offlinesql.append("from drama_orderoffline o, drama_orderoffline_item i ");
				offlinesql.append("where i.tradeno = o.tradeno ");
				offlinesql.append("and i.pricebillid = ? order by o.ordertime ");
				return jdbcTemplate.queryForList(offlinesql.toString(), recordid);
			}
		});
		
		//线上
		StringBuilder onlinesql = new StringBuilder("");
		onlinesql.append("select o.memberid, o.status, o.paidtime, o.otherfee, o.disreason, o.ordertime, 'ONLINE' as orderfrom, ");
		onlinesql.append("i.tradeno, i.quantity, (i.totalfee - i.disfee) as totalfee, i.hasrefund, i.taopiaonum ");
		onlinesql.append("from drama_orderonline o, drama_orderonline_item i ");
		onlinesql.append("where i.tradeno = o.tradeno ");
		onlinesql.append("and i.pricebillid = ? order by o.ordertime ");
		List<Map<String, Object>> onlineorderList = jdbcTemplate.queryForList(onlinesql.toString(), recordid);
		orderList.addAll(onlineorderList);
		
		try {
			orderList.addAll(offlinefuture.get());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//票务
		
		return orderList;
	}
	
	/**
	 * 查询订单
	 */
	@Override
	public ModelMap queryDramaOrder(String tradeno, String orderform, ModelMap model) {
		if(DramaConst.ONLINE.equals(orderform)) {
			DramaOrderOnline order = daoService.getObject(DramaOrderOnline.class, tradeno);
			if(order != null) {
				model.put("order", order);
				
				Drama drama = daoService.getObject(Drama.class, order.getDramaid());
				model.put("drama", drama);
				
				List<DramaOrderOnLineItem> onlineitems = daoService.getObjectListByField(DramaOrderOnLineItem.class, "tradeno", tradeno);
				model.put("itemlist", onlineitems);
				if(CollectionUtils.isNotEmpty(onlineitems)) {
					Long placeid = onlineitems.get(0).getPlaceid();
					DramaBaseData place = getBaseData(placeid, DramaConst.PLACE, Long.class);
					model.put("place", place);
					List<Long> pricebillids = BeanUtil.getBeanPropertyList(onlineitems, "pricebillid", true);
					List<Long> refundpricebillids = BeanUtil.getBeanPropertyList(onlineitems, "refundpricebillid", true);
					if(CollectionUtils.isNotEmpty(pricebillids)) {
						Map<Long, DramaPriceBill> pricebillmap = daoService.getObjectMap(DramaPriceBill.class, pricebillids);
						model.put("pricebillmap", pricebillmap);
					}
					if(CollectionUtils.isNotEmpty(refundpricebillids)) {
						Map<Long, DramaPriceBill> refundpricebillmap = daoService.getObjectMap(DramaPriceBill.class, refundpricebillids);
						model.put("refundpricebillmap", refundpricebillmap);
					}
				}
				
				DramaRefundOnline refund = daoService.getObject(DramaRefundOnline.class, tradeno);
				if(refund == null) {
					model.put("refund", "null");
				} else {
					model.put("refund", refund);
				}
				
				List<DramaDiscountItem> discountlist = daoService.getObjectListByField(DramaDiscountItem.class, "relatedid", tradeno);
				if(CollectionUtils.isEmpty(discountlist)) {
					model.put("discountlist", "null");
				} else {
					model.put("discountlist", discountlist);
				}
			}
		} else if(DramaConst.OFFLINE.equals(orderform)) {
			DramaOrderOffline order = daoService.getObject(DramaOrderOffline.class, tradeno);
			if(order != null) {
				model.put("order", order);
				
				Drama drama = daoService.getObject(Drama.class, order.getDramaid());
				model.put("drama", drama);
				
				List<DramaOrderOffLineItem> offlineitems = daoService.getObjectListByField(DramaOrderOffLineItem.class, "tradeno", tradeno);
				model.put("itemlist", offlineitems);
				if(CollectionUtils.isNotEmpty(offlineitems)) {
					Long placeid = offlineitems.get(0).getPlaceid();
					DramaBaseData place = getBaseData(placeid, DramaConst.PLACE, Long.class);
					model.put("place", place);
					List<Long> pricebillids = BeanUtil.getBeanPropertyList(offlineitems, "pricebillid", true);
					List<Long> refundpricebillids = BeanUtil.getBeanPropertyList(offlineitems, "refundpricebillid", true);
					if(CollectionUtils.isNotEmpty(pricebillids)) {
						Map<Long, DramaPriceBill> pricebillmap = daoService.getObjectMap(DramaPriceBill.class, pricebillids);
						model.put("pricebillmap", pricebillmap);
					}
					if(CollectionUtils.isNotEmpty(refundpricebillids)) {
						Map<Long, DramaPriceBill> refundpricebillmap = daoService.getObjectMap(DramaPriceBill.class, refundpricebillids);
						model.put("refundpricebillmap", refundpricebillmap);
					}
				}
				
				DramaRefundOffline refund = daoService.getObject(DramaRefundOffline.class, tradeno);
				if(refund == null) {
					model.put("refund", "null");
				} else {
					model.put("refund", refund);
				}
				model.put("discountlist", "null");
			}
		}
		return model;
	}
	
	
}

