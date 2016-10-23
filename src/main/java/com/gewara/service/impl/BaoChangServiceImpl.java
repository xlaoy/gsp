package com.gewara.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.BaoChangReqBill;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.service.BaoChangService;
import com.gewara.service.DaoService;
import com.gewara.service.SettleJitiService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

@Service("baoChangServiceImpl")
public class BaoChangServiceImpl implements BaoChangService {

	private GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("settleJitiServiceImpl")
	private SettleJitiService settleJitiService;
	
	/**
	 * Ԥ����
	 */
	@Override
	public boolean prePay(BaoChang bc, SettleConfig sc, Place p, String optUser, String type) {
		if("online".equals(type)) {
			//����Ԥ����¼
			DownloadRecorder re = new DownloadRecorder(bc.getRecordId(), BillTypeEnums.PAYBILL);
			re.setSpecial(SettleConstant.BC);
			re.setVendorNo(sc.getVenderNo());
			re.setNativeMoney("" + bc.getCostAmount());
			re.setOptUser(optUser);
			re.setStatus(SettleConstant.JSSUBMIT);
			daoService.saveObject(re);
		}
		
		bc.setStatus(SettleConstant.YESPAY);
		bc.setPayTime(DateUtil.getCurFullTimestamp());
		bc.setOptUser(optUser);
		daoService.saveObject(bc);
		
		return true;
	}
	
	
	/**
	 * ����Ԥ����
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean rePayBaochang(BaoChang bc, String optUser) {
		DownloadRecorder dlRecorder = null;
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("settlementId", bc.getRecordId()));
		query.add(Restrictions.eq("special", SettleConstant.BC));
		query.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		List<DownloadRecorder> dls = daoService.findByCriteria(query);
		if (!dls.isEmpty()) {
			dlRecorder = dls.get(0);
			dlRecorder.setMaxDownCount(dlRecorder.getMaxDownCount() + 1);
			dlRecorder.setAddTime(DateUtil.getCurFullTimestamp());
			dlRecorder.setOptUser(optUser);
			dlRecorder.setStatus(SettleConstant.JSSUBMIT);
			daoService.updateObject(dlRecorder);
		} else {
			return false;
		}
		bc.setPayTime(DateUtil.getCurFullTimestamp());
		bc.setOptUser(optUser);
		daoService.saveObject(bc);
		return true;
	}
	
	/**
	 *  ���
	 */
	@Override
	public boolean bcReqMoney(BaoChang bc, SettleConfig sc, Place p,
			String optUser) {
		BaoChangReqBill bcrb = new BaoChangReqBill();
		bcrb.setPjtCode(bc.getPjtCode());
		bcrb.setRelatedId(String.valueOf(bc.getPlayId()));
		bcrb.setPlaceId(p.getRecordId());
		bcrb.setMovieName(bc.getFilmName());
		bcrb.setPjtDesc("��" + p.getName() + "����" + bc.getFilmName() + "����" + bc.getPlayId() + "���İ�����Ŀ");
		bcrb.setVendorNo(sc.getVenderNo());
		bcrb.setVendorName(sc.getVenderName());
		bcrb.setAmount(bc.getBcAmount());
		bcrb.setOptUser(optUser);
		bcrb.setReqTime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(bcrb);
		
		bc.setReqbillStatus(SettleConstant.COMM_Y);
		daoService.saveObject(bc);
		return true;
	}
	
