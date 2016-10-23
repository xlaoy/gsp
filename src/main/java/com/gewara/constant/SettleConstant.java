package com.gewara.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.gewara.enums.CheckBillStatusEnums;

/**
 * 
 * @ClassName: SettleStatusConstant  
 * @Description:结算状态记录
 * @author yujun.su@gewara.com
 * @date 2014-9-28 上午10:51:01
 */
public class SettleConstant {
	
	public static final String hostname = "db41.gewara.cn";
	/***
	 * 结算时间类型
	 */
	
	public static final String SETTLE_BASE_ORDERTIME = "ORDERTIME";
	public static final String SETTLE_BASE_USETIME = "USETIME";
	/**
	 * 卖品结算状态，是
	 */
	public static final String IS_GOODS_SETTLE_Y = "Y";
	/**
	 * 卖品结算状态，否
	 */
	public static final String IS_GOODS_SETTLE_N = "N";
	
	/**
	 * 查询卖品订单明细的请求来源
	 * CB——checkbill，对账单
	 */
	public static final String CB_GOODS_DETAIL_FROM = "CB";
	/**
	 * 查询卖品订单明细的请求来源
	 * SB——settleBill，结算单
	 */	
	public static final String SB_GOODS_DETAIL_FROM = "SB";
	
	/**
	 * 通道费结算，按月
	 */
	public static final String CHANNEL_CYCLE_SETTLE_MONTH = "MONTH";
	
	/**
	 * 通道费结算 按多少天
	 */
	public static final String CHANNEL_CYCLE_SETTLE_DAYS = "DAYS";
	
	/**
	 * 结算单类型
	 * 
	 */
	public static final String TAG_SETTLEMENTBILL_CHANNEL = "CHANNEL";
	
	public static final String TAG_SETTLEMENTBILL_GOODS = "GOODS";
	
	public static final String TAG_SETTLEMENTBILL_TICKET = "TICKET";
	
	/**
	 * 是否有效
	 */
	/**
	 * 有效
	 */
	public static final String STATUS_VALID = "Y";
	
	/**
	 * 无效
	 */
	public static final String STATUS_INVALID = "N";
	
	/**
	 * 订单类型
	 */
	public final static String ORDER_TYPE_TICKET = "ticket";
	public final static String ORDER_TYPE_GOODS = "goods";
	
	public final static String CHANNEL_SETTLE_METHOD_TICKET = "TICKET";
	public final static String CHANNEL_SETTLE_METHOD_ORDER = "ORDER";
	
	public final static String SUBMIT_MARK = "submit_mark";
	/**
	 * 缓存regionname
	 */
	public final static String GSP_CACHE_REGIONNAME = "service";
	
	/**
	 * 自动对账状态定义
	 */
	public static final String SYSTEM = "system";
	
	public static final String HFH = "HFH";
	public static final String MTX = "MTX";
	public static final String STPF = "STPF";
//	public static final String JY = "JY";
	public static final String MJY = "MJY";
//	public static final String XFLH = "XFLH";
	public static final String XFLH2 = "XFLH2";
	
	public static final List<String> OPEN_TYPES = Arrays.asList(new String[]{HFH,MTX,STPF,XFLH2,MJY});
	//处理状态
	public static final Map<String,String> AUTORECONSTATUSMAP = new HashMap<String,String>();
	
	//待核对
	public static final String AUTO_RECON_STATUS_WAIT_PROCESS = "WAIT_PROCESS";
	
	//核对中
	public static final String AUTO_RECON_STATUS_PROCESSING = "PROCESSING";
	
	//核对完成
	public static final String AUTO_RECON_STATUS_FINISH = "FINISH";
	
	//对账失败
	public static final String AUTO_RECON_STATUS_FAILURE = "FAILURE";
	
	
	
	//订单对账结果
	//正常
	public static final String AUTO_RECON_RESULT_NORMAL = "NORMAL";
	//异常
	public static final String AUTO_RECON_RESULT_ABNORMAL = "ABNORMAL";
	
	/**
	 * 通道费供应商类型
	 */
	/**
	 * 系统方
	 */
	public static final String CHANNEL_VENDOR_SYS = "SYS";
	
	/**
	 * 院线
	 */
	public static final String CHANNEL_VENDOR_THEATRES = "THEATRES";
	
