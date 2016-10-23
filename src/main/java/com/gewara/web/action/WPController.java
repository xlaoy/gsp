package com.gewara.web.action;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.SettlementBillService;
import com.gewara.service.WPService;

/**
 * 微票
 * @author user
 *
 */
@Controller
public class WPController extends AnnotationController {

	@Autowired
	@Qualifier("WPServiceImpl")
	private WPService WPServiceImpl;
	
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;
	
	
	/**
	 * 微票月账单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/wp/queryWPBill.xhtml")
	public String queryWPMonthBill(SettlementBill sb, Integer pageNo, Integer pageSize, ModelMap model, HttpServletRequest request) {
		String url = "/platform/wp/queryWPBill.xhtml";
		String optUser = getLogonUser(request).getUsername();
		model = WPServiceImpl.queryWPMonthBill(sb, pageNo, pageSize, url, optUser, model);
		return "/wp/monthWPbill.vm";
	}
	
	/**
	 * 影院月账单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/wp/queryWPPlaceBill.xhtml")
	public String queryWPPlaceBill(CheckBill ck, String isXls, Integer pageNo, Integer pageSize, ModelMap model, HttpServletResponse response) {
		String url = "/platform/wp/queryWPPlaceBill.xhtml";
		model = WPServiceImpl.queryWPPlaceBill(ck, isXls, pageNo, pageSize, url, model);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "微票影院账单" , response);
			return "/wp/placeWPbill_xls.vm";
		}
		return "/wp/placeWPbill.vm";
	}
	
	/**
	 * 微票日账单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/wp/queryWPdayBill.xhtml")
	public String queryWPdayBill(CheckBill ck, ModelMap model) {
		String url = "/platform/wp/queryWPdayBill.xhtml";
		model = WPServiceImpl.queryWPPlaceBill(ck, null, 0, 50, url, model);
		return "/wp/dayWPbill.vm";
	}
	
	/**
	 * 导出订单
	 * @param type
	 * @param settleId
	 * @param model
	 * @param response
	 * @return
	 */
	@RequestMapping("/platform/wp/exportWPOrder.xhtml")
	public String exportWPOrder(String type, Long recordId, String duan, Timestamp start, Timestamp end, ModelMap model, HttpServletResponse response) {
		model = WPServiceImpl.exportWPOrder(type, recordId, duan, start, end, model);
		download("xls", "订单", response);
		return "/downloadtemplate/orderExport.vm";
	}
	
	/**
	 * 导出退单
	 * @param type
	 * @param settleId
	 * @param model
	 * @param response
	 * @return
	 */
	@RequestMapping("/platform/wp/exportWPRefund.xhtml")
	public String exportWPRefund(String type, Long recordId, Timestamp start, Timestamp end, ModelMap model, HttpServletResponse response) {
		model = WPServiceImpl.exportWPRefund(type, recordId, start, end, model);
		download("xls", "退单", response);
		return "/downloadtemplate/refundExport.vm";
	}
	
	/**
	 * 确认结算
	 * @param recordIds
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/wp/confirmSettleBill.xhtml")
	public String confirmSettleBill(String recordId, ModelMap model, HttpServletRequest request){
		if (StringUtils.isBlank(recordId)) {
			return showJsonError(model, "参数错误！");
		}
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(recordId));
		if(bill == null) {
			return showJsonError(model, "单据不存在！");
		}
		
		bill.setStatus(CheckBillStatusEnums.WAITINGPAY.getStatus());
		daoService.updateObject(bill);
		
		return showJsonSuccess(model, "已确认!");
	}
	
	/**
	 * 申请调整
	 * @param model
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/platform/wp/saveAdjust.xhtml")
	public String saveAdjust(ModelMap model, HttpServletRequest request) throws IOException{
		
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
		String recordStr = multipartRequest.getParameter("recordId");
		if (StringUtils.isBlank(recordStr) || recordStr == null){
			return alertMessage(model, "非法操作.");
		}
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(recordStr));
		if (bill == null) {
			return alertMessage(model,"结算单参数异常：" + recordStr);
		}
		if (!CheckBillStatusEnums.GEWACONFIRMED.getStatus().equals(bill.getStatus())) {
			return alertMessage(model,"结算单当前状态不允许提交申请调整。code：" + bill.getStatus());
		}
		
		String optUser = getLogonUser(request).getUsername();
		String attachePath = "";
		MultipartFile file = multipartRequest.getFile("file");
		if (file != null && file.getSize() != 0){
			ResultCode uploadR = WPServiceImpl.saveToRemote(file.getBytes(), file.getOriginalFilename(), optUser);
			if (!uploadR.isSuccess()){
				return alertMessage(model,uploadR.getMsg());
			}
			attachePath = (String) uploadR.getRetval();
		}
		
		String comments = multipartRequest.getParameter("comments");
		ResultCode result = settlementBillService.saveAdjustment(bill, null, comments, attachePath, null, optUser, "MERCHANT");
		if (!result.isSuccess()){
			return alertMessage(model, result.getMsg());
		}
		
		return alertMessageSuccess(model,"已提交， 请等待反馈..");
	}
	
}
