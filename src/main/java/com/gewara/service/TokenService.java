/** 
 */
package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.gsp.Place;
import com.gewara.vo.PlaceVO;

/**
 * 结算系统发送token供用户登陆使用.
 * 登陆步骤：
 * 1.获取登陆token  
 * 2.登陆用户携带token跳转结算系统  $absPath/login.xhtml
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 13, 2013  10:33:23 AM
 */
public interface TokenService {
	
	/**
	 * 获取登陆token， 用户跳转到结算系统时， 需携带此token进行验证登陆 
	 * @param userFlag 登陆用户标识(唯一性)
	 * @param ip       登陆用户IP
	 * @return
	 */
	public String getToken(String userFlag, String ip);

	/**
	 * @param dataJson
	 * @return
	 */
	String getPlaceData(Map<String, Object> dataMap);

	/**
	 * @param allObjects
	 * @return
	 */
	List<PlaceVO> transforToVo(List<Place> allObjects);
}