	/**
	 * 影院
	 */
	public static final String CHANNEL_VENDOR_CINEMA = "CINEMA";
	
	
	//发票状态
	public static final String INVOSTANEW = "INVOSTANEW";//待审核
	public static final String INVOSTAPASS = "INVOSTAPASS";//审核通过
	public static final String INVOSTAUNPASS = "INVOSTAUNPASS";//审核不通过
	public static final String INVOSTAAUTH = "INVOSTAAUTH";//已认证
	public static final String INVOSTAUNAUTH = "INVOSTAUNAUTH";//认证失败
	public static final String INVOSTADIKOU = "INVOSTADIKOU";//已抵扣
	public static final String INVOSTAUNDIKOU = "INVOSTAUNDIKOU";//抵扣失败
	
	public static final Map<String,String> BILLTYPEMAP = new HashMap<String,String>();
	public static final Map<String,String> NOTICKETSETTLEBILLSTATUSMAP = new HashMap<String,String>();
	public static final Map<String,String> CHANNELVENDORTYPE = new TreeMap<String,String>();
	public static final Set<String> UNSETTLECATE = new HashSet<String>();
	public static final Map<String,String> INVOICESTATUSMAP = new HashMap<String,String>();
	
	public static final List<String> WDCATE = new ArrayList<String>(); 
	
