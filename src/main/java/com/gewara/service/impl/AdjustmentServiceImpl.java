/** 
 */
package com.gewara.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.AdjustmentStatusEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.AdjustmentService;
import com.gewara.service.DaoService;
import com.gewara.service.SettlementBillService;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 21, 2013  1:26:30 PM
 */
@Service("adjustmentService")
public class AdjustmentServiceImpl implements AdjustmentService{
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;

	@Override
	public ResultCode saveAdjust(String tag, String relateId, Double adjustNumber, Double amount, String reason, 
			String comments, String user,String attachFilePath) {
		String configId = relateId;
		Long settleBillId = null;
		if (!AdjustTypeEnums.PLACE.getType().equals(tag)){
			SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(relateId));
			if (bill == null)
				return ResultCode.getFailure("没有找到关联的结算单");
			configId = bill.getConfigId();
			settleBillId = bill.getRecordId();
			String status = bill.getStatus();
			if (!CheckBillStatusEnums.NEW.getStatus().equals(status) 
				&& !CheckBillStatusEnums.READJUST.getStatus().equals(status) 
				&& !CheckBillStatusEnums.GEWAREADJUST.getStatus().equals(status))
				return ResultCode.getFailure("只允许调整：" + CheckBillStatusEnums.NEW.getDisplay() + "、" + CheckBillStatusEnums.READJUST.getDisplay() + " 的结算单," + 
											 "此结算单状态："+ status);
		}
		
		Adjustment adj = new Adjustment(tag, relateId, adjustNumber, amount, reason, comments, user, configId);
		adj.setSettleBillId(settleBillId);
		adj.setAttachFilePath(attachFilePath);
		daoService.saveObject(adj);
		return ResultCode.getSuccess("已调整， 审核通过之后生效");
	}

	@Override
	public List<Adjustment> queryListByTradeNo(List<String> adjustedTradeNo,
			String type) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("tag", type));
		query.add(Restrictions.in("relateId", adjustedTradeNo));
		return daoService.findByCriteria(query);
	}

	@Override
	public int countingUnApprovedAdjustment(String placeId, String reason, String operator) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if (StringUtils.isNotBlank(reason))
			query.add(Restrictions.eq("reason", reason));
		
		if (StringUtils.isNotBlank(placeId))
			query.add(Restrictions.eq("configId", placeId));
		
		if (StringUtils.isNotBlank(operator)){
			Restrictions.like("operator", operator, MatchMode.ANYWHERE);
		}
		
		query.add(Restrictions.eq("isSettled", "N"));
		query.add(Restrictions.eq("status", AdjustmentStatusEnums.NEW.getStatus()));
		query.setProjection(Projections.countDistinct("recordId"));
		Object r = daoService.findByCriteria(query).get(0);
		return Integer.valueOf(r.toString());
		
	}

	@Override
	public List<Adjustment> queryUnApprovedAdjustment(String placeId, String reason, String operator, 
																		Integer pageNo, Integer pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if (StringUtils.isNotBlank(reason))
			query.add(Restrictions.eq("reason", reason));
		
		if (StringUtils.isNotBlank(placeId))
			query.add(Restrictions.eq("configId", placeId));
		
		if (StringUtils.isNotBlank(operator)){
			Restrictions.like("operator", operator, MatchMode.ANYWHERE);
		}
		
		query.add(Restrictions.eq("isSettled", "N"));
		query.add(Restrictions.eq("status", AdjustmentStatusEnums.NEW.getStatus()));
		query.addOrder(Order.desc("addTime"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	@Override
	public ResultCode approvedAdjust(SettlementBill bill, Adjustment ad) {
		if (bill != null){
			settlementBillService.afterAdjust(bill, ad);
			ad.setIsSettled("Y");
			ad.setUpdateTime(DateUtil.getCurFullTimestamp());
		}
		ad.setStatus(AdjustmentStatusEnums.APPROVED.getStatus());
		daoService.saveObject(ad);
		return ResultCode.SUCCESS;
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.AdjustmentService#countingAdjustment(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public int countingAdjustment(String placeId, String reason, String status, User gewaUser) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if (StringUtils.isNotBlank(reason))
			query.add(Restrictions.eq("reason", reason));
		
		if (StringUtils.isNotBlank(placeId))
			query.add(Restrictions.eq("configId", placeId));
		
		if (StringUtils.isNotBlank(status))
			query.add(Restrictions.eq("status", status));
		
//		if (gewaUser != null)
//			query.add(Restrictions.eq("operator", gewaUser.getUsername()));
		
		query.setProjection(Projections.countDistinct("recordId"));
		Object r = daoService.findByCriteria(query).get(0);
		return Integer.valueOf(r.toString());
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.AdjustmentService#queryAdjustment(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<Adjustment> queryAdjustment(String placeId, String reason,
			String status, User gewaUser, Integer pageNo, Integer pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		if (StringUtils.isNotBlank(reason))
			query.add(Restrictions.eq("reason", reason));
		
		if (StringUtils.isNotBlank(placeId))
			query.add(Restrictions.eq("configId", placeId));
		
		if (StringUtils.isNotBlank(status))
			query.add(Restrictions.eq("status", status));
		
//		if (gewaUser != null)
//			query.add(Restrictions.eq("operator", gewaUser.getUsername()));
		
		//调整申请汇总排序，按照状态、结算单号、调整时间
		query.addOrder(Order.desc("addTime"));
		query.addOrder(Order.asc("status"));
		query.addOrder(Order.asc("settleBillId"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}
	@Override
	public List<Adjustment> queryAdsBySettleBill(SettlementBill settleBill){
		if (settleBill == null)
			return new ArrayList<Adjustment>();
		
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("settleBillId", settleBill.getRecordId()));
		query.add(Restrictions.eq("status", AdjustmentStatusEnums.APPROVED.getStatus()));
		return daoService.findByCriteria(query);
	}
}
