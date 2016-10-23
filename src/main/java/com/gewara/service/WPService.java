package com.gewara.service;

import java.sql.Timestamp;

import org.springframework.ui.ModelMap;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.SettlementBill;

public interface WPService {

	//查询微票月账单
	ModelMap queryWPMonthBill(SettlementBill sb, Integer pageNo, Integer pageSize, String url, String optUser, ModelMap model);
	
	//影院月账单
	ModelMap queryWPPlaceBill(CheckBill ck, String isXls, Integer pageNo, Integer pageSize, String url, ModelMap model);
	
	//导出订单
	ModelMap exportWPOrder(String type, Long recordId, String duan,  Timestamp start, Timestamp end, ModelMap model);
	
	//导出退单
	ModelMap exportWPRefund(String type, Long recordId, Timestamp start, Timestamp end, ModelMap model);
	
	//保存文件
	ResultCode saveToRemote(byte[] fileBuff, String fileName, String optUser);
	
	//检验
	boolean checkWP();
	
	//重置
	SettlementBill resetWP();
	
	//生成影院月单据
	boolean createWPPlaceMonthBill(SettlementBill bill);
	
	//生成影院日单据
	boolean createWPPlaceDayBill(SettlementBill bill);
	
	//计算影院日账单
	void calculateWPPlaceDayBill(SettlementBill bill);
	
	//查询要计算的微票账单
	SettlementBill queryWatingWP();
	
	//计算影院月账单
	void calculateWPPlaceMonthBill(SettlementBill bill);
	
	//计算微票月账单
	void calculateWPBill(SettlementBill bill);
	
	//计算微票日账单
	void calculateWPDayBill(SettlementBill bill);
}
