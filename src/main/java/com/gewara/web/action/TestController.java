/** 
 */
package com.gewara.web.action;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gewara.constant.DramaConst;
import com.gewara.constant.SettleConstant;
import com.gewara.dao.DataExtractionDao;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.GspJob;
import com.gewara.model.ZSysChange;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaSettleBill;
import com.gewara.model.gsp.BaoChang;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.PayableBill;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SettlementBillExtend;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.ChannelSettleService;
import com.gewara.service.DataExtractionService;
import com.gewara.service.DramaBillService;
import com.gewara.service.DramaDoColleService;
import com.gewara.service.GewaOrderProvider;
import com.gewara.service.GoodsSettleService;
import com.gewara.service.RefundmentService;
import com.gewara.service.SettleJitiService;
import com.gewara.service.SettlementBillService;
import com.gewara.support.VelocityTemplate;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;
import com.gewara.util.MapRow;
import com.gewara.util.MiscUtil;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.RecordIdUtils;
import com.gewara.vo.JobVo;
import com.gewara.web.support.GewaMultipartResolver;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 1, 2013  3:31:01 PM
 */
@Controller
public class TestController extends AnnotationController implements InitializingBean{
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	
	@Autowired
	@Qualifier("refundmentService")
	private RefundmentService refundmentService;
	
	@Autowired
	@Qualifier("goodsSettleService")
	private GoodsSettleService goodsSettleService;
	
	@Autowired
	@Qualifier("channelSettleService")
	private ChannelSettleService channelSettleService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate; 
	
