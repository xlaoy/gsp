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
* @Description: 卖品结算管理 action 类型定义
* @author yujun.su@gewara.com
* @date 2014-9-26 下午1:31:15
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
	* @Description:打开卖品结算主页面，主页面内嵌三个页签，分别是 对账单、结算单、卖品订单明细
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
	 * @Description:卖品对账单查询，页面首次打开默认不查询，只初始化页面
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
	 * @Description:根据请求来源不同查询不同的订单明细，若为对账单来源，billId为对账单的id,则查询卖品对账单表，若为
	 * 				结算单来源，billId为结算单的id,擦好像卖品结算单表
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
	 * @Description:分页查询结算单
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
//		request.getSession().setAttribute("pdata", "[{\"name\":\"五角场万达电影城\",\"recordId\":\"63364,TICKET\",\"pinyin\":\"wjcwddyc\"},{\"name\":\"龙之梦影城\",\"recordId\":\"16,TICKET\",\"pinyin\":\"lzmyc\"},{\"name\":\"永华电影城\",\"recordId\":\"10,TICKET\",\"pinyin\":\"yhdyc\"}];");
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
			this.download("xls", "卖品结算单" , response);
			return "/downloadtemplate/merchantGoodsSettleBills.vm";
		}
		return "goodssettle/merchantGoodsSettleBills.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadMerchantOrderDetails  
	 * @Description:商户端订单查询
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
			this.download("xls", "卖品订单明细", response);
			return "/downloadtemplate/merchantGoodsOrderDetails.vm";
		}
		return "goodssettle/merchantGoodsOrderDetails.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryCheckBillBySettleId  
	 * @Description:根据结算单ID查询对账单
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
	@RequestMapping("platform/goodssettle/queryUnPrintTicketOrders.xhtml")
	public String queryUnPrintTicketOrders(ModelMap model,Timestamp startTime,Timestamp endTime,String placeId,Integer pageNo , Integer pageSize){
		model = goodsSettleService.queryUnPrintTicketOrders(model,startTime,endTime,placeId,pageNo,pageSize);
		return "goodssettle/unPrintGoods.vm";
	}
}
