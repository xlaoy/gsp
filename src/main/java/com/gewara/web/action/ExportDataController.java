package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.ExportSettlementEntity;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleJiti;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.MapRow;
import com.gewara.util.MiscUtil;
import com.gewara.vo.SettleInvoice;

/**
 * 
 * @author user
 *
 */

@Controller
public class ExportDataController extends AnnotationController {
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@RequestMapping("platform/settlement/exportSettlementBills.xhtml")
	public String exportSettlementBills(ModelMap model){
		return "/settlement/toExportpage.vm";
	}
	
	/*@RequestMapping("platform/settlement/exportChannelBaseData.xhtml")
	public String exportChannelBaseData(ModelMap model,Timestamp start,Timestamp end,HttpServletResponse response){
		if(null == start || null == end){
			return "/settlement/exportChannelBaseData.vm";
		}
		List<Object> params = new ArrayList<Object>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT p.name , a.place_id , a.month, a.ticket , a.amount, p.brand_name , p.open_type, p.city_code ");
		sb.append(" FROM ( SELECT place_id , DATE_TRUNC('month',deal_time) AS MONTH ,SUM(quantity) AS ticket , SUM(total_cost) AS amount ");
		sb.append("  FROM  gewa_order WHERE deal_time >= ? AND deal_time < ? AND category NOT IN('PNX' ,'GEWA' , 'GPTBS' ,'TXPC') ");
		sb.append("  GROUP BY place_id ,DATE_TRUNC('month' ,deal_time) ");
		sb.append("    ) AS a LEFT JOIN place p ON a.place_id = p.place_id");
		params.add(start);
		params.add(end);
		List<Map<String, Object>> qryMap = jdbcTemplate.queryForList(sb.toString(),params.toArray());
		for(Map<String, Object> m : qryMap){
			m.put("cityName",cityInfoHolder.getCityNameByCode(m.get("city_code") == null ? "" :  m.get("city_code").toString()));
		}
		model.put("qryMap", qryMap);
		this.download("xls",DateUtil.formatDate(start) + "-" + DateUtil.formatDate(end) + "通道计提基础数据", response);
		return "/downloadtemplate/exportChannelBaseData.vm";
	}*/
	
	
	/**
	 * 手动记账
	 * @param recordIds
	 * @param isOnline
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/settlement/jizhang.xhtml")
	public String jizhang(String tag, String month, ModelMap model){
		if(StringUtils.isBlank(tag)) {
			return showJsonError(model, "结算单类型为空！");
		}
		if(StringUtils.isBlank(month)) {
			return showJsonError(model, "月份为空！！");
		}
		
		Timestamp startTime = DateUtil.parseTimestamp(month + "-01", "yyyy-MM-dd");
		Timestamp temp = DateUtil.parseTimestamp(month + "-02", "yyyy-MM-dd");
		Calendar cd = Calendar.getInstance();
		cd.setTimeInMillis(temp.getTime());
		cd.add(Calendar.MONTH, 1);
		Timestamp endTime =  new Timestamp(cd.getTimeInMillis());
		
		String update = "update settlement_bill set has_jizhang = 'Y' where tag = ? and start_time >= ? and end_time < ? ";
		
		jdbcTemplate.update(update, tag, startTime, endTime);
		
		return showJsonSuccess(model, "记账成功！");
	}
	
	/**
	 * 导出提计成本结算单
	 * @param tag
	 * @param month
	 * @param model
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/settlement/exportTiji.xhtml")
	public String exportTiji(String tag, String month, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		if(StringUtils.isBlank(tag)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(month)) {
			return "/settlement/toExportpage.vm";
		}
		
		Timestamp startTime = DateUtil.parseTimestamp(month + "-01", "yyyy-MM-dd");
		Timestamp temp = DateUtil.parseTimestamp(month + "-02", "yyyy-MM-dd");
		Calendar cd = Calendar.getInstance();
		cd.setTimeInMillis(temp.getTime());
		cd.add(Calendar.MONTH, 1);
		Timestamp endTime =  new Timestamp(cd.getTimeInMillis());
		
		DetachedCriteria query = DetachedCriteria.forClass(SettleJiti.class);
		query.add(Restrictions.eq("billtype", tag));
		query.add(Restrictions.ge("starttime", startTime));
		query.add(Restrictions.lt("endtime", endTime));
		query.addOrder(Order.asc("starttime"));
		query.addOrder(Order.desc("vendercode"));
		List<SettleJiti> jitilist = daoService.findByCriteria(query);
		model.put("jitilist", jitilist);
		
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(tag)) {
			download("xls", month + "影票结算单", response);
			return "/downloadtemplate/ticket.vm";
		} else if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(tag)) {
			download("xls", month + "卖品结算单", response);
			return "/downloadtemplate/goods.vm";
		} else if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)) {
			download("xls", month + "通道费结算单", response);
			return "/downloadtemplate/channel.vm";
		} else {
			return "/settlement/toExportpage.vm";
		}
	}
	
	
	@RequestMapping("/platform/settlement/exportOrder.xhtml")
	public String exportOrder(String type, Timestamp start, Timestamp end, String placeIds, String cate, String ordertype, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(type)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(cate)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(ordertype)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(placeIds)) {
			return "/settlement/toExportpage.vm";
		}
		if(start == null) {
			return "/settlement/toExportpage.vm";
		}
		if(end == null) {
			return "/settlement/toExportpage.vm";
		}
		
		String[] t = placeIds.split(",");
		if(t.length > 100) {
			return "/settlement/toExportpage.vm";
		}
		
		StringBuilder sb = new StringBuilder("");
		sb.append("select p.name, o.* from gewa_order o LEFT OUTER JOIN place p on p.place_id = o.place_id ");
		sb.append("where o.order_type = ? ");
		if("dealTime".equals(type)) {
			sb.append("and o.deal_time >= ? ");
			sb.append("and o.deal_time < ? ");
		} else {
			sb.append("and o.use_time >= ? ");
			sb.append("and o.use_time < ? ");
		}
		if(SettleConstant.ZL.equals(cate)) {
			for(String str : SettleConstant.UNSETTLECATE) {
				sb.append("and o.category <> '" + str + "' ");
			}
		} else if(SettleConstant.XN.equals(cate)) {
			sb.append("and o.category = 'GEWA' ");
		} else if(SettleConstant.GW.equals(cate)) {
			sb.append("and o.category = 'GPTBS' ");
		} else if(SettleConstant.WP.equals(cate)) {
			sb.append("and o.category = 'WP' ");
		}
		sb.append("and o.place_id in ( ");
		sb.append(placeIds);
		sb.append(" ) ");
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sb.toString(), ordertype, start, end);
		model.put("mapList", mapList);
		download("xls", "订单", response);
		return "/downloadtemplate/orderExport.vm";
	}
	
	@RequestMapping("/platform/settlement/exportRefund.xhtml")
	public String exportRefund(String type, Timestamp start, Timestamp end, String placeIds, String cate,  String ordertype, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(type)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(cate)) {
			return "/settlement/toExportpage.vm";
		}
		if(StringUtils.isBlank(placeIds)) {
			return "/settlement/toExportpage.vm";
		}
		if(start == null) {
			return "/settlement/toExportpage.vm";
		}
		if(end == null) {
			return "/settlement/toExportpage.vm";
		}
		
		String[] t = placeIds.split(",");
		if(t.length > 100) {
			return "/settlement/toExportpage.vm";
		}
		
		StringBuilder sb = new StringBuilder("");
		sb.append("select p.name, r.*, o.outer_id from refundment r LEFT OUTER JOIN place p on r.relate_id = p.place_id ");
		sb.append("LEFT OUTER JOIN gewa_order o on o.tradeno = r.tradeno ");
		sb.append("where r.order_type = ? ");
		if("refundTime".equals(type)) {
			sb.append("and r.refund_time >= ? ");
			sb.append("and r.refund_time < ? ");
		} else {
			sb.append("and r.use_time >= ? ");
			sb.append("and r.use_time < ? ");
		}
		if(SettleConstant.ZL.equals(cate)) {
			for(String str : SettleConstant.UNSETTLECATE) {
				sb.append("and r.refund_category <> '" + str + "' ");
			}
		} else if(SettleConstant.XN.equals(cate)) {
			sb.append("and r.refund_category = 'GEWA' ");
		} else if(SettleConstant.GW.equals(cate)) {
			sb.append("and r.refund_category = 'GPTBS' ");
		} else if(SettleConstant.WP.equals(cate)) {
			sb.append("and r.refund_category = 'WP' ");
		}
		sb.append("and r.relate_id in ( ");
		sb.append(placeIds);
		sb.append(" ) ");
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sb.toString(), ordertype, start, end);
		model.put("mapList", mapList);
		download("xls", "退单", response);
		return "/downloadtemplate/refundExport.vm";
	}
	
	@RequestMapping("/platform/settlement/exportZipeiOrder.xhtml")
	public String exportZipeiOrder(String month, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(month)) {
			return "/settlement/toExportpage.vm";
		}
		month = month + "-01";
		Timestamp start = DateUtil.parseTimestamp(month, "yyyy-MM-dd");
		Timestamp end = MiscUtil.addMonth(start, 1);
		
		StringBuilder sb = new StringBuilder("");
		sb.append("select p.name, o.* from gewa_order o LEFT OUTER JOIN place p on p.place_id = o.place_id ");
		sb.append("where o.order_type = 'TICKET' ");
		sb.append("and o.deal_time >= ? ");
		sb.append("and o.deal_time < ? ");
		sb.append("and o.order_status = 'paid_return' ");
		sb.append("and (o.category <> 'PNX' or o.category <> 'GEWA' or o.category <> 'GPTBS' or o.category <> 'TXPC' or o.category <> 'WP') ");
		sb.append("and not EXISTS ( ");
		sb.append("select 1 from refundment r where r.tradeno = o.tradeno and r.refund_time >= ? ");
		sb.append(") ");
		
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sb.toString(), start, end, start);
		model.put("mapList", mapList);
		download("xls", "自赔订单", response);
		return "/downloadtemplate/orderExport.vm";
	}
	
	/**
	 * 导出影票结算单发票统计
	 * @param month
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/platform/settlement/exportSettleInvoice.xhtml")
	public String exportSettleInvoice(String month, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(month)) {
			return "/settlement/toExportpage.vm";
		}
		
		//String sqlinv = "select invoicecode from invoice where status = 'INVOSTADIKOU' and dikoumonth = ? ";
		//List<String> invoicecodelist = jdbcTemplate.queryForList(sqlinv, String.class, month);
		
		Timestamp startTime = DateUtil.parseTimestamp(month + "-01", "yyyy-MM-dd");
		Timestamp temp = DateUtil.parseTimestamp(month + "-02", "yyyy-MM-dd");
		Timestamp endTime = MiscUtil.addMonth(temp, 1);
		
		List<SettleInvoice> silist = new ArrayList<SettleInvoice>();
		//if(CollectionUtils.isNotEmpty(invoicecodelist)) {
			StringBuilder sb = new StringBuilder("");
			/*sb.append("select distinct sb.record_id, sb.start_time, sb.end_time, sb.config_id, sb.order_total_amount ");
			sb.append("from invoice_settlerelate isr, settlement_bill sb ");
			sb.append("where isr.settleid = sb.record_id ");
			sb.append("and sb.tag = 'TICKET' ");
			sb.append("and isr.invoicecode in " + DramaUtil.sqlinList(invoicecodelist, String.class));*/
			sb.append("select sb.record_id, sb.start_time, sb.end_time, sb.config_id, sb.order_total_amount ");
			sb.append("from settlement_bill sb ");
			sb.append("where sb.tag = 'TICKET' ");
			sb.append("and sb.status = 'SETTLED' ");
			sb.append("and sb.start_time >= ? ");
			sb.append("and sb.end_time < ? ");
			List<Map<String, Object>> settlelist = jdbcTemplate.queryForList(sb.toString(), startTime, endTime);
			
