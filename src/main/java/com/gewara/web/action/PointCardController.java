package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.PointCardOrder;
import com.gewara.model.gsp.PointCardPlaceBill;
import com.gewara.model.gsp.PointCardSettleBill;
import com.gewara.service.PointCardService;
import com.gewara.service.impl.PointCardServiceImpl;
import com.gewara.util.BeanUtil;
import com.gewara.util.DramaUtil;
import com.gewara.web.util.PageUtil;


@Controller
public class PointCardController extends AnnotationController {

	@Autowired
	@Qualifier("pointCardService")
	private PointCardService pointCardService;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate;
	
	/**
	 * 微票点卡账单查询
	 * @param recordid
	 * @param mouth
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("platform/pointcard/queryWPPointCard.xhtml")
	public String queryWPPointCard(Long recordid, String ctype, Timestamp start, Timestamp end, Long settleid,
			Integer pageNo, Integer pageSize, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 35 : pageSize;
		
		DetachedCriteria querycount = DetachedCriteria.forClass(PointCardSettleBill.class);
		DetachedCriteria querylist = DetachedCriteria.forClass(PointCardSettleBill.class);
		
		querycount = getWPPointCardDeta(querycount, recordid, ctype, settleid, start, end, null);
		querylist = getWPPointCardDeta(querylist, recordid, ctype, settleid, start, end, null);
		
		querycount.setProjection(Projections.rowCount());
		if(SettleConstant.MONTH.equals(ctype)) {
			querylist.addOrder(Order.desc("starttime"));
		} else {
			querylist.addOrder(Order.asc("starttime"));
		}
		
		
		int count = Integer.valueOf(daoService.findByCriteria(querycount).get(0).toString());
		List<PointCardSettleBill> sblist = new ArrayList<PointCardSettleBill>();
		
		if(count > 0) {
			sblist = daoService.findByCriteria(querylist, pageSize * pageNo, pageSize);
			String url = "platform/pointcard/queryWPPointCard.xhtml";
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("recordid", recordid);
			m.put("ctype", ctype);
			m.put("start", start);
			m.put("end", end);
			m.put("settleid", settleid);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		model.put("sblist", sblist);
		model.put("recordid", recordid);
		model.put("ctype", ctype);
		model.put("start", start);
		model.put("end", end);
		model.put("settleid", settleid);
		return "wp/wpPointCard.vm";
	}
	
	private DetachedCriteria getWPPointCardDeta(DetachedCriteria query, Long recordid, String ctype,  Long settleid,
			Timestamp start, Timestamp end, Long partnerid) {
		if(recordid != null) {
			query.add(Restrictions.eq("recordid", recordid));
		}
		if(StringUtils.isEmpty(ctype)) {
			ctype = SettleConstant.MONTH;
		}
		query.add(Restrictions.eq("ctype", ctype));
		if(start != null) {
			query.add(Restrictions.ge("starttime", start));
		}
		if(end != null) {
			query.add(Restrictions.le("endtime", end));
		}
		if(partnerid != null) {
			query.add(Restrictions.eq("partnerid", partnerid));
		}
		if(settleid != null) {
			query.add(Restrictions.eq("relatedbillid", settleid));
		}
		return query;
	}
	
	/**
	 * 微票影院点卡账单查询
	 * @param recordid
	 * @param mouth
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("platform/pointcard/queryPlaceWPPointCard.xhtml")
	public String queryPlaceWPPointCard(Long settleid, String ctype, Long cinemaid, Timestamp start, Timestamp end, 
			String isXls, Integer pageNo, Integer pageSize, ModelMap model, HttpServletResponse response) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		if(!StringUtils.isEmpty(isXls)) {
			pageSize = pageSize == null ? 10000 : pageSize;
		} else {
			pageSize = pageSize == null ? 35 : pageSize;
		}
		
		DetachedCriteria querycount = DetachedCriteria.forClass(PointCardPlaceBill.class);
		DetachedCriteria querylist = DetachedCriteria.forClass(PointCardPlaceBill.class);
		
		querycount = getPlaceWPPointCardDeta(querycount, settleid, ctype, start, end, null, cinemaid);
		querylist = getPlaceWPPointCardDeta(querylist, settleid, ctype, start, end, null, cinemaid);
		
		querycount.setProjection(Projections.rowCount());
		if(SettleConstant.MONTH.equals(ctype)) {
			querylist.addOrder(Order.desc("starttime"));
		} else {
			querylist.addOrder(Order.asc("starttime"));
		}
		querylist.addOrder(Order.asc("cinemaid"));
		
		int count = Integer.valueOf(daoService.findByCriteria(querycount).get(0).toString());
		List<PointCardPlaceBill> sblist = new ArrayList<PointCardPlaceBill>();
		
		if(count > 0) {
			sblist = daoService.findByCriteria(querylist, pageSize * pageNo, pageSize);
			String url = "platform/pointcard/queryPlaceWPPointCard.xhtml";
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("settleid", settleid);
			m.put("ctype", ctype);
			m.put("start", start);
			m.put("end", end);
			m.put("cinemaid", cinemaid);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		model.put("sblist", sblist);
		model.put("settleid", settleid);
		model.put("ctype", ctype);
		model.put("start", start);
		model.put("end", end);
		model.put("cinemaid", cinemaid);
		if(!StringUtils.isEmpty(isXls)) {
			this.download("xls", "wp-cinema-pointcard", response);
			return "wp/wpPlacePointCard_xls.vm";
		}
		return "wp/wpPlacePointCard.vm";
	}
	
	private DetachedCriteria getPlaceWPPointCardDeta(DetachedCriteria query, Long settleid, String ctype, 
			Timestamp start, Timestamp end, Long partnerid, Long cinemaid) {
		if(settleid != null) {
			query.add(Restrictions.eq("relatedbillid", settleid));
		}
		if(StringUtils.isEmpty(ctype)) {
			ctype = SettleConstant.MONTH;
		}
		query.add(Restrictions.eq("ctype", ctype));
		if(start != null) {
			query.add(Restrictions.ge("starttime", start));
		}
		if(end != null) {
			query.add(Restrictions.le("endtime", end));
		}
		if(partnerid != null) {
			query.add(Restrictions.eq("partnerid", partnerid));
		}
		if(cinemaid != null) {
			query.add(Restrictions.eq("cinemaid", cinemaid));
		}
		return query;
	}
	
	/**
	 * 导出点卡订单
	 * @param recordid
	 * @param mouth
	 * @param model
	 * @return
	 */
	@RequestMapping("platform/pointcard/exportPointCardOrder.xhtml")
	public String exportPointCardOrder(Long settleid, String etype, String isXls, ModelMap model, HttpServletResponse response) {
		if(settleid == null) {
			return showError(model, "账单号为空");
		}
		if(StringUtils.isEmpty(etype)) {
			return showError(model, "账单类型为空");
		}
		List<PointCardOrder> orderlist = new ArrayList<PointCardOrder>();
		if("wpc".equals(etype)) {//系统账单
			PointCardSettleBill pcsb = daoService.getObject(PointCardSettleBill.class, settleid);
			if(pcsb == null) {
				return showError(model, "PointCardSettleBill账单为空");
			}
			model.put("pcsb", pcsb);
			orderlist = pointCardService.getPointCardOrders(pcsb.getPartnerid(), null, pcsb.getStarttime(), pcsb.getEndtime(), null);
			List<PointCardOrder> refundlist = pointCardService.getPointCardOrders(pcsb.getPartnerid(), null, pcsb.getStarttime(), pcsb.getEndtime(), PointCardServiceImpl.PAID_RETURN);
			getRefundOrderList(model, refundlist);
		} else if("ppc".equals(etype)) {//影院账单
			PointCardPlaceBill pcpb = daoService.getObject(PointCardPlaceBill.class, settleid);
			if(pcpb == null) {
				return showError(model, "PointCardPlaceBill账单为空");
			}
			model.put("pcpb", pcpb);
			orderlist = pointCardService.getPointCardOrders(pcpb.getPartnerid(), pcpb.getCinemaid(), pcpb.getStarttime(), pcpb.getEndtime(), null);
			List<PointCardOrder> refundlist = pointCardService.getPointCardOrders(pcpb.getPartnerid(), pcpb.getCinemaid(), pcpb.getStarttime(), pcpb.getEndtime(), PointCardServiceImpl.PAID_RETURN);
			getRefundOrderList(model, refundlist);
		} else {
			return showError(model, "账单类型不正确");
		}
		model.put("orderlist", orderlist);
		if(StringUtils.isNotEmpty(isXls)) {
			this.download("xls", "pointcard-order", response);
			return "wp/pointCardOrderList_xls.vm";
		}
		model.put("etype", etype);
		return "wp/pointCardOrderList.vm";
	}
	
