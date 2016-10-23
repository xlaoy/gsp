package com.gewara.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.gsp.DiffPriceBill;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.DiffPriceBillService;


@Service("diffPriceBillServiceImpl")
public class DiffPriceBillServiceImpl implements DiffPriceBillService {

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	/**
	 * 根据结算单id查询对应的异价单详情
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<DiffPriceBill> queryDiffPriceBillDetail(SettlementBill setl) {
		DetachedCriteria query = DetachedCriteria.forClass(DiffPriceBill.class);
		
		if(setl.getStartTime() != null) {
			query.add(Restrictions.ge("start", setl.getStartTime()));
		}
		if(setl.getEndTime() != null) {
			query.add(Restrictions.le("end", setl.getEndTime()));
		}
		
		query.add(Restrictions.eq("settleBillId", setl.getRecordId()));
		return daoService.findByCriteria(query);
	}

}
