package com.gewara.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.DramaConst;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaDiscountItem;
import com.gewara.model.drama.DramaJitiBill;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaPriceBill;
import com.gewara.model.drama.DramaPriceRate;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.model.drama.DramaVersion;
import com.gewara.service.DaoService;
import com.gewara.service.DramaDoColleService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MapRow;
import com.gewara.util.WebLogger;

@Service("dramaDoCollecServiceImpl")
public class DramaDoCollecServiceImpl implements DramaDoColleService {
	
	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate; 
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	/**
	 * 手动同步演出
	 */
	@Override
	public String syncDramaByDramaid(List<Long> dramaidlist) {
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.recordid, v.dramaname, v.pretype, v.updatetime, v.adddate, v.releasedate, v.enddate, d.origin ");
		sql.append("from webdata.view_drama v left outer join webdata.VIEW_DRAMAPROFILE d on d.DRAMAID = v.recordid ");
		sql.append("where v.recordid in " + DramaUtil.sqlinList(dramaidlist, Long.class));
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString());
		
		if(CollectionUtils.isEmpty(mapList)) {
			return "项目不存在!";
		}
		updateDrama(mapList);
		
		return "同步成功";
	}
	
	/**
	 * 处理同步演出项目数据
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateDrama(List<Map<String, Object>> mapList) {
		for(Map<String, Object> map : mapList) {
			MapRow row = new MapRow(map);
			Long dramaid = row.getLongWithNull("recordid");
			
			DetachedCriteria query = DetachedCriteria.forClass(DramaVersion.class);
			query.add(Restrictions.eq("dramaid", dramaid));
			query.addOrder(Order.desc("endtime"));
			List<DramaVersion> dvlist = daoService.findByCriteria(query);
			
			if(CollectionUtils.isEmpty(dvlist)) {
				Drama drama = new Drama();
				drama.setRecordid(dramaid);
				drama.setDramaname(row.getStringWithNull("dramaname"));
				drama.setPretype(row.getStringWithNull("pretype"));
				drama.setAddtime(row.getTimestamp("adddate"));
				drama.setOrigin(row.getStringWithNull("origin"));
				drama.setGspupdatetime(DateUtil.getCurFullTimestamp());
				daoService.addObject(drama);
				
				DramaVersion dversion = new DramaVersion();
				dversion.setDramaid(dramaid);
				dversion.setStarttime(row.getTimestamp("releasedate"));
				dversion.setEndtime(row.getTimestamp("enddate"));
				dversion.setUpdatetime(row.getTimestamp("updatetime"));
				dversion.setGspupdatetime(DateUtil.getCurFullTimestamp());
				daoService.addObject(dversion);
			} else {
				//将最新的一个版本的starttime跟上一个版本的endtime进行比较
				DramaVersion dv = dvlist.get(0);
				Timestamp oldendtime = dv.getEndtime();
				Timestamp starttime = row.getTimestamp("releasedate");
				
				Drama drama = daoService.getObject(Drama.class, dramaid);
				if(drama != null) {
					drama.setDramaname(row.getStringWithNull("dramaname"));
					drama.setPretype(row.getStringWithNull("pretype"));
					drama.setAddtime(row.getTimestamp("adddate"));
					drama.setOrigin(row.getStringWithNull("origin"));
					drama.setGspupdatetime(DateUtil.getCurFullTimestamp());
					daoService.updateObject(drama);
				}
				
				if(starttime.getTime() <= oldendtime.getTime()) {
					dv.setEndtime(row.getTimestamp("enddate"));
					dv.setUpdatetime(row.getTimestamp("updatetime"));
					dv.setGspupdatetime(DateUtil.getCurFullTimestamp());
					daoService.updateObject(dv);
				} else {
					DramaVersion dversion = new DramaVersion();
					dversion.setDramaid(dramaid);
					dversion.setStarttime(row.getTimestamp("releasedate"));
					dversion.setEndtime(row.getTimestamp("enddate"));
					dversion.setUpdatetime(row.getTimestamp("updatetime"));
					dversion.setGspupdatetime(DateUtil.getCurFullTimestamp());
					daoService.addObject(dversion);
				}
			}
		}
	}
	
	/**
	 * 手动同步场次
	 * @param dramaidlist
	 * @return
	 */
	@Override
	public String syncPlayItemByDramaid(List<Long> dramaidlist) {
		if(CollectionUtils.isEmpty(dramaidlist)) {
			return "项目id为空";
		}
		String sql = "select v.recordid, v.dpid, v.dramaid, v.playtime from WEBDATA.VIEW_OPEN_DRAMA_ITEM v where v.dramaid in " + DramaUtil.sqlinList(dramaidlist, Long.class);
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString());
		if(CollectionUtils.isEmpty(mapList)) {
			return "没查询相关场次";
		}
		for(Map<String, Object> map : mapList) {
			MapRow row = new MapRow(map);
			Long recordid = row.getLongWithNull("recordid");
			Long dpid = row.getLongWithNull("dpid");
			Long dramaid = row.getLongWithNull("dramaid");
			Timestamp playtime = row.getTimestamp("playtime");
			DramaPlayItem playitem = new DramaPlayItem(recordid, dpid, dramaid, playtime);
			daoService.saveObject(playitem);
		}
		return "同步成功！";
	}
	
	/**
	 * 手动同步配置版本
	 * @param dramaid
	 * @return
	 */
	@Override
	public String syncDramConfigByDramaId(Long dramaid) {
		
		String result = "";
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.dramaid, v.suppliercode, v.distype from webdata.view_drama_settleprice v ");
		sql.append("where v.dramaid = ? ");
		sql.append("group by v.dramaid, v.suppliercode, v.distype ");
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString(), dramaid);
		
		if(CollectionUtils.isEmpty(mapList)) {
			result = "项目" + dramaid + "没有查询到配置记录！";
			return result;
		}
		
		List<Map<String, Object>> finalmapList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> configmap : mapList) {
			MapRow rowdconfig = new MapRow(configmap);
			String suppliercode = rowdconfig.getStringWithNull("suppliercode");
			String distype = rowdconfig.getStringWithNull("distype");
			if(StringUtils.isEmpty(suppliercode) || StringUtils.isEmpty(distype)) {
				result = "项目【" + dramaid + "】存在供应商编码或者扣率类型为空的配置，请及时与相关业务人员确认>>";
				logger.warn(result);
			} else {
				finalmapList.add(configmap);
			}
		}
		
		for(Map<String, Object> map : finalmapList) {
			result += processDramaVersionConfig(map, false);
		}
		
		logger.warn(result);
		return result;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String processDramaVersionConfig(Map<String, Object> configmap, boolean sysjob) {
		
		String result = "同步完成！";
		
		MapRow rowdconfig = new MapRow(configmap);
		long dramaid = rowdconfig.getLongWithNull("dramaid");
		String suppliercode = rowdconfig.getStringWithNull("suppliercode");
		String distype = rowdconfig.getStringWithNull("distype");
		
		//获取最新的演出信息
		String dramasql = "select v.adddate, v.releasedate, v.enddate, v.pretype from webdata.view_drama v where v.recordid = ? ";
		List<Map<String, Object>> dramaMaplist = shJdbcTemplate.queryForList(dramasql, dramaid);
		if(CollectionUtils.isEmpty(dramaMaplist)) {
			result = "【项目：" + dramaid + "】的在演出项目列表中不存在，请通知相关业务人员！";
			logger.warn(result);
			if(sysjob) {
				GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
			}
			return result;
		}
		Map<String, Object> dramaMap = dramaMaplist.get(0);
		MapRow rowdrama = new MapRow(dramaMap);
		Timestamp starttime = rowdrama.getTimestamp("releasedate");
		Timestamp endtime = rowdrama.getTimestamp("enddate");
		
		if(endtime.getTime() < starttime.getTime()) {
			result = "【项目：" + dramaid + "】的出现‘开始演出时间’比‘结束演出时间’小的版本，请通知相关业务人员！";
			logger.warn(result);
			if(sysjob) {
				GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
			}
			return result;
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.eq("suppliercode", suppliercode));
		query.add(Restrictions.eq("distype", distype));
		query.addOrder(Order.desc("endtime"));
		List<DramaConfig> dcList = daoService.findByCriteria(query);
		
		if(CollectionUtils.isNotEmpty(dcList)) {
			//取出最新的版本配置
			DramaConfig dc = dcList.get(0);
			Timestamp oldendtime = dc.getEndtime();
			
			if(starttime.getTime() <= oldendtime.getTime()) {
				/* 
				 * 1.如果配置的状态是“待完成”，“待审核”，
				 * 2.如果配置的状态是“已审核”，“结算中”，“已完成”直接更新结束时间并发邮件进行通知，
				 */
				String status = dc.getStatus();
				if(DramaConst.DISCARD.equals(status) || 
						DramaConst.WAITFINISH.equals(status) || 
						DramaConst.WAITAPPROVAL.equals(status)) {
					dc.setEndtime(endtime);
					dc.setGspupdatetime(DateUtil.getCurFullTimestamp());
					daoService.updateObject(dc);
				} else if(DramaConst.HASAPPROVAL.equals(status) || DramaConst.SETTLEING.equals(status)) {
					
					dc.setEndtime(endtime);
					dc.setStatus(DramaConst.WAITAPPROVAL);
					dc.setCanjiti(DramaConst.N);
					dc.setGspupdatetime(DateUtil.getCurFullTimestamp());
					daoService.updateObject(dc);
					
					DetachedCriteria qry = DetachedCriteria.forClass(DramaPriceRate.class);
					qry.add(Restrictions.eq("configid", dc.getRecordid()));
					qry.add(Restrictions.eq("status", DramaConst.AVAILABLE));
					List<DramaPriceRate> prlist = daoService.findByCriteria(qry);
					for(DramaPriceRate rate : prlist) {
						rate.setStatus(DramaConst.EXPIRED);
						rate.setGspupdatetime(DateUtil.getCurFullTimestamp());
						daoService.updateObject(rate);
					}
					
					logger.warn("【演出配置id：" + dc.getRecordid() + "】审批通过之后有数据更新!");
				} else {
					result = "【演出配置id：" + dc.getRecordid() + "】已经结算完成，但是还有配置数据更新，请联系相关业务人员！";
					logger.warn(result);
					if(sysjob) {
						GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
					}
				}
			} else {
				//新生成一个配置版本
				createDramaConfig(suppliercode, oldendtime, endtime, dramaid, distype);
			}
		} else {
			createDramaConfig(suppliercode, rowdrama.getTimestamp("adddate"), endtime, dramaid, distype);
		}
		return result;
	}
	
	/**
	 * 同步演出项目配置版本数据
	 */
	@SuppressWarnings("unchecked")
	private void createDramaConfig(String suppliercode, Timestamp starttime, Timestamp endtime, Long dramaid, String distype) {
		/*
		 * 配置的开始时间使用addtime，为了兼容以前可能存在同一个项目使用多次演出，所以设定时间开始
		 */
		Timestamp cutpoint = DateUtil.parseTimestamp(DramaConst.STARTSETTLETIME, "yyyy-MM-dd");
		if(starttime.getTime() < cutpoint.getTime()) {
			starttime = cutpoint;
		}
		Long dramaversionid = 0l;
		DetachedCriteria query = DetachedCriteria.forClass(DramaVersion.class);
		query.add(Restrictions.eq("dramaid", dramaid));
		query.add(Restrictions.eq("endtime", endtime));
		List<DramaVersion> dvlist = daoService.findByCriteria(query);
		if(CollectionUtils.isNotEmpty(dvlist)) {
			dramaversionid = dvlist.get(0).getRecordid();
		}
		String tmp = DateUtil.format(starttime, "yyyy-MM-dd");
		starttime = DateUtil.parseTimestamp(tmp, "yyyy-MM-dd");
		DramaConfig dconfig = new DramaConfig(dramaversionid, suppliercode, starttime, endtime, dramaid, distype);
		daoService.addObject(dconfig);
	}
	
	
	/**
	 * 计算单据
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String fullSettleBill(DramaSettleBill bill, boolean sysjob) {
		
		Map<String, Object> resultmap = checkFullbill(bill);
		String result = resultmap.get("result").toString();
		if(!DramaConst.OK.equals(result)) {
			return result;
		}
		
		Long dramaid = Long.valueOf(resultmap.get("dramaid").toString());
		Timestamp start = (Timestamp)resultmap.get("start");
		//如果是手动来刷新单据并且为项目的最后一期单据，则结束时间取当前时间
		Timestamp end = null;
		if(!sysjob && DramaConst.Y.equals(bill.getLastbill())) {
			end = DateUtil.getCurFullTimestamp();
		} else {
			end = (Timestamp)resultmap.get("end");
		}
		List<Long> playids = (ArrayList<Long>)resultmap.get("playids");
		List<DramaPriceBill> priceBillList = (List<DramaPriceBill>)resultmap.get("priceBillList");
		
		List<DramaPriceBill> finishPriceBillList = new ArrayList<DramaPriceBill>();
		Set<String> Morder = new HashSet<String>();
		Set<String> Mrefund = new HashSet<String>();
		
		for(DramaPriceBill priceBill : priceBillList) {
			
			DramaPriceRate priceRate = daoService.getObject(DramaPriceRate.class, priceBill.getPricerateid());
			
			//线上订单计算
			onlineOrderCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill, Morder);
			//线上退单
			onlineRefundCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill, Mrefund);
			
			//线下订单计算
			offlineOrderCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill);
			//线下退计算
			offlineRefundCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill);
			
			//票务订单计算
			//票务退单计算
			
			//结算金额=(票面金额-退票金额)*折扣
			Double settleamount = Double.valueOf(String.format("%.2f", ((priceBill.getTicketamount() - priceBill.getRefundamount()) * priceRate.getDiscount()) / 100));
			priceBill.setSettleamount(settleamount);
			priceBill.setPayamount(priceBill.getSettleamount() - priceBill.getMadisamount());
			priceBill.setGspupdatetime(DateUtil.getCurFullTimestamp());
			daoService.updateObject(priceBill);
			finishPriceBillList.add(priceBill);
		}
		
		Double syspayamount = 0.0;
		for(DramaPriceBill priceBill : finishPriceBillList) {
			bill.setTicketnum(bill.getTicketnum() + priceBill.getTicketnum());
			bill.setTicketamount(bill.getTicketamount() + priceBill.getTicketamount());
			bill.setRefundnum(bill.getRefundnum() + priceBill.getRefundnum());
			bill.setRefundamount(bill.getRefundamount() + priceBill.getRefundamount());
			bill.setSettleamount(bill.getSettleamount() + priceBill.getSettleamount());
			bill.setMadisamount(bill.getMadisamount() + priceBill.getMadisamount());
			syspayamount += priceBill.getPayamount();
		}
		
		Map<String, Double> dismap = getOrderDiscount(Morder, Mrefund);
		bill.setMbddisamount(dismap.get("mbddisamount"));
		bill.setButieamount(dismap.get("butieamount"));
		bill.setSyspayamount(syspayamount - bill.getMbddisamount() - bill.getButieamount());
		bill.setPayamount(bill.getSyspayamount());
		
		if(bill.getPayamount().doubleValue() == 0) {
			bill.setStatus(DramaConst.INVALID);
		} else {
			bill.setStatus(DramaConst.WAITINGPAY);
		}
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		
		daoService.updateObject(bill);
		
		result = "计算成功";
		return result;
	}
	
	/**
	 * 检查计算单据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> checkFullbill(DramaSettleBill bill) {
		
		Map<String, Object> resultmap = new HashMap<String, Object>();
		String result;
		
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, bill.getConfigid());
		if(dconfig == null) {
			settleBack(bill);
			result = "结算单" + bill.getRecordid() + "的配置为空";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		if(DramaConst.WAITFINISH.equals(dconfig.getStatus()) || DramaConst.WAITAPPROVAL.equals(dconfig.getStatus())) {
			result = "结算单" + bill.getRecordid() + "的配置状态不正确";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		List<DramaPriceBill> priceBillList = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", bill.getRecordid());
		if(CollectionUtils.isEmpty(priceBillList)) {
			settleBack(bill);
			result = "结算单" + bill.getRecordid() + "没有价格对账单";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		for(DramaPriceBill priceBill : priceBillList) {
			DramaPriceRate priceRate = daoService.getObject(DramaPriceRate.class, priceBill.getPricerateid());
			if(priceRate == null) {
				settleBack(bill);
				result = "价格对账单" + priceBill.getRecordid() + "价格扣率配置为空";
				logger.error(result);
				resultmap.put("result", result);
				return resultmap;
			}
		}
		DramaVersion dv = daoService.getObject(DramaVersion.class, dconfig.getDramaversionid());
		DetachedCriteria query = DramaUtil.getDramaPlayItemDetaByDV(dv);
		List<DramaPlayItem> playlist = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(playlist)) {
			settleBack(bill);
			result = "结算单" + bill.getRecordid() + "演出版本没有查询到场次!";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		
		result = DramaConst.OK;
		resultmap.put("dramaid", bill.getDramaid());
		resultmap.put("start", bill.getStarttime());
		resultmap.put("end", bill.getEndtime());
		resultmap.put("playids", BeanUtil.getBeanPropertyList(playlist, "dpid", true));
		resultmap.put("priceBillList", priceBillList);
		resultmap.put("result", result);
		
		return resultmap;
	}
	
	//异常单据
	private void settleBack(DramaSettleBill bill) {
		bill.setStatus(DramaConst.EXCEPTION);
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(bill);
	}
	
	/**
	 * 计算计提成本单据
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String fullJitiBill(DramaJitiBill bill) {
		String result = DramaConst.OK;
		
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, bill.getConfigid());
		DramaVersion dv = daoService.getObject(DramaVersion.class, dconfig.getDramaversionid());
		DetachedCriteria query = DramaUtil.getDramaPlayItemDetaByDV(dv);
		List<DramaPlayItem> playlist = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(playlist)) {
			result =  "计提单配置" + dconfig.getRecordid() + "演出版本没有查询到场次!";
			logger.error(result);
			return result;
		}
		List<Long> playids = BeanUtil.getBeanPropertyList(playlist, "dpid", true);
		
		DetachedCriteria pquery = DetachedCriteria.forClass(DramaPriceRate.class);
		pquery.add(Restrictions.eq("configid", dconfig.getRecordid()));
		pquery.add(Restrictions.eq("status", DramaConst.AVAILABLE));
		List<DramaPriceRate> priceRatelist = daoService.findByCriteria(pquery);
		
		if(CollectionUtils.isEmpty(priceRatelist)) {
			result =  "计提单配置" + dconfig.getRecordid() + "没有查询到可用的扣率配置!";
			logger.error(result);
			return result;
		}
		
		List<DramaPriceBill> finishPriceBillList = new ArrayList<DramaPriceBill>();
		Set<String> Morder = new HashSet<String>();
		Set<String> Mrefund = new HashSet<String>();
		Long dramaid = bill.getDramaid();
		Timestamp start = bill.getStarttime();
		Timestamp end = bill.getEndtime();
		
		for(DramaPriceRate priceRate : priceRatelist) {
			
			DramaPriceBill priceBill = new DramaPriceBill(null, null, null, null, null);
			
			//线上订单计算
			onlineOrderCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill, Morder);
			//线上退单
			onlineRefundCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill, Mrefund);
			
			//线下订单计算
			offlineOrderCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill);
			//线下退计算
			offlineRefundCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill);
			
			//票务订单计算
			//票务退单计算
			
			//结算金额=(票面金额-退票金额)*折扣
			Double settleamount = Double.valueOf(String.format("%.2f", ((priceBill.getTicketamount() - priceBill.getRefundamount()) * priceRate.getDiscount()) / 100));
			priceBill.setSettleamount(settleamount);
			priceBill.setPayamount(priceBill.getSettleamount() - priceBill.getMadisamount());
			finishPriceBillList.add(priceBill);
		}
		
		Double syspayamount = 0.0;
		for(DramaPriceBill priceBill : finishPriceBillList) {
			bill.setTicketamount(bill.getTicketamount() + priceBill.getTicketamount());
			bill.setRefundamount(bill.getRefundamount() + priceBill.getRefundamount());
			bill.setSettleamount(bill.getSettleamount() + priceBill.getSettleamount());
			bill.setMadisamount(bill.getMadisamount() + priceBill.getMadisamount());
			syspayamount += priceBill.getPayamount();
		}
		
		Map<String, Double> dismap = getOrderDiscount(Morder, Mrefund);
		bill.setMbddisamount(dismap.get("mbddisamount"));
		bill.setButieamount(dismap.get("butieamount"));
		bill.setPayamount(syspayamount - bill.getMbddisamount() - bill.getButieamount());
		bill.setProfitamount(bill.getTicketamount() - bill.getRefundamount() - bill.getSettleamount());
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		
		daoService.saveObject(bill);
		
		return result;
	}
	
	/**
	 * 线上订单数据聚合计算
	 * @param dramaid
	 * @param starttime
	 * @param endtime
	 * @param settlebase
	 * @param needupdate
	 * @param priceRate
	 * @param priceBill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void onlineOrderCollection(Long dramaid, Timestamp starttime, Timestamp endtime, List<Long> playids, String dotype,
			DramaPriceRate priceRate, DramaPriceBill priceBill, Set<String> Morder) {
		
		StringBuilder ordersql = new StringBuilder("");
		ordersql.append("select i.recordid, i.tradeno, i.placeid, i.quantity, i.totalfee, i.disfee, i.taopiaonum ");
		ordersql.append("from drama_orderonline o, drama_orderonline_item i ");
		ordersql.append("where o.tradeno = i.tradeno ");
		ordersql.append("and o.dramaid = ? ");
		ordersql.append("and o.ordertime >= ? ");
		ordersql.append("and o.ordertime < ? ");
		ordersql.append("and i.playid in " + DramaUtil.sqlinList(playids, Long.class));
		ordersql.append("and i.tipid in (" + priceRate.getItempriceid() + ") ");
		if(DramaConst.Y.equals(priceRate.getDisticket())) {
			ordersql.append("and i.disid in (" + priceRate.getDisrecordid() + ") ");
		} else {
			ordersql.append("and i.disid is null ");
		}
		
		List<Map<String, Object>> orderMapList = jdbcTemplate.queryForList(ordersql.toString(), dramaid, starttime, endtime);
		
		if(CollectionUtils.isNotEmpty(orderMapList)) {
			
			Long placeid = null;
			Long quantity = 0l;
			Double totalfee = 0.0;
			Double disfee = 0.0;
			Double madisamount = 0.0;
			Long taopiaonum = 0l;
			List<String> tradenoList = new ArrayList<String>();
			List<Long> recordidList = new ArrayList<Long>();
			
			for(Map<String, Object> order : orderMapList) {
				MapRow row = new MapRow(order);
				String tradeno = row.getStringWithNull("tradeno");
				placeid = row.getLong("placeid");
				quantity += row.getLong("quantity");
				totalfee += row.getDouble("totalfee");
				disfee += row.getDouble("disfee");
				taopiaonum += row.getLong("taopiaonum");
				if(!tradenoList.contains(tradeno)) {
					tradenoList.add(tradeno);
				}
				Morder.add(tradeno);
				recordidList.add(row.getLongWithNull("recordid"));
			}
			//查询兑换券
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.eq("cardtype", DramaConst.CARDTYPE_A));
			quy.add(Restrictions.eq("amount", priceRate.getPrice()));
			quy.add(Restrictions.in("relatedid", tradenoList.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				madisamount += discount.getAmount();
			}
			
			priceBill.setPlaceid(placeid);
			priceBill.setTicketnum(priceBill.getTicketnum() + quantity);
			priceBill.setTicketamount(priceBill.getTicketamount() + (totalfee - disfee));
			priceBill.setTaopiaonum(priceBill.getTaopiaonum() + taopiaonum);
			priceBill.setMadisamount(priceBill.getMadisamount() + madisamount);
			//更新以计算的orderitem
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateItemBySettle(priceBill.getRecordid(), recordidList, "drama_orderonline_item");
			}
		}
	}
	
	
	/**
	 * 线上退单数据聚合计算
	 * @param dramaid
	 * @param starttime
	 * @param endtime
	 * @param needupdate
	 * @param priceRate
	 * @param priceBill
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void onlineRefundCollection(Long dramaid, Timestamp starttime, Timestamp endtime, List<Long> playids, String dotype,
			DramaPriceRate priceRate, DramaPriceBill priceBill, Set<String> Mrefund) {
		
		StringBuilder refundsql = new StringBuilder("");
		refundsql.append("select i.recordid, i.quantity, i.totalfee, i.disfee, i.taopiaonum, r.tradeno ");
		refundsql.append("from drama_refundonline r, drama_orderonline_item i ");
		refundsql.append("where r.tradeno = i.tradeno ");
		refundsql.append("and r.issettle = 'Y' ");
		refundsql.append("and r.dramaid = ? ");
		refundsql.append("and r.refundtime >= ? ");
		refundsql.append("and r.refundtime < ? ");
		refundsql.append("and i.playid in " + DramaUtil.sqlinList(playids, Long.class));
		refundsql.append("and i.tipid in (" + priceRate.getItempriceid() + ") ");
		if(DramaConst.Y.equals(priceRate.getDisticket())) {
			refundsql.append("and i.disid in (" + priceRate.getDisrecordid() + ") ");
		} else {
			refundsql.append("and i.disid is null ");
		}
		List<Map<String, Object>> refundMapList = jdbcTemplate.queryForList(refundsql.toString(), dramaid, starttime, endtime);
		
		if(CollectionUtils.isNotEmpty(refundMapList)) {
			
			Long quantity = 0l;
			Double totalfee = 0.0;
			Double disfee = 0.0;
			Double madisamount = 0.0;
			Long taopiaonum = 0l;
			List<String> tradenoList = new ArrayList<String>();
			List<Long> recordidList = new ArrayList<Long>();
			
			for(Map<String, Object> refund : refundMapList) {
				MapRow row = new MapRow(refund);
				String tradeno = row.getStringWithNull("tradeno");
				quantity += row.getLong("quantity");
				totalfee += row.getDouble("totalfee");
				disfee += row.getDouble("disfee");
				taopiaonum += row.getLong("taopiaonum");
				if(!tradenoList.contains(tradeno)) {
					tradenoList.add(tradeno);
				}
				Mrefund.add(tradeno);
				recordidList.add(row.getLongWithNull("recordid"));
			}
			//查询兑换券
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.eq("cardtype", DramaConst.CARDTYPE_A));
			quy.add(Restrictions.eq("amount", priceRate.getPrice()));
			quy.add(Restrictions.in("relatedid", tradenoList.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				madisamount += discount.getAmount();
			}
			
			priceBill.setRefundnum(priceBill.getRefundnum() + quantity);
			priceBill.setRefundamount(priceBill.getRefundamount() +  (totalfee - disfee));
			priceBill.setTaopiaonum(priceBill.getTaopiaonum() - taopiaonum);
			priceBill.setMadisamount(priceBill.getMadisamount() - madisamount);
			//更新已经计算的refund
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateRefundBySettle(priceBill.getRecordid(), recordidList, "drama_orderonline_item");
			}
		}
	}
	
	/**
	 * 线下订单数据聚合计算
	 * @param dramaid
	 * @param starttime
	 * @param endtime
	 * @param settlebase
	 * @param needupdate
	 * @param priceRate
	 * @param priceBill
	 * @return
	 */
	private void offlineOrderCollection(Long dramaid, Timestamp starttime, Timestamp endtime, List<Long> playids, String dotype,
			DramaPriceRate priceRate, DramaPriceBill priceBill) {
		
		StringBuilder ordersql = new StringBuilder("");
		ordersql.append("select i.recordid, i.placeid, i.tradeno, i.quantity, i.totalfee, i.disfee, i.taopiaonum ");
		ordersql.append("from drama_orderoffline o, drama_orderoffline_item i ");
		ordersql.append("where o.tradeno = i.tradeno ");
		ordersql.append("and o.dramaid = ? ");
		ordersql.append("and o.ordertime >= ? ");
		ordersql.append("and o.ordertime < ? ");
		ordersql.append("and i.playid in " + DramaUtil.sqlinList(playids, Long.class));
		ordersql.append("and i.tipid in (" + priceRate.getItempriceid() + ") ");
		if(DramaConst.Y.equals(priceRate.getDisticket())) {
			ordersql.append("and i.disid in (" + priceRate.getDisrecordid() + ") ");
		} else {
			ordersql.append("and i.disid is null ");
		}
		
		List<Map<String, Object>> orderMapList = jdbcTemplate.queryForList(ordersql.toString(), dramaid, starttime, endtime);
		
		if(CollectionUtils.isNotEmpty(orderMapList)) {
			
			Long placeid = null;
			Long quantity = 0l;
			Double totalfee = 0.0;
			Double disfee = 0.0;
			Long taopiaonum = 0l;
			List<String> tradenoList = new ArrayList<String>();
			List<Long> recordidList = new ArrayList<Long>();
			
			for(Map<String, Object> order : orderMapList) {
				MapRow row = new MapRow(order);
				placeid = row.getLong("placeid");
				quantity += row.getLong("quantity");
				totalfee += row.getDouble("totalfee");
				disfee += row.getDouble("disfee");
				taopiaonum += row.getLong("taopiaonum");
				String tradeno = row.getStringWithNull("tradeno");
				if(!tradenoList.contains(tradeno)) {
					tradenoList.add(tradeno);
				}
				recordidList.add(row.getLongWithNull("recordid"));
			}
			priceBill.setPlaceid(placeid);
			priceBill.setTicketnum(priceBill.getTicketnum() + quantity);
			priceBill.setTicketamount(priceBill.getTicketamount() + (totalfee - disfee));
			priceBill.setTaopiaonum(priceBill.getTaopiaonum() + taopiaonum);
			//更新以计算的orderitem
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateItemBySettle(priceBill.getRecordid(), recordidList, "drama_orderoffline_item");
			}
		}
		
	}
	
	
	/**
	 * 线下退单数据聚合计算
	 * @param dramaid
	 * @param starttime
	 * @param endtime
	 * @param needupdate
	 * @param priceRate
	 * @param priceBill
	 * @return
	 */
	private void offlineRefundCollection(Long dramaid, Timestamp starttime, Timestamp endtime, List<Long> playids, String dotype,
			DramaPriceRate priceRate, DramaPriceBill priceBill) {
		
		StringBuilder refundsql = new StringBuilder("");
		refundsql.append("select i.recordid, i.quantity, i.totalfee, i.disfee, i.taopiaonum, r.tradeno ");
		refundsql.append("from drama_refundoffline r, drama_orderoffline_item i ");
		refundsql.append("where r.tradeno = i.tradeno ");
		refundsql.append("and r.dramaid = ? ");
		refundsql.append("and r.refundtime >= ? ");
		refundsql.append("and r.refundtime < ? ");
		refundsql.append("and i.playid in " + DramaUtil.sqlinList(playids, Long.class));
		refundsql.append("and i.tipid in (" + priceRate.getItempriceid() + ") ");
		if(DramaConst.Y.equals(priceRate.getDisticket())) {
			refundsql.append("and i.disid in (" + priceRate.getDisrecordid() + ") ");
		} else {
			refundsql.append("and i.disid is null ");
		}
		List<Map<String, Object>> refundMapList = jdbcTemplate.queryForList(refundsql.toString(), dramaid, starttime, endtime);
		
		if(CollectionUtils.isNotEmpty(refundMapList)) {
			
			Long quantity = 0l;
			Double totalfee = 0.0;
			Double disfee = 0.0;
			Long taopiaonum = 0l;
			List<String> tradenoList = new ArrayList<String>();
			List<Long> recordidList = new ArrayList<Long>();
			
			for(Map<String, Object> refund : refundMapList) {
				MapRow row = new MapRow(refund);
				String tradeno = row.getStringWithNull("tradeno");
				quantity += row.getLong("quantity");
				totalfee += row.getDouble("totalfee");
				disfee += row.getDouble("disfee");
				taopiaonum += row.getLong("taopiaonum");
				if(!tradenoList.contains(tradeno)) {
					tradenoList.add(tradeno);
				}
				recordidList.add(row.getLongWithNull("recordid"));
			}
			priceBill.setRefundnum(priceBill.getRefundnum() + quantity);
			priceBill.setRefundamount(priceBill.getRefundamount() +  (totalfee - disfee));
			priceBill.setTaopiaonum(priceBill.getTaopiaonum() - taopiaonum);
			//更新已经计算的refund
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateRefundBySettle(priceBill.getRecordid(), recordidList, "drama_orderoffline_item");
			}
		}
		
	}
	
	/**
	 * 更新已经计算的item
	 */
	private void updateItemBySettle(Long priceBillId, List<Long> recordidList, String tableName) {
		List<List<Long>> groupList = BeanUtil.partition(recordidList, 500);
		for(List<Long> recordids : groupList) {
			String sql = "update " + tableName + " set pricebillid = ? where recordid in " + DramaUtil.sqlinList(recordids, Long.class);
			jdbcTemplate.update(sql, priceBillId);
		}
	}
	
	/**
	 * 更新已经计算的refund
	 */
	private void updateRefundBySettle(Long priceBillId, List<Long> recordidList, String tableName) {
		List<List<Long>> groupList = BeanUtil.partition(recordidList, 500);
		for(List<Long> recordids : groupList) {
			String sql = "update " + tableName + " set refundpricebillid = ?, hasrefund = 'Y' where recordid in " + DramaUtil.sqlinList(recordids, Long.class);
			jdbcTemplate.update(sql, priceBillId);
		}
	}
	
	/**
	 * 计算补差券、低值券金额
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Double> getOrderDiscount(Set<String> Morder, Set<String> Mrefund) {
		
		Map<String, Double> map = new HashMap<String, Double>();
		Double mbddisamount = 0.0;
		Double butieamount = 0.0;
		
		if(CollectionUtils.isNotEmpty(Morder)) {
			//查询兑补差券、抵制券
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.ECARD));
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.in("cardtype", DramaConst.CARDTYPE_LIST));
			quy.add(Restrictions.in("relatedid", Morder.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				mbddisamount += discount.getAmount();
			}
			
			//查询场馆补贴
			DetachedCriteria quby = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.PARTNER));
			quby.add(Restrictions.eq("cardtype", DramaConst.SOLDTYPE_M));
			quby.add(Restrictions.in("relatedid", Morder.toArray()));
			List<DramaDiscountItem> bdiscountlist = daoService.findByCriteria(quby);
			for(DramaDiscountItem discount : bdiscountlist) {
				butieamount += discount.getAmount();
			}
		}
		
		if(CollectionUtils.isNotEmpty(Mrefund)) {
			//查询兑补差券、抵制券
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.ECARD));
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.in("cardtype", DramaConst.CARDTYPE_LIST));
			quy.add(Restrictions.in("relatedid", Mrefund.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				mbddisamount -= discount.getAmount();
			}
			
			//查询场馆补贴
			DetachedCriteria quby = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.PARTNER));
			quby.add(Restrictions.eq("cardtype", DramaConst.SOLDTYPE_M));
			quby.add(Restrictions.in("relatedid", Mrefund.toArray()));
			List<DramaDiscountItem> bdiscountlist = daoService.findByCriteria(quby);
			for(DramaDiscountItem discount : bdiscountlist) {
				butieamount -= discount.getAmount();
			}
		}

		map.put("mbddisamount", mbddisamount);
		map.put("butieamount", butieamount);
		return map;
	}
	
	/**
	 * 更新演出配置
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String updateDramConfig(DramaConfig dconfig, boolean sysjob) {
		String result = "";
		Long recordid = dconfig.getRecordid();
		Long dramaversionid = dconfig.getDramaversionid();
		String suppliercode = dconfig.getSuppliercode();
		Long dramaid = dconfig.getDramaid();
		
		//logger.warn("正在同步配置【" + dramaversionid + "|" + suppliercode + "|" + dramaid + "】");
		
		if(StringUtils.isEmpty(suppliercode)) {
			result = "【配置：" + recordid + "】供应商为空，不能更新！";
			logger.warn(result);
			return result;
		}
		
		if(!DramaConst.PERCENT.equals(dconfig.getDistype())) {
			result = "【配置：" + recordid + "】的折扣不是百分比，不能更新！";
			logger.warn(result);
			return result;
		}
		
		if(DramaConst.FINISH.equals(dconfig.getStatus())) {
			result = "【配置：" + recordid + "】配置已完成，不能更新！";
			logger.warn(result);
			return result;
		}
		
		if(dramaversionid == 0) {
			DetachedCriteria query = DetachedCriteria.forClass(DramaVersion.class);
			query.add(Restrictions.eq("dramaid", dramaid));
			query.add(Restrictions.eq("endtime", dconfig.getEndtime()));
			List<DramaVersion> dvlist = daoService.findByCriteria(query);
			if(CollectionUtils.isEmpty(dvlist)) {
				result = "【配置：" + recordid + "】关联不上演出版本，请检查确认！";
				logger.warn(result);
				return result;
			} else {
				dramaversionid = dvlist.get(0).getRecordid();
				dconfig.setDramaversionid(dramaversionid);
			}
		}
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.dramaid, v.suppliercode from webdata.view_drama_settleprice v ");
		sql.append("where v.distype = 'percent' and v.dramaid = ? and v.suppliercode = ? group by v.dramaid, v.suppliercode ");
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString(), dramaid, suppliercode);
		
		if(mapList.size() == 0) {
			result = "【配置：" + recordid + "】没有同步到结算配置，请检查确认！";
			logger.warn(result);
			return result;
		} 
		if(mapList.size() > 1) {
			result = "【配置：" + recordid + "】出现了不同的价格使用不同的结算方式，请检查确认！";
			logger.warn(result);
			if(sysjob) {
				GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
			}
			return result;
		}
			
		//处理以前配置数据
		cleanDramaConfig(dconfig);
		
		StringBuilder settlesql = new StringBuilder("");
		settlesql.append("select v.settlebase, v.settlecycle, v.addtime, v.settleid, v.discount, v.distype, v.theatreprice, v.threcordid ");
		settlesql.append("from webdata.view_drama_settleprice v where v.distype = 'percent' and v.dramaid = ? and v.suppliercode = ? ");
		List<Map<String, Object>> settleList = shJdbcTemplate.queryForList(settlesql.toString(), dramaid, suppliercode);
		
		//填充DramaConfig的【项目id	供应商	项目开始时间	项目结束时间	结算方式	结算周期	状态	是否可结算】
		dconfig = fullDramaConfig(dconfig, settleList.get(0));
		
		//填充DramaPriceRate的【扣率	扣率类型	settleid	价格	itempriceid】
		List<DramaPriceRate> pricerateList = fullDramaPriceRate1(settleList);
		
		//填充DramaPriceRate的【itempriceid seatpriceid direcordid 是否存在套票】
		pricerateList = fullDramaPriceRate2(pricerateList);
		
		//填充DramaPriceRate的【演出项目配置id】
		pricerateList = fullDramaPriceRate3(pricerateList, dconfig);
		
		//保存DramaPriceRate
		daoService.addObjectList(pricerateList);
		
		result = "同步更新完成！";
			
		return result;
	}
	
	/**
	 * 处理价以前配置数据
	 *  若没有数据不操作
	 *  若配置是未审核的状态，将DramaPriceRate的AVAILABLE状态的数据删除，因为财务人员不审批的时候，每天同步一次会产生许多数据，只好删掉
	 *  若配置是已审核的状态，将DramaPriceRate的状态置为EXPIRED
	 * @param config
	 */
	private void cleanDramaConfig(DramaConfig config) {
		long configId = config.getRecordid();
		String status = config.getStatus();
		if(DramaConst.WAITAPPROVAL.equals(status)) {
			String deletesql = "delete from drama_pricerate where configid = ? and status = 'AVAILABLE' ";
			jdbcTemplate.update(deletesql, configId);
		} else if(DramaConst.HASAPPROVAL.equals(status) || DramaConst.SETTLEING.equals(status)) {
			String deletesql = "update drama_pricerate set status = 'EXPIRED' where configid = ? and status = 'AVAILABLE' ";
			jdbcTemplate.update(deletesql, configId);
		}
	}
	
	/**
	 * 填充DramaConfig的【项目id	供应商	项目开始时间	项目结束时间	结算方式	结算周期	状态	是否可结算】
	 * @param tmp 
	 * @param dramaMap
	 * @return
	 */
	private DramaConfig fullDramaConfig(DramaConfig dconfig, Map<String, Object> map) {
		
		MapRow rowconfig = new MapRow(map);
		
		dconfig.setSettlebase(rowconfig.getStringWithNull("settlebase"));
		dconfig.setSettlecycle(rowconfig.getStringWithNull("settlecycle"));
		dconfig.setAddtime(rowconfig.getTimestamp("addtime"));
		dconfig.setStatus(DramaConst.WAITAPPROVAL);
		dconfig.setGspupdatetime(DateUtil.getCurFullTimestamp());
		dconfig.setCanjiti(DramaConst.N);
		//保存DramaConfig
		daoService.updateObject(dconfig);
		
		return dconfig;
	}
	
	
	
	/**
	 * 填充DramaPriceRate的【扣率	扣率类型	settleid	价格	itempriceid】
	 * @param settleList
	 * @return
	 */
	private List<DramaPriceRate> fullDramaPriceRate1(List<Map<String, Object>> settleList) {
		List<BigDecimal> priceList = BeanUtil.getBeanPropertyList(settleList, "theatreprice", true);
		List<DramaPriceRate> pricerateList = new ArrayList<DramaPriceRate>();
		for(BigDecimal price : priceList) {
			DramaPriceRate priceRate = new DramaPriceRate();
			priceRate.setItempriceid("");
			for(Map<String, Object> settle : settleList) {
				MapRow row = new MapRow(settle);
				if(price.doubleValue() == row.getDouble("theatreprice")) {
					priceRate.setDiscount(row.getDouble("discount"));
					priceRate.setSettleid(row.getStringWithNull("settleid"));
					priceRate.setPrice(price.doubleValue());
					String itempriceid = row.getString("threcordid");
					if("".equals(priceRate.getItempriceid())) {
						priceRate.setItempriceid(itempriceid);
					} else {
						priceRate.setItempriceid(priceRate.getItempriceid() + "," + itempriceid);
					}
				}
			}
			pricerateList.add(priceRate);
		}
		return pricerateList;
	}
	
	/**
	 * 填充DramaPriceRate的【itempriceid seatpriceid direcordid  是否存在套票】
	 * @param pricerateList
	 * @return
	 */
	private List<DramaPriceRate> fullDramaPriceRate2(List<DramaPriceRate> pricerateList) {
		List<DramaPriceRate> completepricerateList = new ArrayList<DramaPriceRate>();
		for(DramaPriceRate priceRate : pricerateList) {
			
			String itemSql = "select v.itempriceid, v.seatpriceid, v.disrecordid from webdata.view_drama_seat_dis v where v.itempriceid in (" + priceRate.getItempriceid() + ") ";
			List<Map<String, Object>> seatPriceList = shJdbcTemplate.queryForList(itemSql);
			
			String seatpriceid = "";
			String disrecordid = "";
			for(Map<String, Object> map : seatPriceList) {
				MapRow row = new MapRow(map);
				String mapseatpriceid = row.getStringWithNull("seatpriceid");
				String mapdirecordid = row.getStringWithNull("disrecordid");
				if(mapseatpriceid != null) {
					if("".equals(seatpriceid)) {
						seatpriceid += mapseatpriceid;
					} else {
						seatpriceid += "," + mapseatpriceid;
					}
				}
				if(mapdirecordid != null) {
					if("".equals(disrecordid)) {
						disrecordid += mapdirecordid;
					} else {
						disrecordid += "," + mapdirecordid;
					}
				}
			}
			priceRate.setDisticket(DramaConst.N);
			priceRate.setSeatpriceid(seatpriceid);
			completepricerateList.add(priceRate);
			//存在套票
			if(!"".equals(disrecordid)) {
				DramaPriceRate disPriceRate = new DramaPriceRate();
				BeanUtil.copyProperties(disPriceRate, priceRate);
				priceRate.setDisrecordid(disrecordid);
				priceRate.setDisticket(DramaConst.Y);
				completepricerateList.add(disPriceRate);
			}
		}
		return completepricerateList;
	}
	
	/**
	 * 填充DramaPriceRate的【演出项目配置id 状态  更新时间】
	 * @param pricerateList
	 * @return
	 */
	private List<DramaPriceRate> fullDramaPriceRate3(List<DramaPriceRate> pricerateList, DramaConfig config) {
		List<DramaPriceRate> completepricerateList = new ArrayList<DramaPriceRate>();
		for(DramaPriceRate pricerate : pricerateList) {
			pricerate.setConfigid(config.getRecordid());
			pricerate.setGspupdatetime(DateUtil.getCurFullTimestamp());
			pricerate.setStatus(DramaConst.AVAILABLE);
			completepricerateList.add(pricerate);
		}
		return completepricerateList;
	}

	
	/**
	 * 生成结算单据
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String createSettleBill(DramaConfig dconfig) {
		
		String result = DramaConst.OK;
		
		Timestamp starttime = dconfig.getLastendtime();
		if(starttime == null) {
			result = "【configid:" + dconfig.getRecordid() + "】出单失败，单据的开始时间为空，请查看配置是否存在问题！";
			logger.error(result);
			return result;
		}
		DetachedCriteria query = DetachedCriteria.forClass(DramaPriceRate.class);
		query.add(Restrictions.eq("configid", dconfig.getRecordid()));
		query.add(Restrictions.eq("status", DramaConst.AVAILABLE));
		List<DramaPriceRate> prList = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(prList)) {
			result = "【configid:" + dconfig.getRecordid() + "】出单失败，该配置下没有价格扣率配置！";
			logger.error(result);
			return result;
		}
		
		Timestamp endtime = null;
		Long curr = DateUtil.getCurDate().getTime();
		String status = DramaConst.SETTLEING;
		String lastbill = DramaConst.N;
		boolean needupdate = true;
		
		while(true) {
			//最后一期已出，无需再算
			if(DramaConst.Y.equals(lastbill)) {
				break;
			}
			endtime = getBillEndTime(starttime, dconfig.getSettlecycle());
			if(endtime == null) {
				needupdate = false;
				break;
			}
			if(endtime.getTime() >= dconfig.getEndtime().getTime()) {
				//最后一期单据，如果结束时间在2月和10月，时间加上7天，尽量计算退票，其他的加3天
				int mouth = DateUtil.getMonth(dconfig.getEndtime());
				if(mouth == 2 || mouth == 10) {
					endtime = DateUtil.addDay(dconfig.getEndtime(), DramaConst.VERINTERVALDAY) ;
				} else {
					endtime = DateUtil.addDay(dconfig.getEndtime(), DramaConst.PUTOFFDAY) ;
				}
				lastbill = DramaConst.Y;
				status = DramaConst.FINISH;
			}
			//最后一期单据要在结束时间之后出来
			if(DramaConst.Y.equals(lastbill)) {
				if(curr < endtime.getTime()) {
					status = DramaConst.SETTLEING;
					lastbill = DramaConst.N;
					break;
				}
			} else {//正常单据限定结束时间之后的3天出单据
				Timestamp putoff = DateUtil.addDay(endtime, DramaConst.PUTOFFDAY);
				if(curr < putoff.getTime()) {
					break;
				}
			}
			logger.warn("正在给【" + dconfig.getDramaid() + "|" + dconfig.getSuppliercode() +"】出【" + starttime + " - " + endtime + "】期单据");

			DramaSettleBill settltbill = new DramaSettleBill(dconfig.getDramaid(), dconfig.getSuppliercode(), starttime, 
					endtime, dconfig.getRecordid(), lastbill);
			daoService.addObject(settltbill);
			
			for(DramaPriceRate pricetate : prList) {
				String rate = pricetate.getDiscount() + "%";
				DramaPriceBill priceBill = new DramaPriceBill(pricetate.getPrice(), pricetate.getDisticket(), 
						rate, settltbill.getRecordid(), pricetate.getRecordid());
				daoService.addObject(priceBill);
			}
			
			starttime = endtime;
		}
		
		if(needupdate) {
			//时间不相等说明才有更新
			if(!starttime.equals(dconfig.getLastendtime())) {
				dconfig.setStatus(status);
				dconfig.setLastendtime(starttime);
				dconfig.setGspupdatetime(DateUtil.getCurFullTimestamp());
				daoService.updateObject(dconfig);
				result = "【configid:" + dconfig.getRecordid() + "】出单完成";
			}
		} else {
			result = "【configid:" + dconfig.getRecordid() + "】出单异常";
		}
		
		return result;
	}
	
	/**
	 * 获取单据的结束时间
	 * @param starttime
	 * @param settlecycle
	 * @return
	 */
	private Timestamp getBillEndTime(Timestamp starttime, String settlecycle) {
		Timestamp endtime = null;
		if(DramaConst.TN.equals(settlecycle)) {
			endtime = DateUtil.addDay(starttime, 1);
		} else if(DramaConst.WEEKLY.equals(settlecycle)) {
			endtime = DateUtil.addDay(starttime, 7);
		} else if(DramaConst.MIDDLE.equals(settlecycle)) {
			int curDay = DateUtil.getDay(starttime);
			if(curDay >= 16){
				endtime = DateUtil.getNextMonthFirstDay(starttime);
			}else{
				endtime = DateUtil.parseTimestamp(DateUtil.getYear(starttime) + "-" + DateUtil.getMonth(starttime) + "-16 00:00:00");
			}
		} else if(DramaConst.MONTHLY.equals(settlecycle)
				|| DramaConst.PROGRAM.equals(settlecycle)
				|| DramaConst.SETTLE.equals(settlecycle)) {
			endtime = DateUtil.getNextMonthFirstDay(starttime);
		}
		return endtime;
	}
	
	/**
	 * 重置初始化单据
	 */
	@Override
	public void initDramaBill(DramaSettleBill bill) {
		List<DramaPriceBill> priceBillList = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", bill.getRecordid());
		
		bill.setTicketnum(0l);
		bill.setTicketamount(0.0);
		bill.setRefundnum(0l);
		bill.setRefundamount(0.0);
		bill.setAdjustnum(0l);
		bill.setAdjustamount(0.0);
		bill.setAdjustdesc("");
		bill.setSettleamount(0.0);
		bill.setMadisamount(0.0);
		bill.setMbddisamount(0.0);
		bill.setButieamount(0.0);
		bill.setPayamount(0.0);
		bill.setStatus(DramaConst.NEW);
		bill.setSyspayamount(0.0);
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		
		daoService.updateObject(bill);
		
		for(DramaPriceBill pricebill : priceBillList) {
			pricebill.setTicketnum(0l);
			pricebill.setTicketamount(0.0);
			pricebill.setRefundnum(0l);
			pricebill.setRefundamount(0.0);
			pricebill.setSettleamount(0.0);
			pricebill.setMadisamount(0.0);
			pricebill.setPayamount(0.0);
			pricebill.setTaopiaonum(0l);
			pricebill.setPlaceid(null);
			pricebill.setGspupdatetime(DateUtil.getCurFullTimestamp());
			daoService.updateObject(pricebill);
		}
	}
}

