package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.api.vo.ResultCode;
import com.gewara.constant.DramaConst;
import com.gewara.drama.dubbo.SupplierInfoVoService;
import com.gewara.drama.vo.SupplierInfoVo;
import com.gewara.model.drama.DramaBaseData;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaDiscountItem;
import com.gewara.model.drama.DramaOrderOffLineItem;
import com.gewara.model.drama.DramaOrderOffline;
import com.gewara.model.drama.DramaOrderOnLineItem;
import com.gewara.model.drama.DramaOrderOnline;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaRefundOffline;
import com.gewara.model.drama.DramaRefundOnline;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.DaoService;
import com.gewara.service.DramaDoColleService;
import com.gewara.service.DramaJobService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MapRow;
import com.gewara.util.WebLogger;

@Service("dramaJobServiceImpl")
public class DramaJobServiceImpl implements DramaJobService {

	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	@Autowired
	@Qualifier("dramaDoCollecServiceImpl")
	protected DramaDoColleService collectionService;
	
	@Autowired
	@Qualifier("supplierInfoService")
	protected SupplierInfoVoService supplierInfoService;
	
	
	/**
	 * 同步演出项目
	 */
	@Override
	public void syncDrama() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCDRAMA);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同步演出项目，同步时间范围：" + startTime + " ~ " + endTime);
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.recordid, v.dramaname, v.pretype, v.updatetime, v.adddate, v.releasedate, v.enddate, d.origin ");
		sql.append("from webdata.view_drama v  left outer join webdata.VIEW_DRAMAPROFILE d on d.DRAMAID = v.recordid ");
		sql.append("where v.updatetime >= ? and v.updatetime < ? ");
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString(), startTime, endTime);
		
		collectionService.updateDrama(mapList);
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("演出项目同步完成");
	}
	
	/**
	 * 同步演出版本配置
	 */
	@Override
	public void syncDramaConfig() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCDRAMACONFIG);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同步演出版本配置，同步时间范围：" + startTime + " ~ " + endTime);
		
		//查询要更新的项目id和供应商记录
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.dramaid, v.suppliercode, v.distype from webdata.view_drama_settleprice v ");
		sql.append("where v.addtime >= ? and v.addtime < ? ");
		sql.append("and v.dramaid is not null ");
		sql.append("group by v.dramaid, v.suppliercode, v.distype ");
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString(), startTime, endTime);
		
		//检查供应商是否有为空的，有空的邮件提醒
		List<Map<String, Object>> finalmapList = new ArrayList<Map<String, Object>>();
		StringBuilder sb = new StringBuilder("");
		for(Map<String, Object> map : mapList) {
			MapRow rowdconfig = new MapRow(map);
			String suppliercode = rowdconfig.getStringWithNull("suppliercode");
			String distype = rowdconfig.getStringWithNull("distype");
			if(StringUtils.isEmpty(suppliercode) || StringUtils.isEmpty(distype)) {
				long dramaid = rowdconfig.getLongWithNull("dramaid");
				sb.append("项目【" + dramaid + "】存在供应商编码或者扣率类型为空的配置，请及时与相关业务人员确认<br>");
			} else {
				finalmapList.add(map);
			}
		}
		
		for(Map<String, Object> map : finalmapList) {
			collectionService.processDramaVersionConfig(map, true);
		}
		
		if(!"".equals(sb.toString())) {
			logger.warn(sb.toString());
			GSPMaill.sendMaill(sb.toString(), GSPSendMaill.DRAMAMAIL);
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("演出版本配置同步完成");
	}
	
	
	/**
	 * 同步线上订单
	 */
	@Override
	public void syncOnlineOrder() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCONLINEORDER);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		if(DateUtil.getDiffDay(endTime, startTime) > 2) {
			endTime = DateUtil.addDay(startTime, 2);
		}
		logger.warn("开始同步线上订单，同步时间范围：" + startTime + " ~ " + endTime);
		
		//同步更新订单的RECORDID
		StringBuilder recordidsql = new StringBuilder("");
		recordidsql.append("select v.RECORDID from webdata.view4js_ticket_order v ");
		recordidsql.append("where v.ORDER_TYPE = 'drama' ");
		recordidsql.append("and v.SETTLE = 'Y' ");
		recordidsql.append("and v.STATUS in ('paid_success', 'paid_return') ");
		recordidsql.append("and v.UPDATETIME >= ? ");
		recordidsql.append("and v.UPDATETIME < ? ");
		List<Long> recordidList = shJdbcTemplate.queryForList(recordidsql.toString(), Long.class, startTime, endTime);
		
		if(CollectionUtils.isNotEmpty(recordidList)) {
			
			String orderquery = "select tradeno from drama_orderonline where tradeno = ? ";
			String orderupdate = "update drama_orderonline set status = ?, updatetime = ?, gspupdatetime = ? where tradeno = ? ";
			String itemquery = "select recordid from drama_orderonline_item where recordid = ? ";
			String itemupdate = "update drama_orderonline_item set gspupdatetime = ? where recordid = ? ";
			
			StringBuilder ordersql = new StringBuilder("");
			ordersql.append("select v.TRADE_NO as tradeno, v.STATUS as status, v.MOVIEID as dramaid, v.paidtime, ");
			ordersql.append("v.memberid, v.ADDTIME as addtime, v.UPDATETIME as updatetime, v.otherfee as otherfee, ");
			ordersql.append("v.discount, v.discount_reason as disreason ");
			ordersql.append("from webdata.view4js_ticket_order v where v.RECORDID = ? ");
			
			String cardsql = "select e.recordid, e.amount, e.soldtype, e.cardtype, e.tag from webdata.view_ecard4gsp e where e.orderid = ? ";
			String placeallowancesql = "select v.RECORDID, v.CARDTYPE, v.AMOUNT, v.tag from webdata.view4placeallowance v where v.ORDERID = ? ";
			
			StringBuilder itemsql = new StringBuilder("");
			itemsql.append("select v.recordid as recordid, v.placeid, v.quantity, v.totalfee, v.unitprice, ");
			itemsql.append("v.tipid, v.disid, v.disfee, v.taopiaonum, v.playid, v.playtime ");
			itemsql.append("from webdata.view_buyitem4gsp v where v.orderid = ? ");
			
			//循环每个orderid
			for(Long orderid : recordidList) {
				//更新订单
				Map<String, Object> ordermap = shJdbcTemplate.queryForMap(ordersql.toString(), orderid);
				MapRow orderrow = new MapRow(ordermap);
				String tradeno = orderrow.getStringWithNull("tradeno");
				String status = orderrow.getString("status");
				Long dramaid = orderrow.getLongWithNull("dramaid");
				Timestamp paidtime = orderrow.getTimestamp("paidtime");
				Long memberid = orderrow.getLongWithNull("memberid");
				Timestamp ordertime = orderrow.getTimestamp("addtime");
				Timestamp updatetime = orderrow.getTimestamp("updatetime");
				Double otherfee = orderrow.getDouble("otherfee");
				String disreason = orderrow.getStringWithNull("disreason");
				List<Map<String, Object>> orderexist = jdbcTemplate.queryForList(orderquery, tradeno);
				if(CollectionUtils.isNotEmpty(orderexist)) {
					jdbcTemplate.update(orderupdate, status, updatetime, DateUtil.getCurFullTimestamp(), tradeno);
				} else {
					DramaOrderOnline order = new DramaOrderOnline(tradeno, status, dramaid, paidtime, memberid, ordertime, 
							updatetime,  otherfee, disreason);
					daoService.addObject(order);
				}
				//更新商户ABCD券
				List<Map<String, Object>> cardlist = shJdbcTemplate.queryForList(cardsql, orderid);
				for(Map<String, Object> cardmap : cardlist) {
					MapRow rowcard = new MapRow(cardmap);
					Long recordid = rowcard.getLongWithNull("recordid");
					String tag = rowcard.getStringWithNull("tag");
					String soldtype = rowcard.getStringWithNull("soldtype");
					String cardtype = rowcard.getStringWithNull("cardtype");
					Double amount = rowcard.getDouble("amount");
					DramaDiscountItem discount = new DramaDiscountItem(recordid, tradeno, tag, cardtype, soldtype, amount);
					daoService.saveObject(discount);
				}
				//更新场馆补贴
				List<Map<String, Object>> allowancelist = shJdbcTemplate.queryForList(placeallowancesql, orderid);
				for(Map<String, Object> allowancemap : allowancelist) {
					MapRow rowallowance = new MapRow(allowancemap);
					Long recordid = rowallowance.getLongWithNull("RECORDID");
					String tag = rowallowance.getStringWithNull("tag");
					String cardtype = rowallowance.getStringWithNull("CARDTYPE");
					Double amount = rowallowance.getDouble("AMOUNT");
					DramaDiscountItem discount = new DramaDiscountItem(recordid, tradeno, tag, cardtype, null, amount);
					daoService.saveObject(discount);
				}
				//更新buyitem
				List<Map<String, Object>> itemMapList =  shJdbcTemplate.queryForList(itemsql.toString(), orderid);
				for(Map<String, Object> map : itemMapList) {
					MapRow itemrow = new MapRow(map);
					Long recordid = itemrow.getLongWithNull("recordid");
					Long placeid = itemrow.getLongWithNull("placeid");
					Long quantity = itemrow.getLong("quantity");
					Double totalfee = itemrow.getDouble("totalfee");
					Double disfee = itemrow.getDouble("disfee");
					Long tipid = itemrow.getLongWithNull("tipid");
					Long disid = itemrow.getLongWithNull("disid");
					Long taopiaonum = itemrow.getLong("taopiaonum");
					Long playid = itemrow.getLongWithNull("playid");
					Timestamp playtime = itemrow.getTimestamp("playtime");
					List<Map<String, Object>> itemexist = jdbcTemplate.queryForList(itemquery, recordid);
					if(CollectionUtils.isNotEmpty(itemexist)) {
						jdbcTemplate.update(itemupdate, DateUtil.getCurFullTimestamp(), recordid);
					} else {
						DramaOrderOnLineItem item = new DramaOrderOnLineItem(recordid, tradeno, placeid, quantity, totalfee, disfee, 
								playtime, playid, tipid, disid, taopiaonum);
						daoService.addObject(item);
					}
				}
				
			}
			
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("同步线上订单同步完成");
	}
	
	/**
	 * 同步线上退单
	 */
	@Override
	public void syncOnlineRefund() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCONLINEREFUND);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		if(DateUtil.getDiffDay(endTime, startTime) > 2) {
			endTime = DateUtil.addDay(startTime, 2);
		}
		logger.warn("开始同步线上退单，同步时间范围：" + startTime + " ~ " + endTime);
		
		StringBuilder refundsql = new StringBuilder("");
		refundsql.append("select r.tradeno, t.MOVIEID as dramaid, t.ADDTIME as ordertime, r.refundtime as refundtime, r.refundtype, t.SETTLE ");
		refundsql.append("from webdata.view_order_refund r , webdata.view4js_ticket_order t ");
		refundsql.append("where r.tradeno = t.TRADE_NO ");
		refundsql.append("and r.ordertype = 'drama' ");
		refundsql.append("and r.status = 'finish' ");
		refundsql.append("and r.orderstatus = 'paid_success' ");
		refundsql.append("and r.ticketrefund in ('Y_NORMAL_R', 'Y_MERCHANT_R', 'Y_MERCHANT_N') ");
		refundsql.append("and r.refundtime >= ? and r.refundtime < ? ");
		List<Map<String, Object>> refundMapList = shJdbcTemplate.queryForList(refundsql.toString(), startTime, endTime);
		
		if(CollectionUtils.isNotEmpty(refundMapList)) {
			
			String refundquery = "select tradeno from drama_refundonline where tradeno = ? ";
			String refundupdate = "update drama_refundonline set gspupdatetime = ? where tradeno = ? ";
			
			for(Map<String, Object> map : refundMapList) {
				MapRow row = new MapRow(map);
				String tradeno = row.getStringWithNull("tradeno"); 
				Long dramaid = row.getLongWithNull("dramaid");
				Timestamp ordertime = row.getTimestamp("ordertime");
				Timestamp refundtime = row.getTimestamp("refundtime");
				String refundtype = row.getStringWithNull("refundtype");
				String issettle = row.getStringWithNull("SETTLE");
				
				List<Map<String, Object>> exist = jdbcTemplate.queryForList(refundquery, tradeno);
				
				if(CollectionUtils.isNotEmpty(exist)) {
					jdbcTemplate.update(refundupdate, DateUtil.getCurFullTimestamp(), tradeno);
				} else {
					DramaRefundOnline refund = new DramaRefundOnline(tradeno, dramaid, ordertime, refundtime, refundtype, issettle);
					daoService.addObject(refund);
				}
			}
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("同步线上退单同步完成");
	}
	
	/**
	 * 同步线下订单
	 */
	@Override
	public void syncOfflineOrder() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCOFFLINEORDER);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		if(DateUtil.getDiffDay(endTime, startTime) > 2) {
			endTime = DateUtil.addDay(startTime, 2);
		}
		logger.warn("开始同步线下订单，同步时间范围：" + startTime + " ~ " + endTime);
		
		//同步更新订单的RECORDID
		StringBuilder recordidsql = new StringBuilder("");
		recordidsql.append("select v.RECORDID from webdata.view_manualdramaorder v where v.CHECKSTATUS = 'checked' and v.UPDATETIME >= ? and v.UPDATETIME < ? ");
		List<Long> recordidList = shJdbcTemplate.queryForList(recordidsql.toString(), Long.class, startTime, endTime);
		
		if(CollectionUtils.isNotEmpty(recordidList)) {
			
			String orderquery = "select tradeno from drama_orderoffline where tradeno = ? ";
			String orderupdate = "update drama_orderoffline set status = ?, updatetime = ?, gspupdatetime = ? where tradeno = ? ";
			String itemquery = "select recordid from drama_orderoffline_item where recordid = ? ";
			String itemupdate = "update drama_orderoffline_item set gspupdatetime = ? where recordid = ? ";
			
			//每次同步500个
			List<List<Long>> groupList = BeanUtil.partition(recordidList, 500);
			for(List<Long> recordids : groupList) {
				//查询订单
				StringBuilder ordersql = new StringBuilder("");
				ordersql.append("select m.TRADE_NO as tradeno, m.CHECKSTATUS as status, m.DRAMAID as dramaid, m.paymethod, ");
				ordersql.append("m.CREATETIME as paidtime, m.CREATETIME as addtime, m.UPDATETIME as updatetime, m.clientname, m.expressfee ");
				ordersql.append("from webdata.view_manualdramaorder m where m.RECORDID in " + DramaUtil.sqlinList(recordids, Long.class));
				List<Map<String, Object>> orderMapList =  shJdbcTemplate.queryForList(ordersql.toString());
				//更新订单
				for(Map<String, Object> map : orderMapList) {
					MapRow row = new MapRow(map);
					String tradeno = row.getStringWithNull("tradeno");
					String status = row.getString("status");
					Long dramaid = row.getLongWithNull("dramaid");
					Timestamp paidtime = row.getTimestamp("paidtime");
					Timestamp ordertime = row.getTimestamp("addtime");
					Timestamp updatetime = row.getTimestamp("updatetime");
					Double otherfee = row.getDouble("expressfee");
					String paymethod = row.getStringWithNull("paymethod");
					String username = row.getStringWithNull("clientname");
					
					List<Map<String, Object>> exist = jdbcTemplate.queryForList(orderquery, tradeno);
					
					if(CollectionUtils.isNotEmpty(exist)) {
						jdbcTemplate.update(orderupdate, status, updatetime, DateUtil.getCurFullTimestamp(), tradeno);
					} else {
						DramaOrderOffline order = new DramaOrderOffline(tradeno, status, dramaid, username, paidtime, 
								ordertime, updatetime, otherfee, paymethod);
						daoService.addObject(order);
					}
				}
				
				//查询buyitem
				StringBuilder itemsql = new StringBuilder("");
				itemsql.append("select m.TRADE_NO as tradeno,  b.RECORDID, b.PLACEID, b.QUANTITY, b.TOTALFEE, b.DISFEE, b.tipid, b.DISID, b.taopiaonum, b.playid, b.PLAYTIME ");
				itemsql.append("from webdata.view_manualbuyitem4gsp b left outer join webdata.view_manualdramaorder m on b.ORDERID = m.RECORDID ");
				itemsql.append("where b.ORDERID in " + DramaUtil.sqlinList(recordids, Long.class));
				List<Map<String, Object>> itemMapList =  shJdbcTemplate.queryForList(itemsql.toString());
				//更新buyitem
				for(Map<String, Object> map : itemMapList) {
					MapRow row = new MapRow(map);
					Long recordid = row.getLongWithNull("RECORDID");
					String tradeno = row.getStringWithNull("tradeno");
					Long placeid = row.getLongWithNull("PLACEID");
					Long quantity = row.getLong("QUANTITY");
					Double totalfee = row.getDouble("TOTALFEE");
					Double disfee = row.getDouble("DISFEE");
					Long tipid = row.getLongWithNull("tipid");
					Long disid = row.getLongWithNull("DISID");
					Long taopiaonum = row.getLong("taopiaonum");
					Long playid = row.getLongWithNull("playid");
					Timestamp playtime = row.getTimestamp("PLAYTIME");
					
					List<Map<String, Object>> exist = jdbcTemplate.queryForList(itemquery, recordid);
					
					if(CollectionUtils.isNotEmpty(exist)) {
						jdbcTemplate.update(itemupdate, DateUtil.getCurFullTimestamp(), recordid);
					} else {
						DramaOrderOffLineItem item = new DramaOrderOffLineItem(recordid, tradeno, placeid, quantity,
								totalfee, disfee, playtime, playid, tipid, disid, taopiaonum);
						daoService.addObject(item);
					}
				}
			}
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("线下订单同步完成");
	}
	
	/**
	 * 同步线下退单
	 */
	@Override
	public void syncOfflineRefund() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNCOFFLINEREFUND);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		if(DateUtil.getDiffDay(endTime, startTime) > 2) {
			endTime = DateUtil.addDay(startTime, 2);
		}
		logger.warn("开始同步线下退单，同步时间范围：" + startTime + " ~ " + endTime);
		
		StringBuilder refundsql = new StringBuilder("");
		refundsql.append("select v.TRADE_NO as tradeno, v.DRAMAID as dramaid, v.CREATETIME as addtime, v.UPDATETIME as refundtime ");
		refundsql.append("from webdata.view_manualdramaorder v where v.CHECKSTATUS = 'checked' and v.ISSUESTATUS = 'back' and v.UPDATETIME >= ? and v.UPDATETIME < ?");
		List<Map<String, Object>> refundMapList = shJdbcTemplate.queryForList(refundsql.toString(), startTime, endTime);
		
		if(CollectionUtils.isNotEmpty(refundMapList)) {
			
			String refundquery = "select tradeno from drama_refundoffline where tradeno = ? ";
			String refundupdate = "update drama_refundoffline set gspupdatetime = ? where tradeno = ? ";
			
			for(Map<String, Object> map : refundMapList) {
				MapRow row = new MapRow(map);
				String tradeno = row.getStringWithNull("tradeno"); 
				Long dramaid = row.getLongWithNull("dramaid");
				Timestamp ordertime = row.getTimestamp("addtime");
				Timestamp refundtime = row.getTimestamp("refundtime");
				
				List<Map<String, Object>> exist = jdbcTemplate.queryForList(refundquery, tradeno);
				
				if(CollectionUtils.isNotEmpty(exist)) {
					jdbcTemplate.update(refundupdate, DateUtil.getCurFullTimestamp(), tradeno);
				} else {
					DramaRefundOffline refund = new DramaRefundOffline(tradeno, dramaid, ordertime, refundtime);
					daoService.addObject(refund);
				}
			}
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("线下退单同步完成");
	}
	
	/**
	 * 同步票务订单
	 */
	@Override
	public void syncPiaowuOrder() {
		
	}
	
	/**
	 * 同步票务退单
	 */
	@Override
	public void syncPiaowuRefund() {
		
	}

	/**
	 * 同步演出场次信息
	 */
	@Override
	public void syncDramaPlayItem() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNSPLAYITEM);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同步演出场次信息，同步时间范围：" + startTime + " ~ " + endTime);
		
		String sql = "select v.recordid, v.dpid, v.dramaid, v.playtime from WEBDATA.VIEW_OPEN_DRAMA_ITEM v where v.updatetime >= ? and v.updatetime < ? ";
		List<Map<String, Object>> maplist = shJdbcTemplate.queryForList(sql, startTime, endTime);
		
		for(Map<String, Object> map : maplist) {
			MapRow row = new MapRow(map);
			Long recordid = row.getLongWithNull("recordid");
			Long dpid = row.getLongWithNull("dpid");
			Long dramaid = row.getLongWithNull("dramaid");
			Timestamp playtime = row.getTimestamp("playtime");
			DramaPlayItem playitem = new DramaPlayItem(recordid, dpid, dramaid, playtime);
			daoService.saveObject(playitem);
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("演出场次信息同步完成");
	}
	
	/**
	 * 同步演出供应商信息
	 */
	@Override
	public void syncDramaSupplier() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNSUPPLIER);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同步演出供应商信息，同步时间范围：" + startTime + " ~ " + endTime);
		
		ResultCode<List<SupplierInfoVo>> resultcode = supplierInfoService.getSupplierInfoListByUpdateTime(startTime, endTime);
		if(!resultcode.isSuccess()) {
			logger.warn("同步演出供应商信息出错：【" + resultcode.getErrcode() + ":" + resultcode.getMsg() + "】");
			return;
		}
		List<SupplierInfoVo> supplierList = resultcode.getRetval();
		String existsql = "select recordid from drama_basedata where type = ? and code = ? ";
		String updatesql = "update drama_basedata set name = ?, special = ?, updatetime = ?, gspupdatetime = ? where type = ? and code = ? ";
		for(SupplierInfoVo sinfo : supplierList) {
			
			String code = sinfo.getSupplierCode();
			String name = sinfo.getSuppliername();
			Timestamp updatetime = sinfo.getUpdateTime();
			String special = "{\"bankAccount\":\"" + sinfo.getBankAccount() + "\",\"bankName\":\"" + sinfo.getBankName() + "\"}";
			
			List<Map<String, Object>> exist = jdbcTemplate.queryForList(existsql, DramaConst.SUPPLIER, code);
			if(CollectionUtils.isNotEmpty(exist)) {
				jdbcTemplate.update(updatesql, name, special, updatetime, DateUtil.getCurFullTimestamp(), DramaConst.SUPPLIER, code);
			} else {
				DramaBaseData data = new DramaBaseData(code, name, DramaConst.SUPPLIER, updatetime);
				data.setSpecial(special);
				daoService.addObject(data);
			}
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("演出供应商信息同步完成");
	}
	
	/**
	 * 同步演出场馆信息
	 */
	@Override
	public void syncDramaPlace() {
		SyncMark mark = daoService.getObject(SyncMark.class, DramaConst.SYNPLACE);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同步演出场馆信息，同步时间范围：" + startTime + " ~ " + endTime);
		
		String viewsql = "select v.recordid, v.name, v.updatetime from webdata.view_theatre v where v.updatetime >= ? and v.updatetime < ? ";
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(viewsql, startTime, endTime);
		
		String existsql = "select recordid from drama_basedata where type = ? and code = ? ";
		String updatesql = "update drama_basedata set name = ?, updatetime = ?, gspupdatetime = ? where type = ? and code = ? ";
		for(Map<String, Object> map : mapList) {
			String code = map.get("recordid").toString();
			String name = map.get("name") == null ? null : map.get("name").toString();
			Timestamp updatetime = map.get("updatetime") == null ? null : (Timestamp)map.get("updatetime");
			
			List<Map<String, Object>> exist = jdbcTemplate.queryForList(existsql, DramaConst.PLACE, code);
			if(CollectionUtils.isNotEmpty(exist)) {
				jdbcTemplate.update(updatesql, name, updatetime, DateUtil.getCurFullTimestamp(), DramaConst.PLACE, code);
			} else {
				DramaBaseData data = new DramaBaseData(code, name, DramaConst.PLACE, updatetime);
				daoService.addObject(data);
			}
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("演出场馆信息同步完成");
	}
	
	
	/**
	 * 自动更新未审核的配置
	 *  每天去同步WAITAPPROVAL配置的时候，会将WAITAPPROVAL状态下的扣率配置删除
	 *  （若不用再去同步WAITAPPROVAL配置的时候，则WAITAPPROVAL状态下的扣率配置可以置为无效，
	 *   但是财务人员不会每天都去审批，演出业务人员有时候也不把配置一次性配全，所以只好每天去同步WAITAPPROVAL配置）
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void sysWaitApprovalDramaConfig() {
		logger.warn("开始自动更新未审核的配置");
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.or(Restrictions.eq("status", DramaConst.WAITFINISH),
				Restrictions.eq("status", DramaConst.WAITAPPROVAL)));
		query.add(Restrictions.eq("distype", DramaConst.PERCENT));
		query.add(Restrictions.ge("endtime", DateUtil.getCurFullTimestamp()));
		List<DramaConfig> configList = daoService.findByCriteria(query);
		
		for(DramaConfig dconfig : configList) {
			collectionService.updateDramConfig(dconfig, true);
		}
		
		logger.warn("自动更新未审核的配置完成");
	}
	
	
	/**
	 * 生成结算单据
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void generateSettltBill() {
		logger.warn("开始生成结算单");
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.or(Restrictions.eq("status", DramaConst.HASAPPROVAL), 
				Restrictions.eq("status", DramaConst.SETTLEING)));
		List<DramaConfig> configList = daoService.findByCriteria(query);
		
		for(DramaConfig dconfig : configList) {
			collectionService.createSettleBill(dconfig);
		}
		
		logger.warn("生成结算单完成");
	}
	
	/**
	 * 自动检查同步基础数据的job
	 *  检查job的LastExecuteTime，如日期不是当天，则发邮件报警
	 */
	@Override
	public boolean checkSysDataJob() {
		logger.warn("开始自动检查同步基础数据的job");
		boolean flag = true;
		
		List<DramaBaseData> dataList = daoService.getObjectListByField(DramaBaseData.class, "type", DramaConst.SYSDATAJOB);
		Date currdate = DateUtil.getCurDate();
		StringBuilder sb = new StringBuilder("");
		
		//检出同步job是否执行完成
		for(DramaBaseData data : dataList) {
			SyncMark mark = daoService.getObject(SyncMark.class, data.getCode());
			Timestamp time = mark.getLastExecuteTime();
			if(time.getTime() != currdate.getTime()) {
				sb.append("【" + data.getName() + ":" + data.getCode() + "】执行的最后时间不是当天，请检查确认！<br>");
			}
		}
		
		if(!"".equals(sb.toString())) {
			sb.append("检查日期：" + DateUtil.formatDate(currdate) + "，此次结算单计算定时任务将终止!");
			logger.error(sb.toString());
			GSPMaill.sendMaill(sb.toString(), GSPSendMaill.DRAMAMAIL);
			flag = false;
			return flag;
		}
		
		//检查前40天订单量是否同步有出入（从4点多同步完订单到执行检验的程序中间时段如果有退票会导致这个检验失效）
		Date lastdata = DateUtil.addDay(currdate, -40);
		
		String onlinegspsql = "select count(1) as gspcount from drama_orderonline where ordertime >= ? and ordertime < ? ";
		String onlineshsql = "select count(1) as shcount from webdata.view4js_ticket_order v where v.ORDER_TYPE = 'drama' and v.SETTLE = 'Y' and v.STATUS in ('paid_success', 'paid_return') and v.ADDTIME >= ? and v.ADDTIME < ?";
		Map<String, Object> gsponline = jdbcTemplate.queryForMap(onlinegspsql, lastdata, currdate);
		Map<String, Object> shonline = shJdbcTemplate.queryForMap(onlineshsql, lastdata, currdate);
		Long gsponlinecount = Long.valueOf(gsponline.get("gspcount").toString());
		Long shonlinecount = Long.valueOf(shonline.get("shcount").toString());
		if(Math.abs(gsponlinecount.longValue() - shonlinecount.longValue()) > 10) {
			sb.append("演出线上订单量异常：" + DateUtil.format(lastdata, "yyyy-MM-dd") + " - " + DateUtil.format(currdate, "yyyy-MM-dd"));
			sb.append("上海线上订单量：" + shonlinecount.longValue() + "，结算系统订单量：" + gsponlinecount.longValue() + "<br>");
		}
		
		String offlinegspsql = "select count(1) as gspcount from drama_orderoffline where ordertime >= ? and ordertime < ? ";
		String offlineshsql = "select count(1) as shcount from webdata.view_manualdramaorder v where v.CHECKSTATUS = 'checked' and v.CREATETIME >= ? and v.CREATETIME < ? ";
		Map<String, Object> gspoffline = jdbcTemplate.queryForMap(offlinegspsql, lastdata, currdate);
		Map<String, Object> shoffline = shJdbcTemplate.queryForMap(offlineshsql, lastdata, currdate);
		Long gspofflinecount = Long.valueOf(gspoffline.get("gspcount").toString());
		Long shofflinecount = Long.valueOf(shoffline.get("shcount").toString());
		if(Math.abs(gspofflinecount.longValue() - shofflinecount.longValue()) > 10) {
			sb.append("演出线下订单量异常：" + DateUtil.format(lastdata, "yyyy-MM-dd") + " - " + DateUtil.format(currdate, "yyyy-MM-dd"));
			sb.append("上海线上订单量：" + gspofflinecount.longValue() + "，结算系统订单量：" + shofflinecount.longValue() + "<br>");
		}
		
		if(!"".equals(sb.toString())) {
			sb.append("检查日期：" + DateUtil.formatDate(currdate) + "，此次单据计算定时任务将终止!");
			logger.error(sb.toString());
			GSPMaill.sendMaill(sb.toString(), GSPSendMaill.DRAMAMAIL);
			flag = false;
			return flag;
		}
		
		logger.warn("自动检查同步基础数据的job完成");
		return flag;
	}
	
}

