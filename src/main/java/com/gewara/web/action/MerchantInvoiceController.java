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
	 * ��Ʊ��ѯ
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
			this.download("xls", "��Ʊ" , response);
			return "invoice/invoice_xls.vm";
		}
		return "invoice/merchInvoice.vm";
	}
	
	/**
	 * ������ӷ�Ʊ
	 * @param settleids
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/showInvoiceAdd.xhtml")
	public String showInvoiceAdd(String settleids, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(settleids)) {
			model.put("errorMsgs", "�������㵥�Ų���Ϊ�գ�");
			return "invoice/merchAddInvoiceError.vm";
		}
		settleids = MiscUtil.sortSettleid(settleids);
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		if(CollectionUtils.isEmpty(allowplaceIds)) {
			model.put("errorMsgs", "�û��޷�������ӰԺ��");
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
				model.put("errorMsgs", "�������㵥��" + id + "�����ڣ�");
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
				model.put("errorMsgs", "������ӰԺid" + placeid + "Ϊ�Ƿ�ӰԺ��");
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
			model.put("errorMsgs", "��Ʊ���Ų���Ϊ�գ�");
			return "invoice/merchAddInvoiceError.vm";
		}
		if(StringUtils.isBlank(opt)) {
			model.put("errorMsgs", "�������Ͳ���Ϊ�գ�");
			return "invoice/merchAddInvoiceError.vm";
		}
		List<InvoiceSettleRelate> isrlist = daoService.getObjectListByField(InvoiceSettleRelate.class, "invoicecode", invoicecode);
		if(CollectionUtils.isEmpty(isrlist)) {
			model.put("errorMsgs", "��Ʊ" + invoicecode + "�������㵥�Ų����ڣ�");
			return "invoice/merchAddInvoiceError.vm";
		}
		Double tatalcost = 0.0;
		Double gwkpamount = 0.0;
		int settlnum = 0;
		for(InvoiceSettleRelate isr : isrlist) {
			SettlementBill sb = daoService.getObject(SettlementBill.class, isr.getSettleid());
			if(sb == null) {
				model.put("errorMsgs", "�������㵥" + isr.getSettleid() + "�����ڣ�");
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
	 * ���淢Ʊ
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/saveInvoice.xhtml")
	public String saveInvoice(Invoice invoice, String opt, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invoice.getInvoicecode())) {
			return showJsonError(model, "��Ʊ���벻��Ϊ�գ�");
		}
		if(StringUtils.isBlank(invoice.getVendername())) {
			return showJsonError(model, "������λ���Ʋ���Ϊ�գ�");
		}
		if(invoice.getInvoicedate() == null) {
			return showJsonError(model, "��Ʊ���ڲ���Ϊ�գ�");
		}
		if(invoice.getTaxamount() == null) {
			return showJsonError(model, "˰���Ϊ�գ�");
		}
		if(invoice.getInvoiceamount() == null) {
			return showJsonError(model, "��Ʊ����Ϊ�գ�");
		}
		if(StringUtils.isBlank(invoice.getSettleids())) {
			return showJsonError(model, "��Ϣ��������");
		}
		
		String optuser = getMerchantUserName(request);
		//String optuser = "test";
		List<String> allowplaceIds = getAllowedPlaceIds(request);
		ResultCode<String> result = invoiceService.merchantSaveInvoice(invoice, opt, optuser, allowplaceIds);
		
		if(result.isSuccess()) {
			Map<String, Object> jsonMap = new HashMap<String, Object>();
			jsonMap.put("retval", "����ɹ����뾡���ʼ�ֽ�ʷ�Ʊ��лл��");
			jsonMap.put("settleids", result.getMsg());
			return showJsonSuccess(model, jsonMap);
		} else {
			return showJsonError(model, result.getMsg());
		}
	}
	
	/**
	 * ɾ����Ʊ
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/merchant/invoice/delInvoice.xhtml")
	public String delInvoice(String invoicecode, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invoicecode)) {
			return showJsonError(model, "��Ʊ����Ϊ�գ�");
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
	 * ��Ʊ���ò�ѯ
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
	 * ��Ʊ��������
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
	 * ���淢Ʊ����
	 * @param invoice
	 * @param model
	 * @return
	 */
	@RequestMapping("/merchant/invoice/saveInvoiceConfig.xhtml")
	public String saveInvoiceConfig(InvoiceConfig invoice, ModelMap model) {
		if(invoice.getPlaceid() == null) {
			return showJsonError(model, "�����쳣��");
		}
		if(invoice.getTaxrate() == null) {
			return showJsonError(model, "��ѡ��˰�ʣ�");
		}
		if(StringUtils.isBlank(invoice.getInvoicetype())) {
			return showJsonError(model, "��ѡ��Ʊ���ͣ�");
		}
		if(StringUtils.isBlank(invoice.getTaxcondition())) {
			return showJsonError(model, "��ѡ��˰�����ʣ�");
		}
		if(SettleConstant.PTFP.equals(invoice.getInvoicetype()) && StringUtils.isBlank(invoice.getContent())) {
			return showJsonError(model, "��ֵ˰��ͨ��Ʊ�뱸ע��˵����");
		}
		invoice.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(invoice);
		return showJsonSuccess(model, "����ɹ���");
	}
	
	@RequestMapping("/merchant/invoice/skiptax.xhtml")
	public String skiptax(String skiptax, ModelMap model, HttpServletRequest request) {
		request.getSession().setAttribute("skiptax", "Y");
		return showJsonSuccess(model, "����ɹ���");
	}
}
