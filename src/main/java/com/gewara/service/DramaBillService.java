package com.gewara.service;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.gewara.model.acl.User;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaBaseData;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaSettleBill;



public interface DramaBillService {

	//演出查询
	ModelMap queryDrama(Drama drama, Long dramaversionid, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//演出配置查询
	ModelMap queryDramaConfig(DramaConfig config, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//审批通过配置
	String passeDramConfig(List<DramaConfig> configList);
	
	//查询基础数据
	ModelMap queryDramaBaseData(DramaBaseData dbData, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//演出结算单查询
	ModelMap queryDramaSettleBill(DramaSettleBill bill, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//演出结算单价格账单查询
	ModelMap queryDramaPriceBill(Long recordid, ModelMap model);
	
	//付款
	void payBill(List<DramaSettleBill> billList, String optUser);
	
	//重新付款
	String rePayBill(Long recordid, String optUser);
	
	//查询请款单
	ModelMap queryReqMoenyBill(DramaSettleBill bill, User user, ModelMap model);
	
	//导出订单
	ModelMap exportOrder(Long recordid, ModelMap model);
	
	//导出退单
	ModelMap exportRefund(Long recordid, ModelMap model);
	
	//导出线上优惠
	ModelMap exportDiscount(Long recordid, ModelMap model);
	
	//导出订单条目
	ModelMap exportOrderItem(Long recordid, ModelMap model);
	
	//查询订单
	ModelMap queryDramaOrder(String tradeno, String orderform, ModelMap model);
	
	//提计成本单据
	ModelMap queryJitiBill(DramaJitiBill bill, Integer pageNo, Integer pageSize, String url, String isXls, ModelMap model);
}