			if(CollectionUtils.isNotEmpty(settlelist)) {
				
				List<Integer> tmplist = BeanUtil.getBeanPropertyList(settlelist, "record_id", true);
				List<Long> settleidlist = new ArrayList<Long>();
				for(Integer inte : tmplist) {
					settleidlist.add(inte.longValue());
				}
				List<String> configidlist = BeanUtil.getBeanPropertyList(settlelist, "config_id", true);
				
				List<SettlementBillExtend> extendlist = daoService.getObjectBatch(SettlementBillExtend.class, "recordid", settleidlist);
				List<SettleJiti> jitilist = daoService.getObjectBatch(SettleJiti.class, "recordid", settleidlist);
				List<Place> placelist = daoService.getObjectBatch(Place.class, "recordId", configidlist);
				List<SettleConfig> configlist = daoService.getObjectBatch(SettleConfig.class, "recordId", configidlist);
				
				Map<Long, SettlementBillExtend> extendmap = new HashMap<Long, SettlementBillExtend>();
				for(SettlementBillExtend extend : extendlist) {
					extendmap.put(extend.getRecordid(), extend);
				}
				
				Map<Long, SettleJiti> jitimap = new HashMap<Long, SettleJiti>();
				for(SettleJiti jiti : jitilist) {
					jitimap.put(jiti.getRecordid(), jiti);
				}
				
				Map<String, Place> placemap = new HashMap<String, Place>();
				for(Place place : placelist) {
					placemap.put(place.getRecordId(), place);
				}
				
				Map<String, SettleConfig> configmap = new HashMap<String, SettleConfig>();
				for(SettleConfig config : configlist) {
					configmap.put(config.getRecordId(), config);
				}
				
				for(Map<String, Object> map : settlelist) {
					MapRow row = new MapRow(map);
					Long recordid = row.getLongWithNull("record_id");
					String configid = row.getStringWithNull("config_id");
					
					SettleInvoice si = new SettleInvoice();
					si.setRecordid(recordid);
					si.setStarttime(row.getTimestamp("start_time"));
					si.setEndtime(row.getTimestamp("end_time"));
					si.setJsamount(row.getDouble("order_total_amount"));
					Place place = placemap.get(configid);
					if(place != null) {
						si.setPlacename(place.getName());
					}
					SettleConfig config = configmap.get(configid);
					if(config != null) {
						si.setVendercode(config.getVenderNo());
					}
					SettleJiti jiti = jitimap.get(recordid);
					if(jiti != null) {
						si.setTaxrate(jiti.getTaxrate());
						si.setJtamount(jiti.getAmount());
						si.setJtkpamount(jiti.getKpamount());
						si.setJtbhsamount(jiti.getExclutax());
						si.setJttaxamount(jiti.getTaxamount());
						SettlementBillExtend extend = extendmap.get(recordid);
						if(extend != null) {
							si.setJskpamount(extend.getBillingamount());
						}
					}
					silist.add(si);
				}
			}
		//}
		model.put("silist", silist);
		download("xls", "结算发票", response);
		return "/downloadtemplate/settleInvoice.vm";
	}
}
