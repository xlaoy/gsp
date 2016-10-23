package com.gewara.constant;

import java.util.ArrayList;
import java.util.List;

public final class DramaConst {

	//定时job
	public static final String SYNCDRAMA = "syncDrama";
	public static final String SYNCDRAMACONFIG = "syncDramaConfig";
	public static final String SYNCONLINEORDER = "syncDramaOnlineOrder";
	public static final String SYNCONLINEREFUND = "syncDramaOnlineRefund";
	public static final String SYNCOFFLINEORDER = "syncDramaOfflineOrder";
	public static final String SYNCOFFLINEREFUND = "syncDramaOfflineRefund";
	public static final String SYNPLACE = "syncDramaPlace";
	public static final String SYNSUPPLIER = "syncDramaSupplier";
	public static final String SYNSPLAYITEM = "syncDramaPlayItem";
	
	//演出配置状态
	public static final String WAITFINISH = "WAITFINISH";//待完成
	public static final String WAITAPPROVAL = "WAITAPPROVAL";//待审核
	public static final String HASAPPROVAL = "HASAPPROVAL";//已审核
	public static final String SETTLEING = "SETTLEING";//结算中
	public static final String FINISH = "FINISH";//已结算
	public static final String DISCARD = "DISCARD";//已废弃
	
	//
	public static final String Y = "Y";
	public static final String N = "N";
	
	//
	public static final String ORDERTIME = "ORDERTIME"; //下单时间
	public static final String USETIME = "USETIME"; //使用时间
	
	
	//价格扣率配置状态
	public static final String AVAILABLE = "AVAILABLE";//可用
	public static final String EXPIRED = "EXPIRED";//过期
	
	//订单来源
	public static final String ONLINE = "ONLINE";//线上
	public static final String OFFLINE = "OFFLINE";//线下
	public static final String PIAOWU = "PIAOWU";//票务
	
	//基础数据类型
	public static final String SYSDATAJOB = "sysdatajob";
	public static final String PLACE = "place";
	public static final String SUPPLIER = "supplier";
	public static final String REPAYUSER = "repayuser";
	
	//结算周期
	public static final String TN = "TN";
	public static final String WEEKLY = "WEEKLY";
	public static final String MIDDLE = "MIDDLE";
	public static final String MONTHLY = "MONTHLY";
	public static final String PROGRAM = "PROGRAM";
	public static final String SETTLE = "SETTLE";
	
	//结算单状态
	public static final String NEW = "NEW";
	public static final String EXCEPTION = "EXCEPTION";//单据异常
	public static final String FULFILLING = "FULFILLING";
	public static final String WAITINGPAY = "WAITINGPAY";
	public static final String SETTLED = "SETTLED";
	public static final String INVALID = "INVALID";
	
	//单据类型
	public static final String SETTLEBILL = "SETTLEBILL"; //结算单
	public static final String PRICEBILL = "PRICEBILL"; //价格对账单
	public static final String TIJIBILL = "TIJIBILL";//提计成本单
	
	//卡类型
	public static final String CARDTYPE_A = "A";//兑换券
	public static final String CARDTYPE_B = "B";//补差券
	public static final String CARDTYPE_C = "C";//补差券
	public static final String CARDTYPE_D = "D";//低值券
	public static final String SOLDTYPE_M = "M";//销售券
	public static final List<String> CARDTYPE_LIST = new ArrayList<String>();
	
	//电子券类型
	public static final String ECARD = "ecard";//
	public static final String PARTNER = "partner";//
	
	//特价类型
	public static final String PERCENT = "percent";//
	public static final String UPRICE = "uprice";//
	
	//项目更新版本间隔时间
	public static final int VERINTERVALDAY = 7;//
	public static final int PUTOFFDAY = 3;//
	
	//系统起始结算日期
	public static final String STARTSETTLETIME = "2016-04-01";//
	
	public static final String OK = "ok";//
	
	//项目组织类型
	public static final String GW = "GW";//格瓦拉
	public static final String WG = "WG";//微格娱乐
	
	static {
		CARDTYPE_LIST.add(CARDTYPE_B);
		CARDTYPE_LIST.add(CARDTYPE_C);
		CARDTYPE_LIST.add(CARDTYPE_D);
	}
}

