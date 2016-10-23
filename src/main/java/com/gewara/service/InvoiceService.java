package com.gewara.service;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Invoice;
import com.gewara.vo.InvoiceVo;

public interface InvoiceService {
	
	//发票查询
	ModelMap queryInvoice(InvoiceVo ivo, String user, List<String> allowplaceIds, String url, Integer pageNo, Integer pageSize, String isXls, ModelMap model);
	
	//保存发票
	ResultCode<String> merchantSaveInvoice(Invoice invoice, String opt, String optuser, List<String> allowplaceIds);
	
	//删除发票
	ResultCode<String> merchantDelInvoice(String invoicecode, List<String> allowplaceIds);
	
	//通过
	void invoicePass(List<Invoice> invlist, String gewauser, String status, String dikoumonth);
}
