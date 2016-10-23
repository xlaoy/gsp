package com.gewara.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.model.gsp.SysData;
import com.gewara.service.DaoService;
import com.gewara.service.SysDataService;
import com.gewara.web.util.PageUtil;

@Service("sysDataService")
public class SysDataServiceImpl implements SysDataService {

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("config")
	protected Config config;
	
	@SuppressWarnings("unchecked")
	@Override
	public ModelMap querySysData(SysData data, Integer pageNo, Integer pageSize, String url, ModelMap model) {
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 15 : pageSize;
		
		List<SysData> dataList = new ArrayList<SysData>();
		String type = data.getType();
		String code = data.getCode();
		String name = data.getName();
		
		DetachedCriteria queryCount = DetachedCriteria.forClass(SysData.class);
		DetachedCriteria queryList = DetachedCriteria.forClass(SysData.class);
		
		if(StringUtils.isNotBlank(code)) {
			queryCount.add(Restrictions.eq("code", code));
			queryList.add(Restrictions.eq("code", code));
		}
		if(StringUtils.isNotBlank(name)) {
			queryCount.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
			queryList.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(type)) {
			queryCount.add(Restrictions.eq("type", type));
			queryList.add(Restrictions.eq("type", type));
		}
		
		queryCount.setProjection(Projections.rowCount());
		queryList.addOrder(Order.desc("code"));
		
		int count = Integer.valueOf(daoService.findByCriteria(queryCount).get(0).toString());
		
		if(count > 0) {
			dataList = daoService.findByCriteria(queryList, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + url, true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("type", type);
			m.put("code", code);
			m.put("name", name);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("dataList", dataList);
		
		return model;
		
	}
	
	
	/**
	 * 查询多个SysData
	 * @param codeList
	 * @param type
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object> Map<String, SysData> getSysDataMap(List<T> codeList, String type, Class<T> cls) {
		List<String> list = new ArrayList<String>();
		if("java.lang.String".equals(cls.getName())) {
			list = (List<String>)codeList;
		} else {
			for(T obj : codeList) {
				list.add(obj.toString());
			}
		}
		DetachedCriteria qry = DetachedCriteria.forClass(SysData.class);
		qry.add(Restrictions.eq("type", type));
		qry.add(Restrictions.in("code", list.toArray()));
		List<SysData> dataList = daoService.findByCriteria(qry);
		Map<String, SysData> map = new HashMap<String, SysData>();
		for(SysData data : dataList) {
			map.put(data.getCode(), data);
		}
		return map;
	}
	
	/**
	 * 查询单个SysData
	 * @param code
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object> SysData getSysData(T code, String type, Class<T> clz) {
		String c;
		if("java.lang.String".equals(clz.getName())) {
			c = (String)code;
		} else {
			c = code.toString();
		}
		DetachedCriteria qry = DetachedCriteria.forClass(SysData.class);
		qry.add(Restrictions.eq("type", type));
		qry.add(Restrictions.eq("code", c));
		List<SysData> dataList = daoService.findByCriteria(qry);
		if(CollectionUtils.isNotEmpty(dataList)) {
			return dataList.get(0);
		}
		return null;
	}
	
	/**
	 * 查询多个SysData
	 * @param codeList
	 * @param type
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, SysData> getSysDataMap(String type) {
		DetachedCriteria qry = DetachedCriteria.forClass(SysData.class);
		qry.add(Restrictions.eq("type", type));
		List<SysData> dataList = daoService.findByCriteria(qry);
		Map<String, SysData> map = new HashMap<String, SysData>();
		for(SysData data : dataList) {
			map.put(data.getCode().toString(), data);
		}
		return map;
	}
}
