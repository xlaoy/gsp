package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.BaoChangReqBill;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.service.BaoChangService;
import com.gewara.service.DaoService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;
import com.gewara.web.util.PageUtil;

/**
 * ����
 * @author user
 *
 */

@Controller
public class BaoChangController extends AnnotationController {

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("baoChangServiceImpl")
	private BaoChangService baoChangServiceImpl;
	
	
	@RequestMapping("/platform/baochang/queryBaoChangDetail.xhtml")
	public String queryBaoChangDetail(Long recordid, ModelMap model) {
		if(recordid == null) {
			return "settlement/baochangDetails.vm";
		}
		BaoChang bc = daoService.getObject(BaoChang.class, recordid);
		if(bc == null) {
			return "settlement/baochangDetails.vm";
		}
		Place place = daoService.getObject(Place.class, bc.getPlaceId());
		model.put("bc", bc);
		model.put("place", place);
		return "settlement/baochangDetails.vm";
	}
	
	/**
	 * ���ݲ�ѯ
	 * @param bc
	 * @param pageNo
	 * @param pageSize
	 * @param isXls
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/platform/baochang/queryBaoChang.xhtml")
	public String queryBaoChang(BaoChang bc, Integer pageNo, Integer pageSize, String isXls, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 10 : pageSize;
		
		List<BaoChang> bcList = new ArrayList<BaoChang>();
		Map<String, Place> placeMap = new HashMap<String, Place>();
		
		Long recordId = bc.getRecordId();
		String placeId = bc.getPlaceId();
		String filmName = bc.getFilmName();
		String status = bc.getStatus();
		String prePay = bc.getPrePay();
		Long settleId = bc.getSettleId();
		Long goodsSettleId = bc.getGoodsSettleId();
		Timestamp startTime = bc.getStartTime();
		Timestamp endTime = bc.getEndTime();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(BaoChang.class);
		DetachedCriteria query = DetachedCriteria.forClass(BaoChang.class);
		
		if(recordId != null) {
			queryCount.add(Restrictions.eq("recordId", recordId));
			query.add(Restrictions.eq("recordId", recordId));
		}
		if(StringUtils.isNotEmpty(placeId)) {
			queryCount.add(Restrictions.eq("placeId", placeId));
			query.add(Restrictions.eq("placeId", placeId));
		}
		if(StringUtils.isNotEmpty(filmName)) {
			queryCount.add(Restrictions.like("filmName", filmName, MatchMode.ANYWHERE));
			query.add(Restrictions.like("filmName", filmName,  MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotEmpty(status)) {
			queryCount.add(Restrictions.eq("status", status));
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotEmpty(prePay)) {
			queryCount.add(Restrictions.eq("prePay", prePay));
			query.add(Restrictions.eq("prePay", prePay));
		}
		if(settleId != null) {
			queryCount.add(Restrictions.eq("settleId", settleId));
			query.add(Restrictions.eq("settleId", settleId));
		}
		if(goodsSettleId != null) {
			queryCount.add(Restrictions.eq("goodsSettleId", goodsSettleId));
			query.add(Restrictions.eq("goodsSettleId", goodsSettleId));
		}
		if(startTime != null) {
			queryCount.add(Restrictions.ge("playTime", startTime));
			query.add(Restrictions.ge("playTime", startTime));
		}
		if(endTime != null) {
			queryCount.add(Restrictions.le("playTime", endTime));
			query.add(Restrictions.le("playTime", endTime));
		}
		
		queryCount.setProjection(Projections.rowCount());
		query.addOrder(Order.desc("playTime"));
		
		int count = Integer.valueOf(hibernateTemplate.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			bcList = daoService.findByCriteria(query, pageSize * pageNo, pageSize);
			List<String> placeIds = BeanUtil.getBeanPropertyList(bcList, "placeId", true);
			placeMap = daoService.getObjectMap(Place.class, placeIds);
		}
		
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo,
				config.getBasePath() + "platform/baochang/queryBaoChang.xhtml",
				true, true);
		
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		m.put("filmName", filmName);
		m.put("status", status);
		m.put("startTime", startTime);
		m.put("endTime", endTime);
		m.put("prePay", prePay);
		m.put("settleId", settleId);
		m.put("goodsSettleId", goodsSettleId);
		m.put("recordId", recordId);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("optUser", getLogonUser(request).getUsername());
		model.put("bcList", bcList);
		model.put("placeMap", placeMap);
		
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "�����б�" , response);
			return "/downloadtemplate/baochang.vm";
		}
		
		return "/settlement/baochang.vm";
	}
	
	/**
	 * Ԥ����
	 * @param recordIds
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/baochang/prePay.xhtml")
	public String prePay(String recordIds, String type, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(recordIds)) {
			return showJsonError(model, "��������!");
		}
		
		String optUser = getLogonUser(request).getUsername();
		String[] ids = recordIds.split(",");
		
		for(String id : ids) {
			BaoChang bc = daoService.getObject(BaoChang.class, Long.valueOf(id));
			if(bc == null) {
				return showJsonError(model,"û���ҵ���ؼ�¼");
			}
			if(!SettleConstant.NOPAY.equals(bc.getStatus())) {
				return showJsonError(model, "����id" + id + "��״̬����ȷ��");
			}
			if(bc.getCostAmount() == null || bc.getCostAmount() == 0) {
				return showJsonError(model, "Ԥ������Ϊ0");
			}
			Place p = daoService.getObject(Place.class, bc.getPlaceId());
			if(p == null) {
				return showJsonError(model, "����id" + id + "��ӰԺ�����ڣ�");
			}
			SettleConfig sc = daoService.getObject(SettleConfig.class, bc.getPlaceId());
			if(sc == null) {
				return showJsonError(model, "����id" + id + "��ӰԺ���ò����ڣ�");
			}
			baoChangServiceImpl.prePay(bc, sc, p, optUser, type);
		}
		
		return showJsonSuccess(model, "Ԥ����ɹ���");
	}
	
	/**
	 * ����Ԥ����
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/baochang/rePayBaochang.xhtml")
	public String rePayBaochang(Long recordId, ModelMap model, HttpServletRequest request){
		
		String optUser = getLogonUser(request).getUsername();
		if(!"jacker.cheng".equals(optUser)) {
			return showJsonError(model,"��û��Ȩ�����¸��");
		}
		
		BaoChang bc = daoService.getObject(BaoChang.class, recordId);
		if(null == bc){
			return showJsonError(model,"û���ҵ���ؼ�¼");
		}
		
		if(!SettleConstant.YESPAY.equals(bc.getStatus()) &&  !SettleConstant.SELLET.equals(bc.getStatus())){
			return showJsonError(model, "����id" + recordId + "��״̬����ȷ��");
		}
		
		if(bc.getBcAmount() == null || bc.getBcAmount() == 0) {
			return showJsonError(model, "Ԥ������Ϊ0");
		}
		
		if(!SettleConstant.COMM_Y.equals(bc.getPrePay())) {
			return showJsonError(model, "����Ԥ����ݣ�");
		}
		
		boolean result = baoChangServiceImpl.rePayBaochang(bc, optUser);
		if(result) {
			return showJsonSuccess(model, "���¸���ɹ���");
		} else {
			return showJsonError(model, "����ʧ��!");
		}
		
	}
	
	/**
	 * ���
	 * @param recordIds
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/baochang/bcReqMoney.xhtml")
	public String bcReqMoney(String recordIds, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(recordIds)) {
			return showJsonError(model,"��������");
		}
		String[] strs = recordIds.split(",");
		for(String id : strs) {
			BaoChang bc = daoService.getObject(BaoChang.class, Long.valueOf(id));
			if(bc == null) {
				return showJsonError(model,"����id��" + id + "�ļ�¼������");
			}
			if(SettleConstant.COMM_Y.equals(bc.getReqbillStatus())) {
				return showJsonError(model,"����id��" + id + "��״̬�������");
			}
			Place p = daoService.getObject(Place.class, bc.getPlaceId());
			if(p == null) {
				return showJsonError(model,"����id��" + id + "��ӰԺ������");
			}
			SettleConfig sc = daoService.getObject(SettleConfig.class, bc.getPlaceId());
			if(sc == null) {
				return showJsonError(model,"����id��" + id + "��ӰԺ���ò�����");
			}
			String optUser = getLogonUser(request).getUsername();
			baoChangServiceImpl.bcReqMoney(bc, sc, p, optUser);
		}
		return showJsonSuccess(model, "���ɹ���");
	}
	
	/**
	 * ��ӡ���
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/baochang/printBcReqMoney.xhtml")
	public String printBcReqMoney(String recordId, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isEmpty(recordId)) {
			alertMessage(model, "�����쳣��");
		}
		DetachedCriteria query = DetachedCriteria.forClass(BaoChangReqBill.class);
		query.add(Restrictions.eq("relatedId", recordId));
		List<BaoChangReqBill> rlist = daoService.findByCriteria(query);
		if(rlist.size() != 1) {
			alertMessage(model, "���쳣��");
		}
		BaoChangReqBill bcrb = rlist.get(0);
		User user = getLogonUser(request);
		if(!bcrb.getOptUser().equals(user.getUsername())) {
			alertMessage(model, "�Ǳ������ܲ鿴��ӡ������");
		}
		
		String reqDateStr = DateUtil.format(DateUtil.getCurDate(), "yyyy��MM��dd��");
		String chsFmtMoney = MiscUtil.convertMoneyChineseFmt(bcrb.getAmount());
		model.put("user", user);
		model.put("reqDateStr", reqDateStr);
		model.put("chsFmtMoney", chsFmtMoney);
		model.put("bcrb", bcrb);
		
		return "/printtemplate/showBcReqMoneybill.vm";
	}
}
