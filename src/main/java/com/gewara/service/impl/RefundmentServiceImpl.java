/** 
 */
package com.gewara.service.impl;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.enums.RefundmentType;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.RefundmentService;
import com.gewara.util.BeanUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 16, 2013  6:02:28 PM
 */
@Service("refundmentService")
public class RefundmentServiceImpl implements RefundmentService{
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Override
	public List<Refundment> queryRefundmentBySettleBill(SettlementBill settlementBill, RefundmentType type){
		
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.eq("settleBillId", settlementBill.getRecordId()));
		query.add(Restrictions.eq("isSettled", "Y"));
		
		if (RefundmentType.REFUND == type){
			query.add(Restrictions.isNotNull("checkBillId"));
		}else {//ÍùÆÚ²îÒì
			query.add(Restrictions.isNull("checkBillId"));
		}
		query.addOrder(Order.desc("refundTime"));
		
		return daoService.findByCriteria(query);
	}

	
	@Override
	public void upgradeR(List<Refundment> re) {
		List<Long> checkBillId = BeanUtil.getBeanPropertyList(re, "checkBillId", true);
		List<CheckBill> checkbills = daoService.getObjectList(CheckBill.class, checkBillId);
		
		Map<Long, Long> beanListToMap = BeanUtil.beanListToMap(checkbills, "recordId", "settlementId", true);
		
		for (Refundment refund : re){
			Long cid = refund.getCheckBillId();
			Long settleId = beanListToMap.get(cid);
			refund.setSettleBillId(settleId);
		}
		daoService.saveObjectList(re);
	}
}
