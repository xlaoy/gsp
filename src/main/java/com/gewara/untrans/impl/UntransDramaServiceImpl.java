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
	 * ���㵥��
	 */
	@Override
	public void calculateSettleBill() {
		logger.warn("��ʼ���㵥��");
		
		List<DramaSettleBill> billList = daoService.getObjectListByField(DramaSettleBill.class, "status", DramaConst.NEW);
		if(CollectionUtils.isEmpty(billList)) {
			logger.warn("�޵���");
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
		logger.warn("���㵥�����");
	}
	
	/**
	 * �������ɱ�����
	 * ��������
	 * 	����Ŀ��ʼʱ�����ڵ��·�1������ÿ��1����Ϊ���ݿ�ʼʱ��
	 *  ��Ŀ��������20�ţ��¸��¼������ᣬС��20�ţ����µס�
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void calculateJitiBill() {
		logger.warn("��ʼ�������ɱ����ݵ�job");
		
		Timestamp cuttime = MiscUtil.addMonth(DateUtil.getCurFullTimestamp(), -1);
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.eq("canjiti", DramaConst.Y));
		query.add(Restrictions.lt("lastjitiendtime", cuttime));
		List<DramaConfig> configlist = daoService.findByCriteria(query);
		
		for(DramaConfig dconfig : configlist) {
			//���ɵ���
			Timestamp starttime = dconfig.getLastjitiendtime();
			if(starttime == null) {
				logger.warn("���ᵥ����" + dconfig.getRecordid() + "��������ʱ��Ϊ�գ�");
				continue;
			}
			Timestamp endtime = DateUtil.getNextMonthFirstDay(starttime);
			
			DramaJitiBill bill = new DramaJitiBill(dconfig.getDramaid(), dconfig.getSuppliercode(),
					starttime, endtime, dconfig.getRecordid());
			
			//���㵥��
			String result = collectionService.fullJitiBill(bill);
			
			if(DramaConst.OK.equals(result)) {
				if(endtime.getTime() > dconfig.getEndtime().getTime()) {
					//��Ŀ����ʱ����20��֮ǰ�������ٳ�������
					if(DateUtil.getDay(new Date(dconfig.getEndtime().getTime())) < 20) {
						dconfig.setCanjiti(DramaConst.N);
					} else {
						//��Ŀ����ʱ�����20�ţ��¸��¼�����һ�ε���
						if(starttime.getTime() > dconfig.getEndtime().getTime()) {
							dconfig.setCanjiti(DramaConst.N);
						}
					}
				}
				dconfig.setLastjitiendtime(endtime);
				daoService.updateObject(dconfig);
			}
		}
		
		logger.warn("�������ɱ����ݵ�job���");
	}
	
}
