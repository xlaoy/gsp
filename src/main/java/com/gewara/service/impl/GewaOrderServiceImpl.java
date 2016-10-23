/** 
 */
package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.enums.OrderTypeEnums;
import com.gewara.enums.SettleBaseEnums;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.DaoService;
import com.gewara.service.GewaOrderService;
import com.gewara.util.BeanUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 13, 2013  11:47:32 AM
 */
@Service("gewaOrderService")
public class GewaOrderServiceImpl implements GewaOrderService {
	private final GewaLogger logger = WebLogger.getLogger(getClass());
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Object[] countingAndSummingGewaOrder(SettleConfig sc, Long playId, String tag, Long relateId, String orderStatus,
			Timestamp start, Timestamp end, String timeField, String tradeNo) {
		
		if(StringUtils.isEmpty(timeField)) {
			timeField = "dealTime";
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (playId != null)
			query.add(Restrictions.eq("playId", playId));
			
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		
		if (relateId != null)
			query.add(Restrictions.eq("relateId", relateId));
		
		if (StringUtils.isNotBlank(orderStatus))
			query.add(Restrictions.eq("orderStatus", orderStatus));
		
		if (StringUtils.isNotBlank(tradeNo))
			query.add(Restrictions.eq("tradeno", tradeNo));
		
		if (start != null)
			query.add(Restrictions.ge(timeField, start));
		
		if (end != null)
			query.add(Restrictions.lt(timeField, end));
		
		//MiscUtil.appendCategoryQueryCondition(query);
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.rowCount());
		pl.add(Projections.sum("quantity"));
		pl.add(Projections.sum("totalCost"));
		query.setProjection(pl);
		return (Object[])hibernateTemplate.findByCriteria(query).get(0);
	}

