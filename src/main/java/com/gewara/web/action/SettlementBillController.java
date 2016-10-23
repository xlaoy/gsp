/** 
 */
package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
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
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.PricedPlayItem;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.SettlementBillSnapshoot;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.model.gsp.SysData;
import com.gewara.service.ChannelSettleService;
import com.gewara.service.DownloadRecorderService;
import com.gewara.service.ReqMoneyBillService;
import com.gewara.service.SettlementBillService;
import com.gewara.service.SysDataService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.vo.SettleBillVo;
import com.gewara.web.util.PageUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 9, 2013  9:50:37 AM
 */
@Controller
public class SettlementBillController extends AnnotationController implements InitializingBean{
	
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	@Autowired
	@Qualifier("reqMoneyBillService")
	private ReqMoneyBillService reqMoneyBillService;
	@Autowired
	@Qualifier("channelSettleService")
	private ChannelSettleService channelSettleService;
	@Autowired
	@Qualifier("downloadRecorderService")
	private DownloadRecorderService downloadRecorderService;
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	
	private static List<String> brandList;
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/settlement/queryconfirmedSettleBill.xhtml")
	public String queryConfirmedSettleBills(DownloadRecorder dr, ModelMap model, Integer pageNo, Integer pageSize){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 20 : pageSize;
		
		List<DownloadRecorder> drList = new ArrayList<DownloadRecorder>();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(DownloadRecorder.class);
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		
		if(StringUtils.isNotEmpty(dr.getSpecial())) {
			queryCount.add(Restrictions.eq("special", dr.getSpecial()));
			query.add(Restrictions.eq("special", dr.getSpecial()));
		}
		if(StringUtils.isNotEmpty(dr.getOptUser())) {
			queryCount.add(Restrictions.eq("optUser", dr.getOptUser()));
			query.add(Restrictions.eq("optUser", dr.getOptUser()));
		}
		if(dr.getStart() != null) {
			queryCount.add(Restrictions.ge("addTime", dr.getStart()));
			query.add(Restrictions.ge("addTime", dr.getStart()));
		}
		if(dr.getEnd() != null) {
			queryCount.add(Restrictions.le("addTime", dr.getEnd()));
			query.add(Restrictions.le("addTime", dr.getEnd()));
		}
		if(dr.getSettlementId() != null) {
			queryCount.add(Restrictions.eq("settlementId", dr.getSettlementId()));
			query.add(Restrictions.eq("settlementId", dr.getSettlementId()));
		}
		queryCount.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		query.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		
		queryCount.setProjection(Projections.rowCount());
		query.addOrder(Order.desc("addTime"));
		
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			drList = daoService.findByCriteria(query, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo,
					config.getBasePath() + "platform/settlement/queryconfirmedSettleBill.xhtml",
					true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("special", dr.getSpecial());
			m.put("optUser", dr.getOptUser());
			m.put("start", dr.getStart());
			m.put("end", dr.getEnd());
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("special", dr.getSpecial());
		model.put("optUser", dr.getOptUser());
		model.put("start", dr.getStart());
		model.put("end", dr.getEnd());
		model.put("drList", drList);
		return "/settlement/confirmedSettleBills.vm";
	}
	

	@RequestMapping("/platform/settlement/queryPricedPlayItem.xhtml")
	public String queryPricedPlayItem(Long settlementId, ModelMap model){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, settlementId);
		if (settleBill == null) {
			return "/settlement/pricedPlayItem.vm";
		}
		
		SettleConfig settleConfig = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
		Place place = daoService.getObject(Place.class, settleBill.getConfigId());
		String setttleBase = SettleBaseEnums.ORDERTIME.getType().equals(settleConfig.getSettleBase()) ? "deal_time" : "use_time";
		
		String sql = "select o.play_id, sum(o.quantity) " +
						"from gewa_order o, priced_play_item p" + 
						" where o.play_id = p.play_id and o.order_status = 'paid_success' and o." + setttleBase + 
						" >= ? and o." + setttleBase + " < ? and o.place_id = ? " + 
						"group by o.play_id";
		List<Map<String, Object>> resultMap = jdbcTemplate.queryForList(sql, settleBill.getStartTime(), settleBill.getEndTime(), settleBill.getRelateId());
		List<Long> playIds = BeanUtil.getBeanPropertyList(resultMap, "play_id", true);
		List<PricedPlayItem> tempList = daoService.getObjectList(PricedPlayItem.class, playIds);
		Map<Long, PricedPlayItem> pricedPlayItemMap = BeanUtil.beanListToMap(tempList, "playId");
		model.put("resultMap", resultMap);
		model.put("ppim", pricedPlayItemMap);
		model.put("place", place);
		
		return "/settlement/pricedPlayItem.vm";
	}
	
