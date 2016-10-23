/** 
 */
package com.gewara.web.action;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.OrderTypeEnums;
import com.gewara.enums.RefundmentType;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.AdjustmentService;
import com.gewara.service.GewaOrderService;
import com.gewara.service.RefundmentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MapRow;
import com.gewara.util.RecordIdUtils;
import com.gewara.vo.PlayItemVo;
import com.gewara.web.util.PageUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 13, 2013  11:44:14 AM
 */
@Controller
public class GewaOrderController extends AnnotationController{
	@Autowired
	@Qualifier("gewaOrderService")
	private GewaOrderService gewaOrderService;
	
	@Autowired
	@Qualifier("adjustmentService")
	private AdjustmentService adjustmentService;
	
	@Autowired
	@Qualifier("refundmentService")
	private RefundmentService refundmentService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@RequestMapping("/merchant/gewaorder/queryPlayItemAggre.xhtml")
	public String merchantQueryPlayItemAggre(String placeId, Long settleId, String isXls, String isPrint, Timestamp start, 
			Timestamp end, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("place", daoService.getObject(Place.class, placeId));
		if (StringUtils.isBlank(placeId))
			return "/gewaorder/merchantPlayItemAggre.vm";
		
		Place place = daoService.getObject(Place.class, placeId);
		
		if (settleId == null)
			setupPlayItemAggre(place, settleId, isXls, start, end, model);
		else
			setupPlayItemForSettle(place, settleId, isXls, start, end, model);
		model.put("placeId", placeId);
		if(StringUtils.isNotBlank(isXls)){
			String t = "";
			if (start != null && end != null)
				t = " " + DateUtil.format(start, "MM-dd") + " 至 " + DateUtil.format(end, "MM-dd");
			this.download("xls", place.getName() + t + " 对账明细",  response);
			return "/downloadtemplate/playItemAggre.vm";
		}
		// setup confirm info
		if (StringUtils.isNotBlank(isXls) || StringUtils.isNotBlank(isPrint)){
			StatusTracker lastMerchant = getMerchantConfirmST(settleId);
			StatusTracker lastGewa = getGewaPayST(settleId);
			if (lastMerchant != null){
				Map<String, String> merchantInfo = getRemoteMerchantInfo(lastMerchant.getOperator());
				if (merchantInfo != null)
					model.put("merinfo", JsonUtils.readJsonToMap(merchantInfo.get("otherInfo")));
			}
			model.put("mst", lastMerchant);
			model.put("gst", lastGewa);
		}
		
		// print check bills
		if(StringUtils.isNotBlank(isPrint)){
			return "/printtemplate/playItemAggre.vm";
		}
		return "/gewaorder/merchantPlayItemAggre.vm";
	}
	
	@RequestMapping("/platform/gewaorder/queryPlayItemAggre.xhtml")
	public String queryPlayItemAggre(String placeId, Long settleId, String isXls, String isPrint, Timestamp start, 
									Timestamp end, ModelMap model, HttpServletResponse response){
		if (StringUtils.isBlank(placeId))
			return "/gewaorder/playItemAggre.vm";
		
		Place place = daoService.getObject(Place.class, placeId);
		if (settleId == null)
			setupPlayItemAggre(place, settleId, isXls, start, end, model);
		else
			setupPlayItemForSettle(place, settleId, isXls, start, end, model);
		
		if(StringUtils.isNotBlank(isXls)){
			String t = "";
			if (start != null && end != null)
				t = " " + DateUtil.format(start, "MM-dd") + " 至 " + DateUtil.format(end, "MM-dd");
			this.download("xls", place.getName() + t + " 对账明细",  response);
			return "/downloadtemplate/playItemAggre.vm";
		}
		// setup confirm info
		if (StringUtils.isNotBlank(isXls) || StringUtils.isNotBlank(isPrint)){
			StatusTracker lastMerchant = getMerchantConfirmST(settleId);
			StatusTracker lastGewa = getGewaPayST(settleId);
			if (lastMerchant != null){
				Map<String, String> merchantInfo = getRemoteMerchantInfo(lastMerchant.getOperator());
				if (merchantInfo != null)
					model.put("merinfo", JsonUtils.readJsonToMap(merchantInfo.get("otherInfo")));
			}
			model.put("mst", lastMerchant);
			model.put("gst", lastGewa);
		}
		
		// print check bills
		if(StringUtils.isNotBlank(isPrint)){
			return "/printtemplate/playItemAggre.vm";
		}
		return "/gewaorder/playItemAggre.vm";
	}
	
