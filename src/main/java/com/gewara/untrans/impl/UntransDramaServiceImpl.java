package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.DramaConst;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.service.DaoService;
import com.gewara.service.DramaDoColleService;
import com.gewara.untrans.UntransDramaService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;

@Service("untransDramaServiceImpl")
public class UntransDramaServiceImpl implements UntransDramaService {

	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("dramaDoCollecServiceImpl")
	protected DramaDoColleService collectionService;
	
	/**
	 * 计算单据
	 */
	@Override
	public void calculateSettleBill() {
		logger.warn("开始计算单据");
		
		List<DramaSettleBill> billList = daoService.getObjectListByField(DramaSettleBill.class, "status", DramaConst.NEW);
		if(CollectionUtils.isEmpty(billList)) {
			logger.warn("无单据");
			return;
		}
		
		for(int i = 0; i < billList.size(); i++) {
			billList.get(i).setStatus(DramaConst.FULFILLING);
			billList.get(i).setGspupdatetime(DateUtil.getCurFullTimestamp());
		}
		
		daoService.saveObjectList(billList);
		
		for(DramaSettleBill bill : billList) {
			collectionService.fullSettleBill(bill, true);
		}
		logger.warn("计算单据完成");
	}
	
	/**
	 * 计算计提成本单据
	 * 出单规则：
	 * 	从项目开始时间所在的月份1号算起，每月1号作为单据开始时间
	 *  项目结束大于20号，下个月继续计提，小于20号，到月底。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void calculateJitiBill() {
		logger.warn("开始计算计提成本单据的job");
		
		Timestamp cuttime = MiscUtil.addMonth(DateUtil.getCurFullTimestamp(), -1);
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.eq("canjiti", DramaConst.Y));
		query.add(Restrictions.lt("lastjitiendtime", cuttime));
		List<DramaConfig> configlist = daoService.findByCriteria(query);
		
		for(DramaConfig dconfig : configlist) {
			//生成单据
			Timestamp starttime = dconfig.getLastjitiendtime();
			if(starttime == null) {
				logger.warn("计提单配置" + dconfig.getRecordid() + "的最后计提时间为空！");
				continue;
			}
			Timestamp endtime = DateUtil.getNextMonthFirstDay(starttime);
			
			DramaJitiBill bill = new DramaJitiBill(dconfig.getDramaid(), dconfig.getSuppliercode(),
					starttime, endtime, dconfig.getRecordid());
			
			//计算单据
			String result = collectionService.fullJitiBill(bill);
			
			if(DramaConst.OK.equals(result)) {
				if(endtime.getTime() > dconfig.getEndtime().getTime()) {
					//项目结束时间在20号之前，不用再出单据了
					if(DateUtil.getDay(new Date(dconfig.getEndtime().getTime())) < 20) {
						dconfig.setCanjiti(DramaConst.N);
					} else {
						//项目结束时间大于20号，下个月继续出一次单据
						if(starttime.getTime() > dconfig.getEndtime().getTime()) {
							dconfig.setCanjiti(DramaConst.N);
						}
					}
				}
				dconfig.setLastjitiendtime(endtime);
				daoService.updateObject(dconfig);
			}
		}
		
		logger.warn("计算计提成本单据的job完成");
	}
	
}