	@RequestMapping("/platform/settlement/settlementbills.xhtml")
	public String settlementbills(SettleBillVo billvo, Integer pageNo, Integer pageSize, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response){
		billvo.setUrl("/platform/settlement/settlementbills.xhtml");
		model = settlementBillService.settlementbills(billvo, pageNo, pageSize, model);
		if(StringUtils.isNotBlank(billvo.getIsXls())){
			this.download("xls", "结算单" , response);
			return "/downloadtemplate/settlementbills.vm";
		}
		//重置调整权限
		String gewauser = getLogonUser(request).getUsername();
		SysData admin = sysDataService.getSysData(gewauser, SettleConstant.RESETADJUST, String.class);
		if(admin != null) {
			model.put("resetadjust", SettleConstant.COMM_Y);
		} else {
			model.put("resetadjust", SettleConstant.COMM_N);
		}
		return "/settlement/settlementbills.vm";
	}
	
	
	
	@RequestMapping("/platform/settlement/queryPayBills.xhtml")
	public String queryPayBills(String placeId, Integer pageNo, Integer pageSize, ModelMap model){
		if (StringUtils.isBlank(placeId))
			return "/settlement/payBills.vm";
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		int totalNumber = settlementBillService.countingPayBills(placeId);
		if (totalNumber <= 0)
			return "/settlement/payBills.vm";
		
		List<SettlementBill>  settleBills = settlementBillService.queryPayBills(placeId, pageNo, pageSize);
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settleBills, "recordId", true);
		
		Map<Long, StatusTracker> merchantConfirm = settlementBillService.queryMerchantConfirm(settleIds);
		Map<Long, StatusTracker> gewaPayBill = settlementBillService.queryGewaPayBill(settleIds);
		
		model.put("sb", settleBills);
		model.put("mc", merchantConfirm);
		model.put("gp", gewaPayBill);
		
