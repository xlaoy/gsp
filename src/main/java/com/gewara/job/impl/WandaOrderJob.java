package com.gewara.job.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.job.JobService;
import com.gewara.service.GewaOrderProvider;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

public class WandaOrderJob extends JobService{
	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	@Autowired
	@Qualifier("gewaOrderProvider")
	private GewaOrderProvider gewaOrderProvider;
	
	public void doWandaOrderJob(){
		logger.warn("万达订单明细Job start");
		gewaOrderProvider.provideGewaOrder();
		logger.warn("万达订单明细Job end");
	}
}