	@Override
	public List<GewaOrder> queryGewaOrder(SettleConfig sc, Long playId, String tag, Long relateId,
			String orderStatus, Timestamp start, Timestamp end, String timeField, String tradeNo, Integer pageNo, Integer pageSize) {
		
		if(StringUtils.isEmpty(timeField)) {
			timeField = "dealTime";
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (playId != null)
			query.add(Restrictions.eq("playId", playId));
		
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		
		if (relateId != null)
			query.add(Restrictions.eq("relateId", relateId));
		
		if (StringUtils.isNotBlank(orderStatus))
			query.add(Restrictions.eq("orderStatus", orderStatus));
		
		if (StringUtils.isNotBlank(tradeNo))
			query.add(Restrictions.eq("tradeno", tradeNo));
		
		if (start != null)
			query.add(Restrictions.ge(timeField, start));
		
		if (end != null)
			query.add(Restrictions.lt(timeField, end));
		
		//MiscUtil.appendCategoryQueryCondition(query);
		
		query.addOrder(Order.asc(timeField));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	
	@Override
	public int countingPlayItem(String tag, Long relateId, Timestamp start, Timestamp end) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (relateId != null)
			query.add(Restrictions.eq("relateId", relateId));
		if (start != null)
			query.add(Restrictions.ge("useTime", start));
		
		if (end != null)
			query.add(Restrictions.lt("useTime", end));
		query.setProjection(Projections.countDistinct("playId"));
		Object r = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(r.toString());
	}
	
	@Override
	public Map<Long, Map<String, Object>> queryPlayItemAggre(String tag, Long relateId,
			Timestamp start, Timestamp end, SettleConfig settleConfig,  List<Long> playItems) {
		Map<Long, Map<String, Object>> finalAggre = new LinkedHashMap<Long, Map<String,Object>>();
		
		Map<Long, List<GewaOrder>> orderMapping = queryOrderPlayMapping(playItems, start, end, settleConfig);
		Map<Long, Map<String,/** orderTotal and orderAmount **/ Object>> orderAggre = arrgreOrder(orderMapping);
		
		Map<Long, Map<String,/** refundTotal and refundAmount **/ Object>> refundMap = aggreRefundPlayMap(playItems, start, end, settleConfig);
		
//		Map<Long, Map<String,/** adjustTotal and adjustAmount and reasons **/ Object>> adjustMap = aggreAdjustPlayMap(orderMapping);
		
		Iterator<Long> iterator = orderAggre.keySet().iterator();
		while(iterator.hasNext()){
			Long playId = iterator.next();
			Map<String, Object> playAggre = finalAggre.get(playId);
			playAggre = playAggre == null ? new HashMap<String, Object>() : playAggre;
			playAggre.putAll(orderAggre.get(playId) == null ? new HashMap<String, Object>() : orderAggre.get(playId));
			playAggre.putAll(refundMap.get(playId) == null ? new HashMap<String, Object>() : refundMap.get(playId));
//			playAggre.putAll(adjustMap.get(playId) == null ? new HashMap<String, Object>() : adjustMap.get(playId));
			finalAggre.put(playId, playAggre);
		}
		return finalAggre;
	}
	
//	/**
//	 * @param orderMapping
//	 * @return
//	 */
//	private Map<Long, Map<String, Object>> aggreAdjustPlayMap(
//			Map<Long, List<GewaOrder>> orderMapping) {
//		Map<Long, Map<String, Object>> finalAdjustAgg = new HashMap<Long, Map<String,Object>>();
//		Iterator<Long> iterator = orderMapping.keySet().iterator();
//		while (iterator.hasNext()){
//			Long playId = iterator.next();
//			Map<String, Object> adjustAggre = aggreSingleAdjust(orderMapping.get(playId));
//			finalAdjustAgg.put(playId, adjustAggre);
//		}
//		return finalAdjustAgg;
//	}
	/**
	 * 
	 * 
	 * @param list
	 * @return
	 */
//	private Map<String, Object> aggreSingleAdjust(List<GewaOrder> list) {
//		//adjustTotal and adjustAmount and reasons
//		
//		
//		if (CollectionUtils.isEmpty(list))
//			return new HashMap<String, Object>();
//		List<String> adjustedTradeNo = new ArrayList<String>();
//		for (int i = 0; i < list.size(); i++){
//			if (list.get(i).isAdjust())
//				adjustedTradeNo.add(list.get(i).getTradeno());
//		}
//		if (CollectionUtils.isEmpty(adjustedTradeNo))
//			return new HashMap<String, Object>();
//		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
//		query.add(Restrictions.eq("tag", AdjustTypeEnums.ORDER.getType()));
//		query.add(Restrictions.in("relateId", adjustedTradeNo));
//		
//		List<Adjustment> adjustments = hibernateTemplate.findByCriteria(query);
//		int adjustTotal = 0;
//		int adjustAmount = 0;
//		for (Adjustment ad : adjustments){
//			adjustTotal +=1;
//			adjustAmount += ad.getAmount();
//		}
//		String reasons = MiscUtil.groupAdjustReasons(adjustments);
//		Map<String, Object> agg = new HashMap<String, Object>();
//		agg.put("adjustTotal", adjustTotal);
//		agg.put("adjustAmount", adjustAmount);
//		agg.put("reasons", reasons);
//		return agg;
//	}


	/**
	 * @param playItems
	 * @return
	 */
	private Map<Long, Map<String, Object>> aggreRefundPlayMap(
			List<Long> playItems, Timestamp start, Timestamp end, SettleConfig settleConfig) {
		//refundTotal and refundAmount
		Map<Long, Map<String, Object>> finalAggre = new HashMap<Long, Map<String,Object>>();
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "refundTime" : "useTime";
		if (start != null)
			query.add(Restrictions.ge(timeField, start));
		
		if (end != null)
			query.add(Restrictions.lt(timeField, end));
		
		
		query.add(Restrictions.in("playId", playItems));
		List<Refundment> refunds = daoService.findByCriteria(query);
		
		Map<Long, List<Refundment>> refundMap = BeanUtil.groupBeanList(refunds, "playId");
		Iterator<Long> iterator = refundMap.keySet().iterator();
		
		Map<String, Object> playAggre = null;
		while (iterator.hasNext()){
			Long playId = iterator.next();
			List<Refundment> rs = refundMap.get(playId);
			int refundTotal = 0;
			int refundAmount = 0;
			for (int i = 0; i < rs.size(); i++){
				refundTotal += rs.get(i).getQuantity() == null ? 0 : rs.get(i).getQuantity();
				refundAmount += rs.get(i).getOldSettle() - rs.get(i).getNewSettle();
			}
			
			playAggre = new HashMap<String, Object>();
			playAggre.put("refundTotal", refundTotal);
			playAggre.put("refundAmount", refundAmount);
			finalAggre.put(playId, playAggre);
		}
		return finalAggre;
	}

	/**
	 * @param orderMapping
	 * @return
	 */
	private Map<Long, Map<String, Object>> arrgreOrder(
			Map<Long, List<GewaOrder>> orderMapping) {
		Map<Long, Map<String, Object>> orderAggre = new LinkedHashMap<Long, Map<String,Object>>();
		Map<String, Object> playAggre = null;
		Iterator<Long> iterator = orderMapping.keySet().iterator();
		while(iterator.hasNext()){
			playAggre = new HashMap<String, Object>();
			Long playId = iterator.next();
			List<GewaOrder> orders = orderMapping.get(playId);
			int orderTotal = 0;
			int totalAmount = 0;
			for(int i = 0; i < orders.size(); i++){
				orderTotal += orders.get(i).getQuantity();
				totalAmount += orders.get(i).getTotalCost();
			}
			
			playAggre.put("orderTotal", orderTotal);
			playAggre.put("orderAmount", totalAmount);
			orderAggre.put(playId, playAggre);
		}
		return orderAggre;
	}

	/**
	 * @param playItems
	 * @return
	 */
	private Map<Long, List<GewaOrder>> queryOrderPlayMapping(
			List<Long> playItems, Timestamp start, Timestamp end, SettleConfig settleConfig) {
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (start != null)
			query.add(Restrictions.ge(timeField, start));
		
		if (end != null)
			query.add(Restrictions.lt(timeField, end));
		
		query.add(Restrictions.in("playId", playItems));
		query.addOrder(Order.asc("useTime"));
		List<GewaOrder> orders = daoService.findByCriteria(query);                                                                  
		Map<Long, List<GewaOrder>> orderMap = BeanUtil.groupBeanList(orders, "playId");
		return orderMap;
	}

	/**
	 * @param start
	 * @param end
	 * @param placeId
	 * @param placeType
	 * @return
	 */
	@Override
	public List<GewaOrder> queryGewaOrderByPlace(Timestamp start, Timestamp end,
			Long placeId, String placeType, SettleConfig settleConfig, String category) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		query.add(Restrictions.ge(timeField, start));
		query.add(Restrictions.lt(timeField, end));
		query.add(Restrictions.eq("relateId", placeId));
		query.add(Restrictions.eq("tag", placeType));
//		query.add(Restrictions.eq("category", category));
		MiscUtil.appendCategoryQueryCondition(query);
		query.add(Restrictions.ne("isSettled", "Y"));
		return daoService.findByCriteria(query);
	}

