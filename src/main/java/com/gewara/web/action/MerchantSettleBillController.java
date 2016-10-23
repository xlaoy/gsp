package com.gewara.web.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.SettleCycleEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.InvoiceConfig;
import com.gewara.model.gsp.InvoiceSettleRelate;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.SettlementBillService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.util.PageUtil;

@Controller
public class MerchantSettleBillController extends AnnotationController {

	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;
	
	@RequestMapping("/merchant/settlement/settlementbills.xhtml")
	public String merchantSettlementbills(String proviceCode,  String placeId, Timestamp start, Timestamp end, String status,String billType, Integer pageNo, Integer pageSize, ModelMap model, String isXls, HttpServletRequest request, HttpServletResponse response){
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> placeIds = new ArrayList<String>();
		if (StringUtils.isNotBlank(placeId)) {
			placeIds.add(placeId);
		} else {
			placeIds = getAllowedPlaceIds(request);
		}
		if (StringUtils.isNotBlank(proviceCode)) {
			placeIds = cityInfoHolder.filteredPlaceIdForProvice(placeIds, proviceCode);
		}
		
		/******发票提示******/
		HttpSession session = request.getSession();
		String skiptax = (String)session.getAttribute("skiptax");
		if(StringUtils.isBlank(skiptax)) {
			skiptax = "N";
		}
		if("N".equals(skiptax)) {
			List<InvoiceConfig> invoicelist = daoService.getObjectBatch(InvoiceConfig.class, "placeid", placeIds);
			if(invoicelist.size() == placeIds.size()) {
				skiptax = "Y";
				request.getSession().setAttribute("skiptax", skiptax);
			}
		}
		model.put("skiptax", skiptax);
		/******发票提示******/
		
		if (CollectionUtils.isEmpty(placeIds)) {
			return "/settlement/merchantSettleBills.vm";
		}
		
		/*********************自动对账加入逻辑****************************/
		List<SettleConfig> configs = daoService.getObjectList(SettleConfig.class, placeIds);
		Map<String,SettleConfig> configMap = BeanUtil.beanListToMap(configs, "recordId");
		
		List<Place> places = daoService.getObjectList(Place.class, placeIds);
		Map<String,Place> placeMap = BeanUtil.beanListToMap(places, "recordId");
		
		model.put("configMap", configMap);
		model.put("placeMap", placeMap);
		model.put("openTypeList", SettleConstant.OPEN_TYPES);
		/*******************************************************/
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		model.put("noTicketStatusMap", SettleConstant.NOTICKETSETTLEBILLSTATUSMAP);
		
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
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
		int totalNumber = settlementBillService.countingSettleBills(placeIds, start, end, status, isDefaulfStatus,billType, null, null);
		if (totalNumber <= 0) {
			return "/settlement/merchantSettleBills.vm";
		}
		
		List<SettlementBill>  settleBills = settlementBillService.querySettleMents(placeIds, start, end, status, "endTime",  pageNo, pageSize, isDefaulfStatus,billType,null, null);
		setupSettleBills(settleBills, model);
		
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo, config.getBasePath() + "merchant/settlement/settlementbills.xhtml", true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		m.put("proviceCode", proviceCode);
		m.put("start", start);
		m.put("end", end);
		m.put("status", status);
		m.put("billType", billType);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "结算单" , response);
			return "/downloadtemplate/settlementbills.vm";
		}
		return "/settlement/merchantSettleBills.vm";
	}
	
	private void setupSettleBills(List<SettlementBill>  settleBills, ModelMap model){
		List<String> recordIds = BeanUtil.getBeanPropertyList(settleBills, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, recordIds);
		Map<String, SettleConfig> scm = daoService.getObjectMap(SettleConfig.class, recordIds);
		Map<Long, SettlementBillExtend>  extendBillMap = new HashMap<Long, SettlementBillExtend>();
		Map<Long, String>  invmap = new HashMap<Long, String>();
		for(SettlementBill bill : settleBills){
			if(!SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag())){
				if(!StringUtils.equals(bill.getStatus(), CheckBillStatusEnums.WAITINGPAY.getStatus())
						&& !StringUtils.equals(bill.getStatus(), CheckBillStatusEnums.SETTLED.getStatus())){
					bill.setOrderTotalAmount(0.0);
					bill.setOrderTotalNumber(0.0);
				}
				bill.setAdjustTotalAmount(0.0);
				bill.setAdjustTotalNumber(0.0);
				
				if(bill.getApplySettleAmount() == null || bill.getApplySettleAmount() == 0){
					bill.setApplySettleAmount(0.0);
				}
				if(bill.getApplySettleNumber() == null || bill.getApplySettleNumber() == 0){
					bill.setApplySettleNumber(0.0);
				}
			}
			if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag()) ||
					(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(bill.getTag()) && SettleConstant.BC.equals(bill.getPlayType()))){
				//查询影票扩展
				SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
				if(extend != null) {
					if(extend.getBillingamount() > bill.getOrderTotalAmount()) {
						extend.setBillingamount(0.0);
					}
					extendBillMap.put(bill.getRecordId(), extend);
				}
			}
			List<InvoiceSettleRelate> isrlist = daoService.getObjectListByField(InvoiceSettleRelate.class, "settleid", bill.getRecordId());
			if(CollectionUtils.isNotEmpty(isrlist)) {
				invmap.put(bill.getRecordId(), "Y");
			} else {
				invmap.put(bill.getRecordId(), "N");
			}
		}
		model.put("sb", settleBills);
		model.put("pm", placeMap);
		model.put("scm", scm);
		model.put("baseSettle", true);
		model.put("extendBillMap", extendBillMap);
		model.put("invmap", invmap);
	}
	
	@RequestMapping("/merchant/settlement/confirmSettleBill.xhtml")
	public String confirmSettleBill(String recordIds, ModelMap model, HttpServletRequest request){
		if (StringUtils.isBlank(recordIds))
			return showJsonError(model, "");
		
		String[] ids = StringUtils.split(recordIds, ",");
		List<Long> idList = new ArrayList<Long>();
		
		for (String id : ids) idList.add(Long.valueOf(id));
		
		List<SettlementBill> bills = daoService.getObjectList(SettlementBill.class, idList);
		Set<String> placeIds = BeanUtil.groupBeanList(bills, "configId").keySet();
		for(String placeId : placeIds){
			if (!isPlaceAllowed(placeId, request))
				return showJsonError(model, "非法操作：" + placeId);
		}
		settlementBillService.merchantBatchConfirn(bills, getMerchantUserId(request), getMerchantUserName(request));
		
		return showJsonSuccess(model, "已确认， 请等待打款..");
	}
	
	
	@RequestMapping("/merchant/settlement/queryPayBills.xhtml")
	public String merchantQueryPayBills(String placeId, Integer pageNo, Integer pageSize, ModelMap model, HttpServletRequest request){
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("placeId", placeId);
		model.put("place", daoService.getObject(Place.class, placeId));
		if (StringUtils.isBlank(placeId))
			return "/settlement/merchantPayBills.vm";
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		int totalNumber = settlementBillService.countingPayBills(placeId);
		model.put("placeId", placeId);
		if (totalNumber <= 0)
			return "/settlement/merchantPayBills.vm";
		
		List<SettlementBill>  settleBills = settlementBillService.queryPayBills(placeId, pageNo, pageSize);
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settleBills, "recordId", true);
		
		Map<Long, StatusTracker> merchantConfirm = settlementBillService.queryMerchantConfirm(settleIds);
		Map<Long, StatusTracker> gewaPayBill = settlementBillService.queryGewaPayBill(settleIds);
		
		model.put("sb", settleBills);
		model.put("mc", merchantConfirm);
		model.put("gp", gewaPayBill);
		return "/settlement/merchantPayBills.vm";
	}
	
	@RequestMapping("/merchant/settlement/saveAdjust.xhtml")
	public String saveAdjust(ModelMap model, HttpServletRequest request) throws IOException{
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
		String recordStr = multipartRequest.getParameter("recordId");
		Long recordId = StringUtils.isNotBlank(recordStr) ? Long.valueOf(recordStr) : null;
		String placeId =  multipartRequest.getParameter("aplaceId");
		
		String comments = multipartRequest.getParameter("comments");
		
		if (StringUtils.isBlank(placeId) || recordId == null){
			return alertMessage(model, "非法操作.");
		}
		
		if (!isPlaceAllowed(placeId, multipartRequest)){
			return alertMessage(model, "非法操作."); 
		}
		
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		if (bill == null)
			return alertMessage(model,"结算单参数异常：" + recordId);
		
		if (!bill.getConfigId().equals(placeId))
			return alertMessage(model,"非法操作：" + placeId);
		
		if (!CheckBillStatusEnums.GEWACONFIRMED.getStatus().equals(bill.getStatus()))
			return alertMessage(model,"结算单当前状态不允许提交申请调整。code：" + bill.getStatus());
		
		String attachePath = "";
		MultipartFile file = multipartRequest.getFile("file");
		dbLogger.warn(recordStr + "::" + recordId + "::" + placeId + "::" + comments + file);
		
		if (file != null && file.getSize() != 0){
			ResultCode uploadR = saveToRemote(file.getBytes(), file.getOriginalFilename(), request);
			if (!uploadR.isSuccess())
				return alertMessage(model,uploadR.getMsg());
			attachePath = (String) uploadR.getRetval();
		}
		
		Long mid = getMerchantUserId(request);
		String mname = getMerchantUserName(request);
		ChangeEntry ce = new ChangeEntry(bill);
		ResultCode result = settlementBillService.saveAdjustment(bill, placeId, comments, attachePath,mid, mname,"MERCHANT");
//		if(StringUtils.isNotEmpty(attachePath)){
//			attachePath = config.getPageMap().get("fileURL") + attachePath ;
//		}
//		adjustmentService.saveAdjust("SETTLEBILL", recordId.toString(), adjustNumber, adjustAmount, "", comments, mname, attachePath,SettleConstant.ADJUST_SOURCE_MERCHANT);
		if (!result.isSuccess())
			return alertMessage(model, result.getMsg());
		
		monitorService.saveChangeLog(mid, SettlementBill.class, bill.
				getRecordId(), ce.getChangeMap(bill));
		
		return alertMessageSuccess(model,"已提交， 请等待反馈..");
	}
	
	public ResultCode saveToRemote(byte[] fileBuff, String fileName, HttpServletRequest request){
		String tempUrl = config.getString("uploadTempURL");
		String remoteUrl = config.getString("uploadComit");
		Map<String, String> params = new HashMap<String, String>();
		params.put("callbackUrl", "no call back");
		
		HttpResult result = HttpUtils.uploadFile(tempUrl, params, fileBuff, "noinput", fileName);
		Map tempResult = JsonUtils.readJsonToMap(result.getResponse());
		String successFile = (String) tempResult.get("successFile");
		if (StringUtils.isBlank(successFile)){
			dbLogger.error("upload file failed:" + result.getResponse());
			return ResultCode.getFailure("上传到临时文件时发生错误");
		}
			
		
		Map<String, String> commitParams = new HashMap<String, String>();
		Long mid = getMerchantUserId(request);
		commitParams.put("userid", mid.toString());
		commitParams.put("systemid", "GSP");
		String dateStr = DateUtil.format(new Date(), "yyyyMM");
		String remoteFold = "/upload/gsp/" + dateStr + "/";
		commitParams.put("path", remoteFold);
		commitParams.put("files", successFile);
		commitParams.put("check", StringUtil.md5(successFile + remoteFold + "GSP" + mid + "GewaUploadFile"));
		HttpResult r = HttpUtils.postUrlAsString(remoteUrl, commitParams);
		if (!"SUCCESS".equals(r.getResponse())){
			dbLogger.error("upload file failed:" + result.getResponse());
			return ResultCode.getFailure(r.getResponse());
		}
			
		return ResultCode.getSuccessReturn(remoteFold + successFile);
	}
	
	@RequestMapping("/merchant/settlement/saveApply.xhtml")
	public String saveApply(ModelMap model, HttpServletRequest request){
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
		String recordStr = multipartRequest.getParameter("recordId1");
		Long recordId = StringUtils.isNotBlank(recordStr) ? Long.valueOf(recordStr) : null;
		String placeId =  multipartRequest.getParameter("aplaceId1");
		if (StringUtils.isBlank(placeId) || recordId == null){
			return alertMessage(model, "非法操作.");
		}
		
		if (!isPlaceAllowed(placeId, multipartRequest)){
			return alertMessage(model, "非法操作."); 
		}
		
		String applyNumberStr = multipartRequest.getParameter("applyNumber");
		Double applyNumber = 0.0;
		if(StringUtils.isNotEmpty(applyNumberStr)){
			if(!applyNumberStr.matches("^-?\\d+$")){
				return alertMessage(model, "申请结算数量只能是整数."); 
			}
			applyNumber = Double.valueOf(multipartRequest.getParameter("applyNumber"));
		}
		
		if(StringUtils.isEmpty(multipartRequest.getParameter("applyAmount"))){
			return alertMessage(model, "申请结算金额不能为空."); 
		}
		
		String applyAmountStr = multipartRequest.getParameter("applyAmount");
//		if(!applyAmountStr.matches("^-?\\d+$")){
//			return alertMessage(model, "申请结算金额只能是整数."); 
//		}
		Double applyAmount = Double.valueOf(applyAmountStr);
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		if (bill == null)
			return alertMessage(model,"结算单参数异常：" + recordId);
		
		if (!bill.getConfigId().equals(placeId))
			return alertMessage(model,"非法操作：" + placeId);
		
		Long mid = getMerchantUserId(request);
		String mname = getMerchantUserName(request);
		ChangeEntry ce = new ChangeEntry(bill);
		//更新结算单数据
		ResultCode result = settlementBillService.saveApply(recordId,applyNumber,applyAmount,mname);
		if (!result.isSuccess())
			return alertMessage(model, result.getMsg());
		
		monitorService.saveChangeLog(mid, SettlementBill.class, bill.
				getRecordId(), ce.getChangeMap(bill));
		
		return alertMessageSuccess(model,"已提交， 请等待反馈..");
	}
	
	
	@RequestMapping("/merchant/settlement/printSettledetails.xhtml")
	public String merchantPrintSettledetails (Long recordId, ModelMap model){
		ResultCode<SettleCycleEnums> setupResult = setupSettlementDetails(recordId, model);
		model.put("isPrint", true);      //indicate this is to print
		if (setupResult.getRetval() == SettleCycleEnums.TN){
			return showRedirect("merchant/gewaorder/queryPlayItemAggre.xhtml", model);
		}else{
			return showRedirect("merchant/checkBill/checkbills.xhtml", model);
		}
	}
	
	
	@RequestMapping("/merchant/settlement/settlementDetails.xhtml")
	public String merchantQuerySettlementDetails(Long recordId, String placeId, ModelMap model){
		ResultCode<SettleCycleEnums> setupResult = setupSettlementDetails(recordId, model);
		model.put("placeId", placeId);   //over write placeId
		if (setupResult.getRetval() == SettleCycleEnums.TN){
			return showRedirect("merchant/gewaorder/queryPlayItemAggre.xhtml", model);
		}else{
			return showRedirect("merchant/checkBill/checkbills.xhtml", model);
		}
		
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
	 * 结算单详情
	 * @param recordId
	 * @param placeId
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/settlement/settleMore.xhtml")
	public String settleMore(String recordId, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(recordId)) {
			return "/invoice/menchantSettleDetails.vm";
		}
		SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		if(settle == null) {
			return "/invoice/menchantSettleDetails.vm";
		}
		SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, Long.valueOf(recordId));
		if(extend != null) {
			if(extend.getBillingamount() >= settle.getOrderTotalAmount()) {
				extend.setBillingamount(0.0);
			}
			model.put("extend", extend);
		}
		if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(settle.getTag())) {
			ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, settle.getRelateId());
			model.put("csc", csc);
		} else {
			Place place = daoService.getObject(Place.class, settle.getConfigId());
			model.put("place", place);
		}
		model.put("settle", settle);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		return "/invoice/menchantSettleDetails.vm";
	}
	
	/**
	 * 结算单详情
	 * @param recordId
	 * @param placeId
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/settlement/baochangList.xhtml")
	public String baochangList(Long recordId, ModelMap model, HttpServletRequest request) {
		if(recordId == null) {
			return "/settlement/merchantBoachangList.vm";
		}
		SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		if(settle == null) {
			return "/settlement/merchantBoachangList.vm";
		}
		List<BaoChang> bclist = new ArrayList<BaoChang>();
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(settle.getTag())) {
			bclist = daoService.getObjectListByField(BaoChang.class, "settleId", recordId);
		}
		if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(settle.getTag())) {
			bclist = daoService.getObjectListByField(BaoChang.class, "goodsSettleId", recordId);
		}
		List<String> placeIds = BeanUtil.getBeanPropertyList(bclist, "placeId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, placeIds);
		model.put("bclist", bclist);
		model.put("recordId", recordId);
		model.put("placeMap", placeMap);
		return "/settlement/merchantBoachangList.vm";
	}
	
}
