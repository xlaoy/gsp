package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.PointCardOrder;
import com.gewara.model.gsp.PointCardPlaceBill;
import com.gewara.model.gsp.PointCardSettleBill;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.DaoService;
import com.gewara.service.PointCardService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MapRow;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;

@Service("pointCardService")
public class PointCardServiceImpl implements PointCardService {

	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	public static final String SYNC_POINTCARD_ORDER = "syncPointCardOrder";
	
	public static final String MOVIE = "movie";
	
	public static final String PAID_RETURN = "paid_return";
	
	
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	/**
	 * 查询点卡订单
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<PointCardOrder> getPointCardOrders(Long partnerid, Long cinemaid, Timestamp starttime, Timestamp endtime, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardOrder.class);
		if(partnerid != null) {
			query.add(Restrictions.eq("partnerid", partnerid));
		}
		if(cinemaid != null) {
			query.add(Restrictions.eq("cinemaid", cinemaid));
		}
		if(starttime != null) {
			query.add(Restrictions.ge("ordertime", starttime));
		}
		if(endtime != null) {
			query.add(Restrictions.lt("ordertime", endtime));
		}
		if(!StringUtils.isEmpty(status)) {
			query.add(Restrictions.eq("status", status));
		}
		query.add(Restrictions.eq("ordertype", "movie"));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * 同步点卡订单
	 */
	@Override
	public void sysPointCardOrder() {
		SyncMark mark = daoService.getObject(SyncMark.class, SYNC_POINTCARD_ORDER);
		Timestamp startTime = mark.getLastExecuteTime();
		//截止时间今天00:00:00
		Timestamp endTime = new Timestamp(DateUtil.getCurDate().getTime());
		if(startTime.getTime() >= endTime.getTime()) {
			return;
		}
		logger.warn("开始同点卡订单，同步时间范围：" + startTime + " ~ " + endTime);
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select recordid, partnerid, outtradeno, tradeno, status, relatedid, cardno, orderamount, ");
		sql.append("carduseamount, outamount, ordertime, addtime, updatetime, cardtype, ordertype, cinemaid, cinemaname, ");
		sql.append("movieid, moviename, edition, movieitemid, roomid, playroom, playtime, ticketnum ");
		sql.append("from WEBDATA.VIEW_PARTNER_CARD_ORDER ");
		sql.append("where updatetime >= ? and updatetime < ? ");
		
		List<Map<String, Object>> maplist = shJdbcTemplate.queryForList(sql.toString(), startTime, endTime);
		for(Map<String, Object> map : maplist) {
			MapRow row = new MapRow(map);
			PointCardOrder order = new PointCardOrder();
			order.setRecordid(row.getLongWithNull("recordid"));
			order.setPartnerid(row.getLongWithNull("partnerid"));
			order.setOuttradeno(row.getStringWithNull("outtradeno"));
			order.setTradeno(row.getStringWithNull("tradeno"));
			order.setStatus(row.getStringWithNull("status"));
			order.setRelatedid(row.getLongWithNull("relatedid"));
			order.setCardno(row.getStringWithNull("cardno"));
			order.setOrderamount(row.getDouble("orderamount"));
			order.setOutamount(row.getDouble("outamount"));
			order.setCarduseamount(row.getDouble("carduseamount"));
			order.setOrdertime(row.getTimestamp("ordertime"));
			order.setUpdatetime(row.getTimestamp("updatetime"));
			order.setCardtype(row.getStringWithNull("cardtype"));
			order.setOrdertype(row.getStringWithNull("ordertype"));
			order.setCinemaid(row.getLongWithNull("cinemaid"));
			order.setCinemaname(row.getStringWithNull("cinemaname"));
			order.setPlaytime(row.getTimestamp("playtime"));
			order.setTicketnum(row.getInteger("ticketnum"));
			StringBuilder otherinfo = new StringBuilder("");
			otherinfo.append("{\"场次id\":\"" + row.getLong("getLong") + "\",");
			otherinfo.append("\"电影id\":\"" + row.getLong("movieid") + "\",");
			otherinfo.append("\"电影名称\":\"" + row.getString("moviename") + "\",");
			otherinfo.append("\"影厅id\":\"" + row.getLong("roomid") + "\",");
			otherinfo.append("\"放映厅名称\":\"" + row.getString("playroom") + "\"}");
			order.setOtherinfo(otherinfo.toString());
			daoService.saveObject(order);
		}
		
		mark.setLastExecuteTime(endTime);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
		logger.warn("同步点卡订单同步完成");
	}
	
