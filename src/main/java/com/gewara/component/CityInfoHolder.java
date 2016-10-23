/** 
 */
package com.gewara.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.gewara.api.service.common.CommonVoService;
import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.common.GewaCityVo;
import com.gewara.model.gsp.Place;
import com.gewara.service.DaoService;
import com.gewara.util.BeanUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Nov 19, 2013  11:58:10 AM
 */
@Component("cityInfoHolder")
public class CityInfoHolder implements InitializingBean{
	@Autowired
	@Qualifier("commonVoService")
	private CommonVoService commonVoService;
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	private List<GewaCityVo> allCity;
	
	private Map<String, String> proviceMap;
	
	private Map<String, String> cityProviceMap;
	
	private Map<String, String> placeIdCityCodeMap;
	
	private Map<String, String> placeIdNameMap;
	
	
	private Map<String, String> cityMap;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		ResultCode<List<GewaCityVo>> allGewaCity = commonVoService.getAllGewaCityList();
		allCity = allGewaCity.getRetval();
		proviceMap = BeanUtil.getKeyValuePairMap(allCity, "provincecode", "provincename");
		cityProviceMap = BeanUtil.getKeyValuePairMap(allCity, "citycode", "provincecode");
		cityMap = BeanUtil.getKeyValuePairMap(allCity, "citycode", "cityname");
		List<Place> allPlace = daoService.getAllObjects(Place.class);
		placeIdCityCodeMap = BeanUtil.getKeyValuePairMap(allPlace, "recordId", "cityCode");
		placeIdNameMap = BeanUtil.getKeyValuePairMap(allPlace, "recordId", "name");
	}
	
	public Map<String, String> getProviceByPlaceId(List<String> placeIds){
		Map<String, String> filteredProvice = new LinkedHashMap<String, String>();
		for (String pid : placeIds){
			String proviceCode = getProviceCodeByPlaceId(pid);
			filteredProvice.put(proviceCode, proviceMap.get(proviceCode));
		}
		return filteredProvice;
	}

	private String getProviceCodeByPlaceId(String placeId){
		String cityCode = placeIdCityCodeMap.get(placeId);
		return cityProviceMap.get(cityCode);
	}
	/**
	 * @param placeIds
	 * @param proviceCode
	 * @return
	 */
	public List<String> filteredPlaceIdForProvice(List<String> placeIds,
			String proviceCode) {
		List<String> filtered = new ArrayList<String>();
		for (String pid : placeIds){
			String cityCode = placeIdCityCodeMap.get(pid);
			String pCode = cityProviceMap.get(cityCode);
			if (StringUtils.equals(proviceCode, pCode)){
				filtered.add(pid);
			}
		}
		return filtered; 
	}
	
	public Map<String, String> getAllCity(){
		return cityMap;
	}
	
	public Map<String, Map<String, String>> wrarpPlaceId(List<String> placeIds){
		Map<String, Map<String, String>> result = new LinkedHashMap<String, Map<String,String>>();
		for (String pid : placeIds){
			String proviceCode = getProviceCodeByPlaceId(pid);
			Map<String, String> placeMap = result.get(proviceCode) == null ? new LinkedHashMap<String, String>() : result.get(proviceCode);
			
			
			placeMap.put(pid, placeIdNameMap.get(pid));
			result.put(proviceCode, placeMap);
		}
		return result;
	}
	
	public String getCityNameByCode(String cityCode){
		return cityMap.get(cityCode);
	}
}
