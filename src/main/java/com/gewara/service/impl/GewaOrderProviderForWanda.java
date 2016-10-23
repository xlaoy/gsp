package com.gewara.service.impl;

import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gewara.Config;
import com.gewara.commons.sign.Sign;
import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.DaoService;
import com.gewara.service.GewaOrderProvider;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebLogger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@Service("gewaOrderProvider")
public class GewaOrderProviderForWanda implements GewaOrderProvider {
	private static final List<Long> NOT_INCLUDE_CINEMA_ID = new ArrayList<Long>();
	
	private static final List<String> EXCEL_TITLE = new ArrayList<String>();
	
	private static final String WD2 = "WD2";
	
	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	private static final Map<String,String> SFTPDETAIL = new HashMap<String, String>();
	
	private static final String SIGN_APPKEY = "gewara";
	
	private static final String SIGN_SECRETCODE = "fd1efd0a381a5489c68448dd7c4be523";

	
	@Autowired
	@Qualifier("config")
	private Config config;
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	static{
		NOT_INCLUDE_CINEMA_ID.add(2088696L);//��������Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(114483306L);//�ɽ�����Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(37925152L);//��������Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(61431049L);//��ɽ����Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(101311273L);//̫������Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(133265263L);//��ɽ����Ӱ��
		NOT_INCLUDE_CINEMA_ID.add(63364L);//��ǳ�����Ӱ��
		
		EXCEL_TITLE.add("�����վ������");
		EXCEL_TITLE.add("Ӱ������");
		EXCEL_TITLE.add("��������ʱ��");
		EXCEL_TITLE.add("����Ӫҵ��");
		EXCEL_TITLE.add("���");
		EXCEL_TITLE.add("����");
		EXCEL_TITLE.add("��������");
		EXCEL_TITLE.add("��ӳ����");
		EXCEL_TITLE.add("Ӱ��");
		EXCEL_TITLE.add("��λ��");
		EXCEL_TITLE.add("ӰƬ����");
		EXCEL_TITLE.add("ӰƬ��ʽ");
		EXCEL_TITLE.add("Ӱ��ID");
		
		
		SFTPDETAIL.put(SettleConstant.SFTP_REQ_USERNAME, "gewara");
		SFTPDETAIL.put(SettleConstant.SFTP_REQ_PASSWORD, "2ob4M8r0sRpPlYRsAa2scC");
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void provideGewaOrder() {
		SFTPDETAIL.put(SettleConstant.SFTP_REQ_HOST, GewaIpConfig.isGewaInnerIp(config.getServerIp()) ? "172.28.11.116" : "103.20.250.45");
		logger.warn(GewaIpConfig.isGewaInnerIp(config.getServerIp()) ? "172.28.11.116" : "103.20.250.45");
		List<SettleConfig> settleConfigs = qryWandaSettleConfigs(WD2);
		List<GewaOrder> allOrders = new ArrayList<GewaOrder>();
		List<Refundment> refunds = new ArrayList<Refundment>();
		int count = settleConfigs.size();
		int current = 0;
		for(SettleConfig cfg : settleConfigs){
			current++;
			logger.warn("��ﶩ����ϸ���ɣ�������ѯ�У�����" + count + "���������ã���ǰִ�е�" + current + "������ǰ������" + allOrders.size() + "������Ʊ������" + refunds.size() + "��");
			List<Long> checkBillIds = qryWandaCheckBillIds(cfg);
			if(CollectionUtils.isEmpty(checkBillIds)){
				continue;
			}
			List<GewaOrder> orders = qryGewaOrderByCksIds(checkBillIds);
			allOrders.addAll(orders);
			refunds.addAll(qryRefundmentByCksIds(checkBillIds));
		}
		logger.warn("��ﶩ����ϸ���ɣ���������" + allOrders.size());
		logger.warn("��ﶩ����ϸ���ɣ���Ʊ��������" + refunds.size());
		//����Excel
		Workbook wb = createExcelFile(allOrders, refunds);
		try {
			logger.warn("��ﶩ����ϸ���ɣ���ʼ�ϴ�excel�ļ�");
			uploadSftp(wb);
			logger.warn("��ﶩ����ϸ���ɣ��ϴ�excel�ļ��ɹ�");
		} catch (Exception e) {
			logger.error("��ﶩ����ϸ���ɣ�����ʧ��");
			e.printStackTrace();
		}
	}

	private Workbook createExcelFile(List<GewaOrder> orders,List<Refundment> refunds){
		logger.warn("��ﶩ����ϸ���ɣ�����Excel��");
		Map<Long,Map<String,Object>> wandaDeptMap = queryWandaMapByInterface();
		
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("��ﶩ����ϸ");
		Row row = sheet.createRow(0);//��������
		Cell cell = null;
		//���ɱ�����
		for(int i = 0 ; i < EXCEL_TITLE.size(); i++){
			cell = row.createCell(i);
			cell.setCellValue(EXCEL_TITLE.get(i));
		}
		List<String> refTradeNoList = BeanUtil.getBeanPropertyList(refunds, "tradeno", true);
		Map<String,GewaOrder> ordMap = BeanUtil.beanListToMap(orders, "tradeno");
		for(int i = 0 ; i < orders.size() ; i++){
			row = sheet.createRow(i + 1);
			//�����վ������
			row.createCell(0).setCellValue(orders.get(i).getOuterId());
			//Ӱ������
			row.createCell(1).setCellValue(wandaDeptMap.get(orders.get(i).getRelateId()) == null ? "" : wandaDeptMap.get(orders.get(i).getRelateId()).get("pcinemaName").toString());
			//��������ʱ��			
			row.createCell(2).setCellValue(DateUtil.formatTimestamp(orders.get(i).getDealTime()));
			//����Ӫҵ��			
			row.createCell(3).setCellValue(this.getSaleDate(orders.get(i)));
			//���
			row.createCell(4).setCellValue(orders.get(i).getTotalCost());
			//����
			row.createCell(5).setCellValue(orders.get(i).getQuantity());
			//��������
			row.createCell(6).setCellValue("��");
			//��ӳ����
			row.createCell(7).setCellValue(DateUtil.formatDate(orders.get(i).getUseTime()));
			//Ӱ��
			row.createCell(8).setCellValue(JsonUtils.readJsonToMap(orders.get(i).getOtherInfo()).remove("Ӱ��").toString());
			//��λ��
			row.createCell(9).setCellValue(JsonUtils.readJsonToMap(orders.get(i).getOtherInfo()).remove("ӰƱ").toString());
			//ӰƬ����
			row.createCell(10).setCellValue(JsonUtils.readJsonToMap(orders.get(i).getOtherInfo()).remove("ӰƬ").toString());
			//ӰƬ��ʽ
			row.createCell(11).setCellValue("");
			//Ӱ��ID
			row.createCell(12).setCellValue(wandaDeptMap.get(orders.get(i).getRelateId()) == null ? "" : wandaDeptMap.get(orders.get(i).getRelateId()).get("pcinemaId").toString());
		}
		for(int j = orders.size() , k = 0; j < orders.size() + refTradeNoList.size() && k < refTradeNoList.size(); j++,k++){
			row = sheet.createRow(j + 1);
			//�����վ������
			row.createCell(0).setCellValue(ordMap.get(refTradeNoList.get(k)).getOuterId());
			//Ӱ������
			row.createCell(1).setCellValue(wandaDeptMap.get(ordMap.get(refTradeNoList.get(k)).getRelateId()) == null ? "" : wandaDeptMap.get(ordMap.get(refTradeNoList.get(k)).getRelateId()).get("pcinemaName").toString());
			//��������ʱ��			
			row.createCell(2).setCellValue(DateUtil.formatTimestamp(ordMap.get(refTradeNoList.get(k)).getDealTime()));
			//����Ӫҵ��			
			row.createCell(3).setCellValue(this.getSaleDate(ordMap.get(refTradeNoList.get(k))));
			//���
			row.createCell(4).setCellValue(ordMap.get(refTradeNoList.get(k)).getTotalCost());
			//����
			row.createCell(5).setCellValue(ordMap.get(refTradeNoList.get(k)).getQuantity());
			//��������
			row.createCell(6).setCellValue("��");
			//��ӳ����
			row.createCell(7).setCellValue(DateUtil.formatDate(ordMap.get(refTradeNoList.get(k)).getUseTime()));
			//Ӱ��
			row.createCell(8).setCellValue(JsonUtils.readJsonToMap(ordMap.get(refTradeNoList.get(k)).getOtherInfo()).remove("Ӱ��").toString());
			//��λ��
			row.createCell(9).setCellValue(JsonUtils.readJsonToMap(ordMap.get(refTradeNoList.get(k)).getOtherInfo()).remove("ӰƱ").toString());
			//ӰƬ����
			row.createCell(10).setCellValue(JsonUtils.readJsonToMap(ordMap.get(refTradeNoList.get(k)).getOtherInfo()).remove("ӰƬ").toString());
			//ӰƬ��ʽ
			row.createCell(11).setCellValue("");
			//Ӱ��ID
			row.createCell(12).setCellValue(wandaDeptMap.get(ordMap.get(refTradeNoList.get(k)).getRelateId()) == null ? "" : wandaDeptMap.get(ordMap.get(refTradeNoList.get(k)).getRelateId()).get("pcinemaId").toString());
		}
		return wb;
	}
	
	private Map queryWandaMapByInterface() {
		logger.warn("��ﶩ����ϸ���ɣ���ѯ���ӰԺ����");
		Map<String,String> params = new HashMap<String,String>();
		params.put("partner", WD2);
		addSign(params);
		String qryWandaDept = config.getString("qryWandaDept");
		HttpResult result = HttpUtils.postUrlAsString(qryWandaDept, params);
		String json  = result.getResponse();
		List<Map> list = JsonUtils.readJsonToObjectList(Map.class,json);
		Map<Long,Map<String,Object>> wandaDeptMap = new HashMap<Long,Map<String,Object>>();
		if(null == list){
			return wandaDeptMap;
		}
		for(Map m : list){
			Long placeId = Long.valueOf(m.get("cinemaId").toString());
			wandaDeptMap.put(placeId, m);
		}
		logger.warn("��ﶩ����ϸ���ɣ���ѯ���ӰԺ���ƣ���ѯ�����ӰԺ������" + wandaDeptMap.entrySet().size());
		return wandaDeptMap;
	}


	private String getSaleDate(GewaOrder order) {
		String wandaOrderId = order.getOuterId();
		if(StringUtils.isEmpty(order.getOuterId())){
			return "";
		}
		String ymd = wandaOrderId.substring(0, 14);
		Timestamp t = DateUtil.parseTimestamp(ymd, "yyyyMMddHHmmss");
		Timestamp start = DateUtil.parseTimestamp(DateUtil.format(t,"yyyy-MM-dd") + " 06:00:00");
		if(t.after(start)){
			return DateUtil.formatDate(t);
		}
		return DateUtil.formatDate(DateUtil.addDay(t,-1));
	}
	
	private List<Refundment> qryRefundmentByCksIds(List<Long> checkBillIds) {
		DetachedCriteria refQry = DetachedCriteria.forClass(Refundment.class);
		refQry.add(Restrictions.in("checkBillId", checkBillIds));
		return daoService.findByCriteria(refQry);
	}

	private List<GewaOrder> qryGewaOrderByCksIds(List<Long> chkIds){
		DetachedCriteria orderQry = DetachedCriteria.forClass(GewaOrder.class);
		orderQry.add(Restrictions.in("checkBillId", chkIds));
		return daoService.findByCriteria(orderQry);
	}
	
	private List<Long> qryWandaCheckBillIds(SettleConfig cfg){
		List<SettlementBill> settlebills = qrySettlementbillBySettleConfig(cfg);
		if(CollectionUtils.isEmpty(settlebills)){
			return new ArrayList<Long>();
		}
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settlebills, "recordId", true);
		DetachedCriteria cksQry = DetachedCriteria.forClass(CheckBill.class);
		cksQry.add(Restrictions.in("settlementId", settleIds));
		
		List<CheckBill> checkBills = daoService.findByCriteria(cksQry);
		return BeanUtil.getBeanPropertyList(checkBills, "recordId", true);
	}


	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: qrySettlementbillBySettleConfig  
	 * @Description:
	 * @param @param config
	 * @param @return
	 * @return List<SettlementBill>
	 * @throws  
	 */
	@Override
	public List<SettlementBill> qrySettlementbillBySettleConfig(SettleConfig cfg) {
		Date currentDate = DateUtil.getCurDate();
		Timestamp end = DateUtil.parseTimestamp(DateUtil.format(DateUtil.getMonthFirstDay(currentDate),"yyyy-MM-dd") + " " + cfg.getSettleTime() + ":00","yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.MONTH, -1);
		Timestamp start = DateUtil.parseTimestamp(DateUtil.format(DateUtil.getMonthFirstDay(c.getTime()),"yyyy-MM-dd")  + " " + cfg.getSettleTime() + ":00","yyyy-MM-dd HH:mm:ss");
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		query.add(Restrictions.ge("startTime", start));
		query.add(Restrictions.le("endTime", end));
		query.add(Restrictions.eq("tag", SettleConstant.TAG_SETTLEMENTBILL_TICKET));
		query.add(Restrictions.eq("configId", cfg.getRecordId()));

		List<SettlementBill> settlebills = daoService.findByCriteria(query);
		return settlebills;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: qryWandaSettleConfigs  
	 * @Description:
	 * @param 
	 * @return void
	 * @throws  
	 */
	@Override
	public List<SettleConfig> qryWandaSettleConfigs(String category) {
		DetachedCriteria query = DetachedCriteria.forClass(SettleConfig.class);
		query.add(Restrictions.in("openType", SettleConstant.WDCATE));
		query.add(Restrictions.not(Restrictions.in("relateId", NOT_INCLUDE_CINEMA_ID)));
		query.add(Restrictions.eq("status", SettleConstant.STATUS_VALID));
		return daoService.findByCriteria(query);
//		return daoService.findByCriteria(query, 0, 2);
	}

	private void uploadSftp(Workbook wb) throws Exception{
		logger.warn("��ﶩ����ϸ���ɣ�����uploadSftp����");
	    Session session = null;
	    Channel channel = null;
	    OutputStream outstream = null;
	    ChannelSftp sftp = null;
	    int port = SettleConstant.SFTP_DEFAULT_PORT;
	    String user = SFTPDETAIL.get(SettleConstant.SFTP_REQ_USERNAME);
	    String pwd = SFTPDETAIL.get(SettleConstant.SFTP_REQ_PASSWORD);
	    String ip = SFTPDETAIL.get(SettleConstant.SFTP_REQ_HOST);
	    logger.warn("user:" + user + " pwd:" + pwd +" ip:" + ip);
	    try {
	    	JSch jsch = new JSch();
	    	if(port <=0){
	    		session = jsch.getSession(user, ip);
	    	}else{
	    		session = jsch.getSession(user, ip ,port);
	    	}
	    	//������������Ӳ��ϣ����׳��쳣
	    	if (session == null) {
	    		throw new Exception("session is null");
	    	}
	    	//���õ�½����������
	    	session.setPassword(pwd);//��������   
	    	//���õ�һ�ε�½��ʱ����ʾ����ѡֵ��(ask | yes | no)
	    	session.setConfig("StrictHostKeyChecking", "no");
	    	//���õ�½��ʱʱ��   
	    	session.connect(3000);
	    	//����sftpͨ��ͨ��
	    	channel = session.openChannel("sftp");
	    	channel.connect(1000);
	    	sftp = (ChannelSftp) channel;
	    	Vector fileList = sftp.ls("/upload");
	    	Iterator<LsEntry> sftpFileNames = fileList.iterator(); 
	    	while(sftpFileNames.hasNext()){
	    		LsEntry isEntity = sftpFileNames.next(); 
	    		String fileName = isEntity.getFilename(); 
	    		if(fileName.startsWith("gewala_")){
	    			String subName = fileName.substring(7,fileName.length() - 4);
	    			Date fileDate = DateUtil.parseDate(subName,"yyyyMMdd");
	    			Date d1 = DateUtil.getMonthFirstDay(fileDate);
	    			Date d2 = DateUtil.getMonthFirstDay(DateUtil.getCurDate());
	    			if(d1.equals(d2)){
	    				logger.warn("��ﶩ����ϸ���ɣ������ϴ�excel��ɾ�������Ѿ����ڵ�excel�ļ�������ɾ�����ļ���Ϊ��" + subName);
	    				sftp.rm("/upload/" + fileName);
	    			}
	    		}
	    	}
	    	outstream = sftp.put("/upload/gewala_" + DateUtil.format(DateUtil.getCurDate(), "yyyyMMdd") +".xls", ChannelSftp.OVERWRITE);
	    	logger.warn("��ﶩ����ϸ���ɣ������ϴ�excel�ļ���sftp,�ļ���/upload/gewala_" + DateUtil.format(DateUtil.getCurDate(), "yyyyMMdd") +".xls");
	    	wb.write(outstream);
	    } catch (Exception e) {
	    	logger.error("��ﶩ����ϸ���ɣ��ϴ�Excelʧ�ܣ�ԭ��" + e.getMessage());
	    	throw e;
	    } finally {
	    	sftp.quit();
	    	channel.disconnect();
	    	session.disconnect();
	    }
	}

	private void addSign(Map<String, String> params){
		params.put("appkey", SIGN_APPKEY);
		params.put("sign", Sign.signMD5(params, SIGN_SECRETCODE));
	}
}
