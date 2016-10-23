/** 
 */
package com.gewara.service;

import com.gewara.model.gsp.StatusTracker;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Feb 24, 2014  10:38:39 AM
 */
public interface StatusTrackerService {
	/**
	 * retrive the last status tracker
	 * @param relateId	
	 * @param from status from 
	 * @param to	status to
	 * @return
	 */
	public StatusTracker getlastStatusTrackerByStatus(Long relateId, String from , String to);
}
