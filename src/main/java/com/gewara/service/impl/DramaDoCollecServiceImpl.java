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
	 * �ֶ�ͬ���ݳ�
	 */
	@Override
	public String syncDramaByDramaid(List<Long> dramaidlist) {
		
		StringBuilder sql = new StringBuilder("");
		sql.append("select v.recordid, v.dramaname, v.pretype, v.updatetime, v.adddate, v.releasedate, v.enddate, d.origin ");
		sql.append("from webdata.view_drama v left outer join webdata.VIEW_DRAMAPROFILE d on d.DRAMAID = v.recordid ");
		sql.append("where v.recordid in " + DramaUtil.sqlinList(dramaidlist, Long.class));
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString());
		
		if(CollectionUtils.isEmpty(mapList)) {
			return "��Ŀ������!";
		}
		updateDrama(mapList);
		
		return "ͬ���ɹ�";
	}
	
	/**
	 * ����ͬ���ݳ���Ŀ����
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
				//�����µ�һ���汾��starttime����һ���汾��endtime���бȽ�
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
	 * �ֶ�ͬ������
	 * @param dramaidlist
	 * @return
	 */
	@Override
	public String syncPlayItemByDramaid(List<Long> dramaidlist) {
		if(CollectionUtils.isEmpty(dramaidlist)) {
			return "��ĿidΪ��";
		}
		String sql = "select v.recordid, v.dpid, v.dramaid, v.playtime from WEBDATA.VIEW_OPEN_DRAMA_ITEM v where v.dramaid in " + DramaUtil.sqlinList(dramaidlist, Long.class);
		List<Map<String, Object>> mapList = shJdbcTemplate.queryForList(sql.toString());
		if(CollectionUtils.isEmpty(mapList)) {
			return "û��ѯ��س���";
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
		return "ͬ���ɹ���";
	}
	
	/**
	 * �ֶ�ͬ�����ð汾
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
			result = "��Ŀ" + dramaid + "û�в�ѯ�����ü�¼��";
			return result;
		}
		
		List<Map<String, Object>> finalmapList = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> configmap : mapList) {
			MapRow rowdconfig = new MapRow(configmap);
			String suppliercode = rowdconfig.getStringWithNull("suppliercode");
			String distype = rowdconfig.getStringWithNull("distype");
			if(StringUtils.isEmpty(suppliercode) || StringUtils.isEmpty(distype)) {
				result = "��Ŀ��" + dramaid + "�����ڹ�Ӧ�̱�����߿�������Ϊ�յ����ã��뼰ʱ�����ҵ����Աȷ��>>";
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
		
		String result = "ͬ����ɣ�";
		
		MapRow rowdconfig = new MapRow(configmap);
		long dramaid = rowdconfig.getLongWithNull("dramaid");
		String suppliercode = rowdconfig.getStringWithNull("suppliercode");
		String distype = rowdconfig.getStringWithNull("distype");
		
		//��ȡ���µ��ݳ���Ϣ
		String dramasql = "select v.adddate, v.releasedate, v.enddate, v.pretype from webdata.view_drama v where v.recordid = ? ";
		List<Map<String, Object>> dramaMaplist = shJdbcTemplate.queryForList(dramasql, dramaid);
		if(CollectionUtils.isEmpty(dramaMaplist)) {
			result = "����Ŀ��" + dramaid + "�������ݳ���Ŀ�б��в����ڣ���֪ͨ���ҵ����Ա��";
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
			result = "����Ŀ��" + dramaid + "���ĳ��֡���ʼ�ݳ�ʱ�䡯�ȡ������ݳ�ʱ�䡯С�İ汾����֪ͨ���ҵ����Ա��";
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
			//ȡ�����µİ汾����
			DramaConfig dc = dcList.get(0);
			Timestamp oldendtime = dc.getEndtime();
			
			if(starttime.getTime() <= oldendtime.getTime()) {
				/* 
				 * 1.������õ�״̬�ǡ�����ɡ���������ˡ���
				 * 2.������õ�״̬�ǡ�����ˡ����������С���������ɡ�ֱ�Ӹ��½���ʱ�䲢���ʼ�����֪ͨ��
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
					
					logger.warn("���ݳ�����id��" + dc.getRecordid() + "������ͨ��֮�������ݸ���!");
				} else {
					result = "���ݳ�����id��" + dc.getRecordid() + "���Ѿ�������ɣ����ǻ����������ݸ��£�����ϵ���ҵ����Ա��";
					logger.warn(result);
					if(sysjob) {
						GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
					}
				}
			} else {
				//������һ�����ð汾
				createDramaConfig(suppliercode, oldendtime, endtime, dramaid, distype);
			}
		} else {
			createDramaConfig(suppliercode, rowdrama.getTimestamp("adddate"), endtime, dramaid, distype);
		}
		return result;
	}
	
	/**
	 * ͬ���ݳ���Ŀ���ð汾����
	 */
	@SuppressWarnings("unchecked")
	private void createDramaConfig(String suppliercode, Timestamp starttime, Timestamp endtime, Long dramaid, String distype) {
		/*
		 * ���õĿ�ʼʱ��ʹ��addtime��Ϊ�˼�����ǰ���ܴ���ͬһ����Ŀʹ�ö���ݳ��������趨ʱ�俪ʼ
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
	 * ���㵥��
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
		//������ֶ���ˢ�µ��ݲ���Ϊ��Ŀ�����һ�ڵ��ݣ������ʱ��ȡ��ǰʱ��
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
			
			//���϶�������
			onlineOrderCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill, Morder);
			//�����˵�
			onlineRefundCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill, Mrefund);
			
			//���¶�������
			offlineOrderCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill);
			//�����˼���
			offlineRefundCollection(dramaid, start, end, playids, DramaConst.SETTLEBILL, priceRate, priceBill);
			
			//Ʊ�񶩵�����
			//Ʊ���˵�����
			
			//������=(Ʊ����-��Ʊ���)*�ۿ�
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
		
		result = "����ɹ�";
		return result;
	}
	
	/**
	 * �����㵥��
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> checkFullbill(DramaSettleBill bill) {
		
		Map<String, Object> resultmap = new HashMap<String, Object>();
		String result;
		
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, bill.getConfigid());
		if(dconfig == null) {
			settleBack(bill);
			result = "���㵥" + bill.getRecordid() + "������Ϊ��";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		if(DramaConst.WAITFINISH.equals(dconfig.getStatus()) || DramaConst.WAITAPPROVAL.equals(dconfig.getStatus())) {
			result = "���㵥" + bill.getRecordid() + "������״̬����ȷ";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		List<DramaPriceBill> priceBillList = daoService.getObjectListByField(DramaPriceBill.class, "settlebillid", bill.getRecordid());
		if(CollectionUtils.isEmpty(priceBillList)) {
			settleBack(bill);
			result = "���㵥" + bill.getRecordid() + "û�м۸���˵�";
			logger.error(result);
			resultmap.put("result", result);
			return resultmap;
		}
		for(DramaPriceBill priceBill : priceBillList) {
			DramaPriceRate priceRate = daoService.getObject(DramaPriceRate.class, priceBill.getPricerateid());
			if(priceRate == null) {
				settleBack(bill);
				result = "�۸���˵�" + priceBill.getRecordid() + "�۸��������Ϊ��";
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
			result = "���㵥" + bill.getRecordid() + "�ݳ��汾û�в�ѯ������!";
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
	
	//�쳣����
	private void settleBack(DramaSettleBill bill) {
		bill.setStatus(DramaConst.EXCEPTION);
		bill.setGspupdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(bill);
	}
	
	/**
	 * �������ɱ�����
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
			result =  "���ᵥ����" + dconfig.getRecordid() + "�ݳ��汾û�в�ѯ������!";
			logger.error(result);
			return result;
		}
		List<Long> playids = BeanUtil.getBeanPropertyList(playlist, "dpid", true);
		
		DetachedCriteria pquery = DetachedCriteria.forClass(DramaPriceRate.class);
		pquery.add(Restrictions.eq("configid", dconfig.getRecordid()));
		pquery.add(Restrictions.eq("status", DramaConst.AVAILABLE));
		List<DramaPriceRate> priceRatelist = daoService.findByCriteria(pquery);
		
		if(CollectionUtils.isEmpty(priceRatelist)) {
			result =  "���ᵥ����" + dconfig.getRecordid() + "û�в�ѯ�����õĿ�������!";
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
			
			//���϶�������
			onlineOrderCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill, Morder);
			//�����˵�
			onlineRefundCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill, Mrefund);
			
			//���¶�������
			offlineOrderCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill);
			//�����˼���
			offlineRefundCollection(dramaid, start, end, playids, DramaConst.TIJIBILL, priceRate, priceBill);
			
			//Ʊ�񶩵�����
			//Ʊ���˵�����
			
			//������=(Ʊ����-��Ʊ���)*�ۿ�
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
	 * ���϶������ݾۺϼ���
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
			//��ѯ�һ�ȯ
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
			//�����Լ����orderitem
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateItemBySettle(priceBill.getRecordid(), recordidList, "drama_orderonline_item");
			}
		}
	}
	
	
	/**
	 * �����˵����ݾۺϼ���
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
			//��ѯ�һ�ȯ
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
			//�����Ѿ������refund
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateRefundBySettle(priceBill.getRecordid(), recordidList, "drama_orderonline_item");
			}
		}
	}
	
	/**
	 * ���¶������ݾۺϼ���
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
			//�����Լ����orderitem
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateItemBySettle(priceBill.getRecordid(), recordidList, "drama_orderoffline_item");
			}
		}
		
	}
	
	
	/**
	 * �����˵����ݾۺϼ���
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
			//�����Ѿ������refund
			if(DramaConst.SETTLEBILL.equals(dotype)) {
				updateRefundBySettle(priceBill.getRecordid(), recordidList, "drama_orderoffline_item");
			}
		}
		
	}
	
	/**
	 * �����Ѿ������item
	 */
	private void updateItemBySettle(Long priceBillId, List<Long> recordidList, String tableName) {
		List<List<Long>> groupList = BeanUtil.partition(recordidList, 500);
		for(List<Long> recordids : groupList) {
			String sql = "update " + tableName + " set pricebillid = ? where recordid in " + DramaUtil.sqlinList(recordids, Long.class);
			jdbcTemplate.update(sql, priceBillId);
		}
	}
	
	/**
	 * �����Ѿ������refund
	 */
	private void updateRefundBySettle(Long priceBillId, List<Long> recordidList, String tableName) {
		List<List<Long>> groupList = BeanUtil.partition(recordidList, 500);
		for(List<Long> recordids : groupList) {
			String sql = "update " + tableName + " set refundpricebillid = ?, hasrefund = 'Y' where recordid in " + DramaUtil.sqlinList(recordids, Long.class);
			jdbcTemplate.update(sql, priceBillId);
		}
	}
	
	/**
	 * ���㲹��ȯ����ֵȯ���
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Double> getOrderDiscount(Set<String> Morder, Set<String> Mrefund) {
		
		Map<String, Double> map = new HashMap<String, Double>();
		Double mbddisamount = 0.0;
		Double butieamount = 0.0;
		
		if(CollectionUtils.isNotEmpty(Morder)) {
			//��ѯ�Ҳ���ȯ������ȯ
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.ECARD));
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.in("cardtype", DramaConst.CARDTYPE_LIST));
			quy.add(Restrictions.in("relatedid", Morder.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				mbddisamount += discount.getAmount();
			}
			
			//��ѯ���ݲ���
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
			//��ѯ�Ҳ���ȯ������ȯ
			DetachedCriteria quy = DetachedCriteria.forClass(DramaDiscountItem.class);
			quy.add(Restrictions.eq("tag", DramaConst.ECARD));
			quy.add(Restrictions.eq("soldtype", DramaConst.SOLDTYPE_M));
			quy.add(Restrictions.in("cardtype", DramaConst.CARDTYPE_LIST));
			quy.add(Restrictions.in("relatedid", Mrefund.toArray()));
			List<DramaDiscountItem> discountlist = daoService.findByCriteria(quy);
			for(DramaDiscountItem discount : discountlist) {
				mbddisamount -= discount.getAmount();
			}
			
			//��ѯ���ݲ���
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
	 * �����ݳ�����
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String updateDramConfig(DramaConfig dconfig, boolean sysjob) {
		String result = "";
		Long recordid = dconfig.getRecordid();
		Long dramaversionid = dconfig.getDramaversionid();
		String suppliercode = dconfig.getSuppliercode();
		Long dramaid = dconfig.getDramaid();
		
		//logger.warn("����ͬ�����á�" + dramaversionid + "|" + suppliercode + "|" + dramaid + "��");
		
		if(StringUtils.isEmpty(suppliercode)) {
			result = "�����ã�" + recordid + "����Ӧ��Ϊ�գ����ܸ��£�";
			logger.warn(result);
			return result;
		}
		
		if(!DramaConst.PERCENT.equals(dconfig.getDistype())) {
			result = "�����ã�" + recordid + "�����ۿ۲��ǰٷֱȣ����ܸ��£�";
			logger.warn(result);
			return result;
		}
		
		if(DramaConst.FINISH.equals(dconfig.getStatus())) {
			result = "�����ã�" + recordid + "����������ɣ����ܸ��£�";
			logger.warn(result);
			return result;
		}
		
		if(dramaversionid == 0) {
			DetachedCriteria query = DetachedCriteria.forClass(DramaVersion.class);
			query.add(Restrictions.eq("dramaid", dramaid));
			query.add(Restrictions.eq("endtime", dconfig.getEndtime()));
			List<DramaVersion> dvlist = daoService.findByCriteria(query);
			if(CollectionUtils.isEmpty(dvlist)) {
				result = "�����ã�" + recordid + "�����������ݳ��汾������ȷ�ϣ�";
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
			result = "�����ã�" + recordid + "��û��ͬ�����������ã�����ȷ�ϣ�";
			logger.warn(result);
			return result;
		} 
		if(mapList.size() > 1) {
			result = "�����ã�" + recordid + "�������˲�ͬ�ļ۸�ʹ�ò�ͬ�Ľ��㷽ʽ������ȷ�ϣ�";
			logger.warn(result);
			if(sysjob) {
				GSPMaill.sendMaill(result, GSPSendMaill.DRAMAMAIL);
			}
			return result;
		}
			
		//������ǰ��������
		cleanDramaConfig(dconfig);
		
		StringBuilder settlesql = new StringBuilder("");
		settlesql.append("select v.settlebase, v.settlecycle, v.addtime, v.settleid, v.discount, v.distype, v.theatreprice, v.threcordid ");
		settlesql.append("from webdata.view_drama_settleprice v where v.distype = 'percent' and v.dramaid = ? and v.suppliercode = ? ");
		List<Map<String, Object>> settleList = shJdbcTemplate.queryForList(settlesql.toString(), dramaid, suppliercode);
		
		//���DramaConfig�ġ���Ŀid	��Ӧ��	��Ŀ��ʼʱ��	��Ŀ����ʱ��	���㷽ʽ	��������	״̬	�Ƿ�ɽ��㡿
		dconfig = fullDramaConfig(dconfig, settleList.get(0));
		
		//���DramaPriceRate�ġ�����	��������	settleid	�۸�	itempriceid��
		List<DramaPriceRate> pricerateList = fullDramaPriceRate1(settleList);
		
		//���DramaPriceRate�ġ�itempriceid seatpriceid direcordid �Ƿ������Ʊ��
		pricerateList = fullDramaPriceRate2(pricerateList);
		
		//���DramaPriceRate�ġ��ݳ���Ŀ����id��
		pricerateList = fullDramaPriceRate3(pricerateList, dconfig);
		
		//����DramaPriceRate
		daoService.addObjectList(pricerateList);
		
		result = "ͬ��������ɣ�";
			
		return result;
	}
	
	/**
	 * �������ǰ��������
	 *  ��û�����ݲ�����
	 *  ��������δ��˵�״̬����DramaPriceRate��AVAILABLE״̬������ɾ������Ϊ������Ա��������ʱ��ÿ��ͬ��һ�λ����������ݣ�ֻ��ɾ��
	 *  ������������˵�״̬����DramaPriceRate��״̬��ΪEXPIRED
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
	 * ���DramaConfig�ġ���Ŀid	��Ӧ��	��Ŀ��ʼʱ��	��Ŀ����ʱ��	���㷽ʽ	��������	״̬	�Ƿ�ɽ��㡿
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
		//����DramaConfig
		daoService.updateObject(dconfig);
		
		return dconfig;
	}
	
	
	
	/**
	 * ���DramaPriceRate�ġ�����	��������	settleid	�۸�	itempriceid��
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
	 * ���DramaPriceRate�ġ�itempriceid seatpriceid direcordid  �Ƿ������Ʊ��
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
			//������Ʊ
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
	 * ���DramaPriceRate�ġ��ݳ���Ŀ����id ״̬  ����ʱ�䡿
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
	 * ���ɽ��㵥��
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String createSettleBill(DramaConfig dconfig) {
		
		String result = DramaConst.OK;
		
		Timestamp starttime = dconfig.getLastendtime();
		if(starttime == null) {
			result = "��configid:" + dconfig.getRecordid() + "������ʧ�ܣ����ݵĿ�ʼʱ��Ϊ�գ���鿴�����Ƿ�������⣡";
			logger.error(result);
			return result;
		}
		DetachedCriteria query = DetachedCriteria.forClass(DramaPriceRate.class);
		query.add(Restrictions.eq("configid", dconfig.getRecordid()));
		query.add(Restrictions.eq("status", DramaConst.AVAILABLE));
		List<DramaPriceRate> prList = daoService.findByCriteria(query);
		if(CollectionUtils.isEmpty(prList)) {
			result = "��configid:" + dconfig.getRecordid() + "������ʧ�ܣ���������û�м۸�������ã�";
			logger.error(result);
			return result;
		}
		
		Timestamp endtime = null;
		Long curr = DateUtil.getCurDate().getTime();
		String status = DramaConst.SETTLEING;
		String lastbill = DramaConst.N;
		boolean needupdate = true;
		
		while(true) {
			//���һ���ѳ�����������
			if(DramaConst.Y.equals(lastbill)) {
				break;
			}
			endtime = getBillEndTime(starttime, dconfig.getSettlecycle());
			if(endtime == null) {
				needupdate = false;
				break;
			}
			if(endtime.getTime() >= dconfig.getEndtime().getTime()) {
				//���һ�ڵ��ݣ��������ʱ����2�º�10�£�ʱ�����7�죬����������Ʊ�������ļ�3��
				int mouth = DateUtil.getMonth(dconfig.getEndtime());
				if(mouth == 2 || mouth == 10) {
					endtime = DateUtil.addDay(dconfig.getEndtime(), DramaConst.VERINTERVALDAY) ;
				} else {
					endtime = DateUtil.addDay(dconfig.getEndtime(), DramaConst.PUTOFFDAY) ;
				}
				lastbill = DramaConst.Y;
				status = DramaConst.FINISH;
			}
			//���һ�ڵ���Ҫ�ڽ���ʱ��֮�����
			if(DramaConst.Y.equals(lastbill)) {
				if(curr < endtime.getTime()) {
					status = DramaConst.SETTLEING;
					lastbill = DramaConst.N;
					break;
				}
			} else {//���������޶�����ʱ��֮���3�������
				Timestamp putoff = DateUtil.addDay(endtime, DramaConst.PUTOFFDAY);
				if(curr < putoff.getTime()) {
					break;
				}
			}
			logger.warn("���ڸ���" + dconfig.getDramaid() + "|" + dconfig.getSuppliercode() +"������" + starttime + " - " + endtime + "���ڵ���");

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
			//ʱ�䲻���˵�����и���
			if(!starttime.equals(dconfig.getLastendtime())) {
				dconfig.setStatus(status);
				dconfig.setLastendtime(starttime);
				dconfig.setGspupdatetime(DateUtil.getCurFullTimestamp());
				daoService.updateObject(dconfig);
				result = "��configid:" + dconfig.getRecordid() + "���������";
			}
		} else {
			result = "��configid:" + dconfig.getRecordid() + "�������쳣";
		}
		
		return result;
	}
	
	/**
	 * ��ȡ���ݵĽ���ʱ��
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
	 * ���ó�ʼ������
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

