/** 
 */
package com.gewara.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.gsp.Place;
import com.gewara.service.DaoService;
import com.gewara.service.PlaceService;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 12, 2013  4:32:28 PM
 */
@Service("placeService")
public class PlaceServiceImpl implements PlaceService{
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Override
	public Place getPlaceByFirstSpell(String firstSpell) {
		if (StringUtils.isBlank(firstSpell))
			return null;
		
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		query.add(Restrictions.like("firstSpell", "%" + firstSpell + "%"));
		List<Place> ps = daoService.findByCriteria(query);
		if (CollectionUtils.isNotEmpty(ps))
			return ps.get(0);
		return null;
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinemas  
	 * @Description:加载所有的给定类型场馆，如影院，体育场馆等
	 * @param placeType
	 * @return List<Place>
	 * @throws
	 */
	@Override
	public List<Place> loadAllCinemas(String placeType) {
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		if(StringUtils.isNotEmpty(placeType)){
			query.add(Restrictions.eq("tag", placeType));
		}
		return daoService.findByCriteria(query);
	}
}
