/** 
 */
package com.gewara.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.DramaConst;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CopeBill;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.GewaUserMappingU8;
import com.gewara.model.gsp.MegerPayRecord;
import com.gewara.model.gsp.PayBill;
import com.gewara.model.gsp.PayableBill;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.RDPay;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.support.VelocityTemplate;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Apr 15, 2014  5:55:37 PM
 */
@Controller
public class VouchController extends AnnotationController implements InitializingBean{
	@Autowired
	@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	/**
	 * U8用户名与SSO用户名映射
	 */
//	private static Map<String, String> U8_USER_MAPPING = new HashMap<String, String>(); 
	
	@RequestMapping("/api/yongyou/downLoadPayBill.xhtml")
	@ResponseBody
	public String downLoadYongYou(Long recordId, String key, HttpServletResponse response, ModelMap model, HttpServletRequest request){
		dbLogger.warn("download:" + recordId);
		String yypass = config.getString("yypass");
		String md5Str = StringUtil.md5(recordId + yypass);
		if (!StringUtils.equals(key, md5Str)){
			dbLogger.warn("download sign error:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "sign error";
		}
		
		if (recordId == null){
			dbLogger.warn("download null object:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "null object";
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		query.add(Restrictions.gt("recordId", recordId));
		query.add(Restrictions.ltProperty("downloadCount", "maxDownCount"));
		query.addOrder(Order.asc("recordId"));
		List<DownloadRecorder> result = daoService.findByCriteria(query, 0, 1);
		if (CollectionUtils.isEmpty(result))
			return "null object";
		
		DownloadRecorder dlRecorder = result.get(0);
		dlRecorder.setRequestedCount(dlRecorder.getRequestedCount() == null ? 1 : dlRecorder.getRequestedCount() + 1);
		dlRecorder.setLastDownloadTime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(dlRecorder);
		
		String dBillDate = DateUtil.formatDate(DateUtil.getCurFullTimestamp());
		String mNativeMoney = dlRecorder.getNativeMoney();
		String vendorNo = dlRecorder.getVendorNo();
		String cOperator = getRealNameByLoginName(dlRecorder.getOptUser());
		String cBillID = "";
		String cAppend = "";
		String cRemark = ""; 
		
		if(SettleConstant.BC.equals(dlRecorder.getSpecial())) {//包场预付款
			BaoChang bc = daoService.getObject(BaoChang.class, dlRecorder.getSettlementId());
			if(bc == null) {
				return "null object";
			}
			if(SettleConstant.NOPAY.equals(bc.getStatus())){
				return "status' not right";
			}
			cBillID = "包场" + bc.getRecordId();
			cRemark = "预付" + bc.getPlayId();
			cAppend =  "包场" + DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHH"); 
		} else if(SettleConstant.MEGER.equals(dlRecorder.getSpecial())) {//合并付款
			MegerPayRecord meger = daoService.getObject(MegerPayRecord.class, dlRecorder.getSettlementId());
			if(meger == null) {
				return "null object";
			}
			cBillID = DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHHmmssSSS");
			cRemark = "格瓦拉(合并付)"+meger.getMinSettltId().toString();
			cAppend = "票款" + DateUtil.format(meger.getPayTime(), "yyyyMMddHH"); 
		} else if(SettleConstant.RONGDUAN.equals(dlRecorder.getSpecial())) {//防熔断预付款
			RDPay pay = daoService.getObject(RDPay.class, dlRecorder.getSettlementId());
			if(pay == null) {
				return "null object";
			}
			cBillID = DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHHmmssSSS");
			cRemark = "防熔断付款"+ pay.getRecordId().toString();
			cAppend = "票款" + DateUtil.format(pay.getPayTime(), "yyyyMMddHH"); 
		}  else if(SettleConstant.DRAMA.equals(dlRecorder.getSpecial())) {//演出付款
			DramaSettleBill bill = daoService.getObject(DramaSettleBill.class, dlRecorder.getSettlementId());
			if(bill == null) {
				return "null object";
			}
			if(!DramaConst.SETTLED.equals(bill.getStatus())) {
				return "status' not right";
			}
			cBillID = DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHHmmssSSS");
			cRemark = "演出付款"+ bill.getRecordid();
			cAppend = "票款" + DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHH"); 
		} else {//结算单付款
			SettlementBill bill = daoService.getObject(SettlementBill.class, dlRecorder.getSettlementId());
			if(bill == null) {
				return "null object";
			}
			if (!CheckBillStatusEnums.SETTLED.getStatus().equals(bill.getStatus())){
				return "status' not right";
			}
			cBillID = DateUtil.format(dlRecorder.getAddTime(), "yyyyMMddHHmmssSSS");
			cRemark = "格瓦拉"+bill.getRecordId().toString();
			cAppend = "票款" + DateUtil.format(bill.getStartTime(), "yyyyMMddHH"); 
		}
		
		PayBill payBill = new PayBill(dlRecorder.getRecordId().toString(), cBillID, dBillDate, mNativeMoney, cAppend, vendorNo, cRemark, cOperator);
		model.put("pb", payBill);
		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		String content = velocityTemplate.parseTemplate("payBill.vm", model);
//		download("xml", "paybill_" + bill.getRecordId().toString(), response);
		dlRecorder.setStatus(SettleConstant.YYGET);
		daoService.saveObject(dlRecorder);
		return content;
	}
	
	@RequestMapping("/api/yongyou/confirmSuccess.xhtml")
	@ResponseBody
	public String confirmSuccess(Long recordId, String key, HttpServletRequest request){
		dbLogger.warn("confirm:" + recordId);
		String yypass = config.getString("yypass");
		String md5Str = StringUtil.md5(recordId + yypass);
		if (!StringUtils.equals(key, md5Str)){
			dbLogger.warn("confirmSuccess sign error:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "sign error";
		}
		
		if (recordId == null){
			dbLogger.warn("confirmSuccess null object:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "null object";
		}
		//TODO: check perimession;
		//TODO: validate sign;
		DownloadRecorder dlRecorder = daoService.getObject(DownloadRecorder.class, recordId);
		if (dlRecorder == null){
			dbLogger.warn("confirmSuccess null object:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "null onject";
		}
		dlRecorder.setDownloadCount(dlRecorder.getDownloadCount() + 1);
		dlRecorder.setStatus(SettleConstant.YYCONFIRM);
		daoService.saveObject(dlRecorder);
		return "ok";
	}
	
	
//	@RequestMapping("/api/yongyou/payableBill.xhtml")
	public String downLoadyyPayableBill(Long recordId, HttpServletResponse response, ModelMap model){
		//TODO: check perimession;
		//TODO: validate sign;
		if (recordId == null)
			return showError(model, "null object");
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, recordId);
		if (bill == null)
			return showError(model, "null object");
		
		if (CheckBillStatusEnums.GEWACONFIRMED.getStatus().equals(bill.getStatus())){
			return showError(model, "status is not right");
		}
		
		SettleConfig sc = daoService.getObject(SettleConfig.class, bill.getConfigId());
		String code = StringUtils.leftPad(bill.getRecordId().toString(), 10, '0');
		String amount = bill.getOrderTotalAmount().toString();
		String digest = bill.getRecordId() + DateUtil.formatTimestamp(bill.getStartTime()) + "-" + DateUtil.formatTimestamp(bill.getEndTime());
		String vouchdate = DateUtil.formatDate(bill.getEndTime());
		String quantity = bill.getOrderTotalNumber().toString();
		String vendorNo = sc.getVenderNo();
		
		PayableBill payBill = new PayableBill(sc.getVenderNo(), code, vouchdate, vendorNo, digest, amount, amount, quantity);
		payBill.addEntity(sc.getVenderNo(), vendorNo, digest, amount, amount);
		model.put("pb", payBill);
		download("xml", "paybill_" + bill.getRecordId().toString(), response);
		return "/voucher/payableBill.vm"; 
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
//		U8_USER_MAPPING.put("yueting.tian", "田玥婷");
//		U8_USER_MAPPING.put("ying.qiu", "邱英");
//		U8_USER_MAPPING.put("yuanyuan.kang", "康圆圆");
//		U8_USER_MAPPING.put("jing.zu", "祖静");
//		U8_USER_MAPPING.put("qi.chen", "陈琦");
//		U8_USER_MAPPING.put("jacker.cheng", "程家宁");
//		U8_USER_MAPPING.put("ruomian.liang", "梁若绵");
	}
	
	@RequestMapping("/vouch/yongyou/gewaUserMapU8.xhtml")
	public String gewaUserMapU8(ModelMap model){
		List<GewaUserMappingU8> mappings = daoService.getAllObjects(GewaUserMappingU8.class);
		model.put("mappings", mappings);
		return "voucher/gewaUserMappingU8.vm";
	}
	
	@RequestMapping("/vouch/yongyou/saveGewaUserMapU8.xhtml")
	public String saveGewaUserMapU8(ModelMap model,HttpServletRequest request,String gewaLoginName,String gewaRealName){
		if(StringUtils.isEmpty(gewaLoginName) || StringUtils.isEmpty(gewaRealName)){
			return showJsonError(model, "参数不正确");
		}
		List<GewaUserMappingU8> allMappings = daoService.getAllObjects(GewaUserMappingU8.class);
		if(CollectionUtils.isNotEmpty(allMappings)){
			List<String> loginNames = BeanUtil.getBeanPropertyList(allMappings, "gewaLoginName", false);
			if(loginNames.contains(gewaLoginName)){
				return showJsonError(model, gewaLoginName + "已存在映射配置");
			}
		}
		String userName = getLogonUser(request).getUsername();
		GewaUserMappingU8 u8 = new GewaUserMappingU8(gewaLoginName, gewaRealName, DateUtil.getCurFullTimestamp(), userName, DateUtil.getCurFullTimestamp(), userName);
		daoService.saveObject(u8);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/vouch/yongyou/deleteGewaUserMapU8.xhtml")
	public String deleteGewaUserMapU8(ModelMap model,Long recordId){
		if(recordId == null){
			return showJsonError(model, "参数异常");
		}
		daoService.removeObjectById(GewaUserMappingU8.class, recordId);
		return showJsonSuccess(model);
	}
	
	private String getRealNameByLoginName(String loginName){
		List<GewaUserMappingU8> mappingList = daoService.getObjectListByField(GewaUserMappingU8.class, "gewaLoginName", loginName);
		if(CollectionUtils.isEmpty(mappingList)){
			return "";
		}
		GewaUserMappingU8 u8 = mappingList.get(0);
		return u8.getGewaRealName();
	}
	
	/**
	 * 应付单
	 * @param recordId
	 * @param key
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	//@RequestMapping("/api/yongyou/copeBillInfo.xhtml")
	@ResponseBody
	public String copeBillInfo(Long recordId, String key, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		
		dbLogger.warn("download:" + recordId);
		String yypass = config.getString("yypass");
		String md5Str = StringUtil.md5(recordId + yypass);
		if (!StringUtils.equals(key, md5Str)){
			dbLogger.warn("download sign error:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "sign error";
		}
		
		if (recordId == null){
			dbLogger.warn("download null object:" + recordId + "," + WebUtils.getHeaderStr(request) + "," + WebUtils.getRemoteIp(request));
			return "null object";
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.in("billType", new String[]{BillTypeEnums.PAYABLEBILL.getType(), BillTypeEnums.ESTIMATE.getType()}));
		query.add(Restrictions.gt("recordId", recordId));
		query.add(Restrictions.ge("addTime", DateUtil.parseTimestamp("2015-11-03", "yyyy-MM-dd")));
		query.add(Restrictions.ltProperty("downloadCount", "maxDownCount"));
		query.addOrder(Order.asc("recordId"));
		List<DownloadRecorder> result = daoService.findByCriteria(query, 0, 1);
		if (CollectionUtils.isEmpty(result)) {
			return "null object";
		}
		
		DownloadRecorder dlRecorder = result.get(0);
		SettlementBill bill = daoService.getObject(SettlementBill.class, dlRecorder.getSettlementId());
		
		CopeBill cb = new CopeBill();
		
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag())) { //影票
			Place place = daoService.getObject(Place.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, bill.getRelateId()));
			if(place == null) {
				return "place is null";
			}
			cb.setCdwCode(String.valueOf(bill.getRelateId()));
			cb.setCcode("500101,22020101");
			cb.setCitemCode("01");
			cb.setCdigest("计提" + place.getName() + "[" + DateUtil.formatTimestamp(bill.getStartTime()) + " - " + DateUtil.formatTimestamp(bill.getEndTime()) + "]票款");
		} else if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(bill.getTag())) {//卖品
			Place place = daoService.getObject(Place.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, bill.getRelateId()));
			if(place == null) {
				return "place is null";
			}
			cb.setCdwCode(String.valueOf(bill.getRelateId()));
			cb.setCcode("500105,22020107");
			cb.setCitemCode("01");
			cb.setCdigest("计提" + place.getName() + "[" + DateUtil.formatTimestamp(bill.getStartTime()) + " - " + DateUtil.formatTimestamp(bill.getEndTime()) + "]卖品");
		} else if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(bill.getTag())) {//通道费
			ChannelSettleConfig chsc = daoService.getObject(ChannelSettleConfig.class, bill.getRelateId());
			if(chsc == null) {
				return "ChannelSettleConfig is null";
			}
			if(SettleConstant.CHANNEL_VENDOR_SYS.equals(chsc.getVendorType())) {//系统方
				cb.setCcode("50010201,2202010201");
			} else if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(chsc.getVendorType())) {//院线/管理公司
				cb.setCcode("50010202,2202010202");
			} else if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(chsc.getVendorType())) {//影院
				cb.setCcode("50010203,2202010203");
			}
			cb.setCdwCode(chsc.getVendorCode());
			cb.setCitemCode(chsc.getPjtcode());
			cb.setCdigest("计提" + chsc.getVendorName() + "[" + DateUtil.formatTimestamp(bill.getStartTime()) + " - " + DateUtil.formatTimestamp(bill.getEndTime()) + "]通道费");
		} else {
			return "nuknow SettlementBill tag: " + bill.getTag();
		}
		
		String status = bill.getStatus();
		cb.setRecordId(dlRecorder.getRecordId());
		cb.setCvouchID(String.valueOf(bill.getRecordId()));
		cb.setDvouchDate(DateUtil.addDay(bill.getEndTime(), 2));
		cb.setIamount(bill.getOrderTotalAmount());
		
		//如果是“商户申请调整”或“等待商户确认”，则需要吧当前金额保存作为后期的冲销金额
		if(CheckBillStatusEnums.READJUST.getStatus().equals(status) || CheckBillStatusEnums.GEWACONFIRMED.getStatus().equals(status)) {
			cb.setStatus(BillTypeEnums.ESTIMATE.getType());
			//dlRecorder.setCxAmount(bill.getOrderTotalAmount());
			daoService.updateObject(dlRecorder);
		}
		//如果是“等待付款”或"已付款"，则需要把之前保存的冲销金额拿出来
		if(CheckBillStatusEnums.WAITINGPAY.getStatus().equals(status) || CheckBillStatusEnums.SETTLED.getStatus().equals(status)) {
			cb.setStatus(BillTypeEnums.PAYABLEBILL.getType());
			DetachedCriteria qry = DetachedCriteria.forClass(DownloadRecorder.class);
			qry.add(Restrictions.eq("settlementId", bill.getRecordId()));
			qry.add(Restrictions.eq("billType", BillTypeEnums.ESTIMATE.getType()));
			List<DownloadRecorder> drp = daoService.findByCriteria(qry, 0, 1);
			//cb.setReversalAmount(drp.get(0).getCxAmount());
		}
		
		model.put("cb", cb);
		response.setContentType("text/xml");
		response.setCharacterEncoding("utf-8");
		String content = velocityTemplate.parseTemplate("copeBill.vm", model);
		
		dlRecorder.setRequestedCount(dlRecorder.getRequestedCount() == null ? 1 : dlRecorder.getRequestedCount() + 1);
		dlRecorder.setLastDownloadTime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(dlRecorder);
		
		return content;
	}

}
