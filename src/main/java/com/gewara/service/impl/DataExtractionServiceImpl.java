/** 
 */
package com.gewara.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.SettleConstant;
import com.gewara.dao.DataExtractionDao;
import com.gewara.enums.DataExtSqlEmums;
import com.gewara.enums.PlaceTypeEnums;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.DiffPriceOrder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.PricedPlayItem;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleConfigUpd;
import com.gewara.model.gsp.SyncMark;
import com.gewara.model.gsp.SysData;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.DaoService;
import com.gewara.service.DataExtractionService;
import com.gewara.service.SyncMarkService;
import com.gewara.service.SysDataService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.MapRow;
import com.gewara.util.MiscUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  10:14:44 AM
 */
@Service("dataExtractionService")
public class DataExtractionServiceImpl implements DataExtractionService{
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("syncMarkService")
	private SyncMarkService syncMarkService;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	@Autowired
	@Qualifier("dataExtractionDao")
	private DataExtractionDao dataExtractionDao;
	
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	
	private final transient GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	private static final String TAG_SYNC_ORDER = "syncOrder";
	private static final String TAG_SYNC_REFUNDMENT = "syncRefundment";
	private static final String TAG_SYNC_PLAYITEM = "syncPlayItem";
	private static final String TAG_SYNC_GOODS = "syncGoods";
	private static final String TAG_SYNC_SETTLECONFIG = "syncSettleConfig";
	private static final String TAG_SYNC_CHANNELCONFIG = "syncChannelConfig";
	private static final String TAG_SYNC_DIFFPRICEORDER = "sysDiffPriceOrder";
	private static final String TAG_SYNC_BAOCHANG = "sysBaoChang";
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Override
	public void syncPlayItemPrice() {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_PLAYITEM);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataTime(DataExtSqlEmums.MAPSYNCPLAYITEM, lastExecuteTime, now);
		dbLogger.warn("同步价格场次：" + rowsData.size());
		List playItems = transforMapToPlayItemPrice(rowsData);
		daoService.saveObjectList(playItems);
		mark.setLastExecuteTime(now);
		daoService.saveObject(mark);
		dbLogger.warn("synced :" + playItems.size() + " play item price");
	}
	/**
	 * @param rowsData
	 * @return
	 */
	private List transforMapToPlayItemPrice(List<Map<String, Object>> rowsData) {
		List<PricedPlayItem> playItemList = new ArrayList<PricedPlayItem>();
		PricedPlayItem price = null;
		for (Map m : rowsData){
			Long playId = Long.valueOf(m.get("MPID").toString());
			Double actualPrice = Double.valueOf( m.get("ACTUALPRICE").toString());
			
			String movieName = (String) m.get("MOVIENAME");
			String roomName = (String) m.get("ROOMNAME");
			Long cinemaId = Long.valueOf(m.get("CINEMAID").toString());
			Timestamp updateTime = DateUtil.parseTimestamp(m.get("UPDATETIME").toString());
			Timestamp playTime = (Timestamp) m.get("PLAYTIME");
			price = new PricedPlayItem(playId, actualPrice, movieName, roomName, cinemaId, updateTime, playTime);
			playItemList.add(price);
		}
		return playItemList;
	}
	@Override
	public void syncOrder(String orderType) {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_ORDER);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		if (DateUtil.getDiffDay(lastExecuteTime, now) > 2){ //每次最多同步两天数据
			now = DateUtil.addDay(lastExecuteTime, 2);
		}
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		syncOrder(lastExecuteTime, now,orderType);
		mark.setLastExecuteTime(now);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
	}
	@Override
	public void syncOrder(Timestamp start,  Timestamp end , String orderType){
		dbLogger.warn("syncOrder " + orderType + " 开始 -> start:" + start + "|end:" + end);
		
		List<Long> idList = new ArrayList<Long>();
		if(SettleConstant.ORDER_TYPE_TICKET.equals(orderType)){
			idList = dataExtractionDao.getDataByTime(DataExtSqlEmums.IDSSYNCORDER, Long.class, orderType, start, end);
			if (CollectionUtils.isEmpty(idList)) {
				return;
			}
			List<SysData> dlist = daoService.getObjectListByField(SysData.class, "type", SettleConstant.SYSCATEGORY);
			List<String> syscatelist = BeanUtil.getBeanPropertyList(dlist, "code", true);
			Set<String> notcatelist = new HashSet<String>();
			List<List<Long>> groupList = BeanUtil.partition(idList, 888);
			for (List<Long> ids: groupList){
				List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataByIds(DataExtSqlEmums.MAPSSYNCORDER, ids.toArray());
				Map<Long, Double> pallmap = getPlaceallowance(ids);
				transforMapToOrder(rowsData, pallmap, syscatelist, notcatelist);
			}
			if (CollectionUtils.isNotEmpty(notcatelist)){
				String content = "<h1>请及时确认以下平台是否需要结算， 并通知系统研发团队,以免影响结算<h1><br>" + notcatelist.toString();
				dbLogger.warn(content);
				GSPMaill.sendMaill(content, GSPSendMaill.SYSADMINMAIL);
			}
		}else if(SettleConstant.ORDER_TYPE_GOODS.equals(orderType)){
			idList = dataExtractionDao.getDataByTime(DataExtSqlEmums.IDSSYNCGOODS, Long.class, orderType, start, end);
			if (CollectionUtils.isEmpty(idList)) {
				return;
			}
			List<List<Long>> groupList = BeanUtil.partition(idList, 888);
			for (List<Long> ids: groupList){
				List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataByIds(DataExtSqlEmums.MAPSSYNCORDER, ids.toArray());
				transforMapToOrder(rowsData, null, null, null);
			}
		}
		
		dbLogger.warn("synced :" + idList.size() + " gewa " +  orderType + "orderrs!!"  + DateUtil.formatTimestamp(start) + "至" + DateUtil.formatTimestamp(end));
	}
	
	
	private Map<Long, Double> getPlaceallowance(List<Long> ids) {
		Map<Long, Double> pallmap = new HashMap<Long, Double>();
		String sql = "select v.ORDERID, sum(v.AMOUNT) as placeamount  from webdata.view4placeallowance v where v.ORDERID in " + DramaUtil.sqlinList(ids, Long.class) + " GROUP BY v.ORDERID ";
		List<Map<String, Object>> maplist = shJdbcTemplate.queryForList(sql);
		if(CollectionUtils.isEmpty(maplist)) {
			return pallmap;
		}
		for(Map<String, Object> map : maplist) {
			MapRow row = new MapRow(map);
			pallmap.put(row.getLongWithNull("ORDERID"), row.getDouble("placeamount"));
		}
		return pallmap;
	}
	
	private void transforMapToOrder(List<Map<String, Object>> rowsData, Map<Long, Double> pallmap,
			List<String> syscatelist, Set<String> notcatelist){
		
		for (Map<String, Object> map : rowsData){
			MapRow row = new MapRow(map);
			Long orderid = row.getLongWithNull("RECORDID");
			String tradeno = row.getStringWithNull("TRADE_NO");
			Long placeId = row.getLongWithNull("CINEMAID");
			Long itemId = row.getLongWithNull("MOVIEID");
			Long playId = row.getLongWithNull("RELATEDID");
			Timestamp dealTime = row.getTimestamp("ADDTIME");
			Timestamp useTime = row.getTimestamp("PLAYTIME");
			Integer quantity = row.getInteger("QUANTITY");
			Double totalCost = row.getDouble("TOTALCOST");
			String orderType = StringUtils.upperCase(row.getString("ORDER_TYPE"));
			String orderStatus = row.getStringWithNull("STATUS");
			String otherInfo = row.getStringWithNull("DESCRIPTION2");
			String category = row.getStringWithNull("CATEGORY");
			double discount = row.getDouble("discount");
			double wabi = row.getDouble("wabi");
			double alipaid = row.getDouble("ALIPAID");
			double gewapaid = row.getDouble("GEWAPAID");
			double otherfee = row.getDouble("otherfee");
			Timestamp updateTime = row.getTimestamp("UPDATETIME");
			
			GewaOrder order = daoService.getObject(GewaOrder.class, tradeno);
			
			if(order == null) {
				order = new GewaOrder(tradeno, placeId, itemId, playId, dealTime, useTime, quantity, totalCost, orderType, orderStatus, otherInfo, category, updateTime);
				order.setSpecial(MiscUtil.getSpecial(map));
				order.setOuterId(getOuterId(map));
				order.setRelateId(MiscUtil.getPlaceId(map, placeId));
				order.setUserBaochang(MiscUtil.getUserBaoChang(map));
				//影票开票相关
				order.setReldiscount(discount);
				order.setAlipaid(alipaid + (gewapaid - wabi) - otherfee);
				
				if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(orderType)) {
					/**
					 * 订单开规则：
					 *  paid_success：订单优惠金额+瓦币-场馆补贴，当结果小于0取0，当结果大于结算金额，取结算金额
					 *  paid_return：直接取结算金额
					 */
					double kpamount = 0;
					double allowance = pallmap.get(orderid) == null ? 0.0 : pallmap.get(orderid);
					if("paid_success".equals(orderStatus)) {
						kpamount = discount + wabi - allowance;
						if(kpamount < 0) {
							kpamount = 0;
						} else if(kpamount > totalCost) {
							kpamount = totalCost;
						}
					} else if("paid_return".equals(orderStatus)) {
						kpamount = totalCost;
					}
					order.setDiscount(kpamount);
					order.setPlaceallowance(allowance);
					if(!syscatelist.contains(category)) {
						notcatelist.add(category);
					}
				}
			} else {
				order.setOrderStatus(orderStatus);
				order.setUpdateTime(updateTime);
			}
			
			daoService.saveObject(order);
		}
		
	}
	
	/**
	 * @param row
	 * @return
	 */
	@Override
	public  String getOuterId(Map<String, Object> row) {
		String outerId = null;
		String category = row.get("CATEGORY") == null ? null : row.get("CATEGORY").toString();
		if(category != null) {
			SysData data = sysDataService.getSysData(category, SettleConstant.SYSCATEGORY, String.class);
			if(data != null) {
				String name = data.getName();
				if(StringUtils.isNotEmpty(name)) {
					if("OTHERINFO".equals(name)) {
						String otherinfo = row.get(name) == null ? null : row.get(name).toString();
						if(otherinfo != null) {
							if(!otherinfo.startsWith("{")) {
								String tradeno = row.get("TRADE_NO") == null ? null : (String)row.get("TRADE_NO");
								dbLogger.error("存在异常OTHERINFO，订单号为：" + tradeno + "，OTHERINFO为：" + otherinfo);
								return outerId;
							}
						}
						Map otherMap = JsonUtils.readJsonToMap(otherinfo);
						if (otherMap != null){
							String special = data.getSpecial();
							outerId = otherMap.get(special) == null ? "" : otherMap.get(special).toString();
						}
					} else {
						outerId = row.get(name) == null ? null : row.get(name).toString();
					}
				}
			}
		}
		return outerId;
	}

	
	@Override
	public void syncPlace() {
		
		/*String sql = "select CINEMA_ID, CHANNEL_ID from WEBDATA.VIEW_CINEMA_CHARGECHANNEL where CHANNEL_TYPE = '院线'  and STATUS=3 and OPERATION_TYPE <> 'DEL' ";
		List<Map<String, Object>> threlist = shJdbcTemplate.queryForList(sql);
		Map<Long, Long> thremap = new HashMap<Long, Long>();
		for(Map<String, Object> map : threlist) {
			MapRow row = new MapRow(map);
			thremap.put(row.getLongWithNull("CINEMA_ID"), row.getLongWithNull("CHANNEL_ID"));
		}*/
		
		List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataByIds(DataExtSqlEmums.MAPSYNCPLACE, null);
		
		List<SettleConfig> configs = daoService.getAllObjects(SettleConfig.class);
		List<String> configIds = BeanUtil.getBeanPropertyList(configs, "recordId", true);
		
		String title = "<h1>新增以下影院， 请配置</h1><br><br>";
		StringBuffer sb = new StringBuffer(title);
		
		String updatesql = "update place set city_code = ?, name = ?, update_time = ?, is_config = ?, open_type = ?, brand_name = ?, pcid = ? where record_id = ? ";
		
		for (Map<String, Object> row : rowsData){
			Long placeId = row.get("RECORDID") == null ? null : Long.valueOf(row.get("RECORDID").toString());
			String placeType = PlaceTypeEnums.TICKET.getType();
			String cityCode = (String)row.get("CITYCODE");
			String openType = (String)row.get("OPENTYPE");
			String name = (String)row.get("NAME");
			Timestamp updateTime = (Timestamp)row.get("UPDATETIME");
			String status = configIds.contains(RecordIdUtils.contactRecordId(placeType, placeId)) ? "Y" : "N";
			String brandName = row.get("BRANDNAME") == null ? null : (String)row.get("BRANDNAME");
			String pcid = row.get("pcid") == null ? null : (String)row.get("pcid");
			
			String recordid = RecordIdUtils.contactRecordId(placeType, placeId);
			
			Place place = daoService.getObject(Place.class, recordid);
			if(place == null) {
				place = new Place(placeId, placeType, cityCode, name, updateTime,openType, status, brandName,pcid);
				daoService.addObject(place);
				sb.append(name + "\t" + brandName + "<br><br>");
			} else {
				jdbcTemplate.update(updatesql, cityCode, name, updateTime, status, openType, brandName, pcid, recordid);
			}
		}
		
		if(!title.equals(sb.toString())) {
			GSPMaill.sendMaill(sb.toString(), GSPSendMaill.SYSADMINMAIL);
		}
		
		dbLogger.warn("synced :" + rowsData.size() + " place");
	}
	
	@Override
	public void syncRefundment() {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_REFUNDMENT);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		List<Long> idList = dataExtractionDao.getDataByTime(DataExtSqlEmums.IDSSYNCREFUND, Long.class, null,lastExecuteTime, now);
		List<List<Long>> groupList = BeanUtil.partition(idList, 888);
		StringBuilder errsb = new StringBuilder("");
		for (List<Long> ids: groupList){
			List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataByIds(DataExtSqlEmums.MAPSYNCREFUND, ids.toArray());
			Collection<Refundment> refundments = transforMapToRefundment(rowsData, errsb);
			daoService.saveObjectList(refundments);
		}
		dbLogger.warn("synced :" + idList.size() + " refundments");
		if(!"".equals(errsb.toString())) {
			dbLogger.error(errsb.toString());
			GSPMaill.sendMaill(errsb.toString(), GSPSendMaill.SYSERRORMAIL);
		}
		mark.setLastExecuteTime(now);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
	}
	/**
	 * @param rowsData
	 * @return
	 */
	private Collection<Refundment> transforMapToRefundment(List<Map<String, Object>> rowsData, StringBuilder errsb) {
		Map<String, Refundment> refundments = new HashMap<String, Refundment>();
		Refundment refundment = null;
		//TRADENO,PLACEID, ORDERTYPE, REFUNDTIME, OLDSETTLE, NEWSETTLE
		for (Map<String, Object> row : rowsData){
			String tradeno = row.get("TRADENO") == null ? null : (String)row.get("TRADENO");
			String orderType = row.get("ORDERTYPE") == null ? null : StringUtils.upperCase((String)row.get("ORDERTYPE"));
			Timestamp refundTime = (Timestamp)row.get("REFUNDTIME");
			Double oldSettle = row.get("OLDSETTLE") == null ? null : Double.valueOf(row.get("OLDSETTLE").toString());
			Double newSettle = row.get("NEWSETTLE") == null ? null : Double.valueOf(row.get("NEWSETTLE").toString());
			Integer quantity = row.get("QUANTITY") == null ? null : Integer.valueOf(row.get("QUANTITY").toString());
			Long relatId = row.get("PLACEID") == null ? null : Long.valueOf(row.get("PLACEID").toString());
			Long playId = row.get("RELATEDID") == null ? null : Long.valueOf(row.get("RELATEDID").toString());
			Timestamp useTime = (Timestamp)row.get("PLAYTIME");
			String catagory = row.get("CATEGORY") == null ? null : (String)row.get("CATEGORY");
			String applyInfo = row.get("APPLYINFO") == null ? null : (String)row.get("APPLYINFO");
			String dealInfo = row.get("DEALINFO") == null ? null : (String)row.get("DEALINFO");
			refundment = new Refundment(tradeno, orderType, refundTime, oldSettle, newSettle, relatId, playId, useTime, catagory, quantity, applyInfo, dealInfo);
			//2是和平影都的id，需要重新设置placeid
			refundment.setRelateId(MiscUtil.getPlaceId(row, relatId));
			GewaOrder order = daoService.getObject(GewaOrder.class, tradeno);
			if(order != null) {
				refundment.setSpecial(order.getSpecial());
			} else if(SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(orderType)) {
				String emailContent = "syncRefundment:订单号为" + tradeno + "只有退单，没有订单，请注意！</br>";
				errsb.append(emailContent);
			}
			refundments.put(tradeno, refundment);
		}
		
		Map<String, Refundment> existedRefunds = daoService.getObjectMap(Refundment.class, refundments.keySet());
		for (Refundment rd : existedRefunds.values()) hibernateTemplate.evict(rd); //
		
		if (CollectionUtils.isNotEmpty(existedRefunds.keySet())) 
			dbLogger.warn("exsited refundmetns:" + existedRefunds.keySet());
		
		StringBuffer errorRefundment = new StringBuffer();
		for (String tradeno : existedRefunds.keySet()){
			Refundment exsitedRefund = existedRefunds.get(tradeno);
			Refundment newRefund = refundments.get(tradeno);
			newRefund.setCheckBillId(exsitedRefund.getCheckBillId());
			newRefund.setIsSettled(exsitedRefund.getIsSettled());
			if (!exsitedRefund.getIsSettled().equals("N")){
				if (!exsitedRefund.getNewSettle().equals(newRefund.getNewSettle()) || !exsitedRefund.getOldSettle().equals(newRefund.getOldSettle())){
					errorRefundment.append(exsitedRefund.getTradeno()).append(":newSettle:" + exsitedRefund.getNewSettle() + "-->" + newRefund.getNewSettle() + 
																				"; oldSettle:" + exsitedRefund.getOldSettle() + "-->" + newRefund.getOldSettle()).append("<br>");
				}
			}
		}
		String emailContent = errorRefundment.toString();
		if (!StringUtils.isEmpty(emailContent)){
			GSPMaill.sendMaill(emailContent, GSPSendMaill.SYSERRORMAIL);
		}
		return refundments.values();
	}

	
		
	@Override
	public void syncGoods(String orderType) {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_GOODS);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		if (DateUtil.getDiffDay(lastExecuteTime, now) > 2){ //每次最多同步两天数据
			now = DateUtil.addDay(lastExecuteTime, 2);
		}
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		syncOrder(lastExecuteTime, now,orderType);
		mark.setLastExecuteTime(now);
		mark.setModifyTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(mark);
	}
	
	@Override
	public void syncGoods(String orderType, Timestamp startTime,Timestamp endTime) {
		Timestamp temp = startTime;
		Timestamp temp1 = startTime;
		do{
			temp1 = DateUtil.addDay(temp, 2);
			syncOrder(temp, temp1,orderType);
			temp = temp1;
		}while((temp.before(endTime) || temp.equals(endTime)));
	}
	
	
	@Override
	public void syncSettleConfig() {
		dbLogger.warn("执行结算配置同步操作 ");
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_SETTLECONFIG);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		now = DateUtil.addMinute(now, -10);
		List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataTime(DataExtSqlEmums.MAPSYNCSETTLECONFIG, lastExecuteTime, now);
		Collection<SettleConfigUpd> settleConfigUpds = transMapToSettleConfigUpd(rowsData);
		daoService.saveObjectList(settleConfigUpds);
		List<String> recordIds = BeanUtil.getBeanPropertyList(settleConfigUpds, "recordId", true);
		List<Place> places = daoService.getObjectList(Place.class, recordIds);
		for(Place p : places){
			p.setIsConfig("Y");
			daoService.saveObjectList(p);
		}
		mark.setLastExecuteTime(now);
		daoService.saveObject(mark);
		dbLogger.warn("synced :" + settleConfigUpds.size() + " settleConfig ");
	}
	
	private Collection<SettleConfigUpd> transMapToSettleConfigUpd(List<Map<String, Object>> rowsData) {
		Map<String,SettleConfigUpd> settleConfigUpdMap = new HashMap<String,SettleConfigUpd>();
		SettleConfigUpd scu = null;
		if(CollectionUtils.isNotEmpty(rowsData)){
			for(Map<String,Object> map : rowsData){
				Long cinemaId = Long.valueOf(map.get("CINEMA_ID").toString());
				String vendorName = map.get("CINEMA_NAME_LONG") == null ? "" : map.get("CINEMA_NAME_LONG").toString();
				String vendorNo = map.get("SUPPLIER_ID") == null ? "" : map.get("SUPPLIER_ID").toString();
				String settleBase = map.get("SETTLEMENT_WAY") ==  null ? "" : map.get("SETTLEMENT_WAY").toString();
				String settleTime = map.get("SETTLEMENT_POINT") == null ? "" : map.get("SETTLEMENT_POINT").toString();
				String zhangtaoNo = map.get("ACCOUNT_ID") == null ? "" : map.get("ACCOUNT_ID").toString();
				Timestamp firstSettle = map.get("SETTLEMENT_DATE") == null ? null : DateUtil.parseTimestamp(map.get("SETTLEMENT_DATE").toString());
				
				String settleCycle = map.get("SETTLEMENT_CYCLE") == null ? "" : map.get("SETTLEMENT_CYCLE").toString();
				Integer settleValue = null;
				Integer offsetValue = null;
				if(settleCycle.equals("MIDDLE")){
					offsetValue = map.get("CYCLE_ONE") == null ? null : Integer.valueOf(map.get("CYCLE_ONE").toString());
				}else if(settleCycle.equals("TN")){
					settleValue = map.get("CYCLE_ONE") == null ? null : Integer.valueOf(map.get("CYCLE_ONE").toString());
				}else{
					settleValue = map.get("CYCLE_ONE") == null ? null : Integer.valueOf(map.get("CYCLE_ONE").toString());
					offsetValue = map.get("CYCLE_TWO") == null ? null : Integer.valueOf(map.get("CYCLE_TWO").toString());
				}
				
				String recordId = RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, cinemaId);
				scu = new SettleConfigUpd();
				scu.setRecordId(recordId);
				scu.setTag(SettleConstant.TAG_SETTLEMENTBILL_TICKET);
				scu.setRelateId(cinemaId);
				scu.setSettleCycle(settleCycle);
				scu.setSettleValue(settleValue);
				scu.setOffsetValue(offsetValue);
				scu.setSettleBase(settleBase);
				scu.setSettleTime(settleTime);
				scu.setFirstSettle(firstSettle);
				scu.setStatus("N");
				scu.setZhangtaoNo(zhangtaoNo);
				scu.setVenderName(vendorName);
				scu.setVenderNo(vendorNo);
				scu.setIsGoodsSettle(SettleConstant.IS_GOODS_SETTLE_N);
				scu.setApplyUser("system");
				scu.setApproveUser("system");
				scu.setPlayType(SettleConstant.COMM_N);
				scu.setPayVenderNo(vendorNo);
				scu.setPayVenderName(vendorName);
				//标示为同步
				scu.setOperateType(SettleConstant.OPERATE_TYPE_SYNC);
				settleConfigUpdMap.put(recordId, scu);
			}
			List<Place> places = daoService.getObjectList(Place.class, settleConfigUpdMap.keySet());
			Map<String,String> placeMap = BeanUtil.beanListToMap(places, "recordId", "openType", true);
			for(String key : settleConfigUpdMap.keySet()){
				settleConfigUpdMap.get(key).setOpenType(placeMap.get(key));
			}
		}
		return settleConfigUpdMap.values();
	}
	@Override
	public void syncChannleConfig() {
		dbLogger.warn("执行通道费配置同步操作 ");
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_CHANNELCONFIG);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		
		List<ChannelSettleConfig> channleConfigs = daoService.getObjectListByField(ChannelSettleConfig.class, "vendorType", SettleConstant.CHANNEL_VENDOR_THEATRES);
		if(CollectionUtils.isEmpty(channleConfigs)){
			dbLogger.warn("没有可以执行同步的通道配置！");
		}else{
			
			Map<Long,ChannelSettleConfig> theatresVendorMap = new HashMap<Long, ChannelSettleConfig>();
			for(ChannelSettleConfig c : channleConfigs){
				theatresVendorMap.put(c.getHoutaiId(), c);
			}
			
			List<Map<String, Object>> rowsData = dataExtractionDao.getRowsDataTime(DataExtSqlEmums.MAPSYNCCHANNELCONFIG, lastExecuteTime, now/*,houtaiIds*/);
			if(CollectionUtils.isNotEmpty(rowsData)){
				Set<Long> vendorIds = new HashSet<Long>();
				dbLogger.warn("查询到的通道映射关系数据条数：" + rowsData.size());
				VendorCinemaRelation vcr = null;
				List<VendorCinemaRelation> addList = new ArrayList<VendorCinemaRelation>();
				List<VendorCinemaRelation> delList = new ArrayList<VendorCinemaRelation>();
				for(Map<String,Object> row : rowsData){
					Long houtaiId = row.get("CHANNEL_ID") == null ? null : Long.valueOf(row.get("CHANNEL_ID").toString());
					Long cinemaId = row.get("CINEMA_ID") ==  null ? null : Long.valueOf(row.get("CINEMA_ID").toString());				
					String operateType = row.get("OPERATION_TYPE") == null ? "" : row.get("OPERATION_TYPE").toString();
					
					Long vendorId = null;
					if(null == theatresVendorMap.get(houtaiId)){
						continue;
					}
					vendorId = theatresVendorMap.get(houtaiId).getRecordId();
					vcr = new VendorCinemaRelation();
					vcr.setVendorRecordId(vendorId);
					vcr.setCinemaRecordId(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, cinemaId));
					vcr.setCreateTime(DateUtil.getCurTruncTimestamp());
					if(SettleConstant.CHANNEL_OPERATE_TYPE_ADD.equals(operateType)){
						addList.add(vcr);
						vendorIds.add(vendorId);
					}
					if(SettleConstant.CHANNEL_OPERATE_TYPE_DEL.equals(operateType)){
						delList.add(vcr);
						vendorIds.add(vendorId);
					}
					if(SettleConstant.CHANNEL_OPERATE_TYPE_UPDATE.equals(operateType)){
						DetachedCriteria query = DetachedCriteria.forClass(VendorCinemaRelation.class);
						query.add(Restrictions.eq("cinemaRecordId",vcr.getCinemaRecordId()));
						query.add(Restrictions.eq("vendorRecordId", vendorId));
						List<VendorCinemaRelation> result = daoService.findByCriteria(query);
						if(CollectionUtils.isEmpty(result)){
							addList.add(vcr);
							vendorIds.add(vendorId);
						}
					}
				}
				if(CollectionUtils.isNotEmpty(addList)){
					daoService.saveObjectList(addList);
					dbLogger.warn("新增通道映射关系数据条数：" + addList.size() + "条");
				}
				if(CollectionUtils.isNotEmpty(delList)){
					String delSQL = "delete from vendor_cinema_relation where cinema_recordId=? and vendor_recordid=?";
					int i= 0;
					for(VendorCinemaRelation r : delList){
						i += jdbcTemplate.update(delSQL, new Object[]{r.getCinemaRecordId(),r.getVendorRecordId()});
					}
					dbLogger.warn("删除通道映射关系数据条数：" + i + "条");
				}
				
				List<ChannelSettleConfig> cfgs = daoService.getObjectBatch(ChannelSettleConfig.class, "recordId", vendorIds);
				for(ChannelSettleConfig c : cfgs){
					c.setVerifyStatus(SettleConstant.STATUS_INVALID);
					dbLogger.warn("通道费状态更改为[未审核],config record id " + c.getRecordId());
				}
				daoService.saveObjectList(cfgs);
				dbLogger.warn("通道费配置状态重置为未审核数量：" + cfgs.size());
			}
		}
		dbLogger.warn("通道配置同步完成！");
		mark.setLastExecuteTime(now);
		daoService.saveObject(mark);
	}
	
	/**
	 * zyj
	 * 同步异价订单
	 */
	@Override
	public void sysDiffPriceOrder() {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_DIFFPRICEORDER);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		if (DateUtil.getDiffDay(lastExecuteTime, now) > 2){ //每次最多同步两天数据
			now = DateUtil.addDay(lastExecuteTime, 2);
		}
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		sysDiffPriceOrderWithTimeCut(lastExecuteTime, now);
		mark.setLastExecuteTime(now);
		daoService.updateObject(mark);
	}
	
	/**
	 * zyj
	 * 根据时间范围来同步异价订单数据
	 * 开始时间是上一次结束的时间，结束时间是现在时间
	 * @param startTime
	 * @param endTime
	 */
	private void sysDiffPriceOrderWithTimeCut(Timestamp startTime, Timestamp endTime) {
		dbLogger.warn("sysDiffPriceOrderWithTimeCut 开始查询异价订单->startTime:" + startTime.toString() + " endTime:" + endTime.toString());
		List<Map<String, Object>> maps = dataExtractionDao.getRowsDataTime(DataExtSqlEmums.MAPSYSDIFFPRICEORDER, startTime, endTime);
		for(Map<String, Object> map : maps) {
			MapRow row = new MapRow(map);
			DiffPriceOrder dpo = daoService.getObject(DiffPriceOrder.class, row.getStringWithNull("TRADE_NO"));
			if(dpo == null) {
				dpo = new DiffPriceOrder();
			}
			dpo.setTradeno(row.getStringWithNull("TRADE_NO"));
			dpo.setMpId(row.getLongWithNull("MPID"));
			dpo.setQuantity(row.getInteger("QUANTITY"));
			dpo.setActualPrice(row.getDouble("COSTPRICE"));
			dpo.setUpdateTime(row.getTimestamp("UPDATETIME"));
			daoService.saveObject(dpo);
		}
		dbLogger.warn("sysDiffPriceOrderWithTimeCut 异价订单同步结束");
	}
	
	/**
	 * 
	 */
	/* (non-Javadoc)
	 * @see com.gewara.service.DataExtractionService#sysBaoChang()
	 */
	@Override
	public void sysBaoChang() {
		SyncMark mark = syncMarkService.getSyncMark(TAG_SYNC_BAOCHANG);
		Timestamp lastExecuteTime = mark.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		if (DateUtil.getDiffDay(lastExecuteTime, now) > 2){ //每次最多同步两天数据
			now = DateUtil.addDay(lastExecuteTime, 2);
		}
		now = DateUtil.addMinute(now, -10);  //bug fix, let sh method executs.
		
		dbLogger.warn("sysBaoChang 开始同步包场列表->startTime:" + lastExecuteTime.toString() + " endTime:" + now.toString());
		List<Map<String, Object>> maps = dataExtractionDao.getRowsDataTime(DataExtSqlEmums.SYSBAOCHANG, lastExecuteTime, now);
		
		List<BaoChang> bcList = new ArrayList<BaoChang>();
		for(Map<String, Object> map : maps) {
			BaoChang bc = new BaoChang();
			//包场id
			if(map.get("RECORDID") == null) {
				continue;
			}
			bc.setRecordId(Long.valueOf(map.get("RECORDID").toString()));
			//影院id
			if(map.get("CINEMAID") == null) {
				continue;
			}
			String placeid = map.get("CINEMAID").toString();
			if(StringUtils.isEmpty(placeid)) {
				continue;
			}
			//影厅名称
			if("2".equals(placeid)) {
				String room = map.get("ROOMNAME") == null ? "" : map.get("ROOMNAME").toString();
				if(StringUtils.isNotBlank(room)) {
					if(room.indexOf(SettleConstant.HEPING_ROMNAME) > 0) {//非和平影厅
						placeid = SettleConstant.HEPING_OUTER_PLACEID + "";
					}
				}
			}
			
			bc.setPlaceId(RecordIdUtils.contactRecordId(SettleConstant.TAG_SETTLEMENTBILL_TICKET, Long.valueOf(placeid)));
			//部门名称
			bc.setDeptName("");
			//场次id
			bc.setPlayId(map.get("MPID") == null ? null : Long.valueOf(map.get("MPID").toString()));
			//场次类型（直连/虚拟/格瓦票务）
			if(map.get("OPENTYPE") == null) {
				continue;
			}
			String openType = map.get("OPENTYPE").toString();
			if("GEWA".equals(openType)) {
				bc.setPlayType(SettleConstant.XN);
			} else if("GPTBS".equals(openType)) {
				bc.setPlayType(SettleConstant.GW);
			} else if(SettleConstant.UNSETTLECATE.contains(openType)){
				bc.setPlayType(openType);
			} else {
				bc.setPlayType(SettleConstant.ZL);
			}
			//是否预付
			if(map.get("PREPAY") == null) {
				continue;
			}
			bc.setPrePay(map.get("PREPAY").toString());
			//包场类型（用户包场/格瓦包场）
			bc.setBcType(map.get("TYPE") == null ? null : map.get("TYPE").toString());
			//电影名称
			bc.setFilmName(map.get("MOVIENAME") == null ? null : map.get("MOVIENAME").toString());
			//放映时间
			bc.setPlayTime(map.get("PLAYTIME") == null ? null : (Timestamp)map.get("PLAYTIME"));
			//包场票数
			bc.setBcNum(map.get("SEATNUM") == null ? 0 : Integer.valueOf(map.get("SEATNUM").toString()));
			//包场金额
			bc.setBcAmount(map.get("AMOUNT") == null ? 0 : Double.valueOf(map.get("AMOUNT").toString()));
			//预付金额
			bc.setPreAmount(map.get("PREAMOUNT") == null ? 0 : Double.valueOf(map.get("PREAMOUNT").toString()));
			//
			bc.setGoodsNum(map.get("GOODSAMOUNT") == null ? 0 : Integer.valueOf(map.get("GOODSAMOUNT").toString()));
			bc.setGoodsAmount(map.get("GOODSMONEY") == null ? 0 : Double.valueOf(map.get("GOODSMONEY").toString()));
			//更新时间
			bc.setUpdateTime((Timestamp)map.get("UPDATETIME"));
			//项目编号
			bc.setPjtCode(map.get("PROJECTNO") == null ? null : map.get("PROJECTNO").toString());
			bc.setSuccessNum(0);
			bc.setSuccessAmount(0.0);
			bc.setSuccessRate(0.00);
			bc.setStatus(SettleConstant.NOPAY);
			bc.setReqbillStatus(SettleConstant.COMM_N);
			bcList.add(bc);
		}
		
		daoService.saveObjectList(bcList);
		dbLogger.warn("sysBaoChang 同步包场列表结束");
		
		mark.setLastExecuteTime(now);
		daoService.updateObject(mark);
	}
	
}
