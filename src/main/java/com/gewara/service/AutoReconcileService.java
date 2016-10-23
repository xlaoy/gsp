package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.gsp.AutoAnalysObject;
import com.gewara.model.gsp.AutoReconciledInfo;
import com.gewara.model.gsp.SettlementBill;

/**
 * 
 * @ClassName: AutoReconcileService  
 * @Description: 自动对账核心功能核心实现  
 * @author yujun.su@gewara.com
 * @date 2015-3-31 下午4:37:35
 */
public interface AutoReconcileService {
	
	boolean loadTemplateFile(AutoReconciledInfo info);
	
	boolean doMenchTempData(AutoReconciledInfo info);
	
	boolean doGWOrder(AutoReconciledInfo info);
	
	void compareOrder(AutoReconciledInfo info);
	
	void doAutoReconcile(AutoReconciledInfo info);

	List<AutoAnalysObject> loadReconResult(Long settlebillId,String status, Integer pageNo, Integer pageSize);

	Integer countReconResult(Long settlebillId, String status);

	int countReconcileResultNum(List<String> placeIds, Timestamp start, Timestamp end, Long settleid);

	List<AutoReconciledInfo> queryReconcileResult(List<String> placeIds,Integer pageNo, Integer pageSize, Timestamp start, Timestamp end, Long settleid);

	List<SettlementBill> querySettlementbillsForAutoRecon(List<String> placeIds, Timestamp start, Timestamp end, Long settleid);

	void deleteReconRecord(Long settlebillId);
}
