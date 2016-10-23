package com.gewara.untrans.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.model.common.GewaConfig;
import com.gewara.untrans.CacheConfigure;
import com.gewara.util.JsonUtils;

@Service("gewaConfigService")
public class GewaConfigServiceImpl implements CacheConfigure{
	@Autowired
	@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	@Autowired@Qualifier("config")
	private Config config;
	@Override
	public Map<String, String> getRegionVersion() {
		GewaConfig gewaConfig = hibernateTemplate.get(GewaConfig.class, config.getCacheVersionKey());
		return JsonUtils.readJsonToMap(gewaConfig.getContent());
	}
	@Override
	public Map<String, String> getServiceCachePre() {
		return new HashMap<String, String>();
	}
}