package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.acl.User;
import com.gewara.model.gsp.ReqMoneyBill;
import com.gewara.model.gsp.SettlementBill;

public interface ReqMoneyBillService {

	List<ReqMoneyBill> qryMoneyBill(Timestamp startTime, Timestamp endTime,String vendorName, String placeId,Long userId,Integer pageNo,Integer pageSize,String settleId);

	Integer countMoneyBill(Timestamp startTime, Timestamp endTime,String vendorName, String placeId,Long userId,String settleId);
	
	void increaseReqMoneyBill(SettlementBill settlementBill,User user);
	
	void deleteReqMoneybill(String recordId,User user); 
}
