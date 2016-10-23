package com.gewara.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.acl.User;
import com.gewara.model.gsp.RDBuckle;
import com.gewara.model.gsp.RDDayCount;
import com.gewara.model.gsp.RDPay;
import com.gewara.model.gsp.RDPrevent;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.service.RongDuanPreventService;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;

/**
 * 
 * @author user
 *
 */
@Controller
public class RongDuanPreventController extends AnnotationController {

	@Autowired
	@Qualifier("rongDuanPreventService")
	private RongDuanPreventService rongDuanPreventService;
	
	/**
	 * 熔断汇总查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/queryRongduanHuizong.xhtml")
	public String queryRongduanHuizong(String vendorName, Integer pageNo, Integer pageSize, ModelMap model) {
		String url = "/platform/rongduan/queryRongduanHuizong.xhtml";
		model = rongDuanPreventService.queryRongduanHuizong(vendorName, pageNo, pageSize, url, model);
		return "/rongduanprevent/rongduanHuizong.vm";
	}
	
	/**
	 * 每天系统订单金额统计
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/queryRongdunDayCount.xhtml")
	public String queryRongdunDayCount(RDDayCount dayc, ModelMap model) {
		model = rongDuanPreventService.queryRongdunDayCount(dayc, model);
		return "/rongduanprevent/rddayCount.vm";
	}
	
	/**
	 * 预付详情
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/queryRongdunPayRecord.xhtml")
	public String queryRongdunPayRecord(RDPay pay, Integer pageNo, Integer pageSize, ModelMap model) {
		String url = "/platform/rongduan/queryRongdunPayRecord.xhtml";
		model = rongDuanPreventService.queryRongdunPayRecord(pay, pageNo, pageSize, url, model);
		return "/rongduanprevent/rdPayRecord.vm";
	}
	
	/**
	 * 扣款详情
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/queryRongdunBuckleRecord.xhtml")
	public String queryRongdunBuckleRecord(RDBuckle buckle, Integer pageNo, Integer pageSize, String isXls, ModelMap model, HttpServletResponse response) {
		String url = "/platform/rongduan/queryRongdunBuckleRecord.xhtml";
		model = rongDuanPreventService.queryRongdunBuckleRecord(buckle, pageNo, pageSize, url, isXls, model);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "扣款单" , response);
			return "/rongduanprevent/rdBuckleRecord_xls.vm";
		}
		return "/rongduanprevent/rdBuckleRecord.vm";
	}
	
	/**
	 * 查询系统详情
	 * @param sysCode
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/queryRDPreventDetail.xhtml")
	public String queryRDPreventDetail(String vendorCode, String opt, ModelMap model) {
		RDPrevent rdp = daoService.getObject(RDPrevent.class, vendorCode);
		model.put("rdp", rdp);
		if("pay".equals(opt)) {
			return "/rongduanprevent/payPop.vm";
		} else if("waringmfy".equals(opt)) {
			return "/rongduanprevent/waringMdyPop.vm";
		} else {
			return "";
		}
		
	}
	
	/**
	 * 预付款
	 * @param sysCode
	 * @param amount
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/rongdunPay.xhtml")
	public String rongdunPay(RDPay pay, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(pay.getVendorCode())){
			return showJsonError(model, "供应商编码不存在！");
		}
		if(pay.getPayAmount() == null){
			return showJsonError(model, "预付金额不存在！");
		}
		if(pay.getPayAmount().doubleValue() <= 0){
			return showJsonError(model, "预付金额不能小于0！");
		}
		RDPrevent rdp = daoService.getObject(RDPrevent.class, pay.getVendorCode());
		if(rdp == null) {
			return showJsonError(model, "系统记录不存在！");
		}
		String optUser = getLogonUser(request).getUsername();
		String result = rongDuanPreventService.rongdunPay(rdp, pay, optUser);
		if("jobrunning".equals(result)) {
			return showJsonError(model, "系统正在进行熔断金额统计，不能预付款，请稍后再付！");
		}
		return showJsonSuccess(model, "预付款成功！");
	}
	
	/**
	 * 修改预警金额
	 * @param vendorCode
	 * @param amount
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/waringMfy.xhtml")
	public String waringMfy(String vendorCode, Double amount, ModelMap model) {
		if(StringUtils.isEmpty(vendorCode)){
			return showJsonError(model, "供应商编码不存在！");
		}
		if(amount == null){
			return showJsonError(model, "预警金额不存在！");
		}
		if(amount.doubleValue() <= 0){
			return showJsonError(model, "预警金额不能小于0！");
		}
		RDPrevent rdp = daoService.getObject(RDPrevent.class, vendorCode);
		if(rdp == null) {
			return showJsonError(model, "系统记录不存在！");
		}
		String result = rongDuanPreventService.waringMfy(rdp, amount);
		if("jobrunning".equals(result)) {
			return showJsonError(model, "系统正在进行熔断金额统计，不能修改预付款，请稍后再付！");
		}
		return showJsonSuccess(model, "修改成功！");
	}
	
	/**
	 * 请款
	 * @param recordId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/rongduan/reqMoney.xhtml")
	public String reqMoney(Long recordId, ModelMap model, HttpServletRequest request) {
		if(recordId == null){
			return showJsonError(model, "参数错误！");
		}
		RDPay pay = daoService.getObject(RDPay.class, recordId);
		if(pay == null){
			return showJsonError(model, "付款记录不存在！");
		}
		RDPrevent rdp = daoService.getObject(RDPrevent.class, pay.getVendorCode());
		if(rdp == null){
			return showJsonError(model, "系统记录不存在！");
		}
		Long userId = getLogonUser(request).getId();
		rongDuanPreventService.reqMoney(pay, rdp, userId);
		return showJsonSuccess(model, "请款成功！");
	}
	
	/**
	 * 打印请款
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/rongduan/printReqMoney.xhtml")
	public String printReqMoney(Long recordId, ModelMap model, HttpServletRequest request) {
		if(recordId == null){
			model.put("errorMsgs", "参数错误！");
			return "showResult.vm";
		}
		ReqMoneyBill reqmonry = daoService.getObject(ReqMoneyBill.class, recordId);
		if(reqmonry == null){
			model.put("errorMsgs", "请款记录不存在！");
			return "showResult.vm";
		}
		User user = getLogonUser(request);
		if(user.getId().longValue() != reqmonry.getPayUserId().longValue()) {
			model.put("errorMsgs", "不是本人请款无法打印！");
			return "showResult.vm";
		}
		RDPay pay = daoService.getObject(RDPay.class, Long.valueOf(reqmonry.getRelateSettleId()));
		if(pay == null) {
			model.put("errorMsgs", "付款记录不存在！");
			return "showResult.vm";
		}
		RDPrevent rdp = daoService.getObject(RDPrevent.class, pay.getVendorCode());
		if(rdp == null) {
			model.put("errorMsgs", "系统记录不存在！");
			return "showResult.vm";
		}
		model.put("bill", reqmonry);
		model.put("reqDateStr", DateUtil.format(DateUtil.getCurDate(), "yyyy年MM月dd日"));
		model.put("user",user);
		model.put("chsFmtMoney", MiscUtil.convertMoneyChineseFmt(reqmonry.getTicketAmount()));
		model.put("receiveDept", rdp.getVendorFullName());
		return "/rongduanprevent/reqMoneybill.vm";
	}
}