		return "/settlement/payBills.vm";
	}
	
	
	@RequestMapping("/platform/settlement/paySettle.xhtml")
	public String paySettle(Long recordId, String isOnline, ModelMap model, HttpServletRequest request){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
		if (settleBill == null)
			return showJsonError(model, "没有找到相关记录");
		
		if (!CheckBillStatusEnums.WAITINGPAY.getStatus().equals(settleBill.getStatus()))
			return showJsonError(model, "结算单当前状态不允许付款到影院. code:" + settleBill.getStatus());
		
		String tag = settleBill.getTag();
		if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)){
			Long vendorId = settleBill.getRelateId();
			ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, vendorId);
			if(StringUtils.isEmpty(csc.getVendorCode())){
				return showJsonError(model, "通道费供应商：" + csc.getVendorName() + " 没有配置供应商编号， 请配置， 本次付款失败！"); 
			}
		}else{
			SettleConfig sc = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
			if (StringUtils.isBlank(sc.getVenderNo())){
				Place place = daoService.getObject(Place.class, sc.getRecordId());
				return showJsonError(model, "影院：" + place.getName() + " 没有配置供应商编号， 请配置， 本次付款失败！");
			}
		}
		
		if (settleBill.getOrderTotalAmount().intValue() == 0){
			return showJsonError(model, "此笔结算单的结算金额为0， 无需付款！");
		}
		
		User user = getLogonUser(request);
		Long uid = user.getId();
		ChangeEntry ce = new ChangeEntry(settleBill);
		ResultCode result = settlementBillService.paySettle(settleBill, user, isOnline);
		if (!result.isSuccess())
			return showJsonError(model,result.getMsg());
		
		monitorService.saveChangeLog(uid, SettlementBill.class, settleBill.
										getRecordId(), ce.getChangeMap(settleBill));
		return showJsonSuccess(model, "成功提交付款");
	}
	
	@RequestMapping("/platform/settlement/batchPaySettle.xhtml")
	public String batchPaySettle(String recordIds, String isOnline, ModelMap model, HttpServletRequest request){
		if(StringUtils.isEmpty(recordIds)){
			return showJsonError(model, "参数错误");
		}
		String[] settleIds = recordIds.split(",");
		for(String settleId : settleIds){
			Long recordId = Long.valueOf(settleId);
			SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
			if (settleBill == null){
				continue;
			}
			
			if (!CheckBillStatusEnums.WAITINGPAY.getStatus().equals(settleBill.getStatus())){
				continue;
			}
			
			String tag = settleBill.getTag();
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)){
				ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, settleBill.getRelateId());
				if(csc == null || StringUtils.isEmpty(csc.getVendorCode())){
					continue;
				}
			}else{
				SettleConfig sc = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
				if (StringUtils.isBlank(sc.getVenderNo())){
					continue;
				}
			}
			
			if (settleBill.getOrderTotalAmount().intValue() <= 0){
				continue;
			}
			
			User user = getLogonUser(request);
			Long uid = user.getId();
			ChangeEntry ce = new ChangeEntry(settleBill);
			ResultCode result = settlementBillService.paySettle(settleBill, user, isOnline);
			if (!result.isSuccess()){
				continue;
			}
			monitorService.saveChangeLog(uid, SettlementBill.class, settleBill.getRecordId(), ce.getChangeMap(settleBill));
			
		}
		return showJsonSuccess(model, "成功提交付款");
	}	
	
	@RequestMapping("/platform/settlement/rePaySettle.xhtml")
	public String rePaySettle(Long recordId, ModelMap model, HttpServletRequest request){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
		if(null == settleBill){
			return showJsonError(model,"没有找到相关记录");
		}
		
		if(!StringUtils.equals(CheckBillStatusEnums.SETTLED.getStatus(), settleBill.getStatus())){
			return showJsonError(model,"结算单当前状态不允许重新付款. code:" + settleBill.getStatus());
		}
		
		if (settleBill.getOrderTotalAmount().intValue() == 0){
			return showJsonError(model, "此笔结算单的结算金额为0， 无需付款！");
		}
		
		User user = getLogonUser(request);
		DownloadRecorder dlRecorder  = downloadRecorderService.getRecorderBySettleBill(settleBill);;
		if(null != dlRecorder){
			ChangeEntry ce = new ChangeEntry(dlRecorder);
			dlRecorder.setMaxDownCount(dlRecorder.getMaxDownCount() + 1);
			dlRecorder.setAddTime(DateUtil.getCurFullTimestamp());
			dlRecorder.setOptUser(getLogonUser(request).getUsername());
			dlRecorder.setStatus(SettleConstant.JSSUBMIT);
			daoService.updateObject(dlRecorder);
			monitorService.saveChangeLog(user.getId(), DownloadRecorder.class, dlRecorder.getRecordId(), ce.getChangeMap(dlRecorder));	
		}
		return showJsonSuccess(model, "成功提交付款");
	}
	

	@RequestMapping("/platform/settlement/confirmPay.xhtml")
	public String confirmPay(Long recordId, ModelMap model, HttpServletRequest request){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
		if (settleBill == null)
			return showJsonError(model, "没有找到相关记录");
		
		if (!CheckBillStatusEnums.MERCHANTCONFIRMED.getStatus().equals(settleBill.getStatus()))
			return showJsonError(model, "结算单当前状态不允许提交付款. code:" + settleBill.getStatus());
		
		User user = getLogonUser(request);
		Long uid = user.getId();
		ChangeEntry ce = new ChangeEntry(settleBill);
		
		ResultCode result = settlementBillService.confirmPay(settleBill, user);
		if (!result.isSuccess())
			return showJsonError(model,result.getMsg());
		
		monitorService.saveChangeLog(uid, SettlementBill.class, settleBill.
				getRecordId(), ce.getChangeMap(settleBill));
		return showJsonSuccess(model, "成功提交到付款");
	}
	
	@RequestMapping("/platform/settlement/gewaReadjust.xhtml")
	public String gewaReadjust(Long recordId, ModelMap model, HttpServletRequest request){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
		if (settleBill == null)
			return showJsonError(model, "没有找到相关记录");
		
		if (!CheckBillStatusEnums.MERCHANTCONFIRMED.getStatus().equals(settleBill.getStatus()))
			return showJsonError(model, "结算单当前状态不允许重新调整. code:" + settleBill.getStatus());
		
		User user = getLogonUser(request);
		Long uid = user.getId();
		ChangeEntry ce = new ChangeEntry(settleBill);
		
		ResultCode result = settlementBillService.gewaReadjust(settleBill, user);
		if (!result.isSuccess())
			return showJsonError(model,result.getMsg());
		
		monitorService.saveChangeLog(uid, SettlementBill.class, settleBill.
				getRecordId(), ce.getChangeMap(settleBill));
		return showJsonSuccess(model, "成功提交到重新调整");
	}
	
	
	
	
	
	/**
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/settlement/tomerchant.xhtml")
	public String toMerchant(Long recordId, ModelMap model, HttpServletRequest request){
		SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
		if (settleBill == null)
			return showJsonError(model,"没有找到相关记录");
		
		if (!CheckBillStatusEnums.READJUST.getStatus().equals(settleBill.getStatus()))
			return showJsonError(model, "结算单当前状态不允许提交到影院审核. code:" + settleBill.getStatus());
		
		User logonUser = getLogonUser(request);
		ChangeEntry ce = new ChangeEntry(settleBill);
		ResultCode resultCode = settlementBillService.toMerchant(settleBill, logonUser);
		if (!resultCode.isSuccess())
			return showJsonError(model, resultCode.getMsg());
		monitorService.saveChangeLog(logonUser.getId(), SettlementBill.class, settleBill.
												getRecordId(), ce.getChangeMap(settleBill));
		
		return showJsonSuccess(model, "成功提交到商户，请等待系统完成");
	}
	
	@RequestMapping("/platform/settlement/settlementDetails.xhtml")
	public String querySettlementDetails (Long recordId, String xls, ModelMap model, HttpServletResponse response){
		/*ResultCode<SettleCycleEnums> setupResult = setupSettlementDetails(recordId, model);
		if (setupResult.getRetval() == SettleCycleEnums.TN){
			return showRedirect("platform/gewaorder/queryPlayItemAggre.xhtml", model);
		}else{
			return showRedirect("platform/checkBill/checkbills.xhtml", model);
		}*/
		if(recordId == null) {
			return showError(model, "参数为空");
		}
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		if(bill == null) {
			return showError(model, "结算单为空！");
		}
		model.put("settle", bill);
		List<CheckBill> cklist = daoService.getObjectListByField(CheckBill.class, "settlementId", recordId);
		model.put("cklist", cklist);
		
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, recordId);
		model.put("extend", extend);
		
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag())) {
			Place p = daoService.getObject(Place.class, bill.getConfigId());
			model.put("p", p);
			return "checkbill/checkbills1.vm";
		} else if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(bill.getTag())) {
			Place p = daoService.getObject(Place.class, bill.getConfigId());
			model.put("p", p);
			return "checkbill/checkbills2.vm";
		} else if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(bill.getTag())) {
			ChannelSettleConfig sc = daoService.getObject(ChannelSettleConfig.class, bill.getRelateId());
			model.put("sc", sc);
			List<Long> placeid = BeanUtil.getBeanPropertyList(cklist, "relateId", true);
			List<String> placerecordids = new ArrayList<String>();
			for(Long id : placeid) {
				placerecordids.add(id + ",TICKET");
			}
			Map<String, Place> placemap = daoService.getObjectMap(Place.class, placerecordids);
			model.put("placemap", placemap);
			if(StringUtils.isNotBlank(xls)) {
				this.download("xls","通道费对账单", response);
				return "checkbill/checkbills3_xls.vm";
			}
			return "checkbill/checkbills3.vm";
		} else {
			return showError(model, "结算单错误！");
		}
		
	}
	
	
	@RequestMapping("/platform/settlement/printSettledetails.xhtml")
	public String printSettledetails (Long recordId, ModelMap model){
		ResultCode<SettleCycleEnums> setupResult = setupSettlementDetails(recordId, model);
		model.put("isPrint", true);      //indicate this is to print
		if (setupResult.getRetval() == SettleCycleEnums.TN){
			return showRedirect("platform/gewaorder/queryPlayItemAggre.xhtml", model);
		}else{
			return showRedirect("platform/checkBill/checkbills.xhtml", model);
		}
	}
	
	
	
	
	@RequestMapping("/platform/settlement/queryAdjustSettlement.xhtml")
	public String queryAdjustSettlement(Integer pageNo, Integer pageSize, ModelMap model,String placeId){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		DetachedCriteria queryCount = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("status", CheckBillStatusEnums.READJUST.getStatus()));
		queryCount.add(Restrictions.eq("status", CheckBillStatusEnums.READJUST.getStatus()));
		if(StringUtils.isNotEmpty(placeId)){
			query.add(Restrictions.eq("configId", placeId));
			queryCount.add(Restrictions.eq("configId", placeId));
		}
		queryCount.setProjection(Projections.rowCount());
		query.addOrder(Order.desc("updateTime"));
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		List<SettlementBill> sbs = null;
		if(count > 0) {
			sbs = daoService.findByCriteria(query, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "/platform/settlement/queryAdjustSettlement.xhtml", true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("placeId", placeId);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		} else {
			return "/settlement/adjustBills.vm";
		}
		
		List<Long> beanPropertyList = BeanUtil.getBeanPropertyList(sbs, "recordId", true);
		List<Adjustment> ads = daoService.getObjectBatch(Adjustment.class, "settleBillId", beanPropertyList);
		Map<Long, List> settleAds = BeanUtil.groupBeanList(ads, "settleBillId");
		model.put("settlead", settleAds);
		
		List<String> placeIds = BeanUtil.getBeanPropertyList(sbs, "configId", true);
		Map<String, Place> pm = daoService.getObjectMap(Place.class, placeIds);
		
		List<Long> settleIds = BeanUtil.getBeanPropertyList(sbs, "recordId", true);
		String idsString  = StringUtils.join(settleIds, ",");
		
		String sql = " select max(record_id) from status_tracker where relate_id in (" + idsString + ") and new_status = 'READJUST' group by relate_id";
		
		List<Long> trackerIds = jdbcTemplate.queryForList(sql, Long.class);
		List<StatusTracker> sts = daoService.getObjectList(StatusTracker.class, trackerIds);
		
		Map<Long, StatusTracker> stm = new HashMap<Long, StatusTracker>();
		
		for (StatusTracker st : sts)
			stm.put(st.getRelateId(), st);
		
		model.put("sbs", sbs);
		model.put("pm", pm);
		model.put("stm", stm);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		return "/settlement/adjustBills.vm";
	}
	
	
	@RequestMapping("/platform/settlement/aggreUnsettled.xhtml")
	public String aggreUnsettled(String cityCode, String brandName, String placeId,Timestamp start, Timestamp end, 
										Integer pageNo, Integer pageSize,  ModelMap model){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 500 : pageSize;
		
		Map<String, String> cityMap = cityInfoHolder.getAllCity();
		model.put("cityMap", cityMap);
		model.put("bm", brandList);
		
		List<SettlementBill> settleBills = querysettlebill(placeId, cityCode, brandName, "", true, start, end, pageSize, pageNo);
		
		if (!CollectionUtils.isNotEmpty(settleBills))
			return "/settlement/unsettledBills.vm";
		
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settleBills, "recordId", true);
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBillSnapshoot.class);
		query.add(Restrictions.in("settlementBillId", settleIds));
		List<SettlementBillSnapshoot> snapshoot = daoService.findByCriteria(query);
		Map<Long, SettlementBillSnapshoot> snapshootingMap = BeanUtil.beanListToMap(snapshoot, "settlementBillId");
		List<String> configIds = BeanUtil.getBeanPropertyList(settleBills, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, configIds);
		model.put("sb", settleBills);
		model.put("sm", snapshootingMap);
		model.put("pm", placeMap);
		
		return "/settlement/unsettledBills.vm";
	}
	
	private ResultCode<SettleCycleEnums> setupSettlementDetails(Long recordId, ModelMap model){
		SettlementBill sb = daoService.getObject(SettlementBill.class, recordId);
		String placeId = sb.getConfigId();
		Place p = daoService.getObject(Place.class, placeId);
		String tag = sb.getTag();
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(tag)){
			SettleConfig settleConfig = daoService.getObject(SettleConfig.class, placeId);
			if (SettleCycleEnums.TN.getType().equals(settleConfig.getSettleCycle())){
				DetachedCriteria query = DetachedCriteria.forClass(CheckBill.class);
				query.add(Restrictions.eq("start", sb.getStartTime()));
				query.add(Restrictions.eq("end", sb.getEndTime()));
				query.add(Restrictions.eq("configId", placeId));
				List<CheckBill> result = daoService.findByCriteria(query);
				CheckBill ck = result.get(0);
				String placeName = daoService.getObject(Place.class, placeId).getName();
				Timestamp start = ck.getStart();
				Timestamp end = ck.getEnd();
				model.put("placeId", placeId);
				model.put("start", start);
				model.put("end", end);
				model.put("placeFirstLetter", placeName);
				model.put("settleId", recordId);
				return ResultCode.getSuccessReturn(SettleCycleEnums.TN);
			}else{
				String status = sb.getStatus();
				Timestamp start = sb.getStartTime();
				Timestamp end = sb.getEndTime();
				model.put("settleId", recordId);
				model.put("placeId", placeId);
				model.put("status", status);
				model.put("start", start);
				model.put("end", end);
				model.put("placeFirstLetter", p.getName());
				return ResultCode.SUCCESS;
			}
		}else{
			model.put("settleId", recordId);
			model.put("placeId", placeId);
			model.put("status", sb.getStatus());
			model.put("start", sb.getStartTime());
			model.put("end", sb.getEndTime());
			return ResultCode.SUCCESS;
		}
			
	}
	
	/**
	 * 
	 * @param placeId
	 * @param cityCode
	 * @param brandName
	 * @param status
	 * @param isEqual
	 * @param start
	 * @param end
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	private List<SettlementBill> querysettlebill(String placeId, String cityCode, String brandName, String status, boolean isEqual, Timestamp start, Timestamp end, int pageSize, int pageNo){
		List<Object> params = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(" select sb from SettlementBill sb, Place p where sb.configId = p.recordId and status != 'INVALID'");
		if (StringUtils.isNotBlank(placeId)){
			hql.append(" and sb.configId = ? ");
			params.add(placeId);
		}
		
		if (StringUtils.isNotBlank(cityCode)){
			hql.append(" and p.cityCode = ? ");
			params.add(cityCode);
		}
		if (StringUtils.isNotBlank(brandName)){
			hql.append(" and p.brandName = ? ");
			params.add(brandName);
		}
		if (start != null){
			hql.append(" and sb.startTime >= ? ");
			params.add(start);
		}
		if (end != null){
			hql.append(" and sb.endTime <= ? ");
			params.add(end);
		}
		if (isEqual){
			if (StringUtils.isBlank(status)){
				params.add(CheckBillStatusEnums.NEW.getStatus());
				hql.append(" and sb.status = ?");
			}
		}else{
			if (StringUtils.isBlank(status)){
				params.add(CheckBillStatusEnums.NEW.getStatus());
				hql.append(" and sb.status != ?");
			}else{
				params.add(status);
				hql.append(" and sb.status = ?");
			}
		}
		
		hql.append(" order by sb.configId, sb.startTime desc");
		return daoService.queryByRowsRange(hql.toString(), pageSize * pageNo, pageSize, params.toArray());
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String sql = " SELECT BRAND_NAME FROM PLACE GROUP BY BRAND_NAME";
		brandList = jdbcTemplate.queryForList(sql, String.class);
	}
	
	
	/**
	 * 手动请款
	 * @param recordIds
	 * @param isOnline
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/settlement/reqMoney.xhtml")
	public String reqMoney(String recordIds, ModelMap model, HttpServletRequest request){
		if(StringUtils.isEmpty(recordIds)){
			return showJsonError(model, "参数错误");
		}
		String[] settleIds = recordIds.split(",");
		for(String settleId : settleIds){
			Long recordId = Long.valueOf(settleId);
			SettlementBill settleBill = daoService.getObject(SettlementBill.class, recordId);
			if (settleBill == null){
				return showJsonError(model, "结算单号：" + settleId + "结算单号不存在！");
			}
			
			if (!CheckBillStatusEnums.WAITINGPAY.getStatus().equals(settleBill.getStatus()) && 
					!CheckBillStatusEnums.SETTLED.getStatus().equals(settleBill.getStatus())){
				return showJsonError(model, "结算单号：" + settleId + "结算状态不正确！");
			}
			
			String tag = settleBill.getTag();
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)){
				ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, settleBill.getRelateId());
				if(csc == null || StringUtils.isEmpty(csc.getVendorCode())){
					return showJsonError(model, "结算单号：" + settleId + "供应商或通道配置不存在！");
				}
			}else{
				SettleConfig sc = daoService.getObject(SettleConfig.class, settleBill.getConfigId());
				if (StringUtils.isBlank(sc.getVenderNo())){
					return showJsonError(model, "结算单号：" + settleId + "供应商不存在！");
				}
			}
			
			if (settleBill.getOrderTotalAmount().intValue() <= 0){
				return showJsonError(model, "结算单号：" + settleId + "结算金额不能为0");
			}
			
			User user = getLogonUser(request);
			Long uid = user.getId();
			ChangeEntry ce = new ChangeEntry(settleBill);
			reqMoneyBillService.increaseReqMoneyBill(settleBill,user);
			monitorService.saveChangeLog(uid, SettlementBill.class, settleBill.getRecordId(), ce.getChangeMap(settleBill));
			
		}
		return showJsonSuccess(model, "成功提交请款");
	}
	
	/**
	 * 跳转到请款单查询
	 * @param recordId
	 * @param placeId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/settlement/gotoQueryReqMoney.xhtml")
	public String gotoQueryReqMoney(String recordId, ModelMap model){
		if(StringUtils.isEmpty(recordId)){
			return showJsonError(model, "参数错误");
		}
		SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		String queryReqMoneyUrl = "platform/reqmoneybill/reqmoneybill.xhtml";
		String params = "?startTime=" + settle.getStartTime() + 
						"&endTime=" + DateUtil.getCurFullTimestamp() + 
						"&placeId=" + settle.getRelateId() +
						"&settleId=" + recordId;
		return showRedirect(queryReqMoneyUrl + params, model);
	}
	
	/**
	 * 跳转到请款单查询
	 * @param recordId
	 * @param placeId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/settlement/printReqMoney.xhtml")
	public String printReqMoney(String recordId, ModelMap model, HttpServletRequest request){
		if(StringUtils.isEmpty(recordId)){
			return showJsonError(model, "参数错误");
		}
		SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		User user = getLogonUser(request);
		List<ReqMoneyBill> reqBills = reqMoneyBillService.qryMoneyBill(
				settle.getStartTime(),
				DateUtil.getCurFullTimestamp(),
				"",
				String.valueOf(settle.getRelateId()),
				user.getId(),
				0,
				1,
				recordId);
		if(CollectionUtils.isEmpty(reqBills)) {
			String errorMsgs = "结算单号" + recordId + "没有查出对应的请款单，不能打印预览！";
			model.put("errorMsgs", errorMsgs);
			return "showResult.vm";
		}
		return showRedirect("platform/reqmoneybill/showReqMoneybill.xhtml?recordId=" + reqBills.get(0).getRecordId(), model);
	}
	
	/**
	 * 结算单详情
	 * @param recordId
	 * @param placeId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/settlement/settleMore.xhtml")
	public String settleMore(String recordId, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(recordId)) {
			return "/settlement/settleDetails.vm";
		}
		SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		if(settle == null) {
			return "/settlement/settleDetails.vm";
		}
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, Long.valueOf(recordId));
		if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(settle.getTag())) {
			ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, settle.getRelateId());
			model.put("csc", csc);
		} else {
			Place place = daoService.getObject(Place.class, settle.getConfigId());
			model.put("place", place);
		}
		model.put("settle", settle);
		model.put("extend", extend);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		return "/settlement/settleDetails.vm";
	}
	
	
}