	@Override
	public List<GewaOrder> queryGoodsOrderByPlace(Timestamp start, Timestamp end,Long placeId, String placeType,String category) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		String timeField = "takeTime";
		query.add(Restrictions.ge(timeField, start));
		query.add(Restrictions.lt(timeField, end));
		query.add(Restrictions.eq("relateId", placeId));
		query.add(Restrictions.eq("tag", placeType));
		query.add(Restrictions.eq("category", category));
		MiscUtil.appendCategoryQueryCondition(query);
//		query.add(Restrictions.ne("isSettled", "Y"));
		query.add(Restrictions.eq("orderStatus", "paid_success"));
		return daoService.findByCriteria(query);
	}
	
	@Override
	public List<GewaOrder> queryGewaOrderByChannelConfig(Timestamp start,Timestamp end, Long relatId, String tag,
			ChannelSettleConfig channelSettleConfig) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		List<VendorCinemaRelation> relations = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", channelSettleConfig.getRecordId());
		List<String> recordIds = BeanUtil.getBeanPropertyList(relations, "cinemaRecordId", true);
		List<Long> placeIds = new ArrayList<Long>();
		for(String recordId : recordIds){
			placeIds.add(Long.valueOf(recordId.split(",")[0]));
		}
		query.add(Restrictions.ge("dealTime", start));
		query.add(Restrictions.lt("dealTime", end));
		query.add(Restrictions.in("relateId", placeIds));
		query.add(Restrictions.eq("tag", OrderTypeEnums.TICKET.getType()));
		MiscUtil.appendCategoryQueryCondition(query);
