/** 
 */
package com.gewara.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.gewara.dao.DataExtractionDao;
import com.gewara.enums.DataExtSqlEmums;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.DaoService;
import com.gewara.util.DateUtil;
import com.gewara.util.MiscUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  11:34:50 AM
 */
@Repository("dataExtractionDao")
public class DataExtractionDaoImpl implements DataExtractionDao{
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Override
	public <T> List<T> getDataByTime(DataExtSqlEmums sqlEnums,
			Class<T> returnType, String orderType ,Timestamp time, Timestamp now) {
		String sql = sqlEnums.getSql();
		if(StringUtils.isEmpty(orderType)){
			return shJdbcTemplate.queryForList(sql, returnType, time, now);			
		}else{
			return shJdbcTemplate.queryForList(sql, returnType, orderType,time, now);
		}
	}
	@Override
	public List<Map<String, Object>> getRowsDataByIds(DataExtSqlEmums sqlEnums, Object[] ids){
		String sql = sqlEnums.getSql();
		if (ids!= null)
			sql = sql.replace("{in}", StringUtils.repeat("?,", ids.length -1) + "?");
		return shJdbcTemplate.queryForList(sql, ids);
	}
	@Override
	public List<Map<String, Object>> getRowsDataTime(DataExtSqlEmums sqlEnums, Timestamp start, Timestamp end){
		String sql = sqlEnums.getSql();
		return shJdbcTemplate.queryForList(sql, start, end);
	}
	@Override
	public List<String> queryRecentNotSuccessStatistic(Timestamp start, Timestamp end){
		String sql = " SELECT PLACEID, REFUNDDATE FROM WEBDATA.view_refund_statistic where status <> 'success' and REFUNDDATE <= ? and REFUNDDATE>= ?";
		List<Map<String, Object>> queryForList = shJdbcTemplate.queryForList(sql, new Object[]{end, start});
		
		List<String> allData = new ArrayList<String>();
		for (Map<String, Object> map : queryForList){
			String data = map.get("PLACEID").toString() + "," + DateUtil.formatDate(DateUtil.parseDate(map.get("REFUNDDATE").toString()));
			allData.add(data);
		}
		return allData;
	}
	/* (non-Javadoc)
	 * @see com.gewara.dao.DataExtractionDao#getSHOrderCount(java.lang.String, java.lang.String)
	 */
	@Override
	public Long getSHOrderCount(Timestamp start, Timestamp orderEnd, Timestamp goodsEnd) {
		
		String sqlOrder = "SELECT count(1) " +
				"FROM WEBDATA.view4js_ticket_order o " +
				"WHERE o.ORDER_TYPE = 'ticket' AND o.SETTLE = 'Y' " +
				"AND o.UPDATETIME >= ?  AND o.UPDATETIME < ? AND o.STATUS IN ('paid_success','paid_return')";
		
		
		Long shOrder = shJdbcTemplate.queryForObject(sqlOrder, new Object[]{start,  orderEnd}, Long.class );
		
		String sqlGoods = "SELECT count(1) " +
				"FROM WEBDATA.view4js_ticket_order o " +
				"WHERE o.ORDER_TYPE = 'goods' AND o.CATEGORY = 'goods' " +
				"AND o.UPDATETIME >= ?  AND o.UPDATETIME < ? AND o.STATUS IN ('paid_success','paid_return')";
		Long shGoods = shJdbcTemplate.queryForObject(sqlGoods, new Object[]{start,  goodsEnd},  Long.class);
		
		return shOrder + shGoods;
	}
	/* (non-Javadoc)
	 * @see com.gewara.dao.DataExtractionDao#getGSPOrderCount()
	 */
	@Override
	public Long getGSPOrderCount(Timestamp start, Timestamp orderEnd, Timestamp goodsEnd) {
		String sqlTicket = "select count(1) from gewa_order where order_type = 'TICKET' and update_time >= ? and update_time < ?";
		long gspTicketNumber = jdbcTemplate.queryForObject(sqlTicket, Long.class, start,orderEnd);
		String sqlGoods = "select count(1) from gewa_order where order_type = 'GOODS' and category = 'goods' and update_time >= ? and update_time < ?";
		long gspGoodsNumber = jdbcTemplate.queryForObject(sqlGoods, Long.class,start,goodsEnd);
		return gspTicketNumber + gspGoodsNumber;
	}
	@Override
	public List<Map<String, Object>> getRowsDataTime(DataExtSqlEmums sqlEnums,Timestamp start, Timestamp end ,List<Long> houtaiIds){
		String sql = sqlEnums.getSql();
		if (houtaiIds!= null)
			sql = sql.replace("{in}", StringUtils.repeat("?,", houtaiIds.size() -1) + "?");
		List<Object> params = new ArrayList<Object>();
		params.add(start);
		params.add(end);
		params.addAll(houtaiIds);
		return shJdbcTemplate.queryForList(sql,params.toArray(new Object[params.size()]));
	}
}