	@Autowired
	@Qualifier("dataExtractionDao")
	private DataExtractionDao dataExtractionDao;
	
	
	@Autowired
	@Qualifier("gewaOrderProvider")
	private GewaOrderProvider gewaOrderProvider;
	
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("shJdbcTemplate")
	private JdbcTemplate shJdbcTemplate;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}
	
	@RequestMapping("/reflect.xhtml")
	public String exectBeanMethod(HttpServletRequest request, String b, String m, ModelMap model) throws Exception{
		if (!checkSpecialUrl(request)) {
			return showJsonError(model, "无权限！");
		}
		final Object bean = applicationContext.getBean(b);
		if(bean == null) {
			return showJsonError(model, "bean不存在！");
		}
		Class clazz = bean.getClass();
		final Method method = clazz.getMethod(m);
		if(method == null) {
			return showJsonError(model, "method不存在！");
		}
		dbLogger.warn(getLogonUser(request).getUsername() + ",clazz:" + clazz.getName() + ", Method:" + method.getName());
		new Thread() {
			@Override
			public void run() {
				 try {
					method.invoke(bean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		return showJsonSuccess(model, "执行中！");
	}
	
	@RequestMapping("/upgradeRefund.xhtml")
	@ResponseBody
	public String updateRefund(HttpServletRequest request){
		if (!checkSpecialUrl(request))
			return "bad request...#";
		DetachedCriteria query = DetachedCriteria.forClass(Refundment.class);
		query.add(Restrictions.isNull("settleBillId"));
		query.add(Restrictions.eq("isSettled", "Y"));
		List<Refundment> refundts = daoService.findByCriteria(query);
		List<List<Refundment>> partition = BeanUtil.partition(refundts, 888);
		for (List<Refundment> re : partition){
			refundmentService.upgradeR(re);
		}
		return String.valueOf(refundts.size());
	}
	
	@RequestMapping("/uploadftp.xhtml")
	@ResponseBody
	public String uploadftp(HttpServletRequest request){
		if (!checkSpecialUrl(request))
			return "bad request...#";
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("downloadCount", 0));
		query.addOrder(Order.desc("addTime"));
		List<DownloadRecorder> recorders = daoService.findByCriteria(query, 0, 10);
		
		if (CollectionUtils.isEmpty(recorders))
			return "no data...";
		dbLogger.warn(recorders.size() + "are waiting to upload");
		
		List settleIds = BeanUtil.getBeanPropertyList(recorders, "settlementId", true);
		Map<Long, SettlementBill> settleMap = daoService.getObjectMap(SettlementBill.class, settleIds);
		FTPClient client = getFTPClient();
		if (client == null)
			return "client is null";
		for (DownloadRecorder recorder : recorders){
			SettlementBill settlementBill = settleMap.get(recorder.getSettlementId());
			boolean isSuccess = uploadPayableBill(client, settlementBill);
			if (isSuccess){
				recorder.setDownloadCount(recorder.getDownloadCount() + 1);
				recorder.setLastDownloadTime(DateUtil.getCurFullTimestamp());
				daoService.saveObject(recorder);
			}
		}
		try {
			dbLogger.warn("upload finished...close connection");
			client.logout();
			client.disconnect();
			dbLogger.warn("upload finished.." + recorders.size());
		} catch (Exception e){
			dbLogger.error("", e);
		}
		return "success";
	}
	
	private FTPClient getFTPClient() {
		FTPClient client = new FTPClient();
		client.setConnectTimeout(1000 * 20);
		try {
			String ftpserver = config.getString("ftpserver");
			Integer ftpport = Integer.valueOf(config.getString("ftpport")); 
			String ftpuser = config.getString("ftpuser");
			String ftppwd = config.getString("ftppwd");
			ftpuser = PKCoderUtil.decodeString(ftpuser);
			ftppwd = PKCoderUtil.decodeString(ftppwd);
			client.connect(ftpserver, ftpport);
			boolean loginResult = client.login(ftpuser, ftppwd);
			if(loginResult){
				return client;
			}
			dbLogger.error("login ftp server faild");
		}catch (Exception e) {
			dbLogger.error("", e);
			return null;
		}
		return null;
	}
	
	private boolean uploadPayableBill(FTPClient client, SettlementBill bill) {
		try {
			SettleConfig sc = daoService.getObject(SettleConfig.class, bill.getConfigId());
			String code = StringUtils.leftPad(bill.getRecordId().toString(), 10, '0');
			String amount = bill.getOrderTotalAmount().toString();
			String digest = bill.getRecordId() + "_" + DateUtil.format(bill.getStartTime(), "yyyyMMdd") + "-" + DateUtil.format(bill.getEndTime(), "yyyyMMdd");
			String vouchdate = DateUtil.formatDate(bill.getEndTime());
			String quantity = bill.getOrderTotalNumber().toString();
			String vendorNo = sc.getVenderNo();
			
			PayableBill payBill = new PayableBill(sc.getVenderNo(), code, vouchdate, vendorNo, digest, amount, amount, quantity);
			payBill.addEntity(sc.getVenderNo(), vendorNo, digest, amount, amount);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("pb", payBill);
			String fileContent = velocityTemplate.parseTemplate("payableBill.vm", model);
			ByteArrayInputStream input = new ByteArrayInputStream(fileContent.getBytes());
			dbLogger.warn("before storeFile--------------" + fileContent.getBytes().length);
			boolean storeFile = client.storeFile("payable_bill_" + bill.getRecordId() + ".xml", input);
			dbLogger.warn("end storeFile============================");
			input.close();
			dbLogger.warn("uploaded payable bill:" + bill.getRecordId() + ", result:" + storeFile);
			return storeFile;
		} catch (Exception e) {
			dbLogger.error("", e);
			return false;
		}
	}
	
	
	@RequestMapping("/doWandaOrders.xhtml")
	@ResponseBody
	public String doWandaOrders(HttpServletRequest request){
		if(!checkSpecialUrl(request)){
			return "无权限操作！";
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				gewaOrderProvider.provideGewaOrder();
			}
		}).start();
		return "订单明细文件生成中，请观察后台日志！";
	}
	
/**********************************************zyj*********************************************/
	@RequestMapping("/gotoSysOpt.xhtml")
	public String gotoSysOpt(HttpServletRequest request, ModelMap model) {
		if (!checkSpecialUrl(request)) {
			return "bad request...#";
		}
		List<SyncMark> markList = daoService.getAllObjects(SyncMark.class);
		model.put("markList", markList);
		
		List<GspJob> gjList = daoService.getAllObjects(GspJob.class);
		model.put("gjList", gjList);
		
		String sql = "with lastjob as (select jobname, max(firetime) as firetime from joblock group by jobname) " +
					 "select j.* from joblock j, lastjob ljb where j.jobname = ljb.jobname and j.firetime = ljb.firetime";
		List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
		List<JobVo> jobList = new ArrayList<JobVo>();
		for(Map<String, Object> map : mapList) {
			JobVo jv = new JobVo();
			jv.setJobname(map.get("jobname").toString());
			jv.setFiretime(map.get("firetime").toString());
			jv.setNextfire(map.get("nextfire").toString());
			jv.setIp(map.get("ip").toString());
			jv.setStatus(map.get("status").toString());
			jobList.add(jv);
		}
		model.put("jobList", jobList);
		return "/sysopt.vm";
	}
	
	/**
	 * 重算影票结算单
	 * @param settleIds
	 * @param request
	 * @return
	 */
	@RequestMapping("/reversesettle.xhtml")
	public String reverseSettleBill(String settleIds, HttpServletRequest request, ModelMap model){
		if (!checkSpecialUrl(request))
			return showJsonError(model, "无权限");
		if (StringUtils.isBlank(settleIds)){
			return showJsonError(model, "参数错误");
		}
		String[] ids = StringUtils.split(settleIds, ",");
		for (String id : ids){
			SettlementBill settle = daoService.getObject(SettlementBill.class, Long.valueOf(id));
			if(settle == null) {
				return showJsonError(model, "结算单不存在");
			}
			if(!SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(settle.getTag())) {
				return showJsonError(model, id + "不是影票结算单");
			}
			settlementBillService.reverseSettleBill(settle);
			settle.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.saveObject(settle);
			settlementBillService.autoToMerchant(settle);
			dbLogger.warn("finish:" + id);
		}
		return showJsonSuccess(model, "计算完成");
	}
	
	/**
	 * 重置卖品结算单
	 * @param settleIds
	 * @param request
	 * @return
	 */
	@RequestMapping("/reverseGoodsSettle.xhtml")
	public String reverseGoodsSettle(String settleIds, HttpServletRequest request, ModelMap model){
		if (!checkSpecialUrl(request))
			return showJsonError(model, "无权限");
		if (StringUtils.isBlank(settleIds)){
			return showJsonError(model, "参数错误");
		}
		
		String[] ids = StringUtils.split(settleIds, ",");
		for (String id : ids){
			SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(id));
			if(bill == null) {
				return showJsonError(model, "结算单不存在");
			}
			if(!SettleConstant.TAG_SETTLEMENTBILL_GOODS.equals(bill.getTag())) {
				return showJsonError(model, id + "不是卖品结算单");
			}
			goodsSettleService.reverseGoodsSettle(bill);
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.saveObject(bill);
			goodsSettleService.autoToMerchant(bill);
			dbLogger.warn("finish:" + id);
		}
		return showJsonSuccess(model, "计算完成");
	}
	
	/**
	 * 重置通道费结算单
	 * @param settleIds
	 * @param request
	 * @return
	 */
	@RequestMapping("/reverseChannelSettle.xhtml")
	public String reverseChannelSettle(final String settleIds, HttpServletRequest request, ModelMap model){
		if (!checkSpecialUrl(request))
			return showJsonError(model, "无权限");
		if (StringUtils.isBlank(settleIds)){
			return showJsonError(model, "参数错误");
		}
		
		new Thread(
			new Runnable() {
				@Override
				public void run() {
					String[] ids = StringUtils.split(settleIds, ",");
					for (String id : ids){
						dbLogger.warn("reverseChannelSettle->start:" + id);
						SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(id));
						if(bill == null) {
							dbLogger.warn("reverseChannelSettle:" + id + "结算单不存在");
							continue;
						}
						if(!SettleConstant.TAG_SETTLEMENTBILL_CHANNEL.equals(bill.getTag())) {
							dbLogger.warn("reverseChannelSettle:" + id + "不是通道费结算单");
							continue;
						}
						channelSettleService.reverseChannelSettle(bill);
						bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
						daoService.saveObject(bill);
						channelSettleService.autoToMerchant(bill);
						dbLogger.warn("reverseChannelSettle->finish:" + id);
					}
				}
			}
		).start();
		
		return showJsonSuccess(model, "计算中");
	}
	
	/**
	 * 检查影票结算单结算
	 * @param request
	 * @return
	 */
	@RequestMapping("/checkTicket.xhtml")
	public String checkTicket(final Timestamp startTime, final Timestamp endTime, final String status, HttpServletRequest request, ModelMap model) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if (startTime == null){
			return showJsonError(model, "参数错误");
		}
		if (endTime == null){
			return showJsonError(model, "参数错误");
		}
		if (StringUtils.isBlank(status)){
			return showJsonError(model, "参数错误");
		}
		
		dbLogger.warn("checkTicket:开始核对" + startTime + "-" + endTime + "状态为" + status + "的影票结算单数据！");
		new Thread(new Runnable() {
			@Override
			public void run() {
				DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
				query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
				query.add(Restrictions.eq("status", status));
				query.add(Restrictions.ge("startTime", startTime));
				query.add(Restrictions.lt("endTime", endTime));
				List<SettlementBill> stList = daoService.findByCriteria(query);
				
				dbLogger.warn("checkTicket:共有" + stList.size() + "影票结算单需要核对，开始核对");
				
				for(int i = 0; i < stList.size(); i++) {
					dbLogger.warn("checkTicket:当前是第" + i );
					SettlementBill bill = stList.get(i);
					
					List<CheckBill> checkbills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
					if (CollectionUtils.isEmpty(checkbills)){
						continue;
					}
					List<Long> cbIds = BeanUtil.getBeanPropertyList(checkbills, "recordId", true);
					
					DetachedCriteria qry = DetachedCriteria.forClass(GewaOrder.class);
					qry.add(Restrictions.in("checkBillId", cbIds));
					List<GewaOrder> orderList = daoService.findByCriteria(qry);
					Double amount = 0.0;
					for(GewaOrder o : orderList) {
						amount += o.getTotalCost();
					}
					if(bill.getSuccTicketAmount().doubleValue() != amount) {
						dbLogger.warn("checkTicket:结算单开始重置" + bill.getRecordId());
						settlementBillService.reverseSettleBill(bill);
						dbLogger.warn("checkTicket:结算单开始重算" + bill.getRecordId());
						settlementBillService.autoToMerchant(bill);
					}
				}
				dbLogger.warn("checkTicket:核对结束");
			}
		}).start();
		
		return showJsonSuccess(model, "开始核对，请查看日志");
	}
	
	/**
	 * 结算单置无效
	 * @param recordId
	 * @param request
	 * @return
	 */
	@RequestMapping("/setInvalid.xhtml")
	public String setInvalid(String recordId, String status, HttpServletRequest request, ModelMap model) {
		if (StringUtils.isBlank(recordId)){
			return showJsonError(model, "参数错误");
		}
		String[] ids = StringUtils.split(recordId, ",");
		for (String id : ids){
			SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(id));
			if(bill == null) {
				return showJsonError(model, "结算单不存在");
			}
			bill.setStatus(status);
			daoService.saveObject(bill);
		}
		return showJsonSuccess(model, "设置完成");
	}
	
	@Autowired
	@Qualifier("dataExtractionService")
	private DataExtractionService dataExtractionService;
	
	/**
	 * 修复订单对账吗
	 * @param category
	 * @param startTime
	 * @param request
	 * @return
	 */
	@RequestMapping("/repairGewaOrderOuterId.xhtml")
	public String repairGewaOrderOuterId(HttpServletRequest request,final String category, final Timestamp startTime, 
			final Timestamp endTime, final Long pliceid, ModelMap model){
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if(StringUtils.isEmpty(category)){
			return showJsonError(model, "参数错误");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				Timestamp start = startTime;
				while(true) {
					dbLogger.warn("fixOuterId->category:" + category + "|startTime:" + start);
					DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
					query.add(Restrictions.eq("category", category));
					query.add(Restrictions.eq("tag", "TICKET"));
					if(pliceid != null) {
						query.add(Restrictions.eq("relateId", pliceid));
					}
					query.add(Restrictions.gt("dealTime", start));
					query.add(Restrictions.lt("dealTime", endTime));
					//query.add(Restrictions.or(Restrictions.isNull("outerId"), Restrictions.eq("outerId", "")));
					query.addOrder(Order.asc("dealTime"));
					List<GewaOrder> list = daoService.findByCriteria(query, 0, 500);
					if(CollectionUtils.isEmpty(list)) {
						break;
					}
					start = list.get(list.size() - 1).getDealTime();
					List<String> tradenos = BeanUtil.getBeanPropertyList(list, "tradeno", true);
					String qsql = "SELECT RECORDID, TRADE_NO, CATEGORY, HFHPASS, OTHERINFO FROM WEBDATA.view4js_ticket_order o WHERE o.TRADE_NO IN " + DramaUtil.sqlinList(tradenos, String.class);
					List<Map<String, Object>> rowsData = shJdbcTemplate.queryForList(qsql);
					dbLogger.warn("fixOuterId->匹配条数：" + rowsData.size());
					for (Map<String, Object> row : rowsData){
						String tradeno = row.get("TRADE_NO") == null ? null : row.get("TRADE_NO").toString();
						if(StringUtils.isEmpty(tradeno)) {
							continue;
						}
						String outerId = dataExtractionService.getOuterId(row);
						if(StringUtils.isNotBlank(outerId)) {
							String sql = "update gewa_order set outer_id = ? where tradeno = ? ";
							jdbcTemplate.update(sql, outerId, tradeno);
						}
					}
				}
				dbLogger.warn("fixOuterId->end|category:" + category);
			}
		}).start();
		return showJsonSuccess(model, "开始修复，请查看日志");
	}	
	
	/**
	 * 重置job状态完成
	 * @param tag
	 * @param model
	 * @return
	 */
	@RequestMapping("/setFinsh.xhtml")
	public String setFinsh(String tag, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if(StringUtils.isEmpty(tag)){
			return showJsonError(model, "参数错误");
		}
		GspJob job = daoService.getObject(GspJob.class, tag);
		if(job == null) {
			return showJsonError(model, "job不存在!");
		}
		job.setStatus(SettleConstant.FINISH);
		job.setUpdateTime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(job);
		return showJsonSuccess(model, "重置成功！");
	}
	
	
	@Autowired
	@Qualifier("dramaBillServiceImpl")
	protected DramaBillService dramaBillService;
	/**
	 * 批量审批演出结算配置
	 * @param tag
	 * @param model
	 * @return
	 */
	@RequestMapping("/appDramaConfig.xhtml")
	public String appDramaConfig(String tag, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		DetachedCriteria query = DetachedCriteria.forClass(DramaConfig.class);
		query.add(Restrictions.eq("status", DramaConst.WAITAPPROVAL));
		List<DramaConfig> configList = daoService.findByCriteria(query);
		dramaBillService.passeDramConfig(configList);
		return showJsonSuccess(model, "审批中！");
	}
	
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private GewaMultipartResolver gewaMultipartResolver;
	
	@Autowired
	@Qualifier("dramaDoCollecServiceImpl")
	protected DramaDoColleService collectionService;
	
	@RequestMapping("/batchSyncDrama.xhtml")
	public String batchSyncDrama(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
 		MultipartFile dramafile = multipartRequest.getFile("dramafile");
		
		List<Long> dramaidlist = new ArrayList<Long>();
		
		try {
			InputStreamReader isr = new InputStreamReader(dramafile.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String dramaid = null;
			while ((dramaid = br.readLine()) != null) {
				dramaidlist.add(Long.valueOf(dramaid));
			}
			isr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(CollectionUtils.isNotEmpty(dramaidlist)) {
			collectionService.syncDramaByDramaid(dramaidlist);
		}
		
		return showRedirect("/gotoSysOpt.xhtml", model);
	}
	
	
	@RequestMapping("/batchSyncConfig.xhtml")
	public String batchSyncConfig(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
 		MultipartFile configfile = multipartRequest.getFile("configfile");
		
		final List<Long> dramaidlist = new ArrayList<Long>();
		
		try {
			InputStreamReader isr = new InputStreamReader(configfile.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String dramaid = null;
			while ((dramaid = br.readLine()) != null) {
				dramaidlist.add(Long.valueOf(dramaid));
			}
			isr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				dbLogger.warn("batchSyncConfig开始同步配置");
				for(Long dramaid : dramaidlist) {
					collectionService.syncDramConfigByDramaId(dramaid);
				}
				dbLogger.warn("batchSyncConfig同步配置完成");
			}
		}).start();
		
		return showRedirect("/gotoSysOpt.xhtml", model);
	}
	
	@RequestMapping("/batchSyncPlayItem.xhtml")
	public String batchSyncPlayItem(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
 		MultipartFile playitemfile = multipartRequest.getFile("playitemfile");
		
		final List<Long> dramaidlist = new ArrayList<Long>();
		
		try {
			InputStreamReader isr = new InputStreamReader(playitemfile.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String dramaid = null;
			while ((dramaid = br.readLine()) != null) {
				dramaidlist.add(Long.valueOf(dramaid));
			}
			isr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(CollectionUtils.isNotEmpty(dramaidlist)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					dbLogger.warn("batchSyncConfig开始同步场次");
					collectionService.syncPlayItemByDramaid(dramaidlist);
					dbLogger.warn("batchSyncConfig同步场次结束");
				}
			}).start();
		}
		
		return showRedirect("/gotoSysOpt.xhtml", model);
	}
	
	@RequestMapping("/dramareststatus.xhtml")
	public String dramareststatus(String status, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if(StringUtils.isEmpty(status)){
			return showJsonError(model, "参数错误");
		}
		List<DramaSettleBill> list = daoService.getObjectListByField(DramaSettleBill.class, "status", status);
		for(DramaSettleBill bill : list) {
			bill.setStatus(DramaConst.NEW);
			daoService.updateObject(bill);
		}
		return showJsonSuccess(model, "重置成功！");
	}
	
	//@RequestMapping("/resetDramaData.xhtml")
	public String resetDramaData(String tag, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetDramaData开始重置");
				
				String sql1 = "delete from drama_config";
				jdbcTemplate.update(sql1);
				dbLogger.warn("drama_config删除完成");
				
				String sql2 = "delete from drama_pricerate";
				jdbcTemplate.update(sql2);
				dbLogger.warn("drama_pricerate删除完成");
				
				String sql3 = "delete from drama_settlebill";
				jdbcTemplate.update(sql3);
				dbLogger.warn("drama_settlebill删除完成");
				
				String sql4 = "delete from drama_pricebill";
				jdbcTemplate.update(sql4);
				dbLogger.warn("drama_pricebill删除完成");
				
				String sql5 = "delete from drama_jitibill";
				jdbcTemplate.update(sql5);
				dbLogger.warn("drama_jitibill删除完成");
				
				String sql6 = "update drama_orderonline_item set pricebillid = null, hasrefund = null";
				jdbcTemplate.update(sql6);
				dbLogger.warn("drama_orderonline_item重置完成");
				
				String sql7 = "update drama_orderoffline_item set pricebillid = null, hasrefund = null";
				jdbcTemplate.update(sql7);
				dbLogger.warn("drama_orderoffline_item重置完成");
				
				String sql8 = "update sync_mark set last_execute_time = '2016-03-01 00:00:00' where tag = 'syncDramaConfig'";
				jdbcTemplate.update(sql8);
				dbLogger.warn("sync_mark重置完成");
				
			}
		}).start();
		
		return showJsonSuccess(model, "重置中！");
	}
	
	//@RequestMapping("/resetHepingPlaceid.xhtml")
	public String resetHepingPlaceid(String tag, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetHepingPlaceid开始重置");
				
				String sql1 = "SELECT CONFIG_ID  FROM SETTLEMENT_BILL where tag= 'TICKET' and CONFIG_ID = '2,TICKET' and status = 'NEW' ";
				List<Map<String, Object>> list = jdbcTemplate.queryForList(sql1);
				if(CollectionUtils.isNotEmpty(list)) {
					dbLogger.warn("resetHepingPlaceid有状态为NEW的结算单！");
					return;
				}
				
				String sql2 = "SELECT MAX(END_TIME) as endtime  FROM SETTLEMENT_BILL where tag= 'TICKET' and CONFIG_ID = '2,TICKET' ";
				Map<String, Object> map = jdbcTemplate.queryForMap(sql2);
				Timestamp endtime = (Timestamp)map.get("endtime");
				
				dbLogger.warn("resetHepingPlaceid开始重置的订单时间为：" + endtime);
				
				DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
				query.add(Restrictions.ge("dealTime", endtime));
				query.add(Restrictions.eq("relateId", 2l));
				query.add(Restrictions.eq("special", "HEPING_OUTER"));
				List<GewaOrder> orderlist = daoService.findByCriteria(query);
				
				List<String> list1 = BeanUtil.getBeanPropertyList(orderlist, "tradeno", true);
				
				List<List<String>> groupList = BeanUtil.partition(list1, 500);
				for(List<String> recordids : groupList) {
					String updateorder = " update gewa_order set place_id = 299977977 where tradeno in " + DramaUtil.sqlinList(recordids, String.class);
					jdbcTemplate.update(updateorder);
					
					String updaterefund = " update refundment set relate_id = 299977977 where tradeno in " + DramaUtil.sqlinList(recordids, String.class);
					jdbcTemplate.update(updaterefund);
				}
				
				dbLogger.warn("resetHepingPlaceid订单重置完成");
				
				SettleConfig sc = daoService.getObject(SettleConfig.class, SettleConstant.HEPING_OUTER_PLACEID + ",TICKET");
				sc.setFirstSettle(endtime);
				daoService.updateObject(sc);
				dbLogger.warn("resetHepingPlaceid开始结算时间重置完成重置完成");
				
				SettleConfig sch = daoService.getObject(SettleConfig.class, "2,TICKET");
				sch.setPayVenderNo(sch.getVenderNo());
				sch.setPayVenderName(sch.getVenderName());
				daoService.updateObject(sch);
				dbLogger.warn("resetHepingPlaceid 2,TICKET 重置完成");
				
			}
		}).start();
		
		return showJsonSuccess(model, "重置中！");
	}
	
	@RequestMapping("/resetOrderDiscount.xhtml")
	public String resetOrderDiscount(final Timestamp mstart, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetOrderDiscount开始");
				
				Timestamp start = mstart;
				Timestamp last = DateUtil.addDay(DateUtil.getCurFullTimestamp(), 1);
				
				String sql = "select tradeno from gewa_order where order_type = 'TICKET' and deal_time >= ? and deal_time < ? ";
				
				String update = "update gewa_order set reldiscount = ? where tradeno = ? ";
				
				while(start.before(last)) {
					Timestamp end = DateUtil.addDay(start, 1);
					dbLogger.warn("resetOrderDiscount开始获取" + start + " ~ " + end + "的订单瓦币和优惠金额!");
					
					List<String> alltradenolist = jdbcTemplate.queryForList(sql, String.class, start, end);
					
					List<List<String>> groupList = BeanUtil.partition(alltradenolist, 500);
					for(List<String> tradenolist : groupList) {
						String shsql = "select o.TRADE_NO, o.DISCOUNT, o.WABI, o.TOTALCOST from WEBDATA.view4js_ticket_order o where o.TRADE_NO in " + DramaUtil.sqlinList(tradenolist, String.class);
						List<Map<String, Object>> ordermap = shJdbcTemplate.queryForList(shsql);
						for(Map<String, Object> map : ordermap) {
							MapRow row = new MapRow(map);
							String tradeno = row.getString("TRADE_NO");
							double discount = row.getDouble("DISCOUNT");
							/*double wabi = row.getDouble("WABI");
							double totalcost = row.getDouble("TOTALCOST");
							discount = discount + wabi;
							if(discount > totalcost) {
								discount = totalcost;
							}*/
							jdbcTemplate.update(update, discount, tradeno);
						}
					}
					
					dbLogger.warn("resetOrderDiscount获取" + start + " ~ " + end + "的订单瓦币和优惠金额完成!");
					start = end;
				}
				
				dbLogger.warn("resetOrderDiscount完成");
				
			}
		}).start();
		
		return showJsonSuccess(model, "重置中！");
	}
	
	
	@RequestMapping("/resetBillamount.xhtml")
	public String resetBillamount(final Timestamp mstart, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				dbLogger.warn("resetBillamount开始");
				
				DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
				query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
				query.add(Restrictions.ne("status", "INVALID"));
				query.add(Restrictions.ge("startTime", mstart));
				List<SettlementBill> billlist = daoService.findByCriteria(query);
				int n = billlist.size();
				dbLogger.warn("resetBillamount共有" + n + "单需要计算开票金额");
				
				for(SettlementBill bill : billlist) {
					List<CheckBill> checklist = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
					List<Long> checkids = BeanUtil.getBeanPropertyList(checklist, "recordId", true);
					if(CollectionUtils.isEmpty(checkids)) {
						continue;
					}
					String sql = "select order_status, discount, total_cost from gewa_order where check_bill_id in " + DramaUtil.sqlinList(checkids, Long.class);
					List<Map<String, Object>> maplist = jdbcTemplate.queryForList(sql);
					
					double billamount = 0.0;
					for(Map<String, Object> map : maplist) {
						MapRow row = new MapRow(map);
						String status = row.getString("order_status");
						double discount = row.getDouble("discount");
						double totalcost = row.getDouble("total_cost");
						if("paid_success".equals(status)) {
							billamount += discount;
						} else if("paid_return".equals(status)) {
							billamount += totalcost;
						}
					}
					
					billamount += bill.getAdjustTotalAmount();
					
					//计算包场成本
					/*DetachedCriteria qry = DetachedCriteria.forClass(BaoChang.class);
					qry.add(Restrictions.eq("placeId", bill.getConfigId()));
					qry.add(Restrictions.eq("playType", bill.getPlayType()));
					qry.add(Restrictions.ge("playTime", bill.getStartTime()));
					qry.add(Restrictions.lt("playTime", bill.getEndTime()));
					List<BaoChang> bcList = daoService.findByCriteria(qry);
					
					for(BaoChang bc : bcList) {
						billamount += bc.getBcAmount() == null ? 0.0 : bc.getBcAmount();
						bc.setSettleId(bill.getRecordId());
						daoService.updateObject(bc);
					}*/
					
					SettlementBillExtend billextend = daoService.getObject(SettlementBillExtend.class, bill.getRecordId());
					if(billextend == null) {
						billextend = new SettlementBillExtend(bill.getRecordId());
					}
					billextend.setBillingamount(billamount - bill.getRefundTotalAmount());
					daoService.saveObject(billextend);
				}
				
				dbLogger.warn("resetBillamount结束");
			}
		}).start();
		
		return showJsonSuccess(model, "计算中！");
	}
	
	
	@RequestMapping("/resetUserKPAmount.xhtml")
	public String resetUserKPAmount(final Timestamp mstart, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetUserKPAmount开始");
				
				Timestamp start = mstart;
				Timestamp last = DateUtil.addDay(DateUtil.getCurFullTimestamp(), 1);
				String sql = "select tradeno from gewa_order where order_type = 'TICKET' and deal_time >= ? and deal_time < ? ";
				String update = "update gewa_order set alipaid = ? where tradeno = ? ";
				
				while(start.before(last)) {
					Timestamp end = DateUtil.addDay(start, 1);
					dbLogger.warn("resetUserKPAmount开始获取" + start + " ~ " + end + "的alipaid!");
					List<String> alltradenolist = jdbcTemplate.queryForList(sql, String.class, start, end);
					List<List<String>> groupList = BeanUtil.partition(alltradenolist, 500);
					for(List<String> tradenolist : groupList) {
						String shsql = "select o.TRADE_NO, o.ALIPAID, o.wabi, o.GEWAPAID from WEBDATA.view4js_ticket_order o where o.TRADE_NO in " + DramaUtil.sqlinList(tradenolist, String.class);
						List<Map<String, Object>> ordermap = shJdbcTemplate.queryForList(shsql);
						for(Map<String, Object> map : ordermap) {
							MapRow row = new MapRow(map);
							String tradeno = row.getString("TRADE_NO");
							double alipaid = row.getDouble("ALIPAID");
							double wabi = row.getDouble("wabi");
							double gewapaid = row.getDouble("GEWAPAID");
							double tmp = alipaid + gewapaid - wabi;
							jdbcTemplate.update(update, tmp, tradeno);
						}
					}
					dbLogger.warn("resetUserKPAmount获取" + start + " ~ " + end + "的alipaid完成!");
					start = end;
				}
				dbLogger.warn("resetUserKPAmount完成");
			}
		}).start();
		
		return showJsonSuccess(model, "重置中！");
	}
	
	@RequestMapping("/delsysChange.xhtml")
	public String delsysChange(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		String sql = "delete from z_sys_change ";
		jdbcTemplate.update(sql);
		return showJsonSuccess(model, "删除完成！");
	}
	
	@RequestMapping("/sysChange.xhtml")
	public String sysChange(final String startstr, final String placeid, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		if(StringUtils.isBlank(startstr)){
			return showJsonError(model, "start不能为空");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				Timestamp curr = DateUtil.getCurFullTimestamp();
				
				dbLogger.warn("sysChange开始start:" + startstr + ",curr:" + curr);
				
				if(StringUtils.isNotBlank(placeid)) {
					SettleConfig sc = daoService.getObject(SettleConfig.class, placeid);
					dosyschange(sc, startstr, curr);
				} else {
					List<SettleConfig> sclist = daoService.getObjectListByField(SettleConfig.class, "status", SettleConstant.COMM_Y);
					for(SettleConfig sc : sclist) {
						dosyschange(sc, startstr, curr);
					}
				}
				
				dbLogger.warn("sysChange完成start:" + startstr + ",curr:" + curr);
			}
		}).start();
		
		return showJsonSuccess(model, "计算中！");
	}
	
	private void dosyschange(SettleConfig sc, String startstr, Timestamp curr) {
		if(sc == null) {
			return;
		}
		if(!SettleConstant.COMM_Y.equals(sc.getStatus())) {
			return;
		}
		String placeid = sc.getRecordId();
		Place place = daoService.getObject(Place.class, placeid);
		if(place == null) {
			return;
		}
		
		startstr = startstr + " " + sc.getSettleTime() + ":00";
		Timestamp start = DateUtil.parseTimestamp(startstr, "yyyy-MM-dd hh:mm:ss");
		Timestamp end = MiscUtil.addMonth(start, 1);
		
		dbLogger.warn("sysChange开始Place:" + placeid + ",start:" + start + ",end" + end + ",curr:" + curr);
		
		Long placerealid = place.getRelateId();
		
		String sql1 = "select DISTINCT category from gewa_order where deal_time >= ? and deal_time < ? and order_type = 'TICKET' and place_id = ? ";
		
		List<String> catelist = jdbcTemplate.queryForList(sql1, String.class, start, end, placerealid);
		if(CollectionUtils.isEmpty(catelist)) {
			return;
		}
		for(String cate : SettleConstant.UNSETTLECATE) {
			if(catelist.contains(cate)) {
				catelist.remove(cate);
			}
		}
		if(catelist.size() <= 1) {
			return;
		}
		String sql2 = "select min(deal_time) as starttime, max(deal_time) as endtime, sum(quantity) as num, sum(total_cost) as amount from gewa_order where deal_time >= ? and deal_time < ? and order_type = 'TICKET' and place_id = ? and category = ? ";
		String sql3 = "select sum(quantity) as num, sum(old_settle - new_settle) as amount from refundment  where refund_time >= ? and refund_time < ? and order_type = 'TICKET' and relate_id = ? and refund_category = ? ";
		for(String cate : catelist) {
			Map<String, Object> change = jdbcTemplate.queryForMap(sql2, start, end, placerealid, cate);
			List<Map<String, Object>> refundlist = jdbcTemplate.queryForList(sql3, start, end, placerealid, cate);
			MapRow row = new MapRow(change);
			ZSysChange zc = new ZSysChange();
			zc.setPlaceid(placeid);
			zc.setStarttime(row.getTimestamp("starttime"));
			zc.setEndtime(row.getTimestamp("endtime"));
			long num = row.getLong("num");
			double amount = row.getDouble("amount");
			if(CollectionUtils.isNotEmpty(refundlist)) {
				Map<String, Object> refund = refundlist.get(0);
				MapRow rfrow = new MapRow(refund);
				num = num - rfrow.getLong("num");
				amount = amount - rfrow.getDouble("amount");
			}
			zc.setNum(num);
			zc.setAmount(amount);
			zc.setCategory(cate);
			zc.setUpdatetime(curr);
			daoService.addObject(zc);
		}
	}
	
	
	@Autowired
	@Qualifier("settleJitiServiceImpl")
	private SettleJitiService settleJitiService;
	/**
	 * 生成6月份影票结算单计提单据
	 * @param startstr
	 * @param placeid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/resetjiti.xhtml")
	public String resetjiti(final Timestamp start, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				Timestamp end = MiscUtil.addMonth(start, 1);
				end = DateUtil.addDay(end, 1);
				
				dbLogger.warn("create6jiti开始start:" + start + ",end:" + end);
				
				DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
				query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
				query.add(Restrictions.ne("status", CheckBillStatusEnums.NEW));
				query.add(Restrictions.ne("status", CheckBillStatusEnums.FULFILLING));
				query.add(Restrictions.ne("status", CheckBillStatusEnums.INVALID));
				query.add(Restrictions.ge("startTime", start));
				query.add(Restrictions.lt("endTime", end));
				List<SettlementBill> billlist = daoService.findByCriteria(query);
				for(SettlementBill bill : billlist) {
					settleJitiService.updateJiti(bill);
				}
				
				dbLogger.warn("create6jiti完成");
			}
		}).start();
		
		return showJsonSuccess(model, "计算中！");
	}
	
	@RequestMapping("/resetbaochang.xhtml")
	public String resetbaochang(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetbaochang开始");
				
				List<BaoChang> bclist = daoService.getAllObjects(BaoChang.class);
				
				for(BaoChang bc : bclist) {
					
					Long placeId = Long.valueOf(bc.getPlaceId().split(",")[0]);
					
					DetachedCriteria qry = DetachedCriteria.forClass(GewaOrder.class);
					qry.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
					qry.add(Restrictions.eq("playId", bc.getPlayId()));
					qry.add(Restrictions.eq("relateId", placeId));
					List<GewaOrder> orderList = daoService.findByCriteria(qry);
					
					Integer bcnum = bc.getBcNum() == null ? 0 : bc.getBcNum();
					Integer userNum = 0;
					Double userAmount = 0.00;
					Integer totalNum = 0;
					Double successAmount = 0.00;
					Integer refundNum = 0;
					Double refundAmount = 0.00;
					Double userRate = 0.00;
					Double kpamount = 0.0;
					if(CollectionUtils.isNotEmpty(orderList)) {
						for(GewaOrder o : orderList) {
							totalNum += o.getQuantity();
							successAmount += o.getTotalCost();
							if("gewa_user".equals(o.getUserBaochang()) || "tuhao_user".equals(o.getUserBaochang())) {
								userNum += o.getQuantity();
								userAmount += o.getTotalCost();
							}
							double discount = o.getDiscount() == null ? 0 : o.getDiscount().doubleValue();
							if("paid_success".equals(o.getOrderStatus())) {
								kpamount += discount;
							} else if("paid_return".equals(o.getOrderStatus())) {
								kpamount += o.getTotalCost();
							}
						}
						if(bcnum != 0) {
							userRate = new BigDecimal((Double.valueOf(userNum) / Double.valueOf(bcnum)) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
						
						List<String> tradenolist = BeanUtil.getBeanPropertyList(orderList, "tradeno", true);
						List<Refundment> refundlist = daoService.getObjectList(Refundment.class, tradenolist);
						for(Refundment r : refundlist) {
							refundNum += r.getQuantity();
							refundAmount += r.getOldSettle() - r.getNewSettle();
						}
					}
					bc.setSuccessNum(userNum);
					bc.setUserAmount(userAmount);
					bc.setTotalNum(totalNum);
					bc.setSuccessAmount(successAmount);
					bc.setRefundNum(refundNum);
					bc.setRefundAmount(refundAmount);
					bc.setKpamount(kpamount);
					bc.setSuccessRate(userRate);
					if(bc.getPreAmount() == null) {
						bc.setPreAmount(0.0);
					}
					if(bc.getBcNum() == null) {
						bc.setBcNum(0);
					}
					if(bc.getBcAmount() == null) {
						bc.setBcAmount(0.0);
					}
					bc.setUpdateTime(DateUtil.getCurFullTimestamp());
					daoService.saveObject(bc);
				}
				
				dbLogger.warn("resetbaochang完成");
			}
		}).start();
		
		return showJsonSuccess(model, "计算中！");
	}
	
	
	@RequestMapping("/resetbaochangPlaceid.xhtml")
	public String resetbaochangPlaceid(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				dbLogger.warn("resetbaochangPlaceid开始");
				
				List<BaoChang> bclist = daoService.getAllObjects(BaoChang.class);
				
				String sql = "SELECT  v.CINEMAID from WEBDATA.VIEW_BAOCHANG_OPI v where v.RECORDID = ? ";
				
				for(BaoChang bc : bclist) {
					
					Map<String, Object> map = shJdbcTemplate.queryForMap(sql, bc.getRecordId());
					
					String placeid = map.get("CINEMAID").toString();
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
					
					daoService.saveObject(bc);
				}
				
				dbLogger.warn("resetbaochangPlaceid完成");
			}
		}).start();
		
		return showJsonSuccess(model, "计算中！");
	}
	
	@RequestMapping("/resetplacepcid.xhtml")
	public String resetplacepcid(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				dbLogger.warn("resetplacepcid开始");
				String qsql = "select v.pcid from webdata.VIEW_CINEMA v where v.recordid = ?";
				String usql = "update place set pcid = ? where record_id = ? ";
				List<Place> placelist = daoService.getAllObjects(Place.class);
				for(Place place : placelist) {
					if(place.getRelateId() != null) {
						List<String> pcidlist = shJdbcTemplate.queryForList(qsql, String.class, place.getRelateId());
						if(CollectionUtils.isNotEmpty(pcidlist)) {
							String pcid = pcidlist.get(0);
							jdbcTemplate.update(usql, pcid, place.getRecordId());
						}
					}
				}
				dbLogger.warn("resetplacepcid完成");
			}
		}).start();
		
		return showJsonSuccess(model, "同步中！");
	}
	
	@RequestMapping("/testtejia.xhtml")
	public String testtejia(Long placeid, ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if(placeid == null) {
			return showJsonError(model, "placeid不能为空");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				dbLogger.warn("testtejia->" + placeid + "开始同步特价订单");
				
				String qsql = "select tradeno from gewa_order where place_id = ? and order_type = 'TICKET' and deal_time >= '2016-09-01 00:00:00' and deal_time < '2016-10-01 00:00:00'";
				List<String> tradenolist = jdbcTemplate.queryForList(qsql, String.class, placeid);
				if(CollectionUtils.isEmpty(tradenolist)) {
					dbLogger.warn("testtejia->" + placeid + "订单为空");
					return;
				}
				String ssql = "select RECORDID, trade_no  from webdata.view4js_ticket_order where trade_no in " + DramaUtil.sqlinList(tradenolist, String.class);
				
				List<Map<String, Object>> trmaplist = shJdbcTemplate.queryForList(ssql);
				Map<Long, String> tradenomap = new HashMap<Long, String>();
				for(Map<String, Object> map : trmaplist) {
					MapRow row = new MapRow(map);
					tradenomap.put(row.getLong("RECORDID"), row.getString("trade_no"));
				}
				List<Long> ids = BeanUtil.getBeanPropertyList(trmaplist, "RECORDID", true);
				
				String sql = "select v.ORDERID, sum(v.AMOUNT) as placeamount  from webdata.view4placeallowance v where v.ORDERID in " + DramaUtil.sqlinList(ids, Long.class) + " GROUP BY v.ORDERID ";
				List<Map<String, Object>> maplist = shJdbcTemplate.queryForList(sql);
				if(CollectionUtils.isEmpty(maplist)) {
					dbLogger.warn("testtejia->" + placeid + "特价为空");
					return;
				}
				String usql = "update gewa_order set placeallowance = ? where tradeno = ? ";
				for(Map<String, Object> map : maplist) {
					MapRow row = new MapRow(map);
					Long recordid = row.getLongWithNull("ORDERID");
					String tradeno = tradenomap.get(recordid);
					double allamount = row.getDouble("placeamount");
					jdbcTemplate.update(usql, allamount, tradeno);
				}
				
				dbLogger.warn("resetplacepcid完成");
			}
		}).start();
		
		return showJsonSuccess(model, "同步中！");
	}
	
	@RequestMapping("/restkpamount.xhtml")
	public String restkpamount(ModelMap model, HttpServletRequest request) {
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				dbLogger.warn("restkpamount开始");
				Timestamp start = DateUtil.parseTimestamp("2016-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
				Timestamp curr = DateUtil.parseTimestamp("2016-10-20 00:00:00", "yyyy-MM-dd HH:mm:ss");
				
				String qsql = "select tradeno, placeallowance from gewa_order where order_type = 'TICKET' and deal_time >= ? and deal_time < ? ";
				
				String updatakpsql = "update gewa_order set discount = ? where tradeno = ? ";
				
				while(start.before(curr)) {
					Timestamp end = DateUtil.addDay(start, 1);
					List<Map<String, Object>> tradenolist = jdbcTemplate.queryForList(qsql, start, end);
					
					List<List<Map<String, Object>>> groupList = BeanUtil.partition(tradenolist, 500);
					for(List<Map<String, Object>> subtradenolist : groupList) {
						
						Map<String, Double> allowancemap = new HashMap<String, Double>();
						for(Map<String, Object> map : subtradenolist) {
							MapRow row = new MapRow(map);
							allowancemap.put(row.getString("tradeno"), row.getDouble("placeallowance"));
						}
						
						List<String> tradenos = BeanUtil.getBeanPropertyList(subtradenolist, "tradeno", true);
						String shsql = "select TRADE_NO, DISCOUNT, WABI, STATUS, TOTALCOST from webdata.view4js_ticket_order where trade_no in " + DramaUtil.sqlinList(tradenos, String.class);
						List<Map<String, Object>> shtradenolist = shJdbcTemplate.queryForList(shsql);
						
						for(Map<String, Object> map : shtradenolist) {
							MapRow row = new MapRow(map);
							String tradeno = row.getString("TRADE_NO");
							String status = row.getString("STATUS");
							double dicount = row.getDouble("DISCOUNT");
							double wabi = row.getDouble("WABI");
							double totalcost = row.getDouble("TOTALCOST");
							double allowance = allowancemap.get(tradeno);
							double kpamount = 0;
							if("paid_success".equals(status)) {
								kpamount = dicount + wabi - allowance;
								if(kpamount < 0) {
									kpamount = 0;
								} else if(kpamount > totalcost) {
									kpamount = totalcost;
								}
							} else if("paid_return".equals(status)) {
								kpamount = totalcost;
							}
							jdbcTemplate.update(updatakpsql, kpamount, tradeno);
						}
						
					}
					dbLogger.warn("restkpamount->start:" + start + " | end:" + end + "已完成");
					start = end;
				}
				
				dbLogger.warn("restkpamount完成");
			}
		}).start();
		
		return showJsonSuccess(model, "同步中！");
	}
}


