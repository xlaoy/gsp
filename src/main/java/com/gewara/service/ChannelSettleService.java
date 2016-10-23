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
 * @Description: ͨ���ѽ���service
 * @author yujun.su@gewara.com
 * @date 2014-10-14 ����10:24:29
 */
public interface ChannelSettleService {
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinemas  
	 * @Description:��������ӰԺ��Ϣ
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
	 * @Description:����ͨ���ѽ���������Ϣ
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
	 * @Description:�������е�ͨ���ѽ���������Ϣ
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
	 * @Description:ɾ����������
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
	 * @Description:���ɹ�Ӧ�̹���ӰԺ��ͨ���ѽ��㵥
	 * @param recordId
	 * @return void
	 * 
	 */
	//void buildChannelSettleBill(Long recordId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryChannelSettlebills  
	 * @Description:��ѯͨ���ѽ��㵥
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
	 * @Description: ͨ���ѽ��㶩����ϸ��ѯ
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
	 * @Description: ͨ���ѽ��㵥���ɷ������ṩ����ʱ��������
	 * @param 
	 * @return void
	 */
	//void buildChannelSettlementBillForJob();

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: generateChannelCheckBill  
	 * @Description:ͨ���Ѷ��˵�
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
	 * @Description:ͨ���ѽ��㵥�����˵����
	 * @param 
	 * @return void
	 */
	//void doChannelSettleFilling();

	/**
	 * ��ҳ��ѯͨ���Ѷ��˵�
	 * @param vendorId 
	 */
	ModelMap queryCheckBillBySettleId(ModelMap model, Long settleId, Long vendorId, Integer pageNo, Integer pageSize);


	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: verifyChannelConfig  
	 * @Description:�������״̬
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
	 * @Description:��ѯδ��˵Ľ�������
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
	 * @Description:��֤�Ƿ�Ӧ�������Ƿ��ظ�
	 * @param @param vendorName
	 * @return int
	 */
	Integer checkVendorNameIsRepeat(String vendorName,Long recordId);

	void autoToMerchant(SettlementBill bill);
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryVendorByName  
	 * @Description:��������ֵģ����ѯ��Ӧ������
	 * @param inputValue
	 * @return List<ChannelSettleConfig>
	 */
	List<ChannelSettleConfig> queryVendorByName(String inputValue);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadConfigAndCinemaById  
	 * @Description:����ͨ���ѽ������� ���� �����Լ� ������ӰԺ
	 * @param recordId
	 * @return Map<String,Object>
	 */
	Map<String, Object> loadConfigAndCinemaById(Long recordId);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: modifyChannelSettleConfig  
	 * @Description:ͨ���ѽ��������޸�
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
	 * @Description:����ͨ���ѽ�������
	 * @param config
	 * @param user
	 * @return void
	 * @throws
	 */
	void abandonChannelConfig(ChannelSettleConfig config, User user);

	int countChannelSettleBills(Long vendorId, Timestamp start, Timestamp end,String status,String reqMoneyStatus);

	List<SettlementBill> querySettleMents(Long vendorId, Timestamp start,Timestamp end, String status, Integer pageNo, Integer pageSize,String reqMoneyStatus);

}
