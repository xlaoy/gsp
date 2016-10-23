package com.gewara.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.enums.BillTypeEnums;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.DownloadRecorderService;

@Service("downloadRecorderService")
public class DownloadRecorderServiceImpl implements DownloadRecorderService {
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Override
	public DownloadRecorder getRecorderBySettleBill(SettlementBill bill) {
		DetachedCriteria query = DetachedCriteria
				.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("settlementId", bill.getRecordId()));
		query.add(Restrictions.eq("billType", BillTypeEnums.PAYBILL.getType()));
		DownloadRecorder dlRecorder = null;
		List<DownloadRecorder> dls = daoService.findByCriteria(query);
		if (!dls.isEmpty()) {
			dlRecorder = dls.get(0);
		}
		return dlRecorder;
	}

}
