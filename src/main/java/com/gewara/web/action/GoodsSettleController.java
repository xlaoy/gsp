package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.service.GoodsSettleService;

/**
 * 
* @ClassName: GoodsSettleController  
* @Description: ��Ʒ������� action ���Ͷ���
* @author yujun.su@gewara.com
* @date 2014-9-26 ����1:31:15
 */
@Controller
public class GoodsSettleController extends AnnotationController {
	
	@Autowired
	@Qualifier("goodsSettleService")
	private GoodsSettleService goodsSettleService;
	/**
	 * 
	* @auth yujun.su@gewara.com 
	* @Title: openGoodsSettlePage  
	* @Description:����Ʒ������ҳ�棬��ҳ����Ƕ����ҳǩ���ֱ��� ���˵������㵥����Ʒ������ϸ
	* @param @return
	* @return String
	* @throws
	 */
	@RequestMapping("platform/goodssettle/goodsSettle.xhtml")
	public String openGoodsSettlePage(){
		return "/goodssettle/goodsSettleMainPage.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsCheckBill  
	 * @Description:��Ʒ���˵���ѯ��ҳ���״δ�Ĭ�ϲ���ѯ��ֻ��ʼ��ҳ��
	 * @param 
	 * @return String
	 * @throws
	 */
	@RequestMapping("platform/goodssettle/queryGoodsCheckBill.xhtml")
	public String queryGoodsCheckBill(ModelMap model, String placeId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		model = goodsSettleService.queryGoodsCheckBill(model, placeId,
				startTime, endTime,pageNo,pageSize);
		return "goodssettle/goodsCheckBill.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsOrderDetails  
	 * @Description:����������Դ��ͬ��ѯ��ͬ�Ķ�����ϸ����Ϊ���˵���Դ��billIdΪ���˵���id,���ѯ��Ʒ���˵�����Ϊ
	 * 				���㵥��Դ��billIdΪ���㵥��id,��������Ʒ���㵥��
	 * @param model
	 * @param billId
	 * @param from
	 * @return String
	 * @throws
	 */
	@RequestMapping("platform/goodssettle/queryGoodsOrderDetails.xhtml")
	public String queryGoodsOrderDetails(ModelMap model, Long billId,
			String from, Integer pageNo, Integer pageSize,String orderNo,String placeId,
			Timestamp startTime, Timestamp endTime) {
		model = goodsSettleService.queryGoodsOrderDetails(model,billId,from,pageNo,pageSize,orderNo,placeId,startTime,endTime);
		return "goodssettle/goodsOrderDetail.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryGoodsSettleBill  
	 * @Description:��ҳ��ѯ���㵥
	 * @param  model
	 * @param  billId
	 * @param  from
	 * @param  pageNo
	 * @param  pageSize
	 * @return String
	 */
	@RequestMapping("platform/goodssettle/queryGoodsSettleBill.xhtml")
	public String queryGoodsSettleBill(ModelMap model, String placeId,
			Timestamp startTime, Timestamp endTime, Integer pageNo,
			Integer pageSize) {
		model = goodsSettleService.queryGoodsSettleBill(model,placeId,startTime,endTime,pageNo, pageSize);
		return "goodssettle/goodsSettleBill.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantGoodsSettleBills  
	 * @Description:
	 * @param proviceCode
	 * @param placeId
	 * @param start
	 * @param end
	 * @param status
	 * @param pageNo
	 * @param pageSize
	 * @param model
	 * @param isXls
	 * @param request
	 * @param response
	 * @return String
	 * @throws
	 */
	
	@RequestMapping("merchant/goodssettle/openMerchantGoodsSettleBills.xhtml")
	public String loadMerchantGoodsSettleBills(String proviceCode,
			String placeId, Timestamp start, Timestamp end, String status,
			Integer pageNo, Integer pageSize, ModelMap model, String isXls,
			HttpServletRequest request, HttpServletResponse response) {
//		request.getSession().setAttribute("pdata", "[{\"name\":\"��ǳ�����Ӱ��\",\"recordId\":\"63364,TICKET\",\"pinyin\":\"wjcwddyc\"},{\"name\":\"��֮��Ӱ��\",\"recordId\":\"16,TICKET\",\"pinyin\":\"lzmyc\"},{\"name\":\"������Ӱ��\",\"recordId\":\"10,TICKET\",\"pinyin\":\"yhdyc\"}];");
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> placeIds = new ArrayList<String>();
		if (StringUtils.isNotBlank(placeId)){
			placeIds.add(placeId);			
		}else{
			placeIds = getAllowedPlaceIds(request);
		}
		if (StringUtils.isNotBlank(proviceCode)){
			placeIds = cityInfoHolder.filteredPlaceIdForProvice(placeIds, proviceCode);
		}
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		model = goodsSettleService.loadMerchantGoodsSettleBills( proviceCode,
				 placeId,placeIds, allowPlaceIds,  start,  end,  status,
				 pageNo,  pageSize,  model,  isXls,
				 request,  response);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", "��Ʒ���㵥" , response);
			return "/downloadtemplate/merchantGoodsSettleBills.vm";
		}
		return "goodssettle/merchantGoodsSettleBills.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantOrderDetails  
	 * @Description:�̻��˶�����ѯ
	 * @param model
	 * @param billId
	 * @param from
	 * @param pageNo
	 * @param pageSize
	 * @param orderNo
	 * @param placeId
	 * @param startTime
	 * @param endTime
	 * @param isXls
	 * @param response
	 * @param @return
	 * @return String
	 */
	@RequestMapping("merchant/goodssettle/queryGoodsOrderDetails.xhtml")
	public String loadMerchantOrderDetails(ModelMap model, Long billId,
			String from, Integer pageNo, Integer pageSize,String orderNo,String placeId,
			Timestamp startTime, Timestamp endTime,String isXls,HttpServletResponse response){
		model = goodsSettleService.loadMerchantOrderDetails(model,billId,from,pageNo,pageSize,orderNo,placeId,startTime,endTime);
		if(StringUtils.isNotEmpty(isXls)){
			this.download("xls", "��Ʒ������ϸ", response);
			return "/downloadtemplate/merchantGoodsOrderDetails.vm";
		}
		return "goodssettle/merchantGoodsOrderDetails.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryCheckBillBySettleId  
	 * @Description:���ݽ��㵥ID��ѯ���˵�
	 * @param model
	 * @param settleId
	 * @param @return
	 * @return String
	 * @throws
	 */
	@RequestMapping("platform/goodssettle/queryCheckBillBySettleId.xhtml")
	public String queryCheckBillBySettleId(ModelMap model,Long settleId){
		model = goodsSettleService.queryCheckBillBySettleId(model,settleId);
		return "goodssettle/goodsCheckBill.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryUnPrintTicketOrders  
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
	@RequestMapping("platform/goodssettle/queryUnPrintTicketOrders.xhtml")
	public String queryUnPrintTicketOrders(ModelMap model,Timestamp startTime,Timestamp endTime,String placeId,Integer pageNo , Integer pageSize){
		model = goodsSettleService.queryUnPrintTicketOrders(model,startTime,endTime,placeId,pageNo,pageSize);
		return "goodssettle/unPrintGoods.vm";
	}
}
