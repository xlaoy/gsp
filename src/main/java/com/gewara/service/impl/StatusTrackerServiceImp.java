/** 
 */
package com.gewara.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.StatusTrackerService;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Feb 24, 2014  10:41:36 AM
 */
@Service("statusTrackerService")
public class StatusTrackerServiceImp implements StatusTrackerService {
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	/* (non-Javadoc)
	 * @see com.gewara.service.StatusTrackerService#getlastStatusTrackerByStatus(java.lang.Long, java.lang.String, java.lang.String)
	 */
	@Override
	public StatusTracker getlastStatusTrackerByStatus(Long relateId,
			String from, String to) {
		DetachedCriteria query = DetachedCriteria.forClass(StatusTracker.class);
		if (relateId != null)
			query.add(Restrictions.eq("relateId", relateId));
		if (StringUtils.isNotBlank(from))
			query.add(Restrictions.eq("oldStatus", from));
		if (StringUtils.isNotBlank(to))
			query.add(Restrictions.eq("newStatus", to));
		
		query.addOrder(Order.desc("addTime"));
		List findByCriteria = hibernateTemplate.findByCriteria(query, 0, 1);
		return  CollectionUtils.isNotEmpty(findByCriteria) ? 
				(StatusTracker)findByCriteria.get(0) : null;
	}

}