	private void getRefundOrderList(ModelMap model, List<PointCardOrder> refundlist) {
		Map<String, Map<String, Object>> refundmap = new HashMap<String, Map<String, Object>>();
		if(CollectionUtils.isEmpty(refundlist)) {
			return;
		}
		List<String> tradenos = BeanUtil.getBeanPropertyList(refundlist, "tradeno", true);
		String sql = "select tradeno, refundtime, amount, refundtype, refundreason from WEBDATA.VIEW_PARTNER_CARD_PAYMENT where tradetype = 'REFUND' and tradeno in " + DramaUtil.sqlinList(tradenos, String.class);
		List<Map<String, Object>> refundmaplist = shJdbcTemplate.queryForList(sql);
		for(Map<String, Object> map : refundmaplist) {
			refundmap.put(map.get("tradeno").toString(), map);
		}
		model.put("refundmap", refundmap);
	}
	
	/**
	 * 微票影院点卡账单查询
	 * @param recordid
	 * @param mouth
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("platform/pointcard/queryPointCardOrder.xhtml")
	public String queryPointCardOrder(Long partnerid, String tradeno, String outtradeno, ModelMap model) {
		model.put("tradeno", tradeno);
		model.put("outtradeno", outtradeno);
		model.put("partnerid", partnerid);
		if(StringUtils.isNotEmpty(tradeno)) {
			PointCardOrder order = daoService.getObjectByUkey(PointCardOrder.class, "tradeno", tradeno.trim());
			queryPointCardPayment(model, order);
		}
		if(StringUtils.isNotEmpty(outtradeno) && partnerid != null) {
			DetachedCriteria query = DetachedCriteria.forClass(PointCardOrder.class);
			query.add(Restrictions.eq("partnerid", partnerid));
			query.add(Restrictions.eq("outtradeno", outtradeno));
			List<PointCardOrder> list = daoService.findByCriteria(query);
			if(CollectionUtils.isNotEmpty(list)) {
				PointCardOrder order = list.get(0);
				queryPointCardPayment(model, order);
			}
		}
		return "wp/pointCardOrder.vm";
	}
	
	private void queryPointCardPayment(ModelMap model, PointCardOrder order) {
		if(order != null) {
			Long partnerid = order.getPartnerid();
			String outtradeno = order.getOuttradeno();
			if(partnerid != null && StringUtils.isNotEmpty(outtradeno)) {
				String sql = "select * from WEBDATA.VIEW_PARTNER_CARD_PAYMENT where partnerid = ? and outtradeno = ?";
				List<Map<String, Object>> paymentlist = shJdbcTemplate.queryForList(sql, partnerid, outtradeno);
				model.put("paymentlist", paymentlist);
			}
		}
		model.put("o", order);
	}
}
