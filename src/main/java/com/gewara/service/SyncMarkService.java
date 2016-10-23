/** 
 */
package com.gewara.service;

import com.gewara.model.gsp.SyncMark;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  10:44:46 AM
 */
public interface SyncMarkService {
	
	/**
	 * 查询syncMark 没有获取到则一句 config.initTime 初始化并持久此对账
	 * @param tag
	 * @return
	 */
	public SyncMark getSyncMark(String tag);
	
}
