/** 
 */
package com.gewara.util;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 21, 2013  10:25:09 AM
 */
public class RecordIdUtils {
	/**
	 * "" + relateId + "," + tag;
	 * @param tag
	 * @param relateId
	 * @return
	 */
	public static String contactRecordId(String tag, Long relateId){
		return "" + relateId + "," + tag;
	}
}
