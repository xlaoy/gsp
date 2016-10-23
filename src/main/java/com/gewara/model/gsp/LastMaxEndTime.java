package com.gewara.model.gsp;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class LastMaxEndTime extends BaseObject {

	private static final long serialVersionUID = -4103516648115095975L;

	//配置id
	private String configId;
	//类别
	private String tag;
	//最后时间的结算单id
	private Long settleId;
	//最后时间的对账单id
	private Long checkId;
	//最后时间
	private Timestamp maxEndTime;
	//更新时间
	private Timestamp updateTime;
	
	public LastMaxEndTime() {
	}
	
	public LastMaxEndTime(String configId, String tag, Long checkId, Timestamp maxEndTime) {
		this.configId = configId;
		this.tag = tag;
		this.checkId = checkId;
		this.maxEndTime = maxEndTime;
		this.updateTime = DateUtil.getCurFullTimestamp();
	}
	
	public String getConfigId() {
		return configId;
	}



	public void setConfigId(String configId) {
		this.configId = configId;
	}



	public String getTag() {
		return tag;
	}



	public void setTag(String tag) {
		this.tag = tag;
	}



	public Long getSettleId() {
		return settleId;
	}



	public void setSettleId(Long settleId) {
		this.settleId = settleId;
	}



	public Long getCheckId() {
		return checkId;
	}



	public void setCheckId(Long checkId) {
		this.checkId = checkId;
	}



	public Timestamp getMaxEndTime() {
		return maxEndTime;
	}



	public void setMaxEndTime(Timestamp maxEndTime) {
		this.maxEndTime = maxEndTime;
	}



	public Timestamp getUpdateTime() {
		return updateTime;
	}



	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}



	@Override
	public Serializable realId() {
		return configId;
	}

}
