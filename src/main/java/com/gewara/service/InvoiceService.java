package com.gewara.service;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Invoice;
import com.gewara.vo.InvoiceVo;

public interface InvoiceService {
	
	//��Ʊ��ѯ
	ModelMap queryInvoice(InvoiceVo ivo, String user, List<String> allowplaceIds, String url, Integer pageNo, Integer pageSize, String isXls, ModelMap model);
	
	//���淢Ʊ
	ResultCode<String> merchantSaveInvoice(Invoice invoice, String opt, String optuser, List<String> allowplaceIds);
	
	//ɾ����Ʊ
	ResultCode<String> merchantDelInvoice(String invoicecode, List<String> allowplaceIds);
	
	//ͨ��
	void invoicePass(List<Invoice> invlist, String gewauser, String status, String dikoumonth);
}
