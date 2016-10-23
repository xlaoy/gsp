/** 
 */
package com.gewara.service;

import java.util.List;

import com.gewara.model.gsp.Place;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 12, 2013  4:31:37 PM
 */
public interface PlaceService {
	
	Place getPlaceByFirstSpell(String firstSpell);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinemas  
	 * @Description:加载所有的给定类型场馆
	 * @param placeType
	 * @return List<Place>
	 * @throws
	 */
	List<Place> loadAllCinemas(String placeType);
}