	/**
	 * 生成系统月账单
	 */
	@Override
	public PointCardSettleBill createPointCardMonthSettleBill(Long partnerid, Timestamp start, boolean recreate) {
		if(partnerid == null) {
			return null;
		}
		if(start == null) {
			return null;
		}
		if(recreate) {
			clearPointCardBill(partnerid, start);
		}
		Timestamp end = MiscUtil.addMonth(start, 1);
		PointCardSettleBill bill = new PointCardSettleBill(partnerid, SettleConstant.MONTH, start, end, null);
		daoService.addObject(bill);
		return bill;
	}
	
	/**
	 * 清除账单
	 * @param partnerid
	 * @param start
	 */
	private void clearPointCardBill(Long partnerid, Timestamp start) {
		PointCardSettleBill bill = getPointCardMonthSettleBill(partnerid, start);
		if(bill == null) {
			return;
		}
		//删除影院月账单
		String delplacemonthsql = "delete from pointcard_placebill where ctype = 'MONTH' and relatedbillid = ? ";
		jdbcTemplate.update(delplacemonthsql, bill.getRecordid());
		//删除影院日账单
		String delplacedaysql = "delete from pointcard_placebill where ctype = 'DAY' and relatedbillid in (select recordid from pointcard_settlebill where ctype = 'DAY' and relatedbillid = ? ) ";
		jdbcTemplate.update(delplacedaysql, bill.getRecordid());
		//删除系统日账单
		String delsettledaysql = "delete from pointcard_settlebill where ctype = 'DAY' and relatedbillid = ? ";
		jdbcTemplate.update(delsettledaysql, bill.getRecordid());
		//删除系统月账单
		String delsettlemonthsql = "delete from pointcard_settlebill where ctype = 'MONTH' and recordid = ? ";
		jdbcTemplate.update(delsettlemonthsql, bill.getRecordid());
	}
	
