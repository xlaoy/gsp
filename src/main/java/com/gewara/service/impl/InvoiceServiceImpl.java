package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.Invoice;
import com.gewara.model.gsp.InvoiceSettleRelate;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.service.DaoService;
import com.gewara.service.InvoiceService;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;
import com.gewara.vo.InvoiceVo;
import com.gewara.web.util.PageUtil;

@Service("invoiceService")
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
	@Qualifier("daoService")
	protected DaoService daoService;
	
	@Autowired
	@Qualifier("config")
	protected Config config;
	
	/**
	 * 发票查询
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap queryInvoice(InvoiceVo ivo, String user, List<String> allowplaceIds, String url, 
			Integer pageNo, Integer pageSize, String isXls, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		Long settleid = ivo.getSettleid();
		if(settleid != null) {
			pageSize = pageSize == null ? 10000 : pageSize;
		} else if(StringUtils.isNotEmpty(isXls)) {
			pageSize = pageSize == null ? 10000 : pageSize;
		} else {
			pageSize = pageSize == null ? 10 : pageSize;
		}
		
		List<Invoice> invoicelist = new ArrayList<Invoice>();
		
		String invoicecode = ivo.getInvoicecode();
		String status = ivo.getStatus();
		String vendername = ivo.getVendername();
		String invoicetype = ivo.getInvoicetype();
		Timestamp invoicedate = ivo.getInvoicedate();
		Timestamp start = ivo.getStart();
		Timestamp end = ivo.getEnd();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(Invoice.class, "iv");
		DetachedCriteria queryList = DetachedCriteria.forClass(Invoice.class, "iv");
		//exists子查询
		DetachedCriteria subquery = DetachedCriteria.forClass(InvoiceSettleRelate.class, "ivsr");
		
		if("MECHANT".equals(user)) {
			subquery.add(Restrictions.in("placeid", allowplaceIds));
		}
		if(settleid != null) {
			subquery.add(Restrictions.eq("settleid", settleid));
		}
		subquery.add(Property.forName("iv.invoicecode").eqProperty("ivsr.invoicecode"));
		
		if(StringUtils.isNotBlank(invoicecode)) {
			queryCount.add(Restrictions.eq("invoicecode", invoicecode));
			queryList.add(Restrictions.eq("invoicecode", invoicecode));
		}
		if(StringUtils.isNotBlank(status)) {
			queryCount.add(Restrictions.eq("status", status));
			queryList.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(invoicetype)) {
			queryCount.add(Restrictions.eq("invoicetype", invoicetype));
			queryList.add(Restrictions.eq("invoicetype", invoicetype));
		}
		if(StringUtils.isNotBlank(vendername)) {
			queryCount.add(Restrictions.like("vendername", vendername, MatchMode.ANYWHERE));
			queryList.add(Restrictions.like("vendername", vendername, MatchMode.ANYWHERE));
		}
		if(invoicedate != null) {
			queryCount.add(Restrictions.eq("invoicedate", invoicedate));
			queryList.add(Restrictions.eq("invoicedate", invoicedate));
		}
		if(start != null) {
			queryCount.add(Restrictions.ge("addtime", start));
			queryList.add(Restrictions.ge("addtime", start));
		}
		if(end != null) {
			queryCount.add(Restrictions.le("addtime", end));
			queryList.add(Restrictions.le("addtime", end));
		}
		queryCount.add(Subqueries.exists(subquery.setProjection(Projections.property("ivsr.recordid"))));
		queryList.add(Subqueries.exists(subquery.setProjection(Projections.property("ivsr.recordid"))));
		
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("addtime"));
		
		try {
			int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
			
			if(count > 0) {
				invoicelist = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
				Map<String, List<InvoiceSettleRelate>> isrlistmap = new HashMap<String, List<InvoiceSettleRelate>>();
				int taxnum = 0;
				Double invoiceamount = 0.0;
				for(Invoice inv : invoicelist) {
					List<InvoiceSettleRelate> isrlist = daoService.getObjectListByField(InvoiceSettleRelate.class, "invoicecode", inv.getInvoicecode());
					isrlistmap.put(inv.getInvoicecode(), isrlist);
					invoiceamount += inv.getInvoiceamount();
					taxnum++;
				}
				if(settleid != null) {
					SettlementBillExtend bill = daoService.getObject(SettlementBillExtend.class, settleid);
					Double billingamount = 0.0;
					if(bill != null) {
						billingamount = bill.getBillingamount();
					}
					model.put("invoiceamount", invoiceamount);
					model.put("billingamount", billingamount);
					model.put("taxnum", taxnum);
				}
				PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("invoicecode", invoicecode);
				m.put("settleid", settleid);
				m.put("invoicedate", invoicedate);
				m.put("vendername", vendername);
				m.put("status", status);
				m.put("invoicetype", invoicetype);
				m.put("start", start);
				m.put("end", end);
				pageUtil.initPageInfo(m);
				model.put("pageUtil", pageUtil);
				model.put("isrlistmap", isrlistmap);
			}
		} catch (Exception e) {
		}
		
		model.put("statusMap", SettleConstant.INVOICESTATUSMAP);
		model.put("invoicelist", invoicelist);
		model.put("settleid", settleid);
		return model;
	}
	
	
	/**
	 * 保存发票
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResultCode<String> merchantSaveInvoice(Invoice invoice, String opt, String optuser, List<String> allowplaceIds) {
		
		Invoice inv = daoService.getObject(Invoice.class, invoice.getInvoicecode());
		Timestamp time = DateUtil.getCurFullTimestamp();
		String settleids = null;
		
		if(SettleConstant.OPERATE_TYPE_ADD.equals(opt)) {
			if(inv != null) {
				return ResultCode.getFailure("发票单号" + invoice.getInvoicecode() + "已使用，请更换！");
			}
			invoice.setDeductibleamount(invoice.getTaxamount());
			invoice.setAddtime(time);
			invoice.setStatus(SettleConstant.INVOSTANEW);
			settleids = MiscUtil.sortSettleid(invoice.getSettleids());
			invoice.setSettleids(settleids);
			invoice.setUpdatetime(time);
			invoice.setOptuser(optuser);
			daoService.addObject(invoice);
			//保存发票和结算单关系
			String[] sids = invoice.getSettleids().split(",");
			for(String sid : sids) {
				SettlementBill sb = daoService.getObject(SettlementBill.class, Long.valueOf(sid));
				if(sb == null) {
					return ResultCode.getFailure("结算单" + sid + "不存在，保存失败！");
				}
				if(!allowplaceIds.contains(sb.getConfigId())) {
					return ResultCode.getFailure("结算单" + sid + "影院关联非法！");
				}
				SettleConfig sc = daoService.getObject(SettleConfig.class, sb.getConfigId());
				String vendorcode = null;
				if(sc != null) {
					vendorcode = sc.getVenderNo();
				}
				InvoiceSettleRelate isr = new InvoiceSettleRelate(invoice.getInvoicecode(), sb.getRecordId(), sb.getConfigId(), vendorcode);
				daoService.addObject(isr);
			}
		} else if(SettleConstant.OPERATE_TYPE_MODIFY.equals(opt)) {
			if(inv == null) {
				return ResultCode.getFailure("发票单号" + invoice.getInvoicecode() + "不存在，无法修改！");
			}
			inv.setInvoicetype(invoice.getInvoicetype());
			inv.setTaxrate(invoice.getTaxrate());
			inv.setVendername(invoice.getVendername());
			inv.setInvoicedate(invoice.getInvoicedate());
			inv.setTaxamount(invoice.getTaxamount());
			inv.setDeductibleamount(invoice.getTaxamount());
			inv.setInvoiceamount(invoice.getInvoiceamount());
			inv.setExcludetaxamount(invoice.getExcludetaxamount());
			inv.setSubmitcontent(invoice.getSubmitcontent());
			inv.setUpdatetime(time);
			inv.setOptuser(optuser);
			daoService.updateObject(inv);
			settleids = inv.getSettleids();
		}
		return ResultCode.getSuccess(settleids);
	}
	
	/**
	 * 删除发票
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ResultCode<String> merchantDelInvoice(String invoicecode, List<String> allowplaceIds) {
		List<InvoiceSettleRelate> isrlist = daoService.getObjectListByField(InvoiceSettleRelate.class, "invoicecode", invoicecode);
		for(InvoiceSettleRelate isr : isrlist) {
			if(!allowplaceIds.contains(isr.getPlaceid())) {
				return ResultCode.getFailure("存在非法关联影院结算单，影院id：" + isr.getPlaceid());
			}
		}
		daoService.removeObjectList(isrlist);
		Invoice invo = daoService.getObject(Invoice.class, invoicecode);
		daoService.removeObject(invo);
		return ResultCode.getSuccess("删除成功!");
	}
	
	/**
	 * 通过
	 */
	@Override
	public void invoicePass(List<Invoice> invlist, String gewauser, String status, String dikoumonth) {
		for(Invoice invoice : invlist) {
			invoice.setStatus(status);
			invoice.setOptuser(gewauser);
			invoice.setUpdatetime(DateUtil.getCurFullTimestamp());
			if(SettleConstant.INVOSTADIKOU.equals(status)) {
				invoice.setDikoumonth(dikoumonth);
			} else {
				invoice.setDikoumonth(null);
			}
			daoService.updateObject(invoice);
		}
	}
	
}
