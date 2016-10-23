package com.gewara.service.impl;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.component.CityInfoHolder;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.OrderTypeEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.GoodsSettleService;
import com.gewara.service.SettleJitiService;
import com.gewara.service.SettlementBillService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;
import com.gewara.vo.UnPrintGoodsVo;
import com.gewara.web.util.PageUtil;

/**
 * 
 * @ClassName: GoodsSettleServiceImpl
 * @Description: 卖品对账单逻辑实现类
 * @author yujun.su@gewara.com
 * @date 2014-9-26 下午3:22:52
 */
@Service("goodsSettleService")
public class GoodsSettleServiceImpl implements GoodsSettleService {
	
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
	@Qualifier("cityInfoHolder")
	private CityInfoHolder cityInfoHolder; 
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	@Autowired
	@Qualifier("monitorService")
	protected MonitorService monitorService;
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	@Autowired
	@Qualifier("settleJitiServiceImpl")
	private SettleJitiService settleJitiService;
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
	
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBill  
	 * @Description:查询卖品对账单数据
	 * @param  model
	 * @param  cinemaId
	 * @param  startTime
	 * @param  endTime
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap queryGoodsCheckBill(ModelMap model, String cinemaId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		pageNo = (pageNo == null) ? 0 : pageNo;
		pageSize = (pageSize == null) ? 20 : pageSize;
		String configId = "";
		if(StringUtils.isNotEmpty(cinemaId)){
			configId = RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_GOODS,Long.valueOf(cinemaId.split(",")[0].toString()));			
		}
		//总数
		int total = countGoodsCheckBill(configId, startTime, endTime);
		//查询结果
		List<CheckBill> goodsSettleCheckBills = queryGoodsCheckBillByPage(configId, startTime, endTime, pageNo, pageSize);
		model.put("goodsSettleCheckBills", goodsSettleCheckBills);
		
