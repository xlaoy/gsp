package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.activemq.broker.util.DefaultAuditLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.service.ReqMoneyBillService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.web.util.PageUtil;

/**
 * 
 * @ClassName: ReqMoneyBillController  
 * @Description:请款单controller  
 * @author yujun.su@gewara.com
 * @date 2015-8-13 下午2:32:43
 */
@Controller
public class ReqMoneyBillController extends AnnotationController implements InitializingBean{
	@Autowired
	@Qualifier("reqMoneyBillService")
	private ReqMoneyBillService reqMoneyBillService;
	
	@RequestMapping("/platform/reqmoneybill/reqmoneybill.xhtml")
	public String qryReqMoneyBill(HttpServletRequest request,HttpServletResponse response, ModelMap model,Timestamp startTime,Timestamp endTime,String vendorName,String placeId,String isXls,Integer pageNo,Integer pageSize,String settleId){
		String url = "reqmoneybill/reqMoneyBill.vm";
		pageNo = null == pageNo ? 0 : pageNo;
		pageSize = null == pageSize ? 10 : pageSize;
		if(StringUtils.isNotEmpty(isXls)){
			pageSize = 10000 ;
		}
		User user = getLogonUser(request);
		List<ReqMoneyBill> reqBills = reqMoneyBillService.qryMoneyBill(startTime,endTime,vendorName,placeId,user.getId(),pageNo,pageSize,settleId);
		Integer count = reqMoneyBillService.countMoneyBill(startTime,endTime,vendorName,placeId,user.getId(),settleId);
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo,config.getBasePath() + "platform/reqmoneybill/reqmoneybill.xhtml", true, true);
		model.put("reqBills", reqBills);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("startTime", startTime);
		m.put("endTime", endTime);
		m.put("vendorName", vendorName);
		m.put("placeId", placeId);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		if(StringUtils.isNotEmpty(isXls)){
			setupSettleCycle(model, reqBills);
			this.download("xls",DateUtil.formatDate(startTime) + "-" + DateUtil.formatDate(endTime) + "请款单", response);
			return "/downloadtemplate/reqMoneyBill.vm";
		}
		return url;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: setupSettleCycle  
	 * @Description:
	 * @param @param model
	 * @param @param reqBills
	 * @return void
	 * @throws  
	 */
	private void setupSettleCycle(ModelMap model, List<ReqMoneyBill> reqBills) {
		Map<String,Timestamp> startMap = new HashMap<String,Timestamp>();
		Map<String,Timestamp> endMap = new HashMap<String,Timestamp>();
		List<Long> relateBillIdList = new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(reqBills)){
			for(ReqMoneyBill rmb : reqBills){
				String relateBillId = rmb.getRelateSettleId();
				String[] relateBillIdArray = relateBillId.split(",");
				for(String billId : relateBillIdArray){
					relateBillIdList.add(Long.valueOf(billId));
				}
				List<SettlementBill> settleBills = daoService.getObjectList(SettlementBill.class, relateBillIdList);
				Map<String,List<SettlementBill>> tagMap = BeanUtil.groupBeanList(settleBills, "tag");
				for(Entry<String, List<SettlementBill>> entry : tagMap.entrySet()){
					List<SettlementBill> subList = entry.getValue();
					List<Timestamp> startTimeList = BeanUtil.getBeanPropertyList(subList, "startTime", true);
					List<Timestamp> endTimeList = BeanUtil.getBeanPropertyList(subList, "endTime", true);
					Collections.sort(startTimeList);
					Collections.sort(endTimeList);
					startMap.put(rmb.getRecordId() + entry.getKey(), startTimeList.get(0));
					endMap.put(rmb.getRecordId() + entry.getKey(), endTimeList.get(endTimeList.size() - 1));
				}
			}
		}
		model.put("startMap", startMap);
		model.put("endMap", endMap);
	}
	
	@RequestMapping("/platform/reqmoneybill/showReqMoneybill.xhtml")
	public String showReqMoneybill(ModelMap model,Long recordId,HttpServletRequest request){
		String url = "/reqmoneybill/showReqMoneybill.vm";
		if(null == recordId){
			return alertMessage(model, "参数异常！");
		}
		ReqMoneyBill bill = daoService.getObject(ReqMoneyBill.class, recordId);
		if(null == bill){
			return alertMessage(model,"请款单异常！");
		}
		User user = getLogonUser(request);
		
		String reqDateStr = DateUtil.format(DateUtil.getCurDate(), "yyyy年MM月dd日");
		String chsFmtMoney = MiscUtil.convertMoneyChineseFmt(bill.getTotalAmount());
		String receiveDept = "";
		if(SettleConstant.REQ_BILL_PLACE.equals(bill.getVendorType())){
			SettleConfig sc = daoService.getObject(SettleConfig.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, Math.abs(bill.getVendorId())));
			if(sc == null) {
				return alertMessage(model,"请款单异常！sc为null");
			}
			receiveDept = sc.getVenderName();
		}else{
			ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, bill.getVendorId());
			if(csc == null) {
				return alertMessage(model,"请款单异常！scs为null");
			}
			receiveDept = csc.getVendorName();
		}
		
		String relateSettleIds = bill.getRelateSettleId();
		String[] settleIds = StringUtils.split(relateSettleIds, ",");
		List<Long> sids = new ArrayList<Long>();
		for(String s : settleIds){
			sids.add(Long.valueOf(s));
		}
		//List<SettlementBill> settleBills = daoService.getObjectList(SettlementBill.class,sids);
		DetachedCriteria q = DetachedCriteria.forClass(SettlementBill.class);
		q.add(Restrictions.in("recordId", sids));
		q.addOrder(Order.desc("startTime"));
		List<SettlementBill> settleBills = daoService.findByCriteria(q);
		Map<Long, SettlementBillExtend> extendmap = daoService.getObjectMap(SettlementBillExtend.class, sids);
		model.put("settleBills", settleBills);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		model.put("reqDateStr", reqDateStr);
		model.put("bill", bill);
		model.put("user",user);
		model.put("chsFmtMoney", chsFmtMoney);
		model.put("receiveDept", receiveDept);
		model.put("extendmap", extendmap);
		return url;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
	}
	
	/**
	 * 删除请款单
	 * @return
	 */
	@RequestMapping("/platform/reqmoneybill/deleteReqMoneybill.xhtml")
	public String deleteReqMoneybill(String recordId, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(recordId)){
			return showJsonError(model, "参数错误");
		}
		User user = getLogonUser(request);
		reqMoneyBillService.deleteReqMoneybill(recordId, user);
		return showJsonSuccess(model, "删除成功！");
	}
}
