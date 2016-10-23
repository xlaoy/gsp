package com.gewara.web.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DramaConst;
import com.gewara.model.acl.User;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.service.DramaBillService;
import com.gewara.service.DramaDoColleService;
import com.gewara.util.DateUtil;

@Controller
public class DramaBillController extends AnnotationController {

	@Autowired
	@Qualifier("dramaBillServiceImpl")
	protected DramaBillService dramaBillService;
	
	@Autowired
	@Qualifier("dramaDoCollecServiceImpl")
	protected DramaDoColleService collectionService;
	
	/**
	 * 演出结算单查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDramaSettleBill.xhtml")
	public String queryDramaSettleBill(DramaSettleBill bill, Integer pageNo, Integer pageSize, ModelMap model) {
		String url = "/platform/drama/queryDramaSettleBill.xhtml";
		model = dramaBillService.queryDramaSettleBill(bill, pageNo, pageSize, url, model);
		return "/drama/dramaSettleBill.vm";
	}
	
	/**
	 * 演出结算单价格账单查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDramaPriceBill.xhtml")
	public String queryDramaPriceBill(Long recordid, String isxls, ModelMap model, HttpServletResponse response) {
		model = dramaBillService.queryDramaPriceBill(recordid, model);
		if(StringUtils.isNotEmpty(isxls)) {
			this.download("xls", "价格对账单" , response);
			return "/drama/dramaPriceBill_xls.vm";
		}
		return "/drama/dramaPriceBill.vm";
	}
	
	/**
	 * 重新计算结算单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/reCalculateBill.xhtml")
	public String reCalculateBill(Long recordid, ModelMap model) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		if(bill == null) {
			return showJsonError(model, "重新计算失败，结算单" + recordid + "不存在！");
		}
		if(DramaConst.SETTLED.equals(bill.getStatus())) {
			return showJsonError(model, "重新计算失败，结算单" + recordid + "是已付款状态！");
		}
		if(DramaConst.FULFILLING.equals(bill.getStatus())) {
			return showJsonError(model, "重新计算失败，结算单" + recordid + "是系统处理中状态！");
		}
		collectionService.initDramaBill(bill);
		String result = collectionService.fullSettleBill(bill, false);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 付款
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/payBill.xhtml")
	public String payBill(String recordids, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(recordids)) {
			return showJsonError(model, "参数错误！");
		}
		String[] recordidList = recordids.split(",");
		List<DramaSettleBill> billList = new ArrayList<DramaSettleBill>();
		for(String recordid : recordidList) {
			DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, Long.valueOf(recordid));
			if(bill == null) {
				return showJsonError(model, "付款失败，结算单" + recordid + "不存在！");
			}
			if(!DramaConst.WAITINGPAY.equals(bill.getStatus())) {
				return showJsonError(model, "付款失败，结算单" + recordid + "的状态不是等待付款！");
			}
			if(bill.getPayamount().doubleValue() <= 0) {
				return showJsonError(model, "付款失败，结算单" + recordid + "的付款金额不能小于或等于0！");
			}
			billList.add(bill);
		}
		String optUser = getLogonUser(request).getUsername();
		dramaBillService.payBill(billList, optUser);
		return showJsonSuccess(model, "付款成功！");
	}
	
	/**
	 * 重新付款
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/rePayBill.xhtml")
	public String rePayBill(Long recordid, ModelMap model, HttpServletRequest request) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		String optUser = getLogonUser(request).getUsername();
		String result = dramaBillService.rePayBill(recordid, optUser);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 加载结算单详情
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/loadSettleBillDetail.xhtml")
	public String loadSettleBillDetail(Long recordid, String type, ModelMap model) {
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		model.put("dbill", bill);
		if("adjust".equals(type)) {
			return "/drama/dramaAdjustPop.vm";
		}
		return "";
	}
	
	/**
	 * 保存调整
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/saveAdjustBill.xhtml")
	public String saveAdjustBill(Long recordid, Long adjustnum, Double adjustamount, String adjustdesc, ModelMap model) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		if(bill == null) {
			return showJsonError(model, "结算单不存在！");
		}
		if(!DramaConst.WAITINGPAY.equals(bill.getStatus())) {
			return showJsonError(model, "结算单不是等待付款状态！");
		}
		if(adjustnum.longValue() == 0 && adjustamount.doubleValue() == 0) {
			return showJsonError(model, "调整数据不能同时为0！");
		}
		bill.setAdjustnum(adjustnum);
		bill.setAdjustamount(adjustamount);
		bill.setAdjustdesc(adjustdesc);
		bill.setPayamount(bill.getSyspayamount() + adjustamount);
		daoService.updateObject(bill);
		return showJsonSuccess(model, "调整成功！");
	}
	
	/**
	 * 申请请款
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/reqMoney.xhtml")
	public String reqMoney(Long recordid, ModelMap model) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		if(bill == null) {
			return showJsonError(model, "结算单不存在！");
		}
		if(!DramaConst.WAITINGPAY.equals(bill.getStatus())
				&& !DramaConst.SETTLED.equals(bill.getStatus())) {
			return showJsonError(model, "结算单状态不正确！");
		}
		if(DramaConst.Y.equals(bill.getReqmoney())) {
			return showJsonError(model, "结算单已请款！");
		}
		bill.setReqmoney(DramaConst.Y);
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(bill);
		return showJsonSuccess(model, "请款成功！");
	}
	
	/**
	 * 打印请款单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/printReqMoenyBill.xhtml")
	public String printReqMoenyBill(Long recordid, ModelMap model, HttpServletRequest request) {
		DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, recordid);
		if(bill == null) {
			model.put("errorMsgs", "结算单不存在！");
			return "showResult.vm";
		}
		User user = getLogonUser(request);
		model = dramaBillService.queryReqMoenyBill(bill, user, model);
		return "/drama/dramaReqMoneybill.vm";
	}
	
	/**
	 * 导出订单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/exportOrder.xhtml")
	public String exportOrder(Long recordid, ModelMap model, HttpServletResponse response) {
		model = dramaBillService.exportOrder(recordid, model);
		this.download("xls", "演出订单" , response);
		return "/drama/dramaOrder_xls.vm";
	}
	
	/**
	 * 导出退单
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/exportRefund.xhtml")
	public String exportRefund(Long recordid, ModelMap model, HttpServletResponse response) {
		model = dramaBillService.exportRefund(recordid, model);
		this.download("xls", "演出退单" , response);
		return "/drama/dramaRefund_xls.vm";
	}
	
	/**
	 * 导出线上优惠
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/exportDiscount.xhtml")
	public String exportDiscount(Long recordid, ModelMap model, HttpServletResponse response) {
		model = dramaBillService.exportDiscount(recordid, model);
		this.download("xls", "线上优惠" , response);
		return "/drama/dramaDiscount_xls.vm";
	}
	
	/**
	 * 导出订单条目
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/exportOrderItem.xhtml")
	public String exportOrderItem(Long recordid, String isXls, ModelMap model, HttpServletResponse response) {
		model = dramaBillService.exportOrderItem(recordid, model);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "演出订单条目" , response);
			return "/drama/dramaOrderItem_xls.vm";
		}
		return "/drama/dramaOrderItemPop.vm";
	}
	
	
	/**
	 * 提计成本单据
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryJitiBill.xhtml")
	public String queryJitiBill(DramaJitiBill bill, Integer pageNo, Integer pageSize, String isXls, ModelMap model, HttpServletResponse response) {
		String url = "/platform/drama/queryJitiBill.xhtml";
		model = dramaBillService.queryJitiBill(bill, pageNo, pageSize, url, isXls, model);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "计提单据" , response);
			return "/drama/dramaMonthJitiBill_xls.vm";
		}
		return "/drama/dramaMonthJitiBill.vm";
	}
}
