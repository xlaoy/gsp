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
	 * 根据结算单id查询对应的异价单详情
	 * @param settleBillId
	 * @return
	 */
	List<DiffPriceBill> queryDiffPriceBillDetail(SettlementBill setl);
	
}
