package com.gewara.service;

import org.springframework.ui.ModelMap;

import com.gewara.model.gsp.RDBuckle;
import com.gewara.model.gsp.RDDayCount;
import com.gewara.model.gsp.RDPay;
import com.gewara.model.gsp.RDPrevent;

public interface RongDuanPreventService {

	//熔断汇总查询
	ModelMap queryRongduanHuizong(String vendorName, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//每天系统订单金额统计
	ModelMap queryRongdunDayCount(RDDayCount dayc, ModelMap model);
	
	//每天系统订单金额统计
	ModelMap queryRongdunPayRecord(RDPay pay, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//扣款详情
	ModelMap queryRongdunBuckleRecord(RDBuckle buckle, Integer pageNo, Integer pageSize, String url, String isXls, ModelMap model);
	
	//预付款
	String rongdunPay(RDPrevent rdp, RDPay pay, String optUser);
	
	//修改预警金额
	String waringMfy(RDPrevent rdp, Double amount);
	
	//请款
	boolean reqMoney(RDPay pay, RDPrevent rdp, Long userId);
	
	//每天防熔断供应商影票统计
	void everyDayCollection();
	
	//熔断统计
	void rongduanCollection();
	
}
