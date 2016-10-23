/** 
 */
package com.gewara.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.enums.DataExtSqlEmums;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  11:34:26 AM
 */
public interface DataExtractionDao {
	/**
	 * 
	 * @param sqlEnums
	 * @param returnType
	 * @param time
	 * @return
	 */
	public <T> List<T> getDataByTime(DataExtSqlEmums sqlEnums, Class<T> returnType, String orderType, Timestamp time, Timestamp now);
	
	/**
	 * 
	 * @param sqlEnums
	 * @param ids
	 * @return
	 */
	public List<Map<String, Object>> getRowsDataByIds(DataExtSqlEmums sqlEnums, Object[] ids);

	/**
	 * @param sqlEnums
	 * @param start
	 * @param end
	 * @return
	 */
	List<Map<String, Object>> getRowsDataTime(DataExtSqlEmums sqlEnums,
			Timestamp start, Timestamp end);

	/**
	 * @return
	 */
	List<String> queryRecentNotSuccessStatistic(Timestamp start, Timestamp end);

	/**
	 * @param start yyyy-mm-dd hh24:mi:ss
	 * @param end yyyy-mm-dd hh24:mi:ss
	 * @return
	 */
	public Long getSHOrderCount(Timestamp start, Timestamp orderEnd, Timestamp goodsEnd);

	/**
	 * @return
	 */
	public Long getGSPOrderCount(Timestamp start, Timestamp orderEnd, Timestamp goodsEnd);

	public List<Map<String, Object>> getRowsDataTime(DataExtSqlEmums sqlEnums,Timestamp start, Timestamp end ,List<Long> houtaiIds);


}
