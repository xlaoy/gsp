package com.gewara.service;

import java.util.List;

import com.gewara.model.gsp.DiffPriceBill;
import com.gewara.model.gsp.SettlementBill;

/**
 * zyj
 * @author user
 *
 */

public interface DiffPriceBillService {

	/**
	 * ���ݽ��㵥id��ѯ��Ӧ����۵�����
	 * @param settleBillId
	 * @return
	 */
	List<DiffPriceBill> queryDiffPriceBillDetail(SettlementBill setl);
	
}
