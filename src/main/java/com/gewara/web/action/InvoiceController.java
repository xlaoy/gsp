package com.gewara.web.action;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.Invoice;
import com.gewara.model.gsp.InvoiceConfig;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SysData;
import com.gewara.service.InvoiceService;
import com.gewara.service.SysDataService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.vo.InvoiceVo;
import com.gewara.web.util.PageUtil;

@Controller
public class InvoiceController extends AnnotationController {

	@Autowired
	@Qualifier("invoiceService")
	protected InvoiceService invoiceService;
	
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	/**
	 * ��Ʊ��ѯ
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/invoice/queryInvoice.xhtml")
	public String queryInvoice(InvoiceVo ivo, Integer pageNo, Integer pageSize, String isXls, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String url = "/platform/invoice/queryInvoice.xhtml";
		model = invoiceService.queryInvoice(ivo, "GEWA", null, url, pageNo, pageSize, isXls, model);
		//��ѯ��Ʊ����Ȩ��
		String gewauser = getLogonUser(request).getUsername();
		SysData invadmin = sysDataService.getSysData(gewauser, SettleConstant.INVOICEADMIN, String.class);
		if(invadmin != null) {
			model.put("invadmin", SettleConstant.COMM_Y);
		} else {
			model.put("invadmin", SettleConstant.COMM_N);
		}
		if(StringUtils.isNotBlank(isXls)) {
			model.put("user", "GW");
			this.download("xls", "��Ʊ" , response);
			return "invoice/invoice_xls.vm";
		}
		return "invoice/invoice.vm";
	}
	
	/**
	 * ���ط�Ʊ����
	 * @return
	 */
	@RequestMapping("/platform/invoice/loadInvoiceDetail.xhtml")
	public String loadInvoiceDetail(String invcode, String opt, String nextstatus, ModelMap model, HttpServletRequest request) {
		String gewauser = getLogonUser(request).getUsername();
		SysData invadmin = sysDataService.getSysData(gewauser, SettleConstant.INVOICEADMIN, String.class);
		if(invadmin != null) {
			model.put("invadmin", SettleConstant.COMM_Y);
		} else {
			model.put("invadmin", SettleConstant.COMM_N);
		}
		Invoice invoice = daoService.getObject(Invoice.class, invcode);
		model.put("invoice", invoice);
		model.put("opt", opt);
		model.put("nextstatus", nextstatus);
		return "invoice/invoiceModifyPop.vm";
	}
	
	
	/**
	 * ͨ��
	 * @return
	 */
	@RequestMapping("/platform/invoice/invoicePass.xhtml")
	public String invoicePass(String invcodes, String status, String dikoumonth, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invcodes)) {
			return showJsonError(model, "invcodes��������");
		}
		if(StringUtils.isBlank(status)) {
			return showJsonError(model, "status��������");
		}
		if(!SettleConstant.INVOSTAPASS.equals(status)
				&& !SettleConstant.INVOSTAAUTH.equals(status)
				&& !SettleConstant.INVOSTADIKOU.equals(status)) {
			return showJsonError(model, "statusδ֪��");
		}
		String gewauser = getLogonUser(request).getUsername();
		SysData invadmin = sysDataService.getSysData(gewauser, SettleConstant.INVOICEADMIN, String.class);
		List<Invoice> invlist = new ArrayList<Invoice>();
		String[] strs = invcodes.split(",");
		for(String code : strs) {
			Invoice invoice = daoService.getObject(Invoice.class, code);
			if(invoice == null) {
				return showJsonError(model, "��Ʊ" + code + "�����ڣ�");
			}
			//����ͨ��
			if(SettleConstant.INVOSTAPASS.equals(status)) {
				if(!SettleConstant.INVOSTANEW.equals(invoice.getStatus()) && invadmin == null) {
					return showJsonError(model, "��Ʊ" + code + "״̬���Ǵ����״̬��");	
				}
			}
			//��֤ͨ��
			if(SettleConstant.INVOSTAAUTH.equals(status)) {
				if(!SettleConstant.ZYFP.equals(invoice.getInvoicetype())) {
					return showJsonError(model, "��Ʊ" + code + "״̬���Ͳ���ר�÷�Ʊ��");	
				}
				if(!SettleConstant.INVOSTAPASS.equals(invoice.getStatus()) && invadmin == null) {
					return showJsonError(model, "��Ʊ" + code + "״̬�������ͨ��״̬��");	
				}
			}
			//�ֿ۳ɹ�
			if(SettleConstant.INVOSTADIKOU.equals(status)) {
				if(!SettleConstant.ZYFP.equals(invoice.getInvoicetype())) {
					return showJsonError(model, "��Ʊ" + code + "״̬���Ͳ���ר�÷�Ʊ��");	
				}
				if(!SettleConstant.INVOSTAAUTH.equals(invoice.getStatus()) && invadmin == null) {
					return showJsonError(model, "��Ʊ" + code + "״̬������֤�ɹ�״̬��");	
				}
				if(StringUtils.isBlank(dikoumonth)) {
					return showJsonError(model, "�ֿ��·ݲ���Ϊ�գ�");
				}
			}
			invlist.add(invoice);
		}
		
		invoiceService.invoicePass(invlist, gewauser, status, dikoumonth);
		return showJsonSuccess(model, "�����ɹ���");
	}
	
	/**
	 * ʧ��
	 * @return
	 */
	@RequestMapping("/platform/invoice/invoiceUnpass.xhtml")
	public String invoiceUnpass(String invcode, String status, String subcontent, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invcode)) {
			return showJsonError(model, "��������");
		}
		if(StringUtils.isBlank(subcontent)) {
			return showJsonError(model, "��ע����Ϊ�գ�");
		}
		
		if(!SettleConstant.INVOSTAUNPASS.equals(status)
				&& !SettleConstant.INVOSTAUNAUTH.equals(status)
				&& !SettleConstant.INVOSTAUNDIKOU.equals(status)) {
			return showJsonError(model, "statusδ֪��");
		}
		
		Invoice invoice = daoService.getObject(Invoice.class, invcode);
		if(invoice == null) {
			return showJsonError(model, "��Ʊ" + invcode + "�����ڣ�");
		}
		
		String gewauser = getLogonUser(request).getUsername();
		SysData invadmin = sysDataService.getSysData(gewauser, SettleConstant.INVOICEADMIN, String.class);
		//������ͨ��
		if(SettleConstant.INVOSTAUNPASS.equals(status)) {
			if(!SettleConstant.INVOSTANEW.equals(invoice.getStatus()) && invadmin == null) {
				return showJsonError(model, "��Ʊ" + invcode + "״̬���Ǵ����״̬��");
			}
		}
		//��֤ʧ��
		if(SettleConstant.INVOSTAUNAUTH.equals(status)) {
			if(!SettleConstant.ZYFP.equals(invoice.getInvoicetype())) {
				return showJsonError(model, "��Ʊ" + invcode + "״̬���Ͳ���ר�÷�Ʊ��");	
			}
			if(!SettleConstant.INVOSTAPASS.equals(invoice.getStatus()) && invadmin == null) {
				return showJsonError(model, "��Ʊ" + invcode + "״̬����ͨ�����״״̬��");	
			}
		}
		//�ֿ�ʧ��
		if(SettleConstant.INVOSTAUNDIKOU.equals(status)) {
			if(!SettleConstant.ZYFP.equals(invoice.getInvoicetype())) {
				return showJsonError(model, "��Ʊ" + invcode + "״̬���Ͳ���ר�÷�Ʊ��");	
			}
			if(!SettleConstant.INVOSTAAUTH.equals(invoice.getStatus()) && invadmin == null) {
				return showJsonError(model, "��Ʊ" + invcode + "״̬������֤�ɹ�״̬��");	
			}
		}
		invoice.setStatus(status);
		invoice.setSubmitcontent(subcontent);
		invoice.setOptuser(gewauser);
		invoice.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(invoice);
		return showJsonSuccess(model, "�����ɹ���");
	}
	
	/**
	 * ���淢Ʊ�޸�
	 * @param invcode
	 * @param status
	 * @param subcontent
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/invoice/updateInvoice.xhtml")
	public String updateInvoice(String invcode, String invoicetype, Double deductibleamount, String subcontent, 
			ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(invcode)) {
			return showJsonError(model, "��Ʊ�Ų���Ϊ�գ�");
		}
		if(StringUtils.isBlank(invoicetype)) {
			return showJsonError(model, "��Ʊ���Ͳ���Ϊ�գ�");
		}
		if(StringUtils.isBlank(subcontent)) {
			return showJsonError(model, "��Ʊ��ע����Ϊ�գ�");
		}
		Invoice invoice = daoService.getObject(Invoice.class, invcode);
		if(invoice == null) {
			return showJsonError(model, "��Ʊ�����ڣ�");
		}
		if(SettleConstant.ZYFP.equals(invoicetype)) {
			if(deductibleamount == null) {
				return showJsonError(model, "ר�÷�Ʊ˰���Ϊ�գ�");
			}
			if(deductibleamount > invoice.getTaxamount()) {
				return showJsonError(model, "ר�÷�Ʊ�ֿ۽��ܴ���˰�");
			}
		} else if(SettleConstant.PTFP.equals(invoicetype)) {
			deductibleamount = 0.0;
			if(!SettleConstant.INVOSTANEW.equals(invoice.getInvoicetype())
					&& !SettleConstant.INVOSTAPASS.equals(invoice.getInvoicetype())) {
				return showJsonError(model, "��Ʊ״̬���Ƹ÷�Ʊ���ܸ�Ϊ��ͨ��Ʊ��");
			}
		}
		String gewauser = getLogonUser(request).getUsername();
		invoice.setInvoicetype(invoicetype);
		invoice.setDeductibleamount(deductibleamount);
		invoice.setSubmitcontent(subcontent);
		invoice.setOptuser(gewauser);
		invoice.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(invoice);
		return showJsonSuccess(model, "�޸ĳɹ���");
	}
	
	/**
	 * ӰƱ���ò�ѯ
	 * @param model
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/invoice/queryInvoiceConfig.xhtml")
	public String queryInvoiceConfig(InvoiceConfig invcfg, Integer pageNo, Integer pageSize, String isXls, 
			ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String url = "/platform/invoice/queryInvoiceConfig.xhtml";
		pageNo = pageNo == null ? 0 : pageNo;
		if(StringUtils.isNotEmpty(isXls)) {
			pageSize = pageSize == null ? 10000 : pageSize;
		} else {
			pageSize = pageSize == null ? 20 : pageSize;
		}
		List<InvoiceConfig> invcfglist = new ArrayList<InvoiceConfig>();
		String placeid = invcfg.getPlaceid();
		String invoicetype = invcfg.getInvoicetype();
		DetachedCriteria queryCount = DetachedCriteria.forClass(InvoiceConfig.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(InvoiceConfig.class);
		if(StringUtils.isNotBlank(placeid)) {
			queryCount.add(Restrictions.eq("placeid", placeid));
			queryList.add(Restrictions.eq("placeid", placeid));
		}
		if(StringUtils.isNotBlank(invoicetype)) {
			queryCount.add(Restrictions.eq("invoicetype", invoicetype));
			queryList.add(Restrictions.eq("invoicetype", invoicetype));
		}
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("updatetime"));
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		Map<String, Place> placemap = new HashMap<String, Place>();
		Map<String, SettleConfig> cfgmap = new HashMap<String, SettleConfig>();
		if(count > 0) {
			invcfglist = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			List<String> placeids = BeanUtil.getBeanPropertyList(invcfglist, "placeid", true);
			placemap = daoService.getObjectMap(Place.class, placeids);
			cfgmap = daoService.getObjectMap(SettleConfig.class, placeids);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("placeid", placeid);
			m.put("invoicetype", invoicetype);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		model.put("invcfglist", invcfglist);
		model.put("placemap", placemap);
		model.put("cfgmap", cfgmap);
		if(StringUtils.isNotBlank(isXls)) {
			this.download("xls", "��Ʊ����" , response);
			return "invoice/invoiceConfig_xls.vm";
		}
		return "invoice/invoiceConfig.vm";
	}
	
	
	/**
	 * ��Ʊ��������
	 * @param placeid
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/invoice/loadInvoiceConfigDetail.xhtml")
	public String loadInvoiceConfigDetail(String opt, Long recordid, ModelMap model) {
		if(SettleConstant.OPERATE_TYPE_MODIFY.equals(opt)) {
			if(recordid == null) {
				String errorMsgs = "recordid�����ڣ�";
				model.put("errorMsgs", errorMsgs);
				return "showResult.vm";
			}
			InvoiceConfig ivnc = daoService.getObject(InvoiceConfig.class, recordid);
			if(ivnc == null) {
				String errorMsgs = "��Ʊ����id" + recordid + "�����ڣ�";
				model.put("errorMsgs", errorMsgs);
				return "showResult.vm";
			}
			String placeid = ivnc.getPlaceid();
			Place place = daoService.getObject(Place.class, placeid);
			model.put("invconfig", ivnc);
			model.put("place", place);
		}
		model.put("opt", opt);
		return "invoice/invoiceUpdate.vm";
	}
	
	/**
	 * ���淢Ʊ����
	 * @param invoice
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/invoice/saveInvoiceConfig.xhtml")
	public String saveInvoiceConfig(InvoiceConfig invoice, String opt, ModelMap model) {
		if(invoice.getPlaceid() == null) {
			return showJsonError(model, "Placeid�����쳣��");
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
		
		if(SettleConstant.OPERATE_TYPE_MODIFY.equals(opt)) {
			if(invoice.getRecordid() == null) {
				return showJsonError(model, "Recordid�����쳣��");
			}
			daoService.updateObject(invoice);
		}
		if(SettleConstant.OPERATE_TYPE_ADD.equals(opt)) {
			invoice.setRecordid(null);
			String placeid = invoice.getPlaceid();
			SettleConfig sc = daoService.getObject(SettleConfig.class, placeid);
			if(sc == null) {
				return showJsonError(model, "ӰԺid" + placeid + "û��ӰԺ���ã�������ӷ�Ʊ���ã�");
			}
			List<InvoiceConfig> invlist = daoService.getObjectListByField(InvoiceConfig.class, "placeid", placeid);
			if(CollectionUtils.isNotEmpty(invlist)) {
				return showJsonError(model, "ӰԺid" + placeid + "�Ѿ���ӷ�Ʊ���ã������ظ���ӣ�");
			}
			daoService.addObject(invoice);
		}
		return showJsonSuccess(model, "����ɹ���");
	}
	
	/**
	 * ɾ����Ʊ����
	 * @param invoice
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/invoice/delInvoiceConfig.xhtml")
	public String delInvoiceConfig(Long recordid, ModelMap model) {
		if(recordid == null) {
			return showJsonSuccess(model, "recordid�����쳣��");
		}
		InvoiceConfig ivnc = daoService.getObject(InvoiceConfig.class, recordid);
		if(ivnc == null) {
			return showJsonSuccess(model, "��Ʊ����id" + recordid + "�����ڣ�");
		}
		daoService.removeObject(ivnc);
		return showJsonSuccess(model, "ɾ���ɹ���");
	}
}
