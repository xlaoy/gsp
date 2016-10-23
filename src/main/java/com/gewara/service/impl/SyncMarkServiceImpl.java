/** 
 */
package com.gewara.service.impl;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.DaoService;
import com.gewara.service.SyncMarkService;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  10:46:26 AM
 */
@Service("syncMarkService")
public class SyncMarkServiceImpl implements SyncMarkService{
	@Autowired
	@Qualifier("config")
	private Config config;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Override
	public SyncMark getSyncMark(String tag) {
		SyncMark mark = daoService.getObject(SyncMark.class, tag);
		if (mark == null){
			synchronized (this.getClass()) {
				Timestamp initTime = DateUtil.parseTimestamp(config.getString("sysInitTime"));
				mark = new SyncMark(tag, initTime);
				daoService.saveObject(mark);
			}
		}
		return mark;
	}
}
