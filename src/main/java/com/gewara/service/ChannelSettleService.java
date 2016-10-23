package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettlementBill;

/**
 * 
 * @ClassName: ChannelSettleService  
 * @Description: 通道费结算service
 * @author yujun.su@gewara.com
 * @date 2014-10-14 上午10:24:29
 */
public interface ChannelSettleService {
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinemas  
	 * @Description:加载所有影院信息
	 * @param model
	 * @param recordId 
	 * @return ModelMap
	 * 
	 */
	ModelMap loadAllCinemas(ModelMap model, String recordId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: saveChannelSettleConfig  
	 * @Description:保存通道费结算配置信息
	 * @param model
	 * @param vendorName
	 * @param houtaiId 
	 * @param vendorCode
	 * @param settleCycle
	 * @param settleDays
	 * @param settleMethod
	 * @param settleMoney
	 * @param orderPercent
	 * @param firstSettleDate
	 * @param cinema
	 * @return ModelMap
	 * 
	 */
	ModelMap saveChannelSettleConfig(ModelMap model, ChannelSettleConfig settleConfig, String[] cinema, User user);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadChannelConfig  
	 * @Description:加载所有的通道费结算配置信息
	 * @param  model
	 * @param vendorId 
	 * @return void
	 * 
	 */
	ModelMap loadChannelConfig(ModelMap model,Integer pageNo, Integer pageSize, Long vendorId, String placeid, String vendorType);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: deleteChannelSettleConfig  
	 * @Description:删除结算配置
	 * @param model
	 * @param recordId
	 * @return void
	 * 
	 */
	void deleteChannelSettleConfig(ModelMap model, Long recordId);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: buildChannelSettleBill  
	 * @Description:生成供应商关联影院的通道费结算单
	 * @param recordId
	 * @return void
	 * 
	 */
	//void buildChannelSettleBill(Long recordId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryChannelSettlebills  
	 * @Description:查询通道费结算单
	 * @param vendorName
	 * @param start
	 * @param end
	 * @return void
	 * 
	 */
	void queryChannelSettlebills(ModelMap model,Integer vendorId, Timestamp start,
			Timestamp end,String reqMoneyStatus,Integer pageNo, Integer pageSize);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryOrderDetailForChannel  
	 * @Description: 通道费结算订单明细查询
	 * @param model
	 * @param settleBillId
	 * @param pageNo
	 * @param pageSize
	 * @param downLoadXls
	 * @return void
	 */
	void queryOrderDetailForChannel(ModelMap model, Long settleBillId,
			Integer pageNo, Integer pageSize, String downLoadXls);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: buildChannelSettlementBillForJob  
	 * @Description: 通道费结算单生成方法，提供给定时器定调用
	 * @param 
	 * @return void
	 */
	//void buildChannelSettlementBillForJob();

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: generateChannelCheckBill  
	 * @Description:通道费对账单
	 * @param 
	 * @return void
	 */
	//void generateChannelCheckBill(List<ChannelSettleConfig> configs, String tag);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @param 
	 * @return void
	 */
	void generateChannelSettleBill(List<ChannelSettleConfig> configs,String tag);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doChannelSettleFilling  
	 * @Description:通道费结算单，对账单填充
	 * @param 
	 * @return void
	 */
	//void doChannelSettleFilling();

	/**
	 * 分页查询通道费对账单
	 * @param vendorId 
	 */
	ModelMap queryCheckBillBySettleId(ModelMap model, Long settleId, Long vendorId, Integer pageNo, Integer pageSize);


	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: verifyChannelConfig  
	 * @Description:更新审核状态
	 * @param  config
	 * @param  user
	 * @return ChannelSettleConfig
	 */
	ChannelSettleConfig verifyChannelConfig(Long recordId,String Status, User user);

	/**
	 * 
	 * @param vendorName 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadUnVerifiedConfig  
	 * @Description:查询未审核的结算配置
	 * @param pageSize
	 * @param pageNo
	 * @param
	 * @return Map<String,Object>
	 */
	Map<String, Object> loadUnVerifiedConfig(String vendorName, Integer pageSize, Integer pageNo);
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: checkVendorNameIsRepeat  
	 * @Description:验证是否供应商名称是否重复
	 * @param @param vendorName
	 * @return int
	 */
	Integer checkVendorNameIsRepeat(String vendorName,Long recordId);

	void autoToMerchant(SettlementBill bill);
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryVendorByName  
	 * @Description:根据输入值模糊查询供应商名称
	 * @param inputValue
	 * @return List<ChannelSettleConfig>
	 */
	List<ChannelSettleConfig> queryVendorByName(String inputValue);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadConfigAndCinemaById  
	 * @Description:根据通道费结算配置 加载 配置以及 关联的影院
	 * @param recordId
	 * @return Map<String,Object>
	 */
	Map<String, Object> loadConfigAndCinemaById(Long recordId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: modifyChannelSettleConfig  
	 * @Description:通道费结算配置修改
	 * @param  recordId
	 * @param  vendorName
	 * @param  vendorCode
	 * @param isSettleRefund 
	 * @param orderPercent 
	 * @param settleMoney 
	 * @return void
	 */
	void modifyChannelSettleConfig(ChannelSettleConfig newconfig, ChannelSettleConfig oldconfig, String[] cinema, String username);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: reverseChannelSettle  
	 * @Description:reverse Channel settlebill
	 * @param object
	 * @return void
	 */
	ResultCode reverseChannelSettle(SettlementBill object);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadPlacesByVendorId  
	 * @Description:
	 * @param @param vendorId
	 * @param @return
	 * @return List<Place>
	 * @throws
	 */
	List<Place> loadPlacesByVendorId(Long vendorId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: abandonChannelConfig  
	 * @Description:废弃通道费结算配置
	 * @param config
	 * @param user
	 * @return void
	 * @throws
	 */
	void abandonChannelConfig(ChannelSettleConfig config, User user);

	int countChannelSettleBills(Long vendorId, Timestamp start, Timestamp end,String status,String reqMoneyStatus);

	List<SettlementBill> querySettleMents(Long vendorId, Timestamp start,Timestamp end, String status, Integer pageNo, Integer pageSize,String reqMoneyStatus);

}
