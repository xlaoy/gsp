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
 * @Description: ��Ʒ����ҵ���߼������� 
 * @author yujun.su@gewara.com
 * @date 2014-9-26 ����1:32:59
 */
public interface GoodsSettleService {
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: saveGoodsSettleCheckBill  
	 * @Description:������Ʒ������˵�
	 * @param checkBill
	 * @return void
	 * @throws
	 */
	//void saveGoodsSettleCheckBill(CheckBill checkBill);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: buildGoodsSettleCheckBillPerDay  
	 * @Description:ÿ�����ɶ��˵���¼�����������˵�����
	 * @param 
	 * @return void
	 * @throws
	 */
	//void buildGoodsSettleCheckBillPerDay(List<SettleConfig> configs, String tag);

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBill  
	 * @Description:��ѯ��Ʒ���˵�����
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
	 * @Description:��ѯ��Ʒ������ϸ
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
	 * @Description:��Ʒ���㵥�̻������ݼ���
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
	 * @Description:��Ʒ�����̻��˽��㵥��ϸ��ѯ
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
	 * @Description:������Ʒ���㵥����
	 * @param 
	 * @return void
	 * @throws
	 */
	void generateGoodsSettleBill();

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doGoodsSettleFilling  
	 * @Description:�ύ���˵�����㵥
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
	 * @Description:������ѯ��Ʒ֧���ɹ�����δ��Ʊ����Ʒ����
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
	 * @Description:������Ʒ����ȡƱʱ��
	 * @param tradeNo
	 * @param takeTime
	 * @return void
	 */
	void updateGoodsTakeTime(String tradeNo,String takeTime);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: updateGoodsTakeTimeByHttpRequest  
	 * @Description:�������� ��Ʒ��Ʊʱ��
	 * @param tradeNos
	 * @return void
	 */
	void updateGoodsTakeTimeByHttpRequest(List<String> tradeNos);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: autoToMerchant  
	 * @Description:�������
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
