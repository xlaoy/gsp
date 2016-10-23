/** 
 */
package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.AdjustmentService;
import com.gewara.service.CheckBillService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;

/**
 * @author Zhicheng.Peng Johnny.Resurgam@Gmail.com
 * 
 *         Aug 6, 2013 3:46:44 PM
 */
@Controller
public class CheckBillController extends AnnotationController {
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	
	@Autowired
	@Qualifier("adjustmentService")
	private AdjustmentService adjustmentService;
	
	@RequestMapping("/platform/checkBill/queryCheckBillDetails.xhtml")
	public String queryCheckBillDetails (Long checkBillId, ModelMap model){
		if (checkBillId == null){
			return showError(model, "参数异常");
		}
		setupQueryCheckBillDetails(checkBillId, model);
		return showRedirect("platform/gewaorder/queryPlayItemAggre.xhtml", model);
	}
	@RequestMapping("/merchant/checkBill/queryCheckBillDetails.xhtml")
	public String merchantQueryCheckBillDetails (Long checkBillId, String placeId, ModelMap model){
		if (checkBillId == null){
			return showError(model, "参数异常");
		}
		setupQueryCheckBillDetails(checkBillId, model);
		model.put("placeId", placeId);
		return showRedirect("merchant/gewaorder/queryPlayItemAggre.xhtml", model);
	}
	
	private void setupQueryCheckBillDetails(Long checkBillId, ModelMap model){
		CheckBill ck = daoService.getObject(CheckBill.class, checkBillId);
		String placeId = ck.getConfigId();
		//String placeName = daoService.getObject(Place.class, placeId).getName();
		Timestamp start = ck.getStart();
		Timestamp end = ck.getEnd();
		model.put("placeId", placeId);
		model.put("start", start);
		model.put("end", end);
		//model.put("placeFirstLetter", placeName);
	}

	@RequestMapping("/platform/checkBill/checkbills.xhtml")
	public String queryCheckBill(Long settleId, String placeId, String status,
			Timestamp start, Timestamp end,
			ModelMap model, String isXls, String isPrint,  HttpServletResponse response) {
		if (StringUtils.isBlank(placeId))
			return "/checkbill/checkbills.vm";
		Place place = daoService.getObject(Place.class, placeId);
		if (place == null)
			return "/checkbill/checkbills.vm";
		
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settleId);
		//zyj
		model.put("settlementBill", settlementBill);
		SettleConfig sc = daoService.getObject(SettleConfig.class, place.getRecordId());
		
		start = defaultTimeForPlace(sc, start);
		end = defaultTimeForPlace(sc, end);
		placeId = MiscUtil.getCheckBillConfigId(settlementBill.getTag(), placeId);
		String tag = settlementBill.getTag();
		int totalNumber = checkBillService.countingCheckBill(placeId, status, start, end,tag);
		if (totalNumber <= 0)
			return "/checkbill/checkbills.vm";
		
		setupCheckbill(settleId, placeId, status, start, end, place, false, 31,tag, model);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		m.put("status", status);
		m.put("start", start);
		m.put("end", end);
		
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
		
		//download check bills
		if(StringUtils.isNotBlank(isXls)){
			String t = "";
			if (start != null && end != null)
				t = " " + DateUtil.format(start, "MM-dd") + " 至 " + DateUtil.format(end, "MM-dd");
			this.download("xls", place.getName() +  t  +  "的对账单",  response);
			return "/downloadtemplate/checkbills.vm";
		}
		// print check bills
		if(StringUtils.isNotBlank(isPrint)){
			model.put("tag", settlementBill.getTag());
			return "/printtemplate/checkbills.vm";
		}
		
		return "/checkbill/checkbills.vm";
	}
	
	@RequestMapping("/merchant/checkBill/checkbills.xhtml")
	public String merchantQueryCheckBill(Long settleId, String placeId, String status,
											Timestamp start, Timestamp end,
											ModelMap model, String isXls, HttpServletRequest request, HttpServletResponse response, String isPrint){
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("placeId", placeId);
		model.put("place", daoService.getObject(Place.class, placeId));
		if (StringUtils.isBlank(placeId))
			return "/checkbill/merchantCheckBills.vm";
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		status = StringUtils.isBlank(status) ? CheckBillStatusEnums.GEWACONFIRMED.getStatus() : status;
		boolean isStatusNotEq = CheckBillStatusEnums.NEW.getStatus().equals(status) ? true : false;
		
		Place place = daoService.getObject(Place.class, placeId);
		SettleConfig sc = daoService.getObject(SettleConfig.class, place.getRecordId());
		start = defaultTimeForPlace(sc, start);
		end = defaultTimeForPlace(sc, end);
		SettlementBill bill = daoService.getObject(SettlementBill.class, settleId);
		String tag = (null == bill ? "" : bill.getTag());
		int totalNumber = checkBillService.countingCheckBill(placeId, status, start, end, isStatusNotEq,tag);
		if (totalNumber <= 0)
			return "/checkbill/merchantCheckBills.vm";
		
		setupCheckbill(settleId, placeId,  status, start, end, place, isStatusNotEq , totalNumber, tag, model);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		m.put("status", status);
		m.put("start", start);
		m.put("end", end);
		
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
			return "/printtemplate/checkbills.vm";
		}
		
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", place.getName() + "的对账单",  response);
			return "/downloadtemplate/checkbills.vm";
		}
		return "/checkbill/merchantCheckBills.vm";
	}
	
	public void setupCheckbill(Long settleId, String placeId, String status,
								Timestamp start, Timestamp end, Place place, boolean isStatusNotEq, int maxSize, String tag,
								ModelMap model){
		List<CheckBill> cks = null; 
		
		if (settleId == null){
			cks = checkBillService.queryCheckBill(placeId,
					status, start, end, maxSize, isStatusNotEq,tag);
		}else{
			cks = daoService.getObjectListByField(CheckBill.class, "settlementId", settleId);
			if (CollectionUtils.isNotEmpty(cks))
				Collections.sort(cks);
		}
		
		List<String> recordIds = BeanUtil.getBeanPropertyList(cks, "configId",true);
		if (settleId != null){
			List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(daoService.getObject(SettlementBill.class, settleId));
			model.put("ads",ads);
		}
		
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class,recordIds);
		model.put("cks", cks);
		model.put("pm", placeMap);
		
		model.put("place", place);
		model.put("pm", placeMap);
		model.put("settle", daoService.getObject(SettlementBill.class, settleId));
	}
	/**
	 * @param operator
	 */
}
