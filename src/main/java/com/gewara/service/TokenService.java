/** 
 */
package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.gsp.Place;
import com.gewara.vo.PlaceVO;

/**
 * ����ϵͳ����token���û���½ʹ��.
 * ��½���裺
 * 1.��ȡ��½token  
 * 2.��½�û�Я��token��ת����ϵͳ  $absPath/login.xhtml
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 13, 2013  10:33:23 AM
 */
public interface TokenService {
	
	/**
	 * ��ȡ��½token�� �û���ת������ϵͳʱ�� ��Я����token������֤��½ 
	 * @param userFlag ��½�û���ʶ(Ψһ��)
	 * @param ip       ��½�û�IP
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
