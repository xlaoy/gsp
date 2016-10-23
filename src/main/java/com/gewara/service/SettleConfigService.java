/** 
 */
package com.gewara.service;

import java.util.List;

import com.gewara.model.ResultCode;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleConfigUpd;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Oct 9, 2013  1:57:11 PM
 */
public interface SettleConfigService {

	/**
	 * @param openType
	 * @return
	 */
	int countingUnApprovedConfig(String openType, String placeId);

	/**
	 * @param openType
	 * @param placeId 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	List<SettleConfigUpd> queryUnApprovedConfig(String openType, String placeId, Integer pageNo,
			Integer pageSize);

	/**
	 * @param sc
	 * @param uid
	 * @return
	 */
	ResultCode removeSettleConfig(SettleConfig sc, long uid);
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: processAfterModifySettleConfig  
	 * @Description:结算配置修改后处理结算单
	 * @param sc
	 * @param oldSettleConfig 
	 * @return void
	 */
	void processBillAfterModifySettleConfig(SettleConfig newSettleConfig, SettleConfig oldSettleConfig);
}