	@RequestMapping("/platform/gewaorder/queryPlayItem.xhtml")
	public String queryPlayItem(String placeId, String placeFirstLetter, Timestamp start, Timestamp end, 
			String isXls, Integer pageNo, Integer pageSize, ModelMap mode, HttpServletResponse response) {
		if(StringUtils.isEmpty(placeId)) {
			return "/gewaorder/playItem.vm";
		}
		if(start == null) {
			return "/gewaorder/playItem.vm";
		}
		if(end == null) {
			return "/gewaorder/playItem.vm";
		}
		
		pageNo = pageNo == null ? 0 : pageNo;
		if(StringUtils.isNotBlank(isXls)) {
			pageSize = pageSize == null ? 5000 : pageSize;
		} else {
			pageSize = pageSize == null ? 15 : pageSize;
		}
		
		
		Long pid = Long.valueOf(placeId.split(",")[0]);
		String countsql = "select count(DISTINCT play_id) as count, sum(quantity) as num, sum(total_cost) as amount from gewa_order where place_id = ? and use_time >= ? and use_time < ? ";
		
		List<PlayItemVo> pvolist = new ArrayList<PlayItemVo>();
		int totalnum = 0;
		double totalamount = 0.0;
		int distotalnum = 0;
		double distotalamount = 0.0;
		
		Map<String, Object> countmap = jdbcTemplate.queryForMap(countsql, pid, start, end);
		MapRow countrow = new MapRow(countmap);
		int count = countrow.getInteger("count");
		
		if(count > 0) {
			
			totalnum = countrow.getInteger("num");
			totalamount = countrow.getDouble("amount");
			
			String discountsql = "select sum(o.quantity) as disnum, sum(o.reldiscount) as discount from gewa_order o where o.place_id = ? and o.use_time >= ? and o.use_time < ? and o.reldiscount > 0 and not EXISTS(select 1 from refundment r where r.tradeno = o.tradeno) ";
			List<Map<String, Object>> discountlist = jdbcTemplate.queryForList(discountsql, pid, start, end);
			if(CollectionUtils.isNotEmpty(discountlist)) {
				Map<String, Object> discountmap = discountlist.get(0);
				MapRow discountrow = new MapRow(discountmap);
				distotalnum = discountrow.getInteger("disnum");
				distotalamount = discountrow.getDouble("discount");
			}
			
			String listsql = "select o.play_id, sum(o.quantity) as num, sum(o.total_cost) as amount, sum(o.reldiscount) as discount from gewa_order o where o.place_id = ? and o.use_time >= ? and o.use_time < ? and not EXISTS(select 1 from refundment r where r.tradeno = o.tradeno) group by o.play_id order by discount desc limit ? offset ? ";
			String osql = "select use_time, other_info from gewa_order where place_id = ? and play_id = ? limit 1 ";
			String dissql = "select sum(o.quantity) as disnum, sum(o.reldiscount) as discount from gewa_order o where o.place_id = ? and  o.play_id = ? and o.reldiscount > 0 and not EXISTS(select 1 from refundment r where r.tradeno = o.tradeno) ";
			String settlesql = "select DISTINCT settlement_id from check_bill where record_id in ( select DISTINCT check_bill_id  from gewa_order where place_id = ? and  play_id = ? and check_bill_id is not null ) ";
			
			List<Map<String, Object>> maplist = jdbcTemplate.queryForList(listsql, pid, start, end, pageSize, (pageNo * pageSize));
			for(Map<String, Object> map : maplist) {
				
				PlayItemVo pvo = new PlayItemVo();
				MapRow row = new MapRow(map);
				Long playid = row.getLong("play_id");
				
				pvo.setPlayid(playid);
				pvo.setNum(row.getInteger("num"));
				pvo.setAmount(row.getDouble("amount"));
				
				Map<String, Object> osmap = jdbcTemplate.queryForMap(osql, pid, playid);
				pvo.setPlaytime((Timestamp)osmap.get("use_time"));
				String info = osmap.get("other_info").toString();
				Map otherMap = JsonUtils.readJsonToMap(info);
				pvo.setMovieName(otherMap.get("影片").toString());
				
				Map<String, Object> dismap = jdbcTemplate.queryForMap(dissql, pid, playid);
				MapRow disrow = new MapRow(dismap);
				pvo.setDisnum(disrow.getInteger("disnum"));
				pvo.setDiscount(disrow.getDouble("discount"));
				
				List<Long> settleid = jdbcTemplate.queryForList(settlesql, Long.class, pid, playid);
				pvo.setSettleid(settleid);
				
				pvolist.add(pvo);
			}
			
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "platform/gewaorder/queryPlayItem.xhtml", true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("placeId", placeId);
			m.put("start", start);
			m.put("end", end);
			m.put("placeFirstLetter", placeFirstLetter);
			pageUtil.initPageInfo(m);
			mode.put("pageUtil", pageUtil);
		}
		double distotalrate = 0.0;
		if(totalamount > 0) {
			distotalrate = new BigDecimal((distotalamount / totalamount) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
		mode.put("totalnum", totalnum);
		mode.put("totalamount", totalamount);
		mode.put("distotalnum", distotalnum);
		mode.put("distotalamount", distotalamount);
		mode.put("distotalrate", distotalrate);
		mode.put("placeId", placeId);
		mode.put("start", start);
		mode.put("end", end);
		mode.put("placeFirstLetter", placeFirstLetter);
		mode.put("pvolist", pvolist);
		if(StringUtils.isNotBlank(isXls)) {
			this.download("xls", "场次列表" , response);
			return "/gewaorder/playItem_xls.vm";
		}
		return "/gewaorder/playItem.vm";
	}
	
	/**
	 * @param place
	 * @param settleId
	 * @param isXls
	 * @param start
	 * @param end
	 * @param model
	 */
	private void setupPlayItemForSettle(Place place, Long settleId,
			String isXls, Timestamp start, Timestamp end, ModelMap model) {
		SettlementBill settle = daoService.getObject(SettlementBill.class, settleId);
		List<CheckBill> cks = daoService.getObjectListByField(CheckBill.class, "settlementId", settleId);
		List<Long> cKIds = BeanUtil.getBeanPropertyList(cks, "recordId", true);
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.in("checkBillId", cKIds));
		List<GewaOrder> orders = daoService.findByCriteria(query);
		Map<Long, List<GewaOrder>> playIdAndOrders = BeanUtil.groupBeanList(orders, "playId");
		
		DetachedCriteria query2 = DetachedCriteria.forClass(Refundment.class);
		query2.add(Restrictions.in("checkBillId", cKIds));
		List<Refundment> refunds = daoService.findByCriteria(query2);
		Map<Long, List<Refundment>> playIdAndRefunds = BeanUtil.groupBeanList(refunds, "playId");
		
		
		Map<Long, GewaOrder> playIdOrderMap = gewaOrderService.queryPlayIdOrderMap(playIdAndOrders.keySet());
		
		Map<Long, Map<String, Object>> aggres = aggreForPlayItem(playIdAndOrders, playIdAndRefunds);
		
		List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(settle);
		model.put("ads",ads);
		model.put("settle", settle);
		model.put("aggres", aggres);
		model.put("place", place);
		model.put("pom", playIdOrderMap);
		model.put("isXls", isXls);
		
		
		
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", place.getRecordId());
		m.put("start", start);
		m.put("end", end);
		
	}


	/**
	 * @param playIdAndOrders
	 * @param playIdAndRefunds
	 * @return
	 */
	private Map<Long, Map<String, Object>> aggreForPlayItem(
			Map<Long, List<GewaOrder>> playIdAndOrders,
			Map<Long, List<Refundment>> playIdAndRefunds) {
		Map<Long, Map<String, Object>> finalAggre = new LinkedHashMap<Long, Map<String,Object>>();
		Map<String, Object> playAggre = null;
		
		for (Long playId : playIdAndOrders.keySet()){
			playAggre = new HashMap<String, Object>();
			List<GewaOrder> playItemOrder = playIdAndOrders.get(playId);
			List<Refundment> playItemRefund = playIdAndRefunds.get(playId);
			Integer orderTotal = 0;
			Double totalAmount = 0.0;
			for(int i = 0; i < playItemOrder.size(); i++){
				orderTotal += playItemOrder.get(i).getQuantity();
				totalAmount += playItemOrder.get(i).getTotalCost();
			}
			
			int refundTotal = 0;
			int refundAmount = 0;
			if (CollectionUtils.isNotEmpty(playItemRefund)){
				for (int i = 0; i < playItemRefund.size(); i++){
					refundTotal += playItemRefund.get(i).getQuantity();
					refundAmount += playItemRefund.get(i).getOldSettle() - playItemRefund.get(i).getNewSettle();
				}
			}
			
			playAggre.put("orderTotal", orderTotal);
			playAggre.put("orderAmount", totalAmount);
			playAggre.put("refundTotal", refundTotal);
			playAggre.put("refundAmount", refundAmount);
			
			finalAggre.put(playId, playAggre);
			
		}
		return finalAggre;
		
	}

	private void setupPlayItemAggre(Place place, Long settleId, String isXls, Timestamp start, 
			Timestamp end, ModelMap model){
		SettleConfig settleConfig = daoService.getObject(SettleConfig.class, place.getRecordId());
		if (settleConfig == null)
			return;
		List<Long> playItems = gewaOrderService.queryPlayItemIds(place.getTag(), place.getRelateId(),
																		start, end, settleConfig);
		if (CollectionUtils.isEmpty(playItems))
			return;
		
		Map<Long, GewaOrder> playIdOrderMap = gewaOrderService.queryPlayIdOrderMap(playItems);
		
		Map<Long, Map<String, Object>> aggres = gewaOrderService.queryPlayItemAggre(place.getTag(), place.getRelateId(), start, end,
																			  settleConfig, playItems);
		
		if (settleId != null){
			SettlementBill settle = daoService.getObject(SettlementBill.class, settleId);
			List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(settle);
			model.put("ads",ads);
			model.put("settle", settle);
		}
		model.put("aggres", aggres);
		model.put("place", place);
		model.put("pom", playIdOrderMap);
		model.put("isXls", isXls);
		
		
		
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", place.getRecordId());
		m.put("start", start);
		m.put("end", end);
	}
	@RequestMapping("/platform/gewaorder/querySimpleOrders.xhtml")
	public String querySimpleOrders(String isXls, String t, String tradeNos, Integer pageNo, Integer pageSize, ModelMap model){
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 1000 : pageSize == null ? 30 : pageSize;
		if (StringUtils.isBlank(tradeNos))
			return "/gewaorder/simpleOrderDetails.vm";
		String[] noArray = StringUtils.split(tradeNos,",");
		
		Object[] aggres = gewaOrderService.countingAndSummingGewaOrder(noArray);
		int totalNumber = ((Long)aggres[0]).intValue();
		if (totalNumber <= 0)
			return "/gewaorder/simpleOrderDetails.vm";
		model.put("aggres", aggres);
		
		List<GewaOrder> orders = gewaOrderService.queryGewaOrder(noArray, pageNo, pageSize);
		model.put("orders", orders);
		adjustAndPlaceAndRefundment(orders, model);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "platform/gewaorder/querySimpleOrders.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("tradeNos", tradeNos);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("t", t);
		return "/gewaorder/simpleOrderDetails.vm";
	}
	
	private void adjustAndPlaceAndRefundment(List<GewaOrder> orders, ModelMap model){
		Map<String, List<GewaOrder>> adjustMap = BeanUtil.groupBeanList(orders, "isAdjusted");
		List<GewaOrder> adjustedOrder = adjustMap.get("Y");
		if (CollectionUtils.isNotEmpty(adjustedOrder)){
			List<String> adjustedTradeNo = BeanUtil.getBeanPropertyList(adjustedOrder, "tradeno", true);
			List<Adjustment> ads = adjustmentService.queryListByTradeNo(adjustedTradeNo, AdjustTypeEnums.ORDER.getType());
			Map<String, Object> adsMap = BeanUtil.getKeyValuePairMap(ads, "relateId", "amount");
			model.put("adsMap", adsMap);
		}
		
		List<String> tradeNos =  BeanUtil.getBeanPropertyList(orders, "tradeno", true);
		
		Map<String, Refundment> refundMap = daoService.getObjectMap(Refundment.class, tradeNos);
		model.put("rm", refundMap);
		
		//场馆数据
		Set<String> placeIds = new HashSet<String>();
		for (GewaOrder o : orders)
			placeIds.add(o.getRelateId() + "," + o.getTag());
		
		Map<String, Place> pm = daoService.getObjectMap(Place.class, placeIds);
		model.put("pm", pm);
	}
	@RequestMapping("/merchant/gewaorder/queryGewarOrder.xhtml")
	public String merchantQueryGewarOrder(Long playId, String tag, Long relateId, String placeId, String status,
			Timestamp start, Timestamp end, String tradeNo, Integer pageNo, Integer pageSize,
			ModelMap model, String placeFirstLetter, String isXls, HttpServletResponse response,HttpServletRequest request){
		//placeId = "2,TICKET";
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("place", daoService.getObject(Place.class, placeId));
		if (StringUtils.isBlank(placeId))
			return "/gewaorder/merchanrOrderDetails.vm";
		
		//FIXME: 
		if (StringUtils.isNotBlank(placeId) ){
			String[] temp = placeId.split(",");
			tag = temp[1];
			relateId = Long.valueOf(temp[0]);
		}
		
		if ((StringUtils.isBlank(tag) || relateId == null) && playId == null)
			return "/gewaorder/merchanrOrderDetails.vm";
		Place p = daoService.getObject(Place.class, RecordIdUtils.contactRecordId(tag, relateId));
		SettleConfig sc = daoService.getObject(SettleConfig.class, p.getRecordId());
		start = defaultTimeForPlace(sc, start);
		end = defaultTimeForPlace(sc, end);
		tag = StringUtils.isNotBlank(tag) ? tag : OrderTypeEnums.TICKET.getType();
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 10000 : pageSize == null ? 30 : pageSize;
		
		Object[] aggres = gewaOrderService.countingAndSummingGewaOrder(sc, playId, tag, relateId, status, start, end, null, tradeNo);
		int totalNumber = ((Long)aggres[0]).intValue();
		model.put("placeId", placeId);
		if (totalNumber <= 0)
			return "/gewaorder/merchanrOrderDetails.vm";
//		model.put("aggres", aggres);
		int quantity = ((Long)aggres[1]).intValue();
//		int totalcost = ((Long)aggres[2]).intValue();
		double totalcost = Double.valueOf(aggres[2].toString());
		List<GewaOrder> orders = gewaOrderService.queryGewaOrder(sc, playId, tag, relateId, status, start, end, null, tradeNo, pageNo, pageSize);
		model.put("orders", orders);
		adjustAndPlaceAndRefundment(orders, model);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "merchant/gewaorder/queryGewarOrder.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("playId", playId);
		m.put("tag", tag);
		m.put("relateId", relateId);
		m.put("placeId", placeId);
		m.put("isSettle", status);
		m.put("start", start);
		m.put("end", end);
		m.put("tradeNo", tradeNo);
		m.put("placeFirstLetter", placeFirstLetter);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("totalquantity", quantity);
		model.put("totalcost", totalcost);
		if(StringUtils.isNotBlank(isXls)){
			String t = "";
			if (start != null && end != null)
				t = " " + DateUtil.format(start, "MM-dd") + " 至 " + DateUtil.format(end, "MM-dd");
			this.download("xls", p.getName() + t + " 的订单明细", response);
			return "/downloadtemplate/orderDetails3.vm";
		}
		
		return "/gewaorder/merchanrOrderDetails.vm";
	}
	
	@RequestMapping("/platform/gewaorder/exportGewarOrder.xhtml")
	public String exportGewarOrder(ModelMap model) {
		return "/gewaorder/exportOrder.vm";
	}
	
	@RequestMapping("/platform/gewaorder/queryGewarOrder.xhtml")
	public String queryGewarOrder(Long playId, String placeId, String status,
									Timestamp start, Timestamp end, String timeField, String tradeNo, Integer pageNo, Integer pageSize,
									ModelMap model, String isXls, HttpServletResponse response){
		if (StringUtils.isBlank(placeId))
			return "/gewaorder/orderDetails.vm";
		
		String[] temp = placeId.split(",");
		String tag = temp[1];
		Long relateId = Long.valueOf(temp[0]);
		Place p = daoService.getObject(Place.class, placeId);
		
		//SettleConfig sc = daoService.getObject(SettleConfig.class, p.getRecordId());
		
		tag = StringUtils.isNotBlank(tag) ? tag : OrderTypeEnums.TICKET.getType();
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 50000 : pageSize == null ? 30 : pageSize;
		
		Object[] aggres = gewaOrderService.countingAndSummingGewaOrder(null,playId, tag, relateId, status, start, end, timeField, tradeNo);
		int totalNumber = ((Long)aggres[0]).intValue();
		if (totalNumber <= 0)
			return "/gewaorder/orderDetails.vm";
		
		model.put("aggres", aggres);
		List<GewaOrder> orders = gewaOrderService.queryGewaOrder(null, playId, tag, relateId, status, start, end, timeField, tradeNo, pageNo, pageSize);
		
		model.put("orders", orders);
		model.put("placeFirstLetter", p.getName());
		adjustAndPlaceAndRefundment(orders, model);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "platform/gewaorder/queryGewarOrder.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("playId", playId);
		m.put("tag", tag);
		m.put("relateId", relateId);
		m.put("placeId", placeId);
		m.put("isSettle", status);
		m.put("start", start);
		m.put("end", end);
		m.put("tradeNo", tradeNo);
		m.put("placeFirstLetter", p.getName());
		m.put("timeField", timeField);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		if(StringUtils.isNotBlank(isXls)){
			String t = "";
			if (start != null && end != null)
				t = " " + DateUtil.format(start, "MM-dd") + " 至 " + DateUtil.format(end, "MM-dd");
			this.download("xls", p.getName() + t + " 的订单明细",  response);
			return "/downloadtemplate/orderDetails.vm";
		}
		return "/gewaorder/orderDetails.vm";
	}
	@RequestMapping("/merchant/gewaorder/querySettleOrderDetails.xhtml")
	public String merchantQuerySettleOrderDetails(Long settleBillId, String placeId,  String isXls, ModelMap model, Integer pageNo, Integer pageSize, HttpServletResponse response){
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 50000 : pageSize == null ? 500 : pageSize;
		SettlementBill bill = daoService.getObject(SettlementBill.class, settleBillId);
		if (bill == null || !StringUtils.equals(placeId, bill.getConfigId()) )
			return "/gewaorder/merchantSettleOrderDetails.vm";
		
		Object[] aggres = gewaOrderService.countAndSumOrderBySettleBill(bill);
		int totalNumber = ((Long)aggres[0]).intValue();
		if (totalNumber <= 0)
			return "/gewaorder/merchantSettleOrderDetails.vm";
		
		List<GewaOrder> successOrders = gewaOrderService.queryGewaOrderBySettleBill(bill, pageNo, pageSize);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "merchant/gewaorder/querySettleOrderDetails.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("settleBillId", settleBillId);
		m.put("placeId", placeId);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("aggres", aggres);
		model.put("orders", successOrders);
		model.put("ORDER", "ORDER");
		if(StringUtils.isNotBlank(isXls)){
			Place place = daoService.getObject(Place.class, bill.getConfigId());
			this.download("xls", place.getName() + " 的订单明细",  response);
			return "/downloadtemplate/orderDetails2.vm";
		}
		return "/gewaorder/merchantSettleOrderDetails.vm";
	}
	
	@RequestMapping("/platform/gewaorder/querySettleOrderDetails.xhtml")
	public String querySettleOrderDetails(Long settleBillId, String isXls, ModelMap model, Integer pageNo, Integer pageSize, HttpServletResponse response){
		pageNo = StringUtils.isNotBlank(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotBlank(isXls) ? 100000 : pageSize == null ? 500 : pageSize;
		SettlementBill bill = daoService.getObject(SettlementBill.class, settleBillId);
		if (bill == null)
			return "/gewaorder/settleOrderDetails.vm";
		
		Object[] aggres = gewaOrderService.countAndSumOrderBySettleBill(bill);
		int totalNumber = ((Long)aggres[0]).intValue();
		if (totalNumber <= 0)
			return "/gewaorder/settleOrderDetails.vm";
		
		List<GewaOrder> successOrders = gewaOrderService.queryGewaOrderBySettleBill(bill, pageNo, pageSize);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "platform/gewaorder/querySettleOrderDetails.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("settleBillId", settleBillId);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("aggres", aggres);
		model.put("orders", successOrders);
		model.put("ORDER", "ORDER");
		if(StringUtils.isNotBlank(isXls)){
			Place place = daoService.getObject(Place.class, bill.getConfigId());
			this.download("xls", place.getName() + " 的订单明细",  response);
			return "/downloadtemplate/orderDetails.vm";
		}
		return "/gewaorder/settleOrderDetails.vm";
	}
	private void setupRefundment(SettlementBill settlementBill, RefundmentType type, ModelMap model){
		List<Refundment> refundments = refundmentService.queryRefundmentBySettleBill(settlementBill, type);
		List<String> tradeNos = BeanUtil.getBeanPropertyList(refundments, "tradeno", true);
		Map<String, GewaOrder> orderMap = daoService.getObjectMap(GewaOrder.class, tradeNos);
		Double[] aggres = aggreRefund(refundments);
		model.put("aggres", aggres);
		model.put("refund", refundments);
		model.put("om", orderMap);
	}
	/**
	 * @param values
	 * @return
	 */
	private Double[] aggreRefund(List<Refundment> refundments) {
		Double[] aggres = new Double[]{0.0,0.0,0.0};
		aggres[0] = (double) refundments.size();
		for (Refundment refund : refundments){
			aggres[1] +=refund.getQuantity();
			aggres[2] +=refund.getOldSettle() - refund.getNewSettle();
		}
		return aggres;
	}

	@RequestMapping("/merchant/gewaorder/querySettleRefundmentDetails.xhtml")
	public String queryMerchantSettleRefundmentDetails(Long settleBillId, ModelMap model, HttpServletRequest request){
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settleBillId);
		//if (!isPlaceAllowed(settlementBill.getConfigId(), request))
			//return show404(model, "right defined!!");
		setupRefundment(settlementBill, RefundmentType.REFUND, model);
		model.put("REFUND", "REFUND");
		return "/gewaorder/merchantSettleRefundOrderDetails.vm";
	}
	@RequestMapping("/merchant/gewaorder/querySettlePreviousDetails.xhtml")
	public String queryMerchantSettlePreviousDetails(Long settleBillId, ModelMap model, HttpServletRequest request){
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settleBillId);
		//if (!isPlaceAllowed(settlementBill.getConfigId(), request))
			//return show404(model, "right defined!!");
		setupRefundment(settlementBill, RefundmentType.PRIVOUSREFUND, model);
		model.put("PRIVOUSREFUND", "PRIVOUSREFUND");
		return "/gewaorder/merchantSettleRefundOrderDetails.vm";
	}
	