	static{
		
		WDCATE.add("WD2");
		WDCATE.add("WD3");
		
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_WAIT_PROCESS, "待对账");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_PROCESSING, "对账中");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_FINISH, "对账完成");
		AUTORECONSTATUSMAP.put(AUTO_RECON_STATUS_FAILURE, "对账失败");
		
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_TICKET, "影票结算单");
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_GOODS, "卖品结算单");
		BILLTYPEMAP.put(TAG_SETTLEMENTBILL_CHANNEL, "通道费结算单");
		
		/**
		 * 非影票结算单状态显示
		 */
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.GEWACONFIRMED.getStatus(), "待商户申请结算");
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.READJUST.getStatus(), "格瓦拉审核中");
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.WAITINGPAY.getStatus(), CheckBillStatusEnums.WAITINGPAY.getDisplay());
		NOTICKETSETTLEBILLSTATUSMAP.put(CheckBillStatusEnums.SETTLED.getStatus(),CheckBillStatusEnums.SETTLED.getDisplay());
		
		/**
		 * 通道商家类型显示
		 */
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_SYS, "系统方");
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_THEATRES, "院线");
		CHANNELVENDORTYPE.put(CHANNEL_VENDOR_CINEMA, "影院");
		
		
		UNSETTLECATE.add("PNX");
		UNSETTLECATE.add("GEWA");
		UNSETTLECATE.add("GPTBS");
		UNSETTLECATE.add("TXPC");
		UNSETTLECATE.add("WP");
		
		INVOICESTATUSMAP.put(INVOSTANEW, "待审核");
		INVOICESTATUSMAP.put(INVOSTAPASS, "审核通过");
		INVOICESTATUSMAP.put(INVOSTAUNPASS, "审核不通过");
		INVOICESTATUSMAP.put(INVOSTAAUTH, "已认证");
		INVOICESTATUSMAP.put(INVOSTAUNAUTH, "认证失败");
		INVOICESTATUSMAP.put(INVOSTADIKOU, "已抵扣");
		INVOICESTATUSMAP.put(INVOSTAUNDIKOU, "抵扣失败");
	}
	
	/**
	 * 异常原因
	 */
	public static final String AUTO_RECON_REASON_NOTFOUND = "格瓦拉无此订单";
	
	public static final String AUTO_RECON_REASON_UNKNOWN = "未知原因";
	
	public static final String AUTO_RECON_REASON_SETTLED = "此订单已结算,结算单号为：{0}";
	
	public static final String AUTO_RECON_REASON_NOTRANGE = "订单不在结算周期内";
	
	public static final String AUTO_RECON_REASON_REFUND = "格瓦拉已退票";
	
	public static final String AUTO_RECON_REASON_MOREORD = "一个对账码对应多个订单";
	
	public static final String AUTO_RECON_REASON_NORECONCODE = "商户订单没有对账码";
	
	public static final String AUTO_RECON_REASON_NQVOTES = "票数不一致";
	
	public static final String AUTO_RECON_REASON_NQAMOUNT = "结算金额不一致";

	/**
	 * 待审核配置来源
	 */
	public static final String OPERATE_TYPE_SYNC = "SYNC";
	
	public static final String OPERATE_TYPE_ADD = "ADD";
	
	public static final String OPERATE_TYPE_MODIFY = "MODIFY";
	
	/**
	 * 卖品 通道费结算状态定义
	 */
	
	public static final String APPLY_SETTLE_WAITAPPLY = "WAITAPPLY";
	
	public static final String APPLY_SETTLE_CHECKING = "CHECKING";
	
	public static final String APPLY_SETTLE_WAITPAY = "WAITPAY";
	
	public static final String APPLY_SETTLE_PAYED = "PAYED";
	
	/**
	 * 调整类型
	 */
	public static final String ADJUST_TYPE_SETTLEMENT = "SETTLEBILL";
	
	public static final String ADJUST_TYPE_PLACE = "PLACE";
	
	/**
	 * 审核状态
	 */
	public static final String ADJUST_STATUS_APPROVED = "APPROVED"; 
	
	public static final String ADJUST_STATUS_REJECTED = "REJECTED";
	
	public static final String ADJUST_STATUS_NEW = "NEW";
	
	/**
	 * 通道操作
	 */
	public static final String CHANNEL_OPERATE_TYPE_ADD = "ADD";
	
	public static final String CHANNEL_OPERATE_TYPE_DEL = "DEL";
	
	public static final String CHANNEL_OPERATE_TYPE_UPDATE = "UPDATE";
	
	/**
	 * 请款单类型
	 */
	/**
	 * 影院
	 */
	public static final String REQ_BILL_PLACE = "PLACE";
	/**
	 * 系统方-院线
	 */
	public static final String REQ_BILL_SYS_THRE = "SYS_THRE";
	
	/**
	 * SFTP字段
	 */
	public static final String SFTP_REQ_HOST = "host";
	public static final String SFTP_REQ_PORT = "port";
	public static final String SFTP_REQ_USERNAME = "username";
	public static final String SFTP_REQ_PASSWORD = "password";
	public static final int SFTP_DEFAULT_PORT = 22;
	
	public static final String COMM_Y = "Y";
	public static final String COMM_N = "N";
	
	/**
	 * 特殊结算标志
	 */
	public static final String WANDA_ACTIVITY = "WANDA_ACTIVITY"; //万达活动场
	public static final String WANDA_NOT_ACTIVITY = "WANDA_NOT_ACTIVITY"; //万达非活动场
	public static final Long HEPING_OUTER_PLACEID = Long.valueOf(299977977); //和平外部影院id
	
	/**
	 * 场次类型
	 */
	public static final String ZL = "ZL"; //直连场
	public static final String XN = "XN"; //虚拟场
	public static final String GW = "GW"; //格瓦票务
	public static final String WP = "WP"; //格瓦票务
	
	/**
	 * 付款方式
	 */
	public static final String BC = "BC"; //包场
	public static final String MEGER = "MEGER"; //合并
	public static final String SETTLE = "SETTLE"; //结算单付款
	public static final String RONGDUAN = "RONGDUAN"; //熔断
	public static final String DRAMA = "DRAMA"; //熔断
	
	public static final String HEPING_ROMNAME = "IMAX"; //
	
	/**
	 * 包场列表数据状态
	 */
	public static final String NOPAY = "NOPAY"; //未付款
	public static final String YESPAY = "YESPAY"; //已付款
	public static final String SELLET = "SELLET"; //已结算
	public static final String PASS = "PASS"; //已过期
	
	/**
	 * 用友对接数据状态
	 */
	public static final String JSSUBMIT = "JSSUBMIT"; //已提交
	public static final String YYGET = "YYGET"; //用友已获取
	public static final String YYCONFIRM = "YYCONFIRM"; //用友已确认
	public static final String YYPAYSECCUSS = "YYPAYSECCUSS"; //用友付款成功
	
	/**
	 * job状态
	 */
	public static final String RUNNING = "RUNNING"; //
	public static final String FINISH = "FINISH"; //
	
	/**
	 * 通道费结算，按月
	 */
	public static final String MONTH = "MONTH";
	
	/**
	 * 通道费结算 按多少天
	 */
	public static final String DAY = "DAY";
	public static final String WPDAY = "WPDAY";
	
	//影院系统数据类型
	public static final String SYSCATEGORY = "SYSCATEGORY";
	public static final String MEGERPAYVENDER = "MEGERPAYVENDER";
	public static final String INVOICEADMIN = "INVOICEADMIN";
	public static final String RESETADJUST = "RESETADJUST";
	
	//
	public static final String ZYFP = "ZYFP";
	public static final String PTFP = "PTFP";
	
	public static final String XGMNSR = "XGMNSR";
	public static final String YBNSR = "YBNSR";
	public static final String JYZS = "JYZS";
	
	public static final Long WPPARTNERID = 50001886l;
}
