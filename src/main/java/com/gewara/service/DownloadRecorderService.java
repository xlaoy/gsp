package com.gewara.service;

import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.SettlementBill;

/**
 * 
 * @author yujun.su
 * @date 2014-09-15
 *
 */
public interface DownloadRecorderService {
	/**
	 * @author yujun.su
	 * @param bill
	 * @return
	 * @description ¸ù¾ÝSettlementBill²éÑ¯DownLoadRecorder
	 */
	DownloadRecorder getRecorderBySettleBill(SettlementBill bill);
}