//		query.add(Restrictions.ne("isChannelSettled", "Y"));
		return daoService.findByCriteria(query);
	}
	
	@Override
	public List<GewaOrder> queryGewaOrderByChannelConfigExt(Timestamp start,
			Timestamp end, Long placeId, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.ge("dealTime", start));
		query.add(Restrictions.lt("dealTime", end));
		query.add(Restrictions.eq("relateId", placeId));
		query.add(Restrictions.eq("tag", OrderTypeEnums.TICKET.getType()));
		MiscUtil.appendCategoryQueryCondition(query);
		return daoService.findByCriteria(query);
	}
	
	@Override
	public List<Refundment> queryRefundmentByChannelConfigExt(Timestamp start, Timestamp end, Long placeId,
			String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.ge("refundTime", start));
		query.add(Restrictions.lt("refundTime", end));
		query.add(Restrictions.eq("relateId", placeId));
		query.add(Restrictions.eq("tag", OrderTypeEnums.TICKET.getType()));
		MiscUtil.appendCategoryQueryCondition(query);
		return daoService.findByCriteria(query);
	}
	
	@Override
	public List<Long> queryPlayItemIds(String tag, Long relateId,
			Timestamp start, Timestamp end, SettleConfig settleConfig) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (relateId != null)
			query.add(Restrictions.eq("relateId", relateId));
		
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		if (start != null)
			query.add(Restrictions.ge(timeField, start));
		
		if (end != null)
			query.add(Restrictions.lt(timeField, end));
		
		query.setProjection(Projections.distinct(Projections.groupProperty("playId")));
		query.addOrder(Order.desc("playId"));
		return daoService.findByCriteria(query, 0, 2000);
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.GewaOrderService#queryPlayIdOrderMap(java.util.List)
	 */
	@Override
	public Map<Long, GewaOrder> queryPlayIdOrderMap(final Collection<Long> playItems) {
		List<GewaOrder> orders = hibernateTemplate.execute(new HibernateCallback<List<GewaOrder>>() {
			@Override
			public List<GewaOrder> doInHibernate(Session session) {
				String hql = " FROM GewaOrder as o1 where o1.tradeno in ( " +
															"select max(o2.tradeno) from GewaOrder as o2 where o2.playId in (:ids) group by o2.playId order by o2.playId desc)";
				
				return session.createQuery(hql).setParameterList("ids", playItems).list();
			}
		});
		
		Map<Long, GewaOrder> m = new LinkedHashMap<Long, GewaOrder>();
		for (GewaOrder o : orders){
			m.put(o.getPlayId(), o);
		}
		return m;
		
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.GewaOrderService#countingAndSummingGewaOrder(java.lang.String[])
	 */
	@Override
	public Object[] countingAndSummingGewaOrder(String[] noArray) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (noArray != null)
			query.add(Restrictions.in("tradeno", noArray));
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.rowCount());
		pl.add(Projections.sum("quantity"));
		pl.add(Projections.sum("totalCost"));
		query.setProjection(pl);
		return (Object[])hibernateTemplate.findByCriteria(query).get(0);
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.GewaOrderService#queryGewaOrder(java.lang.String[], java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<GewaOrder> queryGewaOrder(String[] noArray, Integer pageNo,
			Integer pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if (noArray != null)
			query.add(Restrictions.in("tradeno", noArray));
		
		query.addOrder(Order.desc("playId"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.GewaOrderService#countAndSumOrderBySettleBill(com.gewara.model.gsp.SettlementBill)
	 */
	@Override
	public Object[] countAndSumOrderBySettleBill(SettlementBill bill) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		List<CheckBill> checkbills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		if (CollectionUtils.isEmpty(checkbills))
			return new Object[]{0L,0L};
		List<Long> cbIds = BeanUtil.getBeanPropertyList(checkbills, "recordId", true);
		
		query.add(Restrictions.in("checkBillId", cbIds));
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.rowCount());
		pl.add(Projections.sum("quantity"));
		pl.add(Projections.sum("totalCost"));
		query.setProjection(pl);
		return (Object[])hibernateTemplate.findByCriteria(query).get(0);
	}
	
	@Override
	public List<GewaOrder> queryGewaOrderBySettleBill(SettlementBill bill,
			Integer pageNo, Integer pageSize) {
		SettleConfig settleConfig = daoService.getObject(SettleConfig.class, bill.getConfigId());
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		String timeField = settleConfig.getSettleBase().equals(SettleBaseEnums.ORDERTIME.getType()) ? "dealTime" : "useTime";
		
		List<CheckBill> checkbills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		if (CollectionUtils.isEmpty(checkbills))
			return new ArrayList<GewaOrder>();
		List<Long> cbIds = BeanUtil.getBeanPropertyList(checkbills, "recordId", true);
		
		query.add(Restrictions.in("checkBillId", cbIds));
//		query.add(Restrictions.ne("isSettled", "Y"));
		query.addOrder(Order.desc(timeField));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	@Override
	public void repairGewaOrderOuterId(String category, Timestamp start) {
		
	}
}
