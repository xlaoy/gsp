/** 
 */
package com.gewara.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.UserLoginConstant;
import com.gewara.model.gsp.Place;
import com.gewara.service.DaoService;
import com.gewara.service.TokenService;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.vo.PlaceVO;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 13, 2013  10:53:26 AM
 */
@Service("tokenService")
public class TokenServiceImpl implements TokenService {
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	/* (non-Javadoc)
	 * @see com.gewara.dubboservice.token.TokenService#getToken(java.lang.String, java.lang.String)
	 */
	@Override
	public String getToken(String userFlag, String ip) {
		if (StringUtils.isBlank(userFlag) || StringUtils.isBlank(ip))
			return "";
		
		String token = StringUtil.md5(UserLoginConstant.prefix + userFlag + ip + System.currentTimeMillis() + "EoF@");
		return token;
	}
	
	@Override
	public String getPlaceData(Map<String, Object> dataMap){
		
		String placeIds = (String) dataMap.get("cinemaid");
		List<String> tempIds = Arrays.asList(placeIds.split(","));
		List<Long> ids = new ArrayList<Long>();
		for (String str : tempIds) ids.add(Long.valueOf(str));
		List<Place> places = findCinemaPlace(ids);
		List<PlaceVO> placeData = transforToVo(places);
		return JsonUtils.writeObjectToJson(placeData);
	}
	/**
	 * @param places
	 * @return
	 */
	@Override
	public List<PlaceVO> transforToVo(List<Place> allObjects) {
		List<PlaceVO> vos = new ArrayList<PlaceVO>();
		PlaceVO v = null;
		for (Place p : allObjects){
			v = new PlaceVO(p.getRecordId(), p.getName(), p.getFirstSpell());
			vos.add(v);
		}
		return vos;
	}
	/**
	 * @param ids
	 * @return
	 */
	private List<Place> findCinemaPlace(List<Long> ids) {
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		query.add(Restrictions.eq("tag", "TICKET"));
		query.add(Restrictions.in("relateId", ids));
		return daoService.findByCriteria(query);
	}
}