	/**
	 * ��������
	 * �����б����ݵļ������Ϊ ��ӳʱ�� > (��ǰʱ�� - 2��) �ľͽ��а����ĳ�Ʊ��������������ɱ�����Ʊ����ʵļ��㡣
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void bcCalculate() {
		Timestamp timecut = DateUtil.addDay(DateUtil.getCurFullTimestamp(), -2);
		dbLogger.error("�������ݼ��㿪ʼ :" + timecut);
		DetachedCriteria query = DetachedCriteria.forClass(BaoChang.class);
		query.add(Restrictions.isNotNull("playTime"));
		query.add(Restrictions.ge("playTime", timecut));
		query.add(Restrictions.isNotNull("playId"));
		List<BaoChang> bcList = daoService.findByCriteria(query);
		for(BaoChang bc : bcList) {
			Long placeId = Long.valueOf(bc.getPlaceId().split(",")[0]);
			
			DetachedCriteria qry = DetachedCriteria.forClass(GewaOrder.class);
			qry.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
			qry.add(Restrictions.eq("playId", bc.getPlayId()));
			qry.add(Restrictions.eq("relateId", placeId));
			List<GewaOrder> orderList = daoService.findByCriteria(qry);
			
			Integer bcnum = bc.getBcNum() == null ? 0 : bc.getBcNum();
			Integer userNum = 0;
			Double userAmount = 0.00;
			Integer totalNum = 0;
			Double successAmount = 0.00;
			Integer refundNum = 0;
			Double refundAmount = 0.00;
			Double userRate = 0.00;
			Double kpamount = 0.0;
			if(CollectionUtils.isNotEmpty(orderList)) {
				for(GewaOrder o : orderList) {
					totalNum += o.getQuantity();
					successAmount += o.getTotalCost();
					if("gewa_user".equals(o.getUserBaochang()) || "tuhao_user".equals(o.getUserBaochang())) {
						userNum += o.getQuantity();
						userAmount += o.getTotalCost();
					}
					double discount = o.getDiscount() == null ? 0 : o.getDiscount().doubleValue();
					if("paid_success".equals(o.getOrderStatus())) {
						kpamount += discount;
					} else if("paid_return".equals(o.getOrderStatus())) {
						kpamount += o.getTotalCost();
					}
				}
				
				List<String> tradenolist = BeanUtil.getBeanPropertyList(orderList, "tradeno", true);
				List<Refundment> refundlist = daoService.getObjectList(Refundment.class, tradenolist);
				for(Refundment r : refundlist) {
					refundNum += r.getQuantity();
					refundAmount += r.getOldSettle() - r.getNewSettle();
					GewaOrder o = daoService.getObject(GewaOrder.class, r.getTradeno());
					if("gewa_user".equals(o.getUserBaochang()) || "tuhao_user".equals(o.getUserBaochang())) {
						userNum -= r.getQuantity();
						userAmount -= r.getOldSettle() - r.getNewSettle();
					}
				}
				
				if(bcnum != 0) {
					userRate = new BigDecimal((Double.valueOf(userNum) / Double.valueOf(bcnum)) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				}
			}
			bc.setSuccessNum(userNum);
			bc.setUserAmount(userAmount);
			bc.setTotalNum(totalNum);
			bc.setSuccessAmount(successAmount);
			bc.setRefundNum(refundNum);
			bc.setRefundAmount(refundAmount);
			bc.setKpamount(kpamount);
			bc.setSuccessRate(userRate);
			bc.setUpdateTime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(bc);
		}
		dbLogger.error("�������ݼ������ ");
	}
	
	/**
	 * ���ɰ������㵥
	 */
	@Override
	public Map<String, Object> createBcSettleBill(Timestamp start, Timestamp end) {
		dbLogger.error("��ʼ���ɰ������㰴�� " + start + "~" + end);
		String bcsql = "select DISTINCT b.place_id from baochang b where b.play_time >= ? and b.play_time < ? ";
		List<String> placeids = jdbcTemplate.queryForList(bcsql, String.class, start, end);
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<SettlementBill> bcbilllist = new ArrayList<SettlementBill>();
		List<SettlementBill> goodsbilllist = new ArrayList<SettlementBill>();
		for(String placeid : placeids) {
			Long relplaceid = Long.valueOf(placeid.split(",")[0]);
			//ӰƱ���㵥
			SettlementBill bill  = new SettlementBill(start, end, SettleConstant.TAG_SETTLEMENTBILL_TICKET, relplaceid, placeid);
			bill.setPlayType(SettleConstant.BC);
			bcbilllist.add(bill);
			daoService.addObject(bill);
			//��Ʒ���㵥
			SettlementBill gdbill  = new SettlementBill(start, end, SettleConstant.TAG_SETTLEMENTBILL_GOODS, relplaceid, placeid);
			gdbill.setPlayType(SettleConstant.BC);
			goodsbilllist.add(gdbill);
			daoService.addObject(gdbill);
		}
		
		map.put("bcbilllist", bcbilllist);
		map.put("goodsbilllist", goodsbilllist);
		dbLogger.error("�������㵥���ɽ����� ");
		return map;
	}
	