	@RequestMapping("/platform/gewaorder/querySettleRefundmentDetails.xhtml")
	public String querySettleRefundmentDetails(Long settleBillId, ModelMap model){
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settleBillId);
		setupRefundment(settlementBill, RefundmentType.REFUND, model);
		model.put("REFUND", "REFUND");
		return "/gewaorder/settleRefundOrderDetails.vm";
	}
	
	@RequestMapping("/platform/gewaorder/querySettlePreviousDetails.xhtml")
	public String querySettlePreviousDetails(Long settleBillId, ModelMap model){
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settleBillId);
		setupRefundment(settlementBill, RefundmentType.PRIVOUSREFUND, model);
		model.put("PRIVOUSREFUND", "PRIVOUSREFUND");
		return "/gewaorder/settleRefundOrderDetails.vm";
	}
	@RequestMapping("/merchant/gewaorder/querySettleAdjust.xhtml")
	public String queryMerchantSettleAdjust(Long settleBillId, String placeId, ModelMap model){
		if (StringUtils.isBlank(placeId) || settleBillId == null)
			return "/adjust/merchantSettleAdjust.vm";
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, settleBillId);
		if (settleBill == null)
			return "/adjust/merchantSettleAdjust.vm";
		List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(settleBill);
		model.put("ads",ads);
		return "/adjust/merchantSettleAdjust.vm";
	}
	
	@RequestMapping("/platform/gewaorder/querySettleAdjust.xhtml")
	public String querySettleAdjust(Long settleBillId, ModelMap model){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, settleBillId);
		if (settleBill == null)
			return "/adjust/merchantSettleAdjust.vm";
		List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(settleBill);
		model.put("ads",ads);
		return "/adjust/settleAdjust.vm";
	}
	
	/**
	 * 根据订单号查询订单信息
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/gewaorder/queryGewarOrderByTrandeno.xhtml")
	public String queryGewarOrderByTrandeno(String tradeNo, ModelMap model) {
		if(StringUtils.isBlank(tradeNo)) {
			return "/gewaorder/orderDetailsByTradeno.vm";
		}
		GewaOrder order = daoService.getObject(GewaOrder.class, tradeNo);
		model.put("o", order);
		if(order != null) {
			if(order.getCheckBillId() != null) {
				CheckBill ch = daoService.getObject(CheckBill.class, order.getCheckBillId());
				model.put("checkbill", ch);
			}
		}
		Refundment refund = daoService.getObject(Refundment.class, tradeNo);
		model.put("r", refund);
		return "/gewaorder/orderDetailsByTradeno.vm";
	}
	
	/**
	 * 商户标记订单是否开票
	 * @param tradeNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/gewaorder/flagOrderkp.xhtml")
	public String flagOrderkp(String tradeNos, ModelMap model) {
		if(StringUtils.isBlank(tradeNos)) {
			return showJsonError(model, "订单号为空！");
		}
		String[] strs = tradeNos.split(",");
		for(String tradeno : strs) {
			GewaOrder order = daoService.getObject(GewaOrder.class, tradeno);
			if(order == null) {
				continue;
			}
			order.setIsAdjusted(SettleConstant.COMM_Y);
			daoService.updateObject(order);
		}
		return showJsonSuccess(model, "标记成功！");
	}
}
