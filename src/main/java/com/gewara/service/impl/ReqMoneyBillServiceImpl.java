package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.SettleConstant;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.DaoService;
import com.gewara.service.ReqMoneyBillService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;

@Service("reqMoneyBillService")
public class ReqMoneyBillServiceImpl implements ReqMoneyBillService {
	private final GewaLogger dbLogger = WebLogger.getLogger(getClass());
	private final ReentrantLock lock = new ReentrantLock();
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	
	@Override
	public List<ReqMoneyBill> qryMoneyBill(Timestamp startTime,Timestamp endTime, String vendorName, String placeId,Long userId,Integer pageNo,Integer pageSize,String settleId) {
		DetachedCriteria query = DetachedCriteria.forClass(ReqMoneyBill.class);
		getQueryCondition(startTime, endTime, vendorName, placeId,userId, query,settleId);
		query.addOrder(Order.asc("recordId"));
		return daoService.findByCriteria(query, pageNo * pageSize, pageSize);
	}

	
	@Override
	public Integer countMoneyBill(Timestamp startTime, Timestamp endTime,String vendorName, String placeId,Long userId,String settleId) {
		DetachedCriteria query = DetachedCriteria.forClass(ReqMoneyBill.class);
		getQueryCondition(startTime, endTime, vendorName, placeId,userId, query,settleId);
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.rowCount());
		query.setProjection(projList);
		Integer count = Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
		return count;
	}
	
	
	private void getQueryCondition(Timestamp startTime, Timestamp endTime,String vendorName, String placeId,Long userId, DetachedCriteria query,String settleId) {
		if(startTime != null) {
			query.add(Restrictions.ge("payTime", startTime));
		}
		if(endTime != null) {
			query.add(Restrictions.le("payTime", endTime));
		}
		if(StringUtils.isNotEmpty(settleId)){
			query.add(Restrictions.like("relateSettleId", settleId, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotEmpty(placeId)){
			Place place = daoService.getObject(Place.class, placeId);
			if(place != null) {
				query.add(Restrictions.eq("vendorName", place.getName()));
			} else {
				dbLogger.warn("placeId:" + placeId + "影院为空！");
			}
		}else if(StringUtils.isNotEmpty(vendorName)){
			query.add(Restrictions.like("vendorName", vendorName, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.eq("payUserId", userId));
	}


	@Override
	public void increaseReqMoneyBill(SettlementBill bill,User user) {
		if(null == bill){
			dbLogger.warn("新增请款单时bill对象为空！！");
			return;
		}
		//已请款，不需要在请款操作！
		if(SettleConstant.COMM_Y.equals(bill.getReqMoneyStatus())){
			dbLogger.warn("结算单" + bill.getRecordId() + "已请款，不需要再请款操作！");
			return;
		}
		boolean locked = lock.tryLock();
		try{
			dbLogger.warn("生成请款单！！settlebillid = " + bill.getRecordId());
			String tag = bill.getTag();
			if(SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(tag)){
				ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class,bill.getRelateId());
				String vendorType = csc.getVendorType();
				if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(vendorType)){
					List<VendorCinemaRelation> rls = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", csc.getRecordId());
					if(CollectionUtils.isNotEmpty(rls)){
						VendorCinemaRelation r = rls.get(0);
						insertOrUpdateReqBill(bill,r,user);
					}
				}else{
					insertOrUpdateReqBill(bill,csc,user);
				}
			}else{
				insertOrUpdateReqBill(bill,user);
			}			
			//(todo)将结算单的请款状态置为“已请款”
			bill.setReqMoneyStatus(SettleConstant.COMM_Y);
			daoService.saveObject(bill);
		}finally{
			if(locked){
				lock.unlock();				
			}
		}
	}

	private void insertOrUpdateReqBill(SettlementBill bill,User user) {
		ReqMoneyBill reqBill = null;
		Date currDate = DateUtil.getCurDate();
		DetachedCriteria query = DetachedCriteria.forClass(ReqMoneyBill.class);
		query.add(Restrictions.eq("vendorType", SettleConstant.REQ_BILL_PLACE));
		query.add(Restrictions.eq("vendorId", bill.getRelateId()));
		query.add(Restrictions.eq("payTime",currDate));
		List<ReqMoneyBill> reqBills = daoService.findByCriteria(query);
        reqBill = setupAmount(bill, currDate, reqBills, SettleConstant.REQ_BILL_PLACE,bill.getRelateId());
        reqBill.setPayUserId(user.getId());
		daoService.saveObject(reqBill);
	}


	private void insertOrUpdateReqBill(SettlementBill bill,ChannelSettleConfig csc,User user) {
		ReqMoneyBill reqBill = null;
		Date currDate = DateUtil.getCurDate();
		DetachedCriteria query = DetachedCriteria.forClass(ReqMoneyBill.class);
		query.add(Restrictions.eq("vendorType", SettleConstant.REQ_BILL_SYS_THRE));
		query.add(Restrictions.eq("vendorId", csc.getRecordId()));
		query.add(Restrictions.eq("payTime",currDate));
		List<ReqMoneyBill> reqBills = daoService.findByCriteria(query);
		reqBill = setupAmount(bill, currDate, reqBills,SettleConstant.REQ_BILL_SYS_THRE,csc.getRecordId());
		reqBill.setPayUserId(user.getId());
		daoService.saveObject(reqBill);
	}


	private void insertOrUpdateReqBill(SettlementBill bill,VendorCinemaRelation r,User user) {
		ReqMoneyBill reqBill = null;
		Date currDate = DateUtil.getCurDate();
		Long cinemaId = Long.valueOf(r.getCinemaRecordId().split(",")[0].toString());
		DetachedCriteria query = DetachedCriteria.forClass(ReqMoneyBill.class);
		query.add(Restrictions.eq("vendorType", SettleConstant.REQ_BILL_PLACE));
		query.add(Restrictions.eq("vendorId", cinemaId));
		query.add(Restrictions.eq("payTime",currDate));
		List<ReqMoneyBill> reqBills = daoService.findByCriteria(query);
		reqBill = setupAmount(bill, currDate, reqBills, SettleConstant.REQ_BILL_PLACE ,cinemaId);
		reqBill.setPayUserId(user.getId());
		daoService.saveObject(reqBill);
	}
	
	private ReqMoneyBill setupAmount(SettlementBill bill, Date currDate,List<ReqMoneyBill> reqBills,String vendorType,Long vendorId) {
		ReqMoneyBill reqBill;
		Place place = null;
		ChannelSettleConfig csc = null;
		SettleConfig sc = null;
		if(SettleConstant.REQ_BILL_PLACE.equals(vendorType)){
			place = daoService.getObject(Place.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, vendorId));	
			sc = daoService.getObject(SettleConfig.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, vendorId));
		}else{
			csc = daoService.getObject(ChannelSettleConfig.class,vendorId);
		}
		if(CollectionUtils.isNotEmpty(reqBills)){
			reqBill = reqBills.get(0);
			if(reqBill.getMinTime().after(bill.getStartTime())){
				reqBill.setMinTime(bill.getStartTime());
			}
			if(reqBill.getMaxTime().before(bill.getEndTime())){
				reqBill.setMaxTime(bill.getEndTime());
			}
			reqBill.setRelateSettleId(reqBill.getRelateSettleId() + "," + bill.getRecordId());
		}else{
			reqBill = new ReqMoneyBill();
			reqBill.setMaxTime(bill.getEndTime());
			reqBill.setMinTime(bill.getStartTime());
			reqBill.setPayTime(currDate);
			long placeId = 0l;
			String vendorCode = "";
			if(place != null) {
				placeId = place.getRelateId();
				vendorCode = sc.getVenderNo();
			}
			reqBill.setVendorId(SettleConstant.REQ_BILL_PLACE.equals(vendorType) ?  placeId : csc.getRecordId());
			reqBill.setVendorName(SettleConstant.REQ_BILL_PLACE.equals(vendorType) ? place.getName() : csc.getVendorName());
			reqBill.setVendorCode(SettleConstant.REQ_BILL_PLACE.equals(vendorType) ? vendorCode : csc.getVendorCode());
			reqBill.setVendorType(vendorType);
			reqBill.setRelateSettleId(bill.getRecordId().toString());
		}
		String tag = bill.getTag();
		if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(tag)){
			reqBill.setTicketAmount(reqBill.getTicketAmount() + bill.getOrderTotalAmount());
		}else if(SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(tag)){
			reqBill.setGoodsAmount(reqBill.getGoodsAmount() + bill.getOrderTotalAmount());
		}else{
			reqBill.setChannelAmount(reqBill.getChannelAmount() + bill.getOrderTotalAmount());
		}
		reqBill.setTotalAmount(reqBill.getTotalAmount() + bill.getOrderTotalAmount());
		return reqBill;
	}


	/**
	 *  删除请款单
	 */
	@Override
	public void deleteReqMoneybill(String recordId, User user) {
		dbLogger.warn(user.getUsername() + "删除请款单：" + recordId);
		ReqMoneyBill rmb = daoService.getObject(ReqMoneyBill.class, Long.valueOf(recordId));
		if(rmb == null) {
			return;
		}
		String[] settleIds = rmb.getRelateSettleId().split(",");
		for(String settleId : settleIds) {
			SettlementBill sb = daoService.getObject(SettlementBill.class, Long.valueOf(settleId));
			if(sb != null) {
				sb.setReqMoneyStatus(SettleConstant.COMM_N);
				daoService.saveObject(sb);
				dbLogger.warn("更新结算单：" + sb.getRecordId() + "请款状态");
			}
		}
		daoService.removeObjectById(ReqMoneyBill.class, Long.valueOf(recordId));
	}
}
