package com.gewara.service;

import java.util.List;

import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;

/**
 * 
 * @ClassName: GewaOrderProvider  
 * @Description: gewara order ��ϸ�ṩ�� 
 * @author yujun.su@gewara.com
 * @date 2015-8-18 ����3:44:47
 */
public interface GewaOrderProvider {
	void provideGewaOrder();
	
	List<SettleConfig> qryWandaSettleConfigs(String category);

	List<SettlementBill> qrySettlementbillBySettleConfig(SettleConfig config);
}
