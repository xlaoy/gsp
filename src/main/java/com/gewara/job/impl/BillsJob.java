/** 
 */
package com.gewara.job.impl;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.enums.BillTypeEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.job.JobService;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.DownloadRecorder;
import com.gewara.model.gsp.PayableBill;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.CheckBillService;
import com.gewara.service.DaoService;
import com.gewara.service.SettlementBillService;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.PKCoderUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 13, 2013  2:02:44 PM
 */
public class BillsJob extends JobService{
	
	@Autowired
	@Qualifier("checkBillService")
	private CheckBillService checkBillService;
	
	@Autowired
	@Qualifier("settlementBillService")
	private SettlementBillService settlementBillService;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	@Autowired
	@Qualifier("config")
	private Config config;
	
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate; 
	
	private final ExecutorService excus = Executors.newFixedThreadPool(5);
	
	public BillsJob(){
	}
	
	/**
	 * 生成结算单
	 */
	public void generateSettlementBill(){
		List<SettleConfig> settleConfigs = daoService.getObjectListByField(SettleConfig.class, "status", "Y");
		for (int i = 0; i < 10; i++){
			settlementBillService.generateSettlementBill(settleConfigs,SettleConstant.TAG_SETTLEMENTBILL_TICKET);
		}
	}
	
	/**
	 * 提交所有未结算订单
	 * 填充影票结算数据
	 * 	1.填充影票对账单
	 * 	2.填充影票结算单
	 */
	public void autoTomerchant(){
		
		ResultCode checkResult = checkDataSafety();
		if (!checkResult.isSuccess()){
			return;
		}
		
		SyncMark validateMark = daoService.getObject(SyncMark.class, "validateNumber");
		Timestamp validateTs = validateMark.getLastExecuteTime();
		long validateTime = validateTs.getTime();
		
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.eq("status", CheckBillStatusEnums.NEW.getStatus()));
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		query.addOrder(Order.desc("playType"));
		List<SettlementBill> sbs  = daoService.findByCriteria(query);
		
		Collections.sort(sbs);
		for (int j = 0; j < sbs.size(); j ++){
			final SettlementBill bill = sbs.get(j);
			long billTime = bill.getEndTime().getTime();
			if (validateTime < billTime) {
				dbLogger.error("订单数量验证失败！！！！" + bill.getRecordId() + " 验证通过最后时间：" + validateTs +  ", 结算单结束时间：" + bill.getEndTime());
				continue;
			}
			if (!checkCheckbillCount(bill)) {
				continue;
			}
			bill.setStatus(CheckBillStatusEnums.FULFILLING.getStatus());
			daoService.updateObject(bill);
			excus.execute(new Runnable() {
				@Override
				public void run() {
					settlementBillService.autoToMerchant(bill);
				}
			});
		}
		
	}
	
	
	private boolean checkCheckbillCount(SettlementBill bill){
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", bill.getRecordId());
		if(CollectionUtils.isEmpty(checkBills)) {
			checkBills = checkBillService.createCheckBillBySettleBill(bill);
		}
		int checkSize = checkBills.size();
		int diffDay = DateUtil.getDiffDay(bill.getStartTime(), bill.getEndTime());
		if (checkSize != diffDay){
			String content = "影票对账单数量异常， 结算单单号：" + bill.getRecordId() + ",check size:" + checkSize + "-->diffDay:" + diffDay;
			dbLogger.error(content);
			GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
			return false;
		}
		return true;
	}
	/**
	 * @return
	 */
	private ResultCode checkDataSafety() {
		SyncMark sm = daoService.getObject(SyncMark.class, "syncOrder");
		Timestamp lastExecuteTime = sm.getLastExecuteTime();
		Timestamp now = DateUtil.getCurFullTimestamp();
		String content = null;
		if (DateUtil.addDay(lastExecuteTime, 1).before(now)){
			content = "订单同步程序异常: lastExecuteTime:" + lastExecuteTime + "---> now:" + now;
		}
		if (content == null) {
			return ResultCode.SUCCESS;
		}
		dbLogger.error(content);
		GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
		return ResultCode.getFailure("");
	}

	/**
	 * 推送应付单
	 */
	public void uploadBill(){
		DetachedCriteria query = DetachedCriteria.forClass(DownloadRecorder.class);
		query.add(Restrictions.eq("downloadCount", 0));
		query.add(Restrictions.or(Restrictions.eq("billType", BillTypeEnums.PAYABLEBILL.getType()), 
									Restrictions.isNull("billType")));
		
		query.addOrder(Order.desc("addTime"));
		List<DownloadRecorder> recorders = daoService.findByCriteria(query, 0, 10);

		if (CollectionUtils.isEmpty(recorders))
			return;
		List settleIds = BeanUtil.getBeanPropertyList(recorders, "settlementId", true);
		Map<Long, SettlementBill> settleMap = daoService.getObjectMap(SettlementBill.class, settleIds);
		
		for (DownloadRecorder recorder : recorders){
			SettlementBill settlementBill = settleMap.get(recorder.getSettlementId());
			FTPClient client = getFTPClient();
			if (client == null)
				return;
			boolean isSuccess = uploadPayableBill(client, settlementBill);
			if (isSuccess){
				recorder.setDownloadCount(recorder.getDownloadCount() + 1);
				recorder.setLastDownloadTime(DateUtil.getCurFullTimestamp());
				daoService.saveObject(recorder);
			}
			try {
				client.logout();
				client.disconnect();
				dbLogger.warn("upload finished.." + recorders.size());
			} catch (Exception e){
				dbLogger.error("", e);
			}
			
		}
	}

	/**
	 * @return
	 */
	private FTPClient getFTPClient() {
		FTPClient client = new FTPClient();
		client.setConnectTimeout(1000 * 20); //20s
		client.setDataTimeout(1000 * 60);//1min;
		try {
			String ftpserver = config.getString("ftpserver");
			Integer ftpport = Integer.valueOf(config.getString("ftpport")); 
			String ftpuser = config.getString("ftpuser");
			String ftppwd = config.getString("ftppwd");
			ftpuser = PKCoderUtil.decodeString(ftpuser);
			ftppwd = PKCoderUtil.decodeString(ftppwd);
//			String ftpserver = "192.168.8.60";
//			Integer ftpport = 21; 
//			String ftpuser = "test";
//			String ftppwd = "test123";
			client.connect(ftpserver, ftpport);
			boolean loginR = client.login(ftpuser, ftppwd);
			client.setActivePortRange(26080,26080);
			client.enterLocalActiveMode();
			dbLogger.warn("login res:"+ client.getReplyCode() + client.getReplyString());
			if(loginR){
				return client;
			}
			dbLogger.error("login ftp server faild");
		}catch (Exception e) {
			dbLogger.error("", e);
			return null;
		}
		return null;
	}

	/**
	 * @param bill
	 */
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
			boolean storeFile = client.storeFile("payablebill" + bill.getRecordId() + ".xml", input);
			input.close();
			dbLogger.warn("storeFile:" + client.getReplyCode() + client.getReplyString());
			dbLogger.warn("uploaded payable bill:" + bill.getRecordId() + ", result:" + storeFile);
			return storeFile;
		} catch (Exception e) {
			dbLogger.warn("storeFile:" + client.getReplyCode() + client.getReplyString());
			dbLogger.error("", e);
			return false;
		}
	}
	
}
