/** 
 */
package com.gewara.service.impl;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleConfigUpd;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.SettleConfigService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Oct 9, 2013  1:57:47 PM
 */

@Service("settleConfigService")
public class SettleConfigServiceImpl implements SettleConfigService{
	private final GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	/* (non-Javadoc)
	 * @see com.gewara.service.SettleConfigService#countingUnApprovedConfig(java.lang.String)
	 */
	@Override
	public int countingUnApprovedConfig(String openType,String placeId) {
		DetachedCriteria query = DetachedCriteria.forClass(SettleConfigUpd.class);
		if (StringUtils.isNotBlank(openType)){
			query.add(Restrictions.eq("openType", openType));
		}
		if(StringUtils.isNotEmpty(placeId)){
			query.add(Restrictions.eq("recordId", placeId));
		}
		
		query.add(Restrictions.eq("status", "N"));
		
		query.setProjection(Projections.countDistinct("recordId"));
		Object r = hibernateTemplate.findByCriteria(query).get(0);
		return Integer.valueOf(r.toString());
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.SettleConfigService#queryUnApprovedConfig(java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<SettleConfigUpd> queryUnApprovedConfig(String openType,String placeId,
			Integer pageNo, Integer pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(SettleConfigUpd.class);
		if (StringUtils.isNotBlank(openType)){
			query.add(Restrictions.eq("openType", openType));
		}
		if(StringUtils.isNotEmpty(placeId)){
			query.add(Restrictions.eq("recordId", placeId));
		}
		query.add(Restrictions.eq("status", "N"));
		query.addOrder(Order.desc("recordId"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}

	/* (non-Javadoc)
	 * @see com.gewara.service.SettleConfigService#removeSettleConfig(com.gewara.model.gsp.SettleConfig, long)
	 */
	@Override
	public ResultCode removeSettleConfig(SettleConfig sc, long uid) {
		String configId = sc.getRecordId();
		String deleteCheckBill = " delete CheckBill cb where cb.status = '" 
									+ CheckBillStatusEnums.NEW.getStatus() + "' and cb.configId = ?" ;
		String deleteSettleBill = " delete SettlementBill sb where sb.status = '" 
									+ CheckBillStatusEnums.NEW.getStatus() + "' and sb.configId = ?" ;
		
		hibernateTemplate.bulkUpdate(deleteCheckBill, configId);
		hibernateTemplate.bulkUpdate(deleteSettleBill, configId);
		
		SettleConfig chanedSc = new SettleConfig();
		BeanUtil.copyProperties(chanedSc, sc);
		chanedSc.setRecordId(DateUtil.getCurDateStr() + "_R_" + sc.getRecordId());
		chanedSc.setStatus("D");
		daoService.removeObject(sc);
		daoService.saveObject(chanedSc);
		
		Place place = daoService.getObject(Place.class, configId);
		place.setIsConfig("N");
		daoService.saveObject(place);
		return ResultCode.getSuccessReturn(chanedSc);
	}
	
	/**
	 *结算配置修改出来
	 */
	@Override
	public void processBillAfterModifySettleConfig(SettleConfig newSettleConfig, SettleConfig oldSettleConfig){
		if(oldSettleConfig == null){
			return;
		}
		String recordId = oldSettleConfig.getRecordId();
		dbLogger.warn("移除oldSettleConfig下未提交的结算单，对账单>>>oldSettleConfig.recordId=" + oldSettleConfig.getRecordId());
		
		String deleteCheckBill = " delete CheckBill cb where cb.status = '"  + CheckBillStatusEnums.NEW.getStatus() + "' and cb.configId = ?" ;
		String deleteSettleBill = " delete SettlementBill sb where sb.status = '" + CheckBillStatusEnums.NEW.getStatus() + "' and sb.configId = ?" ;

		int delCheckBillNum = hibernateTemplate.bulkUpdate(deleteCheckBill, recordId);
		int delSettleBillNum = hibernateTemplate.bulkUpdate(deleteSettleBill, recordId);
		dbLogger.warn("recordId = " + recordId + "删除结算单：" + delSettleBillNum + " 条记录，删除对账单：" + delCheckBillNum + " 条记录！");
		
		String tag = SettleConstant.TAG_SETTLEMENTBILL_TICKET;
		String configId = newSettleConfig.getRecordId();
		
		//firstSettle 不相等并且 新配置首次结算时间 > 旧配置首次结算时间  按新配置项补充结算单
		String sql = " SELECT CONFIG_ID,MAX(END_TIME) FROM SETTLEMENT_BILL where tag= ? and config_id=? GROUP BY CONFIG_ID";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql,tag,configId);
		if(CollectionUtils.isEmpty(result)){
			return;
		}
		Map<String,Object> lastDateMap = result.get(0);
		Timestamp billLastEndDate = Timestamp.valueOf(lastDateMap.get("MAX").toString());
		
		Timestamp firstSettleDate =  DateUtil.parseTimestamp(DateUtil.format(newSettleConfig.getFirstSettle(), "yyyy-MM-dd") + " " + newSettleConfig.getSettleTime() + ":00", "yyyy-MM-dd HH:mm:ss"); 
		Timestamp start = DateUtil.parseTimestamp(DateUtil.format(billLastEndDate, "yyyy-MM-dd") + " " + newSettleConfig.getSettleTime() + ":00", "yyyy-MM-dd HH:mm:ss");
		
		List<String> diffField = this.compareSettleConfigBeans(newSettleConfig, oldSettleConfig);
		if(CollectionUtils.isEmpty(diffField)){
			return;
		}
		if(!newSettleConfig.getFirstSettle().equals(oldSettleConfig.getFirstSettle()) 
				&& newSettleConfig.getFirstSettle().after(oldSettleConfig.getFirstSettle())){
			// 查询历史对账单
			//如果首次结算日期大于max(end_time) 则为修改了首次结算日期
			if(firstSettleDate.after(billLastEndDate)){
				//补充结算单
				SettlementBill initedBill = new SettlementBill(start, firstSettleDate, tag, newSettleConfig.getRelateId(), configId);
				dbLogger.warn("补充结算单:configId : " + configId + start.toString() + "——" + firstSettleDate.toString());
				daoService.saveObject(initedBill);
				//补充一天对账单
				Timestamp checkBillEnd = DateUtil.addDay(start, 1);
				CheckBill checkBill = new CheckBill(start, checkBillEnd, tag, newSettleConfig.getRelateId());
				daoService.saveObject(checkBill);
				hibernateTemplate.flush();
				queryUnSettledOrdersAndSendMail(firstSettleDate, start);
			}
		}
	}
	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: queryUnSettledOrdersAndSendMail  
	 * @Description:
	 * @param @param billLastEndDate
	 * @param @param start
	 * @param @param field
	 * @return void
	 * @throws  
	 */
	private void queryUnSettledOrdersAndSendMail(Timestamp start,Timestamp end) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.or(Restrictions.and(Restrictions.ge("dealTime", start), Restrictions.le("dealTime", end)), Restrictions.and(Restrictions.ge("useTime", start), Restrictions.le("useTime", end))));
		query.add(Restrictions.eq("isSettled", "N"));
		query.add(Restrictions.eq("orderStatus", "paid_success"));
		List<GewaOrder> orders =  daoService.findByCriteria(query);
		if(CollectionUtils.isNotEmpty(orders)){
			Integer number = 0;
			Double amount = 0.0;
			List<String> ordNos = new ArrayList<String>();
			for(GewaOrder ord : orders){
				number += ord.getQuantity();
				amount += ord.getTotalCost();
				ordNos.add(ord.getTradeno());
			}
			String content = "结算配置修改导致产生一些不在结算单期间的订单,订单号:[" + StringUtils.join(ordNos, ",") +"],合计票数：" + number + ",金额：" + amount;
			GSPMaill.sendMaill(content, GSPSendMaill.SYSADMINMAIL);
		}
	}
	
	private List<String> compareSettleConfigBeans(SettleConfig newConfig,SettleConfig oldConfig){
		List<String> diffField = new ArrayList<String>();
		Field[] fields = newConfig.getClass().getDeclaredFields();
		try {
			for(Field f : fields){
				f.setAccessible(true);
				Object o1  = f.get(newConfig);
				Object o2  = f.get(oldConfig);
				if(f.getName().equals("firstSettle")){
					Timestamp newFirstSettle = (Timestamp) o1;
					Timestamp oldFirstSettle = (Timestamp) o2;
					if(!newFirstSettle.equals(oldFirstSettle)){
						diffField.add(f.getName());
					}
				}else if(f.getName().equals("settleBase")){
					String newSettleBase = o1.toString();
					String oldSettleBase = o2.toString();
					if(!StringUtils.equals(newSettleBase, oldSettleBase)){
						diffField.add(f.getName());
					}
				}else if(f.getName().equals("settleTime")){
					String newSettleTime = o1.toString();
					String oldSettleTime = o2.toString();
					if(!StringUtils.equals(newSettleTime, oldSettleTime)){
						diffField.add(f.getName());
					}
				}
			}
		} catch (Exception e) {
			dbLogger.error("SettleConfig对比属性值出错！");
		} 
		return diffField;
	}
}