		List<Long> relateIds = BeanUtil.getBeanPropertyList(goodsSettleCheckBills, "relateId", true);
		if(CollectionUtils.isNotEmpty(relateIds)){
			DetachedCriteria query = DetachedCriteria.forClass(Place.class);
			query.add(Restrictions.in("relateId", relateIds));
			List<Place> places = daoService.findByCriteria(query);
			Map<String, Place> placeMap = BeanUtil.beanListToMap(places, "relateId");			
			model.put("pm", placeMap);		
		}
		//分页参数
		PageUtil pageUtil = new PageUtil(total, pageSize, pageNo,config.getBasePath()+ "platform/goodssettle/queryGoodsCheckBill.xhtml",true, true);
		Place place = daoService.getObject(Place.class, cinemaId);
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("placeId", cinemaId);
		params.put("startTime", DateUtil.formatDate(startTime));
		params.put("endTime", DateUtil.formatDate(endTime));
		if(null != place){
			params.put("placeFirstLetter", place.getName());			
		}
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return model;
	}
	
	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBillByPage  
	 * @Description:
	 * @param cinemaId
	 * @param startTime
	 * @param endTime
	 * @param pageNo
	 * @param pageSize
	 * @return List<GoodsSettleCheckBill>
	 * @throws  
	 */
	private List<CheckBill> queryGoodsCheckBillByPage(String cinemaId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		DetachedCriteria query = getGoodsCheckBillDetachedCriteria(cinemaId, startTime, endTime);
		List<CheckBill> list = daoService.findByCriteria(query, pageSize * pageNo, pageSize);
		return list;
	}

	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBillNumber  
	 * @Description:查询卖品对账单总数
	 * @param  cinemaId
	 * @param  startTime
	 * @param  endTime
	 * @param 
	 * @return int
	 * @throws
	 */
	private int countGoodsCheckBill(String cinemaId, Timestamp startTime,
			Timestamp endTime) {
		DetachedCriteria query = getGoodsCheckBillDetachedCriteria(cinemaId, startTime, endTime);
		query.setProjection(Projections.rowCount());
		Object result = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(result.toString());
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getGoodsCheckBillDetachedCriteria  
	 * @Description:
	 * @param cinemaId
	 * @param startTime
	 * @param endTime
	 * @param query
	 * @return void
	 * @throws  
	 */
	private DetachedCriteria getGoodsCheckBillDetachedCriteria(String cinemaId,
			Timestamp startTime, Timestamp endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
		if(StringUtils.isNotEmpty(cinemaId)){
			query.add(Restrictions.eq("configId", cinemaId));
		}
		
		if(startTime!=null){
			query.add(Restrictions.ge("start", startTime));
		}
		
		if(endTime!=null){
			query.add(Restrictions.lt("end", endTime));
		}
		query.add(Restrictions.eq("tag", OrderTypeEnums.GOODS.getType()));
		return query;
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsOrderDetails  
	 * @Description:
	 * @param model
	 * @param billId
	 * @param from
	 * @param pageNo
	 * @param pageSize
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap queryGoodsOrderDetails(ModelMap model, Long billId,
			String from, Integer pageNo, Integer pageSize,String orderNo,String placeId,
			Timestamp startTime, Timestamp endTime) {
		pageNo = (pageNo == null ? 0 : pageNo);
		pageSize = (pageSize == null ? 30 : pageSize);
		
		//查询结果
		List<GewaOrder> list = queryGoodsOrderByPage(from, billId, pageNo, pageSize,orderNo,placeId,startTime,endTime);
		model.put("goodsOrderList", list);
		
		//影院列表
		List<Long> cinemaIds = BeanUtil.getBeanPropertyList(list, "relateId", true);
		
		Map<Long, String> placeMap = new HashMap<Long, String>();
		for(Long cid : cinemaIds){
			placeMap.put(cid,daoService.getObjectListByField(Place.class, "relateId", cid).get(0).getName());
		}
		
		
		model.put("pm", placeMap);		
		
		int total = countGoodsOrderByCondition(from,billId,orderNo,placeId,startTime,endTime);
		Place place = daoService.getObject(Place.class, placeId);
		//分页参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("placeId", placeId);
		params.put("from", from);
		params.put("billId", billId);
		if(null != place){
			params.put("placeFirstLetter", place.getName());
		}
		params.put("orderNo", orderNo);
		params.put("startTime", DateUtil.formatDate(startTime));
		params.put("endTime", DateUtil.formatDate(endTime));
		PageUtil pageUtil = new PageUtil(total, pageSize, pageNo,config.getBasePath()+ "platform/goodssettle/queryGoodsOrderDetails.xhtml",true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		return model;
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: countGoodsOrderByCondition  
	 * @Description:订单明细查询计数
	 * @param  from
	 * @param  billId
	 * @return int
	 * @throws
	 */
	private int countGoodsOrderByCondition(String from,Long billId,String orderNo,String placeId,Timestamp startTime,Timestamp endTime) {
		DetachedCriteria query = getGoodsOrderDetachedCriteria(from,billId,orderNo,placeId,startTime,endTime);
		query.setProjection(Projections.rowCount());
		Object count = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(count.toString());
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsOrderByPage  
	 * @Description: 分页查询结果
	 * @param from
	 * @param billId
	 * @param pageNo
	 * @param pageSize
	 * @return List<GewaOrder>
	 * @throws
	 */
	private List<GewaOrder> queryGoodsOrderByPage(String from, Long billId,
			Integer pageNo, Integer pageSize, String orderNo, String placeId,
			Timestamp startTime, Timestamp endTime) {
		List<GewaOrder> list = new ArrayList<GewaOrder>();
		DetachedCriteria query = getGoodsOrderDetachedCriteria(from, billId,orderNo,placeId,startTime,endTime);
		list = daoService.findByCriteria(query, pageSize * pageNo, pageSize);
		return list;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getGoodsOrderDetachedCriteria  
	 * @Description:
	 * @param from
	 * @param billId
	 * @return void
	 * @throws  
	 */
	private DetachedCriteria getGoodsOrderDetachedCriteria(String from, Long billId,String orderNo,String placeId,Timestamp startTime,Timestamp endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (StringUtils.isNotEmpty(from)) {
			if (SettleConstant.CB_GOODS_DETAIL_FROM.equals(from)) {
				// 先查询出对账单，按对账单时间、影院查询卖品订单
				CheckBill checkBill = daoService.getObject(CheckBill.class, billId);
				if (checkBill != null) {
					// 打票时间在此区间的卖品应纳入此卖品账单。如 1月1日0点0分到2月1日0点0分
					if (checkBill.getStart() != null && checkBill.getEnd() != null) {
						query.add(Restrictions.ge("takeTime",checkBill.getStart()))
							 .add(Restrictions.lt("takeTime",checkBill.getEnd()));
					}
					//影院ID
					String p = checkBill.getConfigId().split(",")[0];
					query.add(Restrictions.eq("relateId", Long.valueOf(p)));
				}
			} else if (SettleConstant.SB_GOODS_DETAIL_FROM.equals(from)) {
				// 先查询出结算单，按结算单时间、影院查询卖品订单
				SettlementBill settleBill = daoService.getObject(SettlementBill.class, billId);
				if(settleBill != null){
					if(settleBill.getStartTime()!=null && settleBill.getEndTime()!=null){
						query.add(Restrictions.ge("takeTime", settleBill.getStartTime()))
							 .add(Restrictions.lt("takeTime", settleBill.getEndTime()));
					}
				}
				if(StringUtils.isNotEmpty(placeId)){
					query.add(Restrictions.eq("relateId", Long.valueOf(placeId.split(",")[0])));
				}
			}
		} else {
			// 按查询条件查询
			if(StringUtils.isNotEmpty(placeId)){
				//影院ID
				String p = placeId.split(",")[0];
				query.add(Restrictions.eq("relateId", Long.valueOf(p)));
			}
			if(StringUtils.isNotEmpty(orderNo)){
				query.add(Restrictions.eq("tradeno", orderNo));
			}
			if(startTime!=null){
				query.add(Restrictions.gt("takeTime", startTime));
			}
			if(endTime!=null){
				query.add(Restrictions.lt("takeTime", endTime));
			}
			//卖品
		}
		query.add(Restrictions.eq("tag", "GOODS"));
		MiscUtil.appendCategoryQueryCondition(query);
		return query;
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsSettleBill  
	 * @Description:卖品结算单查询
	 * @param model
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @param pageNo
	 * @param pageSize
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap queryGoodsSettleBill(ModelMap model, String placeId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		pageNo = (pageNo == null ? 0 : pageNo);
 		pageSize = (pageSize == null ? 10 : pageSize);
		
		List<SettlementBill> list = queryGoodsSettleBillByPage(placeId,startTime,endTime,pageNo,pageSize);
		model.put("goodsSettleBills", list);
		
		List<String> configIds = BeanUtil.getBeanPropertyList(list, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, configIds);
		model.put("pm", placeMap);
		
		int count = countGoodsSettleBill(placeId,startTime,endTime);
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath()+ "platform/goodssettle/queryGoodsSettleBill.xhtml", true, true);
		Place place = daoService.getObject(Place.class, placeId);
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("placeId", placeId);
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		if(null != place){
			params.put("placeFirstLetter", place.getName());
		}
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return model;
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: countGoodsSettleBill  
	 * @Description:
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @return int
	 * @throws
	 */
	private int countGoodsSettleBill(String placeId, Timestamp startTime,
			Timestamp endTime) {
		DetachedCriteria query = getGoodsSettleBillDetachedCriteria(placeId, startTime, endTime);
		query.setProjection(Projections.rowCount());
		Object o = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(o.toString());
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsSettleBillByPage  
	 * @Description:分页查询卖品结算单数据
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @param pageNo
	 * @param pageSize
	 * @return List<SettlementBill>
	 * @throws
	 */
	private List<SettlementBill> queryGoodsSettleBillByPage(String placeId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		DetachedCriteria query = getGoodsSettleBillDetachedCriteria(placeId,startTime,endTime);
		query.addOrder(Order.desc("startTime"));
		List<SettlementBill> bills = daoService.findByCriteria(query, pageNo * pageSize, pageSize);
		return bills;
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: getGoodsSettleBillDetachedCriteria  
	 * @Description: 卖品结算单查询，拼接查询条件
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @return DetachedCriteria
	 * @throws
	 */
	private DetachedCriteria getGoodsSettleBillDetachedCriteria(String placeId,
			Timestamp startTime, Timestamp endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if(StringUtils.isNotEmpty(placeId)){
			query.add(Restrictions.eq("configId", placeId));
		}
		if(startTime!=null){
			query.add(Restrictions.ge("startTime", startTime));
		}
		if(endTime!=null){
			query.add(Restrictions.le("endTime", endTime));
		}
		query.add(Restrictions.eq("tag", "GOODS"));
		return query;
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantGoodsSettleBills  
	 * @Description:卖品结算单商户端数据加载
	 * @param proviceCode
	 * @param placeId
	 * @param placeIds
	 * @param allowPlaceIds
	 * @param start
	 * @param end
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @param model
	 * @param isXls
	 * @param request
	 * @param response
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap loadMerchantGoodsSettleBills(String proviceCode,
			String placeId,List<String> placeIds,List<String> allowPlaceIds, Timestamp start, Timestamp end, String status,
			Integer pageNo, Integer pageSize, ModelMap model, String isXls,
			HttpServletRequest request, HttpServletResponse response) {
		
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("proviceMap", cityInfoHolder.getProviceByPlaceId(allowPlaceIds));
		
		boolean isDefaulfStatus = true;
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 1000 : pageSize == null ? 150 : pageSize;
		status = StringUtils.isBlank(status) ? CheckBillStatusEnums.NEW.getStatus() : status;
		isDefaulfStatus = CheckBillStatusEnums.NEW.getStatus().equals(status) ? true : false;
		model.put("placeId", placeId);
		model.put("place", daoService.getObject(Place.class, placeId));
		
		int totalNumber = countGoodsSettleBillsForMch(placeIds, start, end, status, isDefaulfStatus);
		List<SettlementBill>  settleBills = queryGoodsSettleBillsForMch(placeIds, start, end, status,pageNo, pageSize, isDefaulfStatus);
		
		setupSettleBills(settleBills, model);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo,
				config.getBasePath() + "merchant/goodssettle/openMerchantGoodsSettleBills.xhtml",
				true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		
		m.put("proviceCode", proviceCode);
		m.put("start", start);
		m.put("end", end);
		m.put("status", status);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		return model;
	}

	/**
	 * 
	 * @param placeIds
	 * @param start
	 * @param end
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @param isDefaulfStatus
	 * @return
	 */
	private List<SettlementBill> queryGoodsSettleBillsForMch(
			List<String> placeIds, Timestamp start, Timestamp end,
			String status, Integer pageNo, Integer pageSize,
			boolean isDefaulfStatus) {
		DetachedCriteria query = getQueryGoodsSettleForMch(placeIds, start, end, status, isDefaulfStatus);
		List<SettlementBill> result = daoService.findByCriteria(query, pageNo * pageSize, pageSize);
		return result;
	}

	/**
	 * 
	 * @param placeIds
	 * @param start
	 * @param end
	 * @param status
	 * @param isStatusNotEq
	 * @return
	 */
	private int countGoodsSettleBillsForMch(List<String> placeIds,
			Timestamp start, Timestamp end, String status,
			boolean isStatusNotEq) {
		DetachedCriteria query = getQueryGoodsSettleForMch(placeIds, start, end, status,isStatusNotEq);
		query.setProjection(Projections.rowCount());
		Object o = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(o.toString());
	}
	
	/**
	 * 
	 * @param settleBills
	 * @param model
	 */
	private void setupSettleBills(List<SettlementBill>  settleBills, ModelMap model){
		List<String> recordIds = BeanUtil.getBeanPropertyList(settleBills, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, recordIds);
		Map<String, SettleConfig> scm = daoService.getObjectMap(SettleConfig.class, recordIds);
		model.put("sb", settleBills);
		model.put("pm", placeMap);
		model.put("scm", scm);
		model.put("baseSettle", true);
	}
	
	/**
	 * 
	 * @param placeIds
	 * @param start
	 * @param end
	 * @param status
	 * @param isStatusNotEq
	 * @return
	 */
	private DetachedCriteria getQueryGoodsSettleForMch(List<String> placeIds,
			Timestamp start, Timestamp end, String status,boolean isStatusNotEq) {
		DetachedCriteria query = DetachedCriteria
				.forClass(SettlementBill.class);
		if(CollectionUtils.isNotEmpty(placeIds)){
			query.add(Restrictions.in("configId", placeIds));		
		}
		if(start != null){
			query.add(Restrictions.ge("startTime", start));
		}
		if(end != null){
			query.add(Restrictions.lt("endTime", end));
		}
		if(StringUtils.isNotEmpty(status)){
			if (!isStatusNotEq){
				query.add(Restrictions.eq("status", status));				
			}else{
				query.add(Restrictions.ne("status", status));
			}
		}
		/**
		 * 卖品结算结算单查询
		 */
		query.add(Restrictions.eq("tag", "GOODS"));
		return query;
	}
	
	/**
	 * 
	 * @param downloadType
	 * @param fileName
	 * @param response
	 */
	protected void download(String downloadType, String fileName,
			HttpServletResponse response) {
		if (StringUtils.equals(downloadType, "xls")) {
			response.setContentType("application/xls");
		} else if (StringUtils.equals(downloadType, "jpg")) {
			response.setContentType("image/jpeg");
		} else {
			response.setContentType("application/x-download");
		}
		fileName = StringUtils.isBlank(fileName) ? "gewara"
				+ DateUtil.format(new Date(), "yyMMdd_hhmmss") : fileName;
		try {
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String(fileName.getBytes("gb2312"), "ISO8859-1")
					+ "." + downloadType);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param downloadType
	 * @param response
	 */
	protected void download(String downloadType, HttpServletResponse response) {
		download(downloadType, "", response);
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantOrderDetails  
	 * @Description:卖品结算商户端结算单明细查询
	 * @param model
	 * @param billId
	 * @param from
	 * @param pageNo
	 * @param pageSize
	 * @param orderNo
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @return ModelMap
	 * @throws
	 */
	@Override
	public ModelMap loadMerchantOrderDetails(ModelMap model, Long billId,
			String from, Integer pageNo, Integer pageSize, String orderNo,
			String placeId, Timestamp startTime, Timestamp endTime) {
		pageSize = (pageSize == null ? 150 : pageSize);
		pageNo = (pageNo == null ? 0 : pageNo);
		List<GewaOrder> orders = this.queryGoodsOrderByPage(from, billId, pageNo, pageSize, orderNo, placeId, startTime, endTime);
		model.put("orders", orders);
		int total = countGoodsOrderByCondition(from,billId,orderNo,placeId,startTime,endTime);
		//分页参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("from", from);
		params.put("billId", billId);
		
		PageUtil pageUtil = new PageUtil(total, pageSize, pageNo,config.getBasePath()+ "platform/goodssettle/queryGoodsOrderDetails.xhtml",true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return model;
	}

	/**
	 *  生成卖品结算单
	 */
	@Override
	public void generateGoodsSettleBill() {
		List<SettleConfig> settleConfigs = getGoodsSettleConfigs();
		String tag = SettleConstant.TAG_SETTLEMENTBILL_GOODS;
		Map<String,Timestamp> lastSettleBillsTime = settlementBillService.getLastSettleBillsEndTimeByConfigs(tag);
		for (SettleConfig settleConfig : settleConfigs){
			Timestamp lastEndTime = lastSettleBillsTime.get(settleConfig.getRecordId()) == null ? settleConfig.getFirstSettle() : lastSettleBillsTime.get(settleConfig.getRecordId());
			Timestamp endTime = new Timestamp(DateUtils.addMonths(lastEndTime, 1).getTime());
			Timestamp now = DateUtil.getCurTruncTimestamp();
			if(endTime.after(now)){
				continue;
			}
			Long relateId = settleConfig.getRelateId();
			String configId = settleConfig.getRecordId();
			SettlementBill initedBill = new SettlementBill(lastEndTime, endTime, tag, relateId, configId); 
			daoService.saveObject(initedBill);
			checkBillService.createCheckBillBySettleBill(initedBill);
		}
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: getGoodsSettleConfigs  
	 * @Description:
	 * @param @return
	 * @return List<SettleConfig>
	 * @throws  
	 */
	private List<SettleConfig> getGoodsSettleConfigs() {
		String hql = "from SettleConfig  where isGoodsSettle = ? and status = ?";
		List<SettleConfig> configs = daoService.findByHql(hql,SettleConstant.IS_GOODS_SETTLE_Y, "Y");
		return configs;
	}
	
	/**
	 * 
	 */
	@Override
	public void autoToMerchant(SettlementBill bill) {
		ChangeEntry ce = new ChangeEntry(bill);
		fulFillSettlementBill(bill);
		StatusTracker st = new StatusTracker(AdjustTypeEnums.SETTLEBILL.getType(), bill.getRecordId(), "System","", "INIT", bill.getStatus(), "GEWA");
		monitorService.saveChangeLog( 0L,SettlementBill.class, bill.getRecordId(), ce.getChangeMap(bill));
		daoService.saveObject(st);
	}

	/**
	 * 
	 * @param bill
	 */
	private void fulFillSettlementBill(SettlementBill bill) {
		List<CheckBill> ckList = checkBillService.goodsCheckBillCollection(bill);
		if(CollectionUtils.isEmpty(ckList)) {
			bill.setStatus(CheckBillStatusEnums.NEW.getStatus());
			daoService.updateObject(bill);
			return;
		}
		
		//hibernateTemplate.flush();
		
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
		
		//Map<String, Double> settleBillPreviousRefund = aggrePreviousGoodsRefund(bill);  
		
		//totalNum -= settleBillPreviousRefund.get("previousRefundNumber");
		//totalAmount -= settleBillPreviousRefund.get("previousRefundAmount");
		
		//bill.setPreviousRefundNumber(settleBillPreviousRefund.get("previousRefundNumber"));
		//bill.setPreviousRefundAmount(settleBillPreviousRefund.get("previousRefundAmount"));
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
	 * 卖品往期差异
	 * @param bill
	 * @return
	 */
	private Map<String, Double> aggrePreviousGoodsRefund(SettlementBill bill) {
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.eq("isSettled", "N"));
		query.add(Restrictions.eq("tag", bill.getTag()));
		query.add(Restrictions.eq("relateId",bill.getRelateId()));
		MiscUtil.appendCategoryQueryCondition(query);
		
		query.add(Restrictions.le("refundTime", bill.getStartTime()));
		query.add(Restrictions.ge("refundTime", DateUtils.addMonths(bill.getStartTime(), -2)));
		
		Double previousRefundNumber = 0.0;
		Double previousRefundAmount = 0.0;
		List<Refundment> refundments = daoService.findByCriteria(query);
		
		for (Refundment refund : refundments){
			previousRefundNumber += refund.getQuantity();
			previousRefundAmount += refund.getOldSettle() - refund.getNewSettle();
			refund.setSettleBillId(bill.getRecordId());
			refund.setIsSettled("Y");
		}
		daoService.saveObjectList(refundments);
		
		Map<String, Double> aggrePreviousRefund = new HashMap<String, Double>();
		aggrePreviousRefund.put("previousRefundNumber", previousRefundNumber);
		aggrePreviousRefund.put("previousRefundAmount", previousRefundAmount);
		return aggrePreviousRefund;
	}
	

	/**
	 * 根据结算单Id查询对账单记录
	 */
	@Override
	public ModelMap queryCheckBillBySettleId(ModelMap model, Long settleId) {
		SettlementBill sbill = daoService.getObject(SettlementBill.class, settleId);
		List<CheckBill> cks = daoService.getObjectListByField(CheckBill.class, "settlementId", sbill.getRecordId());
		model.put("goodsSettleCheckBills", cks);
		
		List<String> cinemaIds = BeanUtil.getBeanPropertyList(cks, "relateId", true);
		
		if(CollectionUtils.isNotEmpty(cinemaIds)){
			DetachedCriteria query = DetachedCriteria.forClass(Place.class);
			query.add(Restrictions.in("relateId", cinemaIds));
			List<Place> places = daoService.findByCriteria(query);
			Map<String, Place> placeMap = BeanUtil.beanListToMap(places, "relateId");			
			model.put("pm", placeMap);		
		}
		
		return model;
	}

	/**
	 * 
	 */
	@Override
	public ModelMap queryUnPrintTicketOrders(ModelMap model, Timestamp start,
			Timestamp end, String cinemaId, Integer pageNo, Integer pageSize) {
		Long placeId = null;
		List<UnPrintGoodsVo> result = new ArrayList<UnPrintGoodsVo>();
		String tag = SettleConstant.TAG_SETTLEMENTBILL_GOODS;
		if(StringUtils.isNotEmpty(cinemaId)){
			placeId = Long.valueOf(cinemaId.split(",")[0].toString());
		}
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if(start != null && end != null){
			query.add(Restrictions.ge("dealTime", start));
			query.add(Restrictions.lt("dealTime", end));
		}else{
			//默认查询一周
			start = DateUtil.addDay(DateUtil.getCurFullTimestamp(), -7);
			end = DateUtil.getCurFullTimestamp();
			query.add(Restrictions.ge("dealTime", start));
			query.add(Restrictions.lt("dealTime",end));
		}
		if(placeId != null){
			query.add(Restrictions.eq("relateId", placeId));
		}
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.isNull("takeTime"));
		MiscUtil.appendCategoryQueryCondition(query);
		query.add(Restrictions.eq("category", SettleConstant.ORDER_TYPE_GOODS));
		List<GewaOrder> orders = daoService.findByCriteria(query);
		List<Long> placeIds = BeanUtil.getBeanPropertyList(orders, "relateId", true);
		List<String> recordIds = new ArrayList<String>();
		for(Long pid : placeIds){
			recordIds.add(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, pid));
		}
		Map<String,Place> placeMap = daoService.getObjectMap(Place.class, recordIds);
		
		Map<Long,List<GewaOrder>> orderGroup = BeanUtil.groupBeanList(orders, "relateId");
		for(Long pid : placeIds){
			//查询卖品退票
			List<GewaOrder> list = orderGroup.get(pid);
			//聚合卖品订单
			Map<String, Object> orderAggre = aggreOrders(list);
			//聚合卖品退票
			List<String> tradeNos = BeanUtil.getBeanPropertyList(list, "tradeno", true);
			DetachedCriteria refundQuery = DetachedCriteria.forClass(Refundment.class);
			refundQuery.add(Restrictions.eq("isSettled", "Y"));
			refundQuery.add(Restrictions.in("tradeno", tradeNos));
			List<Refundment> refundments =  daoService.findByCriteria(refundQuery);
			Map<String, Object> refundAggre = aggreRefund(refundments);
			Integer unPrintQuantity = Integer.valueOf(orderAggre.get("orderTotal").toString()) - Integer.valueOf(refundAggre.get("refundTotal").toString());
			Integer unPrintAmount = Integer.valueOf(orderAggre.get("orderAmount").toString()) - Integer.valueOf(refundAggre.get("refundAmount").toString());
			UnPrintGoodsVo vo = new UnPrintGoodsVo(pid, start, end, unPrintQuantity, unPrintAmount);
			result.add(vo);
		}
		model.put("pm", placeMap);
		model.put("result", result);
		return model;
	}

	/**
	 * 
	 * @param orders
	 * @return
	 */
	private Map<String, Object> aggreOrders(List<GewaOrder> orders) {
		Map<String, Object> agg = new HashMap<String, Object>();
		Double orderTotal = 0.0;
		Double orderAmount = 0.0;
		for (GewaOrder order : orders) {
		orderTotal += order.getQuantity();
		orderAmount += order.getTotalCost();
		}
		agg.put("orderTotal", orderTotal);
		agg.put("orderAmount", orderAmount);
		return agg;
	}

	/**
	* @param start
	* @param end
	* @param placeId
	* @param placeType
	* @return
	*/
	private Map<String, Object> aggreRefund(List<Refundment> refundments) {
		Map<String, Object> agg = new HashMap<String, Object>();
		Double refundTotal = 0.0; 
		Double refundAmount = 0.0;
		
		for (Refundment refundment : refundments){
		refundTotal+= refundment.getQuantity() == null ? 0 : refundment.getQuantity();
		refundAmount+=refundment.getOldSettle() - refundment.getNewSettle();
		}
		agg.put("refundments", refundments);
		agg.put("refundTotal", refundTotal);
		agg.put("refundAmount", refundAmount);
		return agg;
	}
	
	/**
	 * 更新卖品订单取票时间
	 */
	@Override
	public void updateGoodsTakeTime(String tradeNo, String takeTime) {
		if(StringUtils.isEmpty(tradeNo) || StringUtils.isEmpty(takeTime)){
			return;
		}
		GewaOrder goods = daoService.getObject(GewaOrder.class, tradeNo);
		if(null != goods){
			Timestamp time = DateUtil.parseTimestamp(takeTime,"yyyy-MM-dd HH:mm:ss");
			goods.setTakeTime(time);
			daoService.updateObject(goods);
		}
	}

	/**
	 * 
	 */
	@Override
	public void updateGoodsTakeTimeByHttpRequest(List<String> tradeNos) {
		//logger.warn("卖品打票时间更新，卖品订单号：[" + tradeNos.toString() + "]");
		if (CollectionUtils.isEmpty(tradeNos)) {
			return;
		}
		String terminalUrl = config.getString("terminalUrl");
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeNos", StringUtils.join(tradeNos, ","));
		HttpResult postUrlAsString = HttpUtils.postUrlAsString(terminalUrl, params);
		if (postUrlAsString.isSuccess()){
			String result = postUrlAsString.getResponse();
			Map<String, String> map = JsonUtils.readJsonToMap(result);
			for(Entry<String, String> entry : map.entrySet()){
				String tradeNo = entry.getKey();
				String takeTime = entry.getValue();
				GewaOrder o = daoService.getObject(GewaOrder.class, tradeNo);
				if(null != o){
					Timestamp time = DateUtil.parseTimestamp(takeTime,"yyyy-MM-dd HH:mm:ss");
					o.setTakeTime(time);
					daoService.updateObject(o);
					//log too large
					//logger.warn("完成打票时间更新，订单号：" + o.getTradeno());
				}
			}
		}else{
			logger.error("synch take time error:" + postUrlAsString.getStatus() + "::" + postUrlAsString.getResponse());
		}
	}

	/**
	 * 
	 */
	@Override
	public ResultCode reverseGoodsSettle(SettlementBill bill) {
		if (bill == null)
			return ResultCode.getFailure("null settle bill");
		
		Long settleId = bill.getRecordId();
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", settleId);
		if (!CollectionUtils.isEmpty(checkBills)){
			final List<Long> ckIds = BeanUtil.getBeanPropertyList(checkBills, "recordId", true);
			final String order = " UPDATE gewa_order SET is_settled = 'N', check_bill_id = null WHERE order_type='GOODS' and " +
					"check_bill_id in :inList";
			final String refundment = " UPDATE refundment SET is_settled = 'N', check_bill_id = null, settle_bill_id = null  WHERE order_type = 'GOODS' and " +
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
		List<Adjustment> ads = daoService.getObjectListByField(Adjustment.class, "settleBillId", bill.getRecordId());
		
		for (Adjustment ad : ads){
			ad.setIsSettled("N");
			ad.setSettleBillId(null);
		}
		daoService.saveObjectList(ads);
		
		List<Refundment> refunds = daoService.getObjectListByField(Refundment.class, "settleBillId", bill.getRecordId());
		
		for (Refundment refund : refunds){
			refund.setIsSettled("N");
			refund.setSettleBillId(null);
		}
		daoService.saveObjectList(refunds);
		
		bill.initAggData();
		daoService.saveObject(bill);
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(extend != null) {
			extend.initData();
			daoService.updateObject(extend);
		}
		return ResultCode.SUCCESS;
	}
}
