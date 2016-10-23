package com.gewara.jms;

import org.apache.camel.language.Simple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.service.GoodsSettleService;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

public class JmsGoodsTakeTimeConsumer implements InitializingBean {
	private final  GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("goodsSettleService")
	private GoodsSettleService goodsSettleService;

	public void updateGoodsOrderTakeTime(@Simple("${body[tradeNo]}")String tradeNo, @Simple("${body[taketime]}")String takeTime) {
		//订单过多，  取消日志
//		dbLogger.warn("JMS-更新卖品打票时间,交易号：" + tradeNo + "-->" + takeTime);
		goodsSettleService.updateGoodsTakeTime(tradeNo, takeTime);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dbLogger.warn("Initia JmsGoodsTakeTimeConsumer ok!");
	}

}
