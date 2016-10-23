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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.MegerPayRecord;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.SettlementBillService;
import com.gewara.service.SysDataService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.web.util.PageUtil;

/**
 * 
 * @author user
 *
 */
@Controller
public class MegerPayController extends AnnotationController {

	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	/**
	 * 查询合并付款
	 * @param payVenderNo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/mergepay/queryMergePay.xhtml")
	public String queryMergePay(String payVenderNo, Timestamp startTime, Timestamp endTime, String isXls, String status,
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		model.put("payVenderNo", payVenderNo);
		model.put("startTime", startTime);
		model.put("endTime", endTime);
		model.put("megermap", sysDataService.getSysDataMap(SettleConstant.MEGERPAYVENDER));
		if(StringUtils.isBlank(payVenderNo) || startTime == null || endTime == null) {
			return "/settlement/mergePay.vm";
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(SettleConfig.class);
		query.add(Restrictions.eq("payVenderNo", payVenderNo));
		query.add(Restrictions.ne("status", "D"));
		List<SettleConfig> scs = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(scs)) {
			return "/settlement/mergePay.vm";
		}
		Map<String, SettleConfig> scm = BeanUtil.beanListToMap(scs, "recordId");
		
		List<String> recordIds = BeanUtil.getBeanPropertyList(scs, "recordId", true);
		DetachedCriteria qry = DetachedCriteria.forClass(SettlementBill.class);
		qry.add(Restrictions.ge("startTime", startTime));
		qry.add(Restrictions.le("endTime", endTime));
		qry.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		if(StringUtils.isNotBlank(status)) {
			if(!"ALL".equals(status)) {
				qry.add(Restrictions.eq("status", status));
			}
		} else {
			status = CheckBillStatusEnums.WAITINGPAY.getStatus();
			qry.add(Restrictions.eq("status", status));
		}
		qry.add(Restrictions.in("configId", recordIds));
		if("1582".equals(payVenderNo)) { //万达的只要非活动场的
			qry.add(Restrictions.or(Restrictions.ne("special", SettleConstant.WANDA_ACTIVITY), Restrictions.isNull("special")));
		}
		qry.add(Restrictions.eq("playType", SettleConstant.ZL));
		qry.addOrder(Order.asc("recordId"));
		List<SettlementBill> settleBills = daoService.findByCriteria(qry);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, recordIds);
		model.put("sb", settleBills);
		model.put("pm", placeMap);
		model.put("scm", scm);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		model.put("status", status);
		if(StringUtils.isNotBlank(isXls)){
			String attachFileName = scs.get(0).getPayVenderName() + "结算单";
			this.download("xls", attachFileName , response);
			return "/downloadtemplate/settlementbills.vm";
		}
		return "/settlement/mergePay.vm";
	}
	
	/**
	 * 合并付款
	 * @param payVenderNo
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/mergepay/mergePay.xhtml")
	public String mergePay(String payVenderNo, Timestamp startTime, Timestamp endTime, String recordIds, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(payVenderNo) || startTime == null || endTime == null) {
			return showJsonError(model, "参数错误");
		}
		
		String[] rids = recordIds.split(",");
		List<Long> idList = new ArrayList<Long>();
		for(String id : rids) {
			idList.add(Long.valueOf(id));
		}
		
		DetachedCriteria qry = DetachedCriteria.forClass(SettlementBill.class);
		qry.add(Restrictions.in("recordId", idList));
		qry.addOrder(Order.asc("recordId"));
		List<SettlementBill> settleBills = daoService.findByCriteria(qry);
		
		if(CollectionUtils.isEmpty(settleBills)) {
			return showJsonError(model, "无结算单据");
		}
		
		User user = getLogonUser(request);
		Long uid = user.getId();
		ResultCode result = null;
		double totalAmount = 0d;
		long minSettleId = settleBills.get(0).getRecordId();
		StringBuilder relateSettltIds = new StringBuilder("");
		for(SettlementBill sb : settleBills) {
			if(!CheckBillStatusEnums.WAITINGPAY.getStatus().equals(sb.getStatus())) {
				return showJsonError(model, "单号" + sb.getRecordId() + "状态不是等待付款");
			}
			if(!SettleConstant.ZL.equals(sb.getPlayType())) {
				return showJsonError(model, "单号" + sb.getRecordId() + "场次类型不是直连");
			}
			ChangeEntry ce = new ChangeEntry(sb);
			result = settlementBillService.paySettle(sb, user, "meger");
			if (!result.isSuccess()) {
				return showJsonError(model,result.getMsg());
			}
			totalAmount += sb.getOrderTotalAmount();
			relateSettltIds.append(sb.getRecordId() + ",");
			monitorService.saveChangeLog(uid, SettlementBill.class, sb.getRecordId(), ce.getChangeMap(sb));
		}
		
		MegerPayRecord mpr = new MegerPayRecord();
		mpr.setPayVernderNo(payVenderNo);
		mpr.setQueryStartTime(startTime);
		mpr.setQueryEndTime(endTime);
		mpr.setPayTime(DateUtil.getCurFullTimestamp());
		mpr.setRelateSettltIds(relateSettltIds.toString());
		mpr.setTotalAmount(totalAmount);
		mpr.setOptUser(getLogonUser(request).getUsername());
		daoService.saveObject(mpr);
		
		DownloadRecorder dlRecorder = new DownloadRecorder(mpr.getMinSettltId(), BillTypeEnums.PAYBILL);
		dlRecorder.setVendorNo(payVenderNo);
		dlRecorder.setNativeMoney("" + totalAmount);
		dlRecorder.setSpecial(SettleConstant.MEGER);
		dlRecorder.setOptUser(getLogonUser(request).getUsername());
		daoService.saveObject(dlRecorder);
		
		return showJsonSuccess(model, "成功提交付款");
	}
	
	/**
	 * 查询已提交的合并付款汇总
	 * @return
	 */
	@RequestMapping("/platform/mergepay/querySubmitPay.xhtml")
	public String querySubmitPay(String payVenderNo, String optUser, String flag, Integer pageNo, String relateSettltIds, Integer pageSize, ModelMap model, HttpServletRequest request) {
		model.put("megermap", sysDataService.getSysDataMap(SettleConstant.MEGERPAYVENDER));
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		
		if(StringUtils.isEmpty(flag)) {
			optUser = getLogonUser(request).getUsername();
		}
		model.put("payVenderNo", payVenderNo);
		model.put("optUser", optUser);
		
		DetachedCriteria query = DetachedCriteria.forClass(MegerPayRecord.class);
		if (StringUtils.isNotEmpty(payVenderNo)){
			query.add(Restrictions.eq("payVernderNo", payVenderNo));
		}
		if(StringUtils.isNotEmpty(optUser)) {
			query.add(Restrictions.eq("optUser", optUser));
		}
		if(StringUtils.isNotEmpty(optUser)) {
			query.add(Restrictions.like("relateSettltIds", relateSettltIds, MatchMode.ANYWHERE));
		}
		query.setProjection(Projections.rowCount());
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0).toString());
		
		if(count == 0) {
			return "/settlement/submitMegerPay.vm";
		}
		
		DetachedCriteria qry = DetachedCriteria.forClass(MegerPayRecord.class);
		if (StringUtils.isNotEmpty(payVenderNo)){
			qry.add(Restrictions.eq("payVernderNo", payVenderNo));
		}
		if(StringUtils.isNotEmpty(optUser)) {
			qry.add(Restrictions.eq("optUser", optUser));
		}
		if(StringUtils.isNotEmpty(optUser)) {
			qry.add(Restrictions.like("relateSettltIds", relateSettltIds, MatchMode.ANYWHERE));
		}
		qry.addOrder(Order.desc("payTime"));
		
		List<MegerPayRecord> mprList = daoService.findByCriteria(qry, pageSize * pageNo, pageSize);
		
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo,
				config.getBasePath() + "platform/mergepay/querySubmitPay.xhtml",
				true, true);
		
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("payVenderNo", payVenderNo);
		m.put("optUser", optUser);
		m.put("relateSettltIds", relateSettltIds);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("mprList", mprList);
		
		
		return "/settlement/submitMegerPay.vm";
	}
}
