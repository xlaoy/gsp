package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.SettlementBill;

/**
 * 
 * @ClassName: GoodsSettleService  
 * @Description: 卖品结算业务逻辑处理类 
 * @author yujun.su@gewara.com
 * @date 2014-9-26 下午1:32:59
 */
public interface GoodsSettleService {
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: saveGoodsSettleCheckBill  
	 * @Description:新增卖品结算对账单
	 * @param checkBill
	 * @return void
	 * @throws
	 */
	//void saveGoodsSettleCheckBill(CheckBill checkBill);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: buildGoodsSettleCheckBillPerDay  
	 * @Description:每天生成对账单记录，不包含对账单数据
	 * @param 
	 * @return void
	 * @throws
	 */
	//void buildGoodsSettleCheckBillPerDay(List<SettleConfig> configs, String tag);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBill  
	 * @Description:查询卖品对账单数据
	 * @param  model
	 * @param  cinemaId
	 * @param  startTime
	 * @param  endTime
	 * @return ModelMap
	 * @throws
	 */
	ModelMap queryGoodsCheckBill(ModelMap model, String cinemaId,
			Timestamp startTime, Timestamp endTime,Integer pageNo,
			Integer pageSize);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsOrderDetails  
	 * @Description:查询卖品订单明细
	 * @param model
	 * @param billId
	 * @param from
	 * @param pageNo
	 * @param pageSize
	 * @return ModelMap
	 * @throws
	 */
	ModelMap queryGoodsOrderDetails(ModelMap model, Long billId, String from,
			Integer pageNo, Integer pageSize, String orderNo, String placeId,
			Timestamp startTime, Timestamp endTime);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsSettleBill  
	 * @Description:
	 * @param model
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @param pageNo
	 * @param pageSize
	 * @return ModelMap
	 * @throws
	 */
	ModelMap queryGoodsSettleBill(ModelMap model, String placeId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantGoodsSettleBills  
	 * @Description:卖品结算单商户端数据加载
	 * @param proviceCode
	 * @param placeId
	 * @param placeIds
	 * @param allowPlaceIds
	 * @param start
	 * @param end
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @param model
	 * @param isXls
	 * @param request
	 * @param response
	 * @return ModelMap
	 * @throws
	 */
	ModelMap loadMerchantGoodsSettleBills(String proviceCode,
			String placeId,List<String> placeIds,List<String> allowPlaceIds, Timestamp start, Timestamp end, String status,
			Integer pageNo, Integer pageSize, ModelMap model, String isXls,
			HttpServletRequest request, HttpServletResponse response);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantOrderDetails  
	 * @Description:卖品结算商户端结算单明细查询
	 * @param model
	 * @param billId
	 * @param from
	 * @param pageNo
	 * @param pageSize
	 * @param orderNo
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @return ModelMap
	 * @throws
	 */
	ModelMap loadMerchantOrderDetails(ModelMap model, Long billId, String from,
			Integer pageNo, Integer pageSize, String orderNo, String placeId,
			Timestamp startTime, Timestamp endTime);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: generateGoodsSettleBill  
	 * @Description:生成卖品结算单，空
	 * @param 
	 * @return void
	 * @throws
	 */
	void generateGoodsSettleBill();

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doGoodsSettleFilling  
	 * @Description:提交对账单与结算单
	 * @param 
	 * @return void
	 * @throws
	 */
	//void doGoodsSettleFilling();

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryCheckBillBySettleId  
	 * @Description:
	 * @param model
	 * @param settleId
	 * @param pageNo
	 * @param pageSize
	 * @param @return
	 * @return ModelMap
	 * @throws
	 */
	ModelMap queryCheckBillBySettleId(ModelMap model, Long settleId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Description:条件查询卖品支付成功但是未打票的卖品订单
	 * @param model
	 * @param start
	 * @param end
	 * @param cinemaId
	 * @param pageNo
	 * @param pageSize
	 * @param @return
	 * @return String
	 */
	ModelMap queryUnPrintTicketOrders(ModelMap model, Timestamp start,
			Timestamp end, String cinemaId, Integer pageNo, Integer pageSize);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: updateGoodsTakeTime  
	 * @Description:更新卖品订单取票时间
	 * @param tradeNo
	 * @param takeTime
	 * @return void
	 */
	void updateGoodsTakeTime(String tradeNo,String takeTime);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: updateGoodsTakeTimeByHttpRequest  
	 * @Description:批量更新 卖品打票时间
	 * @param tradeNos
	 * @return void
	 */
	void updateGoodsTakeTimeByHttpRequest(List<String> tradeNos);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: autoToMerchant  
	 * @Description:数据填充
	 * @param bill
	 * @return void
	 */
	void autoToMerchant(SettlementBill bill);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: reverseGoodsSettle  
	 * @Description: reverse goods settlebills
	 * @param object
	 * @return void
	 */
	ResultCode reverseGoodsSettle(SettlementBill object);
}
