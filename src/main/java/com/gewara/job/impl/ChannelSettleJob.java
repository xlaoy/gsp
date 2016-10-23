package com.gewara.job.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.job.JobService;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.ChannelSettleService;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.DateUtil;

public class ChannelSettleJob extends JobService{
	@Autowired
	@Qualifier("channelSettleService")
	private ChannelSettleService channelSettleService;
	
	@Autowired 
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	
	
	/**
	 * 
	 * @auth yujun.su@gewara.com
	 * @param
	 */
	public void generateChannelSettleBill(){
		dbLogger.warn("generate channel settle bill job start....");
		String tag = SettleConstant.TAG_SETTLEMENTBILL_CHANNEL;
		DetachedCriteria query = DetachedCriteria.forClass(ChannelSettleConfig.class);
		query.add(Restrictions.eq("status", SettleConstant.COMM_Y));
		query.add(Restrictions.eq("verifyStatus", SettleConstant.COMM_Y));
		List<ChannelSettleConfig> configs = daoService.findByCriteria(query);
		for(int i =0 ; i < 10 ; i++){
			channelSettleService.generateChannelSettleBill(configs,tag);			
		}
		dbLogger.warn("generate channel settle bill job end....");
	}
	
	
	public void doChannelSettleFilling(){
		dbLogger.warn("fulfill channel settle bill job start....");
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("status", CheckBillStatusEnums.NEW.getStatus()));
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_CHANNEL));
		List<SettlementBill> settlementBills = daoService.findByCriteria(query);
		
		Timestamp now = DateUtil.getCurFullTimestamp();
		for(SettlementBill bill : settlementBills) {
			int diff = DateUtil.getDiffDay(now, bill.getEndTime());
			if(diff < 2){
				continue;
			}
			if(!checkCheckbillCount(bill)) {
				continue;
			}
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.updateObject(bill);
			channelSettleService.autoToMerchant(bill);
		}
		
		dbLogger.warn("fulfill channel settle bill job end....");
	}
	
	/**
	 * 检验逻辑：通道配置下面对应的影院的个数要和对账单生成的基本数据条数保持相等
	 * @param bill
	 * @return
	 */
	private boolean checkCheckbillCount(SettlementBill bill){
		ChannelSettleConfig csc = daoService.getObject(ChannelSettleConfig.class, bill.getRelateId());
		if(csc == null) {
			dbLogger.warn("通道结算配置不存在->recordId：" + bill.getRelateId());
			return false;
		}
		if(!SettleConstant.COMM_Y.equals(csc.getVerifyStatus()) || !SettleConstant.COMM_Y.equals(csc.getStatus())) {
			dbLogger.warn("通道结算配置还未通过审核->recordId：" + csc.getRecordId());
			return false;
		}
		if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(csc.getVendorType())){
			List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
			if(CollectionUtils.isEmpty(checkBills)) {
				checkBills = checkBillService.createPlaceCheckBillBySettleBill(bill);
			}
			int checkSize = checkBills.size();
			List<VendorCinemaRelation> rls = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", csc.getRecordId());
			if (checkSize != rls.size()){
				String content = "通道费对账单数量异常， 结算单单号：" + bill.getRecordId() + ",check size:" + checkSize + "-->rls.size:" + rls.size();
				dbLogger.error(content);
				GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
				return false;
			}
			return true;
		} 
		return true;
	}
}
