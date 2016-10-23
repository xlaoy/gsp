package com.gewara.web.action;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.gewara.constant.SettleConstant;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Invoice;
import com.gewara.model.gsp.InvoiceConfig;
import com.gewara.model.gsp.InvoiceSettleRelate;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.service.InvoiceService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;
import com.gewara.vo.InvoiceVo;

@Controller
public class MerchantInvoiceController extends AnnotationController {

	@Autowired
	@Qualifier("invoiceService")
	protected InvoiceService invoiceService;
	
	/**
	 * 发票查询
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/queryInvoice.xhtml")
	public String queryInvoice(InvoiceVo ivo, Integer pageNo, Integer pageSize, String isXls, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		String url = "/merchant/invoice/queryInvoice.xhtml";
		model = invoiceService.queryInvoice(ivo, "MECHANT", allowplaceIds, url, pageNo, pageSize, isXls, model);
		if(StringUtils.isNotBlank(isXls)) {
			this.download("xls", "发票" , response);
			return "invoice/invoice_xls.vm";
		}
		return "invoice/merchInvoice.vm";
	}
	
	/**
	 * 弹出添加发票
	 * @param settleids
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/showInvoiceAdd.xhtml")
	public String showInvoiceAdd(String settleids, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(settleids)) {
			model.put("errorMsgs", "关联结算单号不能为空！");
			return "invoice/merchAddInvoiceError.vm";
		}
		settleids = MiscUtil.sortSettleid(settleids);
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		if(CollectionUtils.isEmpty(allowplaceIds)) {
			model.put("errorMsgs", "用户无法关联到影院！");
			return "invoice/merchAddInvoiceError.vm";
		}
		
		List<InvoiceConfig> ivconfiglist = daoService.getObjectListByField(InvoiceConfig.class, "placeid", allowplaceIds.get(0));
		if(CollectionUtils.isNotEmpty(ivconfiglist)) {
			InvoiceConfig ivc = ivconfiglist.get(0);
			SettleConfig sc = daoService.getObject(SettleConfig.class, ivc.getPlaceid());
			if(sc != null) {
				ivc.setVendername(sc.getPayVenderName());
			}
			model.put("invc", ivconfiglist.get(0));
		}
		
		String[] tempstrs = settleids.split(",");
		List<String> ids = Arrays.asList(tempstrs);
		List<String> placeids = new ArrayList<String>();
		Double tatalcost = 0.0;
		Double gwkpamount = 0.0;
		int settlnum = 0;
		for(String id : ids) {
			SettlementBill sb = daoService.getObject(SettlementBill.class, Long.valueOf(id));
			if(sb == null) {
				model.put("errorMsgs", "关联结算单号" + id + "不存在！");
				return "invoice/merchAddInvoiceError.vm";
			}
			SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, sb.getRecordId());
			tatalcost += sb.getOrderTotalAmount();
			placeids.add(sb.getConfigId());
			if(extend != null) {
				gwkpamount += extend.getBillingamount() == null ? 0.0 : extend.getBillingamount();
			}
			settlnum++;
		}
		for(String placeid : placeids) {
			if(!allowplaceIds.contains(placeid)) {
				model.put("errorMsgs", "关联的影院id" + placeid + "为非法影院！");
				return "invoice/merchAddInvoiceError.vm";
			}
		}
		
		model.put("settlnum", settlnum);
		model.put("tatalcost", tatalcost);
		model.put("gwkpamount", gwkpamount);
		model.put("settleids", settleids);
		model.put("opt", SettleConstant.OPERATE_TYPE_ADD);
		return "invoice/merchAddInvoice.vm";
	}
	
	/**
	 * 
	 * @param settleids
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/showInvoice.xhtml")
	public String showInvoice(String invoicecode, String opt, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invoicecode)) {
			model.put("errorMsgs", "发票单号不能为空！");
			return "invoice/merchAddInvoiceError.vm";
		}
		if(StringUtils.isBlank(opt)) {
			model.put("errorMsgs", "操作类型不能为空！");
			return "invoice/merchAddInvoiceError.vm";
		}
		List<InvoiceSettleRelate> isrlist = daoService.getObjectListByField(InvoiceSettleRelate.class, "invoicecode", invoicecode);
		if(CollectionUtils.isEmpty(isrlist)) {
			model.put("errorMsgs", "发票" + invoicecode + "关联结算单号不存在！");
			return "invoice/merchAddInvoiceError.vm";
		}
		Double tatalcost = 0.0;
		Double gwkpamount = 0.0;
		int settlnum = 0;
		for(InvoiceSettleRelate isr : isrlist) {
			SettlementBill sb = daoService.getObject(SettlementBill.class, isr.getSettleid());
			if(sb == null) {
				model.put("errorMsgs", "关联结算单" + isr.getSettleid() + "不存在！");
				return "invoice/merchAddInvoiceError.vm";
			}
			SettlementBillExtend extend = daoService.getObject(SettlementBillExtend.class, isr.getSettleid());
			tatalcost += sb.getOrderTotalAmount();
			if(extend != null) {
				gwkpamount += extend.getBillingamount() == null ? 0.0 : extend.getBillingamount();
			}
			settlnum++;
		}
		
		Invoice invoice = daoService.getObject(Invoice.class, invoicecode);
		if(SettleConstant.OPERATE_TYPE_ADD.equals(opt)) {
			invoice.setInvoicecode(null);
		}
		
		model.put("settlnum", settlnum);
		model.put("tatalcost", tatalcost);
		model.put("gwkpamount", gwkpamount);
		model.put("settleids", invoice.getSettleids());
		model.put("opt", opt);
		model.put("invc", invoice);
		return "invoice/merchAddInvoice.vm";
	}
	
	
	/**
	 * 保存发票
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/saveInvoice.xhtml")
	public String saveInvoice(Invoice invoice, String opt, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invoice.getInvoicecode())) {
			return showJsonError(model, "发票号码不能为空！");
		}
		if(StringUtils.isBlank(invoice.getVendername())) {
			return showJsonError(model, "销货单位名称不能为空！");
		}
		if(invoice.getInvoicedate() == null) {
			return showJsonError(model, "开票日期不能为空！");
		}
		if(invoice.getTaxamount() == null) {
			return showJsonError(model, "税额不能为空！");
		}
		if(invoice.getInvoiceamount() == null) {
			return showJsonError(model, "发票金额不能为空！");
		}
		if(StringUtils.isBlank(invoice.getSettleids())) {
			return showJsonError(model, "信息不完整！");
		}
		
		String optuser = getMerchantUserName(request);
		//String optuser = "test";
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		ResultCode<String> result = invoiceService.merchantSaveInvoice(invoice, opt, optuser, allowplaceIds);
		
		if(result.isSuccess()) {
			Map<String, Object> jsonMap = new HashMap<String, Object>();
			jsonMap.put("retval", "保存成功，请尽快邮寄纸质发票，谢谢！");
			jsonMap.put("settleids", result.getMsg());
			return showJsonSuccess(model, jsonMap);
		} else {
			return showJsonError(model, result.getMsg());
		}
	}
	
	/**
	 * 删除发票
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/delInvoice.xhtml")
	public String delInvoice(String invoicecode, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invoicecode)) {
			return showJsonError(model, "发票号码为空！");
		}
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		ResultCode<String> result = invoiceService.merchantDelInvoice(invoicecode, allowplaceIds);
		if(result.isSuccess()) {
			return showJsonSuccess(model, result.getMsg());
		} else {
			return showJsonError(model, result.getMsg());
		}
	}
	
	/**
	 * 发票配置查询
	 * @param model
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/merchant/invoice/queryInvoiceConfig.xhtml")
	public String queryInvoiceConfig(ModelMap model, HttpServletRequest request) {
		List<String> placeIds = getAllowedPlaceIds(request);
		List<Place> placelist = daoService.getObjectList(Place.class, placeIds);
		List<SettleConfig> configs = daoService.getObjectList(SettleConfig.class, placeIds);
		Map<String,SettleConfig> configMap = BeanUtil.beanListToMap(configs, "recordId");
		List<InvoiceConfig> invoicelist = daoService.getObjectBatch(InvoiceConfig.class, "placeid", placeIds);
		Map<String,InvoiceConfig> invoiceMap = new HashMap<String,InvoiceConfig>();
		for(InvoiceConfig invoice : invoicelist) {
			invoiceMap.put(invoice.getPlaceid(), invoice);
		}
		model.put("placelist", placelist);
		model.put("configMap", configMap);
		model.put("invoiceMap", invoiceMap);
		return "invoice/merchInvoiceConfig.vm";
	}
	
	/**
	 * 发票配置详情
	 * @param placeid
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/invoice/loadInvoiceConfigDetail.xhtml")
	public String loadInvoiceConfigDetail(String placeid, ModelMap model) {
		Place place = daoService.getObject(Place.class, placeid);
		SettleConfig setconfig = daoService.getObject(SettleConfig.class, placeid);
		List<InvoiceConfig> invoicelist = daoService.getObjectListByField(InvoiceConfig.class, "placeid", placeid);
		if(CollectionUtils.isNotEmpty(invoicelist)) {
			model.put("invconfig", invoicelist.get(0));
		}
		model.put("place", place);
		model.put("setconfig", setconfig);
		return "invoice/merchInvoiceDetail.vm";
	}
	
	/**
	 * 保存发票配置
	 * @param invoice
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/invoice/saveInvoiceConfig.xhtml")
	public String saveInvoiceConfig(InvoiceConfig invoice, ModelMap model) {
		if(invoice.getPlaceid() == null) {
			return showJsonError(model, "参数异常！");
		}
		if(invoice.getTaxrate() == null) {
			return showJsonError(model, "请选择税率！");
		}
		if(StringUtils.isBlank(invoice.getInvoicetype())) {
			return showJsonError(model, "请选择发票类型！");
		}
		if(StringUtils.isBlank(invoice.getTaxcondition())) {
			return showJsonError(model, "请选择税务资质！");
		}
		if(SettleConstant.PTFP.equals(invoice.getInvoicetype()) && StringUtils.isBlank(invoice.getContent())) {
			return showJsonError(model, "增值税普通发票请备注好说明！");
		}
		invoice.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(invoice);
		return showJsonSuccess(model, "保存成功！");
	}
	
	@RequestMapping("/merchant/invoice/skiptax.xhtml")
	public String skiptax(String skiptax, ModelMap model, HttpServletRequest request) {
		request.getSession().setAttribute("skiptax", "Y");
		return showJsonSuccess(model, "保存成功！");
	}
}
