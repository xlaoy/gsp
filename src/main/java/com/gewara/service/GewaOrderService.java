/** 
 */
package com.gewara.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 13, 2013  11:47:15 AM
 */
public interface GewaOrderService {

	/**
	 * @param tag
	 * @param relateId
	 * @param isSettle
	 * @param start
	 * @param end
	 * @param tradeNo
	 * @return
	 */
	Object[] countingAndSummingGewaOrder(SettleConfig sc,Long playId, String tag, Long relateId, String isSettle,
			Timestamp start, Timestamp end, String timeField, String tradeNo);

	/**
	 * @param tag
	 * @param relateId
	 * @param isSettle
	 * @param start
	 * @param end
	 * @param tradeNo
	 * @return
	 */
	List<GewaOrder> queryGewaOrder(SettleConfig sc, Long playId, String tag, Long relateId, String isSettle,
			Timestamp start, Timestamp end, String timeField, String tradeNo, Integer pageNo, Integer pageSize);

	/**
	 * @param placeId
	 * @param start
	 * @param end
	 * @return
	 */
	int countingPlayItem(String tag, Long relateId, Timestamp start, Timestamp end);

	/**
	 * @param placeId
	 * @param start
	 * @param end
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	Map<Long, Map<String, Object>> queryPlayItemAggre(String tag, Long relateId,
			Timestamp start, Timestamp end, SettleConfig settleConfig,  List<Long> playItems);
	
	/**
	 * @param start
	 * @param end
	 * @param placeId
	 * @param placeType
	 * @param settleConfig
	 * @return
	 */
	List<GewaOrder> queryGewaOrderByPlace(Timestamp start, Timestamp end,
			Long placeId, String placeType, SettleConfig settleConfig, String category);

	/**
	 * @param tag
	 * @param relateId
	 * @param start
	 * @param end
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<Long> queryPlayItemIds(String tag, Long relateId,
			Timestamp start, Timestamp end, SettleConfig settleConfig);

	/**
	 * @param playItems
	 * @return
	 */
	Map<Long, GewaOrder> queryPlayIdOrderMap(Collection<Long> playItems);

	/**
	 * @param noArray
	 * @return
	 */
	Object[] countingAndSummingGewaOrder(String[] noArray);

	/**
	 * @param noArray
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<GewaOrder> queryGewaOrder(String[] noArray, Integer pageNo,
			Integer pageSize);

	/**
	 * @param bill
	 * @return
	 */
	Object[] countAndSumOrderBySettleBill(SettlementBill bill);

	/**
	 * @param bill
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<GewaOrder> queryGewaOrderBySettleBill(SettlementBill bill,
			Integer pageNo, Integer pageSize);

	List<GewaOrder> queryGewaOrderByChannelConfig(Timestamp start,
			Timestamp end, Long relatId, String tag,
			ChannelSettleConfig channelSettleConfig);
	
	List<GewaOrder> queryGewaOrderByChannelConfigExt(Timestamp start,
			Timestamp end, Long placeId, String tag);
	
	List<Refundment> queryRefundmentByChannelConfigExt(Timestamp start,
			Timestamp end, Long placeId, String tag);

	/**
	 * 查询卖品订单
	 */
	List<GewaOrder> queryGoodsOrderByPlace(Timestamp start, Timestamp end,
			Long placeId, String placeType, String category);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: repairGewaOrderOuterId  
	 * @Description:根据category修复历史订单中的对账码
	 * @param @param category
	 * @return void
	 * @throws
	 */
	void repairGewaOrderOuterId(String category, Timestamp time);
}