	/**
	 * ���������㵥
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void fullBcSettleBill(SettlementBill bill) {
		if(bill == null) {
			dbLogger.error("�������㵥Ϊ�գ� ");
			return;
		}
		if(!SettleConstant.BC.equals(bill.getPlayType()) && !SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(bill.getTag())) {
			dbLogger.error("���ţ�" + bill.getRecordId() + "���ǰ������㵥�� ");
			return;
		}
		DetachedCriteria query = DetachedCriteria.forClass(BaoChang.class);
		query.add(Restrictions.eq("placeId", bill.getConfigId()));
		query.add(Restrictions.ge("playTime", bill.getStartTime()));
		query.add(Restrictions.lt("playTime", bill.getEndTime()));
		List<BaoChang> bclist = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(bclist)) {
			dbLogger.error("���ţ�" + bill.getRecordId() + "û�а����б����ݣ� ");
			return;
		}
		
		Long num = 0l;
		Double amount = 0.0;
		Double prepaynum = 0.0;
		Double prepayamount = 0.0;
		Double buchaamount = 0.0;
		Double kpamount = 0.0;
		for(BaoChang bc : bclist) {
			num += bc.getBcNum();
			amount += bc.getBcAmount();
			if(SettleConstant.YESPAY.equals(bc.getStatus())) {
				prepaynum += bc.getBcNum();
				prepayamount += bc.getBcAmount();
			}
			
			buchaamount += bc.getBuchaAmount();
			
			kpamount += bc.getKpamount();
			
			bc.setSettleId(bill.getRecordId());
			bc.setUpdateTime(DateUtil.getCurFullTimestamp());
			if(SettleConstant.NOPAY.equals(bc.getStatus())) {
				bc.setStatus(SettleConstant.SELLET);
			}
			daoService.updateObject(bc);
		}
		bill.setSuccTicketNumber(Double.valueOf(num));
		bill.setSuccTicketAmount(amount);
		bill.setOrderTotalNumber(Double.valueOf(num));
		bill.setOrderTotalAmount(amount - prepayamount);
		
		bill.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		
		daoService.updateObject(bill);
		SettlementBillExtend billextend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(billextend == null) {
			billextend = new SettlementBillExtend(bill.getRecordId());
			billextend.setBcPrePay(prepayamount);
			billextend.setBcBucha(buchaamount);
			billextend.setBillingamount(kpamount + buchaamount);
			daoService.saveObject(billextend);
		}
		settleJitiService.updateJiti(bill);
	}
	
	/**
	 * ���������Ʒ���㵥
	 */
	@Override
	public void fullBcGoodsSettleBill(SettlementBill bill) {
		if(bill == null) {
			dbLogger.error("��Ʒ�������㵥Ϊ�գ� ");
			return;
		}
		if(!SettleConstant.BC.equals(bill.getPlayType()) && !SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(bill.getTag())) {
			dbLogger.error("���ţ�" + bill.getRecordId() + "������Ʒ�������㵥�� ");
			return;
		}
		DetachedCriteria query = DetachedCriteria.forClass(BaoChang.class);
		query.add(Restrictions.eq("placeId", bill.getConfigId()));
		query.add(Restrictions.ge("playTime", bill.getStartTime()));
		query.add(Restrictions.lt("playTime", bill.getEndTime()));
		List<BaoChang> bclist = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(bclist)) {
			dbLogger.error("���ţ�" + bill.getRecordId() + "û�а����б����ݣ� ");
			return;
		}
		
		Long num = 0l;
		Double amount = 0.0;
		Double prepaynum = 0.0;
		Double prepayamount = 0.0;
		for(BaoChang bc : bclist) {
			num += bc.getGoodsNum();
			amount += bc.getGoodsAmount();
			if(SettleConstant.YESPAY.equals(bc.getStatus())) {
				prepaynum += bc.getGoodsNum();
				prepayamount += bc.getGoodsAmount();
			}
			
			bc.setGoodsSettleId(bill.getRecordId());
			bc.setUpdateTime(DateUtil.getCurFullTimestamp());
			if(SettleConstant.NOPAY.equals(bc.getStatus())) {
				bc.setStatus(SettleConstant.SELLET);
			}
			daoService.updateObject(bc);
		}
		bill.setSuccTicketNumber(Double.valueOf(num));
		bill.setSuccTicketAmount(amount);
		bill.setOrderTotalNumber(Double.valueOf(num));
		bill.setOrderTotalAmount(amount - prepayamount);
		
		if(amount <= 0) {
			bill.setStatus(CheckBillStatusEnums.INVALID.getStatus());
		} else {
			bill.setStatus(CheckBillStatusEnums.GEWACONFIRMED.getStatus());
		}
		
		daoService.updateObject(bill);
		SettlementBillExtend billextend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
		if(billextend == null) {
			billextend = new SettlementBillExtend(bill.getRecordId());
			billextend.setBcPrePay(prepayamount);
			daoService.saveObject(billextend);
		}
	}
}
