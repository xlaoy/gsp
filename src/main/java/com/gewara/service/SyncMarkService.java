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
	 * ��ѯsyncMark û�л�ȡ����һ�� config.initTime ��ʼ�����־ô˶���
	 * @param tag
	 * @return
	 */
	public SyncMark getSyncMark(String tag);
	
}