	/**
	 * 生成系统日账单
	 */
	@Override
	public List<PointCardSettleBill> createPointCardDaySettleBill(PointCardSettleBill bill) {
		if(bill == null) {
			return null;
		}
		List<PointCardSettleBill> daybilllist = new ArrayList<PointCardSettleBill>();
		Timestamp start = bill.getStarttime();
		while(start.before(bill.getEndtime())) {
			Timestamp end = DateUtil.addDay(start, 1);
			PointCardSettleBill daybill = new PointCardSettleBill(bill.getPartnerid(), SettleConstant.DAY, start, end, bill.getRecordid());
			daoService.addObject(daybill);
			daybilllist.add(daybill);
			start = end;
		}
		return daybilllist;
	}
	
	
	/**
	 * 生成影院日账单
	 */
	@Override
	public List<Map<String, Object>> createPointCardDayPlaceBill(List<PointCardSettleBill> daybilllist, PointCardSettleBill bill) {
		if(CollectionUtils.isEmpty(daybilllist)) {
			return null;
		}
		String sql = "select cinemaid, cinemaname from pointcard_order where partnerid = ? and ordertype = ? and ordertime >= ? and ordertime < ? group by cinemaid, cinemaname ";
		List<Map<String, Object>> placelist = jdbcTemplate.queryForList(sql, bill.getPartnerid(), MOVIE, bill.getStarttime(), bill.getEndtime());
		if(CollectionUtils.isEmpty(placelist)) {
			logger.warn("createPointCardDayPlaceBill:没有查询到影院订单！");
			return null;
		}
		for(Map<String, Object> place : placelist) {
			MapRow row = new MapRow(place);
			for(PointCardSettleBill daybill : daybilllist) {
				PointCardPlaceBill placebill = new PointCardPlaceBill(bill.getPartnerid(), row.getLongWithNull("cinemaid"), 
						row.getStringWithNull("cinemaname"), SettleConstant.DAY, daybill.getStarttime(), 
						daybill.getEndtime(), daybill.getRecordid());
				daoService.addObject(placebill);
			}
		}
		return placelist;
	}
	
	
	/**
	 * 生成影院月账单
	 */
	@Override
	public void createPointCardMonthPlaceBill(PointCardSettleBill bill, List<Map<String, Object>> placelist) {
		if(bill == null) {
			return;
		}
		if(CollectionUtils.isEmpty(placelist)) {
			return;
		}
		for(Map<String, Object> place : placelist) {
			MapRow row = new MapRow(place);
			PointCardPlaceBill placebill = new PointCardPlaceBill(bill.getPartnerid(), row.getLongWithNull("cinemaid"), 
					row.getStringWithNull("cinemaname"), SettleConstant.MONTH, bill.getStarttime(), 
					bill.getEndtime(), bill.getRecordid());
			daoService.addObject(placebill);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PointCardSettleBill getPointCardMonthSettleBill(Long partnerid, Timestamp start) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardSettleBill.class);
		query.add(Restrictions.eq("partnerid", partnerid));
		query.add(Restrictions.eq("starttime", start));
		query.add(Restrictions.eq("ctype", SettleConstant.MONTH));
		List<PointCardSettleBill> billlist = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(billlist)) {
			return null;
		}
		if(billlist.size() > 1) {
			logger.warn("getPointCardMonthSettleBill:系统结算单查询多条记录错误！");
			return null;
		}
		return billlist.get(0);
	}
	
	/**
	 * 计算影院日账单
	 */
	@Override
	public void calculatePointCardDayPlaceBill(PointCardSettleBill bill) {
		if(bill == null) {
			return;
		}
		List<PointCardSettleBill> daybillist = getPointCardDaySettleBillByMonth(bill);
		if(CollectionUtils.isEmpty(daybillist)) {
			logger.warn("calculatePointCardDayPlaceBill:系统日账单为空！");
			return;
		}
		for(PointCardSettleBill daybill : daybillist) {
			List<PointCardPlaceBill> placedaybillist = getPointCardDayPlaceBillByDaySettle(daybill);
			for(PointCardPlaceBill placedaybill : placedaybillist) {
				//出票
				List<PointCardOrder> orderlist = getPointCardOrders(bill.getPartnerid(), placedaybill.getCinemaid(), 
						placedaybill.getStarttime(), placedaybill.getEndtime(), null);
				for(PointCardOrder order : orderlist) {
					placedaybill.setSuccessnum(placedaybill.getSuccessnum() + order.getTicketnum());
					placedaybill.setSuccessamount(placedaybill.getSuccessamount() + order.getCarduseamount());
				}
				
				//退票
				List<PointCardOrder> refundlist = getPointCardOrders(bill.getPartnerid(), placedaybill.getCinemaid(), 
						placedaybill.getStarttime(), placedaybill.getEndtime(), PAID_RETURN);
				for(PointCardOrder refund : refundlist) {
					placedaybill.setRefundnum(placedaybill.getRefundnum() + refund.getTicketnum());
					placedaybill.setRefundamount(placedaybill.getRefundamount() + refund.getCarduseamount());
				}
				
				placedaybill.setTotalnum(placedaybill.getSuccessnum() - placedaybill.getRefundnum());
				placedaybill.setTotalamount(placedaybill.getSuccessamount() - placedaybill.getRefundamount());
				placedaybill.setKpamount(placedaybill.getTotalamount());
				daoService.updateObject(placedaybill);
			}
		}
	}
	
	
	/**
	 * 计算系统日账单
	 */
	@Override
	public void calculatePointCardDaySettleBill(PointCardSettleBill bill) {
		if(bill == null) {
			return;
		}
		List<PointCardSettleBill> daybillist = getPointCardDaySettleBillByMonth(bill);
		if(CollectionUtils.isEmpty(daybillist)) {
			logger.warn("calculatePointCardDaySettleBill:系统日账单为空！");
			return;
		}
		for(PointCardSettleBill daybill : daybillist) {
			List<PointCardPlaceBill> placedaybillist = getPointCardDayPlaceBillByDaySettle(daybill);
			for(PointCardPlaceBill placedaybill : placedaybillist) {
				//出票
				daybill.setSuccessnum(daybill.getSuccessnum() + placedaybill.getSuccessnum());
				daybill.setSuccessamount(daybill.getSuccessamount() + placedaybill.getSuccessamount());
				
				//退票
				daybill.setRefundnum(daybill.getRefundnum() + placedaybill.getRefundnum());
				daybill.setRefundamount(daybill.getRefundamount() + placedaybill.getRefundamount());
				
				daybill.setTotalnum(daybill.getTotalnum() + placedaybill.getTotalnum());
				daybill.setTotalamount(daybill.getTotalamount() + placedaybill.getTotalamount());
				daybill.setKpamount(daybill.getKpamount() + placedaybill.getKpamount());
				
				daoService.updateObject(daybill);
			}
		}
	}
	
	/**
	 * 计算系统月账单
	 */
	@Override
	public void calculatePointCardMonthSettleBill(PointCardSettleBill bill) {
		if(bill == null) {
			return;
		}
		List<PointCardSettleBill> daybillist = getPointCardDaySettleBillByMonth(bill);
		if(CollectionUtils.isEmpty(daybillist)) {
			logger.warn("calculatePointCardMonthSettleBill:系统日账单为空！");
			return;
		}
		for(PointCardSettleBill daybill : daybillist) {
			//出票
			bill.setSuccessnum(bill.getSuccessnum() + daybill.getSuccessnum());
			bill.setSuccessamount(bill.getSuccessamount() + daybill.getSuccessamount());
			
			//退票
			bill.setRefundnum(bill.getRefundnum() + daybill.getRefundnum());
			bill.setRefundamount(bill.getRefundamount() + daybill.getRefundamount());
			
			bill.setTotalnum(bill.getTotalnum() + daybill.getTotalnum());
			bill.setTotalamount(bill.getTotalamount() + daybill.getTotalamount());
			bill.setKpamount(bill.getKpamount() + daybill.getKpamount());
			
			daoService.updateObject(bill);
		}
	}
	
	/**
	 * 计算影院月账单
	 */
	@Override
	public void calculatePointCardPlacePlaceBill(PointCardSettleBill bill) {
		if(bill == null) {
			return;
		}
		List<PointCardPlaceBill> placebillist = getPointCardMonthPlaceBillBySettle(bill);
		if(CollectionUtils.isEmpty(placebillist)) {
			logger.warn("calculatePointCardPlacePlaceBill:影院月账单为空！");
			return;
		}
		for(PointCardPlaceBill monthbill : placebillist) {
			List<PointCardPlaceBill> placedaybillist = getPointCardDayPlaceBillByMonth(monthbill);
			if(CollectionUtils.isEmpty(placedaybillist)) {
				continue;
			}
			for(PointCardPlaceBill daybill : placedaybillist) {
				//出票
				monthbill.setSuccessnum(monthbill.getSuccessnum() + daybill.getSuccessnum());
				monthbill.setSuccessamount(monthbill.getSuccessamount() + daybill.getSuccessamount());
				
				//退票
				monthbill.setRefundnum(monthbill.getRefundnum() + daybill.getRefundnum());
				monthbill.setRefundamount(monthbill.getRefundamount() + daybill.getRefundamount());
				
				monthbill.setTotalnum(monthbill.getTotalnum() + daybill.getTotalnum());
				monthbill.setTotalamount(monthbill.getTotalamount() + daybill.getTotalamount());
				monthbill.setKpamount(monthbill.getKpamount() + daybill.getKpamount());
				
				daoService.updateObject(monthbill);
			}
		}
	}
	
	/**
	 * 根据系统月账单获取系统日账单列表
	 * @param monthbill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<PointCardSettleBill> getPointCardDaySettleBillByMonth(PointCardSettleBill bill) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardSettleBill.class);
		query.add(Restrictions.eq("partnerid", bill.getPartnerid()));
		query.add(Restrictions.eq("ctype", SettleConstant.DAY));
		query.add(Restrictions.eq("relatedbillid", bill.getRecordid()));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * 根据系统日账单获取影院日账单列表
	 * @param monthbill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<PointCardPlaceBill> getPointCardDayPlaceBillByDaySettle(PointCardSettleBill daybill) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardPlaceBill.class);
		query.add(Restrictions.eq("partnerid", daybill.getPartnerid()));
		query.add(Restrictions.eq("ctype", SettleConstant.DAY));
		query.add(Restrictions.eq("relatedbillid", daybill.getRecordid()));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * 根据系统月账单获取影院月账单列表
	 * @param monthbill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<PointCardPlaceBill> getPointCardMonthPlaceBillBySettle(PointCardSettleBill bill) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardPlaceBill.class);
		query.add(Restrictions.eq("partnerid", bill.getPartnerid()));
		query.add(Restrictions.eq("ctype", SettleConstant.MONTH));
		query.add(Restrictions.eq("relatedbillid", bill.getRecordid()));
		return daoService.findByCriteria(query);
	}
	
	/**
	 * 根据影院月账单获取影院日账单列表
	 * @param monthbill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<PointCardPlaceBill> getPointCardDayPlaceBillByMonth(PointCardPlaceBill monthbill) {
		DetachedCriteria query = DetachedCriteria.forClass(PointCardPlaceBill.class);
		query.add(Restrictions.eq("partnerid", monthbill.getPartnerid()));
		query.add(Restrictions.eq("cinemaid", monthbill.getCinemaid()));
		query.add(Restrictions.eq("ctype", SettleConstant.DAY));
		query.add(Restrictions.ge("starttime", monthbill.getStarttime()));
		query.add(Restrictions.le("endtime", monthbill.getEndtime()));
		return daoService.findByCriteria(query);
	}
}
