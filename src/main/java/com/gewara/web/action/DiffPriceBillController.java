package com.gewara.web.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.DiffPriceBill;
import com.gewara.model.gsp.DiffPriceOrder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DiffPriceBillService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RecordIdUtils;

/**
 * zyj
 * 异价单
 * @author user
 *
 */

@Controller
public class DiffPriceBillController extends AnnotationController {

	@Autowired
	@Qualifier("diffPriceBillServiceImpl")
	private DiffPriceBillService diffPriceBillService;
	
	/**
	 * 根据结算单id查询所有数据
	 * @param settleId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/diffPriceBill/diffPriceBillDetail.xhtml")
	public String queryDiffPriceBillDetail(SettlementBill setl, ModelMap model) {
		List<DiffPriceBill> dpbList = diffPriceBillService.queryDiffPriceBillDetail(setl);
		int diffPriceOrderNumber = 0;
		int diffRefundOrderNumber = 0;
		double diffPriceAmount = 0.0;
		for(int i = 0; i < dpbList.size(); i++) {
			DiffPriceBill dpb = dpbList.get(i);
			diffPriceOrderNumber += dpb.getDiffOrderNum();
			diffRefundOrderNumber += dpb.getDiffRefundNum();
			diffPriceAmount += dpb.getDiffAmount();
		}
		model.put("dpbList", dpbList);
		model.put("diffPriceOrderNumber", diffPriceOrderNumber);
		model.put("diffRefundOrderNumber", diffRefundOrderNumber);
		model.put("diffPriceAmount", diffPriceAmount);
		if(setl.getStartTime() == null && setl.getEndTime() == null) {
			setl = daoService.getObject(SettlementBill.class, setl.getRecordId());
		}
		model.put("setl", setl);
		return "/checkbill/diffPriceBills.vm";
	}

	/**
	 * 导出excel
	 * @param setl
	 * @param model
	 * @param response
	 * @return
	 */
	@RequestMapping("/platform/diffPriceBill/downloadDiffPriceBill.xhtml")
	public String downloadDiffPriceBill(SettlementBill setl, ModelMap model, HttpServletResponse response) {
		Place place = daoService.getObject(Place.class, RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, setl.getRelateId()));
		List<DiffPriceBill> dpbList = diffPriceBillService.queryDiffPriceBillDetail(setl);
		int diffPriceOrderNumber = 0;
		int diffRefundOrderNumber = 0;
		double diffPriceAmount = 0.0;
		for(int i = 0; i < dpbList.size(); i++) {
			DiffPriceBill dpb = dpbList.get(i);
			diffPriceOrderNumber += dpb.getDiffOrderNum();
			diffRefundOrderNumber += dpb.getDiffRefundNum();
			diffPriceAmount += dpb.getDiffAmount();
		}
		model.put("dpbList", dpbList);
		model.put("diffPriceOrderNumber", diffPriceOrderNumber);
		model.put("diffRefundOrderNumber", diffRefundOrderNumber);
		model.put("diffPriceAmount", diffPriceAmount);
		model.put("place", place);
		
		String t = " " + DateUtil.format(setl.getStartTime(), "MM-dd") + " 至 " + DateUtil.format(setl.getEndTime(), "MM-dd");
		this.download("xls", place.getName() +  t  +  "的异价单",  response);
		
		return "/downloadtemplate/diffPriceBills.vm";
	}
	

	
	/**
	 * 通过checkbillid查询异价订单明细
	 * @param checkId
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/diffPriceBill/queryOrderDetailByRecordId.xhtml")
	public String queryOrderDetailByRecordId(Long recordId, String type, String isXls, ModelMap model, HttpServletResponse response) {
		List<DiffPriceOrder> diffPricList = null;
		if("check".equals(type)) {
			diffPricList = daoService.getObjectListByField(DiffPriceOrder.class, "chekcBillId", recordId);
		} else if("settlt".equals(type)) {
			diffPricList = daoService.getObjectListByField(DiffPriceOrder.class, "settleBillId", recordId);
		}
		if(CollectionUtils.isNotEmpty(diffPricList)) {
			List<String> tradenos = BeanUtil.getBeanPropertyList(diffPricList, "tradeno", true);
			Map<String, Double> realPrice = BeanUtil.beanListToMap(diffPricList, "tradeno", "actualPrice", false);
			List<GewaOrder> orderList = daoService.getObjectBatch(GewaOrder.class, "tradeno", tradenos);
			//查看退票订单
			List<Refundment> refundmentList = daoService.getObjectBatch(Refundment.class, "tradeno", tradenos);
			if(CollectionUtils.isNotEmpty(refundmentList)) {
				for(int i = 0; i < orderList.size(); i++) {
					for(Refundment rfd : refundmentList) {
						if(rfd.getTradeno().equals(orderList.get(i).getTradeno())) {
							orderList.get(i).setOrderStatus("paid_return");
						}
					}
				}
			}
			model.put("orders", orderList);
			model.put("realPrice", realPrice);
		}
		if(StringUtils.isNotBlank(isXls)) {
			this.download("xls", recordId + "结算单异价订单明细",  response);
			return "/downloadtemplate/diffpriceOrderDetailsByCheckId.vm";
		} else {
			return "/gewaorder/diffpriceOrderDetailsByCheckId.vm";
		}
	}

}
