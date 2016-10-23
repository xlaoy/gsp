package com.gewara.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.AutoAnalysObject;
import com.gewara.model.gsp.AutoReconciledInfo;
import com.gewara.model.gsp.AutoreconExceptionResult;
import com.gewara.model.gsp.AutoreconGWOrder;
import com.gewara.model.gsp.AutoreconMenchOrder;
import com.gewara.model.gsp.AutoreconMenchTemp;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.GewaOrder;
import com.gewara.model.gsp.Refundment;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.AutoReconcileService;
import com.gewara.service.DaoService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.MapRow;
import com.gewara.util.WebLogger;

@Service("autoReconcileService")
public class AutoReconcileServiceImpl implements AutoReconcileService {
	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("daoService")
	protected DaoService daoService;
	
	@Autowired
	@Qualifier("config")
	private Config config;
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 加载处理对账模板文件
	 *  现在对账模板文件，将数据插入到对账临时表中autorecon_menchtemp
	 */
	@Override
	public boolean loadTemplateFile(AutoReconciledInfo info) {
		InputStream inputStream = null;
		Workbook wb = null;
		boolean isContinue = true;
		try {
			URL url = new URL(config.getString("fileURL") + info.getFilePath());
			inputStream = url.openStream();
			wb = WorkbookFactory.create(inputStream);
		} catch (Exception e) {
			logger.error("loadTemplateFile settltid=" + info.getSettlementId() + " 自动对账失败，原因：" + e.getMessage());
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("loadTemplateFile: workbook create error");
			daoService.saveObject(info);
			isContinue = false;
		} finally {
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("inputStream close failure");
				}
			}
		}
		if(!isContinue){
			return false;
		}
		if(wb == null) {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("loadTemplateFile: workbook is null");
			daoService.saveObject(info);
			return false;
		}
		try {
			Sheet sheet = wb.getSheetAt(0);
			int totalRows = sheet.getLastRowNum();
			for(int i = 1; i <= totalRows; i++) {
				Row row = sheet.getRow(i);
				AutoreconMenchTemp tmp = new AutoreconMenchTemp();
				tmp.setSettleid(info.getSettlementId());
				tmp.setTradeno(row.getCell(0).getStringCellValue());
				tmp.setRecencode(row.getCell(1).getStringCellValue());
				
				int type2 = row.getCell(2).getCellType();
				if(Cell.CELL_TYPE_STRING == type2) {
					tmp.setNum(Double.valueOf(row.getCell(2).getStringCellValue()));
				} else if(Cell.CELL_TYPE_NUMERIC == type2) {
					tmp.setNum(row.getCell(2).getNumericCellValue());
				} else {
					throw new RuntimeException("票数格式解析出现错误！");
				}
				
				int type3 = row.getCell(3).getCellType();
				if(Cell.CELL_TYPE_STRING == type3) {
					tmp.setAmount(Double.valueOf(row.getCell(3).getStringCellValue()));
				} else if(Cell.CELL_TYPE_NUMERIC == type3) {
					tmp.setAmount(row.getCell(3).getNumericCellValue());
				} else {
					throw new RuntimeException("结算金额格式解析出现错误！");
				}
				
				tmp.setPrice(tmp.getAmount() / tmp.getNum());
				
				daoService.addObject(tmp);
			}
			info.setRemark("loadTemplateFile: analyze workbook success");
			daoService.saveObject(info);
			return true;
		} catch (Exception e) {
			logger.error("loadTemplateFile settltid=" + info.getSettlementId() + " 自动对账失败，原因：" + e.getMessage());
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("loadTemplateFile: analyze workbook error");
			daoService.saveObject(info);
			return false;
		}
	}
	
	/**
	 * 将autorecon_menchtemp中的数据处理聚合到autorecon_menchorder中，处理好后删除autorecon_menchtemp中记录
	 * 	autorecon_menchtemp中的数据可能是按票来统计的
	 *  autorecon_menchorder中才会按照订单来统计
	 */
	@Override
	public boolean doMenchTempData(AutoReconciledInfo info) {
		String sql = "select tradeno, recencode, sum(num) as num, avg(price) as price, sum(amount) as amount from autorecon_menchtemp where settleid = ? group by tradeno, recencode ";
		List<Map<String, Object>> tmpmaplist = jdbcTemplate.queryForList(sql, info.getSettlementId());
		if(CollectionUtils.isEmpty(tmpmaplist)) {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("doMenchTempData: get AutoreconMenchTempList empty");
			daoService.saveObject(info);
			return false;
		}
		int allnum = 0;
		double allamount = 0.0;
		for(Map<String, Object> tmpmap : tmpmaplist) {
			MapRow row = new MapRow(tmpmap);
			AutoreconMenchOrder morder = new AutoreconMenchOrder();
			morder.setSettleid(info.getSettlementId());
			morder.setTradeno(row.getStringWithNull("tradeno"));
			morder.setRecencode(row.getStringWithNull("recencode"));
			allnum += row.getInteger("num");
			morder.setNum(row.getInteger("num"));
			morder.setPrice(row.getDouble("price"));
			allamount += row.getDouble("amount");
			morder.setAmount(row.getDouble("amount"));
			daoService.addObject(morder);
		}
		info.setMerchantOrderNumber(Double.valueOf(allnum));
		info.setMerchantOrderAmount(allamount);
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, info.getSettlementId());
		//对账金额相同，还对个毛啊
		if(allamount == bill.getSuccTicketAmount().doubleValue()) {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("doMenchTempData: mench allamount equeal gwsuccessamount");
			daoService.saveObject(info);
			return false;
		}
		
		String del = "delete from autorecon_menchtemp where settleid = ? ";
		jdbcTemplate.update(del, info.getSettlementId());
		info.setRemark("doMenchTempData: group order data success");
		daoService.saveObject(info);
		return true;
	}
	
	/**
	 * 加载处理格瓦拉结算单需要对账的订单
	 *  将订单数据加载到autorecon_gworder中
	 */
	@Override
	public boolean doGWOrder(AutoReconciledInfo info) {
		List<CheckBill> cklist = daoService.getObjectListByField(CheckBill.class, "settlementId", info.getSettlementId());
		if(CollectionUtils.isEmpty(cklist)) {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("doGWOrder: cklist is empty");
			daoService.saveObject(info);
			return false;
		}
		StringBuilder sb = new StringBuilder("");
		sb.append("select o.tradeno, o.outer_id, o.quantity, o.total_cost, d.actual_price, (d.actual_price * d.quantity) as ytotal_cost ");
		sb.append("from gewa_order o left outer join diff_price_order d on o.tradeno = d.trade_no ");
		sb.append("where o.order_type = 'TICKET' and o.place_id = ? and o.check_bill_id = ? ");
		double totalamount = 0.0;
		for(CheckBill ck : cklist) {
			List<Map<String, Object>> orderlist = jdbcTemplate.queryForList(sb.toString(), ck.getRelateId(), ck.getRecordId());
			for(Map<String, Object> map : orderlist) {
				MapRow row = new MapRow(map);
				AutoreconGWOrder gorder = new AutoreconGWOrder();
				gorder.setSettleid(info.getSettlementId());
				gorder.setTradeno(row.getStringWithNull("tradeno"));
				gorder.setRecencode(row.getStringWithNull("outer_id"));
				gorder.setNum(row.getInteger("quantity"));
				gorder.setOprice(row.getDouble("total_cost") / row.getInteger("quantity"));
				gorder.setOamount(row.getDouble("total_cost"));
				gorder.setYprice(row.getDouble("actual_price"));
				gorder.setYamount(row.getDouble("ytotal_cost"));
				//有异价的订单最终使用异价数据
				if(row.getDouble("actual_price") > 0) {
					gorder.setPrice(gorder.getYprice());
					gorder.setAmount(gorder.getYamount());
				} else {
					gorder.setPrice(gorder.getOprice());
					gorder.setAmount(gorder.getOamount());
				}
				totalamount += gorder.getAmount();
				daoService.addObject(gorder);
			}
		}
		if(totalamount != info.getGewaOrderAmount()) {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("doGWOrder: the totalamount is not equal GewaOrderAmount");
			daoService.saveObject(info);
			return false;
		}
		info.setRemark("doGWOrder: gewaorder process success");
		daoService.saveObject(info);
		return true;
	}
	
	/**
	 * 订单比价，找出差异异常订单，将异常订单结果放入autorecon_result中
	 */
	@Override
	public void compareOrder(AutoReconciledInfo info) {
		//找出gw中对账码为空的
		String gnullsql = "select tradeno, num, oprice, oamount, yprice, yamount, price, amount from autorecon_gworder where settleid = ? and (recencode is null or recencode = '') ";
		List<Map<String, Object>> gnullmaplist = jdbcTemplate.queryForList(gnullsql, info.getSettlementId());
		for(Map<String, Object> map : gnullmaplist) {
			MapRow row = new MapRow(map);
			AutoreconExceptionResult result = new AutoreconExceptionResult();
			result.setSettleid(info.getSettlementId());
			result.setGtradeno(row.getStringWithNull("tradeno"));
			result.setNum(row.getInteger("num"));
			result.setOprice(row.getDouble("oprice"));
			result.setOamount(row.getDouble("oamount"));
			result.setYprice(row.getDouble("yprice"));
			result.setYamount(row.getDouble("yamount"));
			result.setGprice(row.getDouble("price"));
			result.setGamount(row.getDouble("amount"));
			daoService.addObject(result);
		}
		//找出gw中有m中没有的对账码
		String gwsql = "select recencode, tradeno, num, oprice, oamount, yprice, yamount, price, amount from autorecon_gworder g "
				+ "where g.settleid = ? and recencode is not null and recencode <> '' "
				+ "and not exists (select 1 from autorecon_menchorder m where g.recencode = m.recencode) ";
		List<Map<String, Object>> gwmaplist = jdbcTemplate.queryForList(gwsql, info.getSettlementId());
		for(Map<String, Object> map : gwmaplist) {
			MapRow row = new MapRow(map);
			AutoreconExceptionResult result = new AutoreconExceptionResult();
			result.setSettleid(info.getSettlementId());
			result.setRecencode(row.getStringWithNull("recencode"));
			result.setGtradeno(row.getStringWithNull("tradeno"));
			result.setNum(row.getInteger("num"));
			result.setOprice(row.getDouble("oprice"));
			result.setOamount(row.getDouble("oamount"));
			result.setYprice(row.getDouble("yprice"));
			result.setYamount(row.getDouble("yamount"));
			result.setGprice(row.getDouble("price"));
			result.setGamount(row.getDouble("amount"));
			daoService.addObject(result);
		}
		
		//找出m中对账码为空的
		String mnullsql = "select tradeno, num, price, amount from autorecon_menchorder where settleid = ? and (recencode is null or recencode = '') ";
		List<Map<String, Object>> mnullmaplist = jdbcTemplate.queryForList(mnullsql, info.getSettlementId());
		for(Map<String, Object> map : mnullmaplist) {
			MapRow row = new MapRow(map);
			AutoreconExceptionResult result = new AutoreconExceptionResult();
			result.setSettleid(info.getSettlementId());
			result.setMtradeno(row.getStringWithNull("tradeno"));
			result.setNum(row.getInteger("num"));
			result.setMprice(row.getDouble("price"));
			result.setMamount(row.getDouble("amount"));
			daoService.addObject(result);
		}
		//找出m中有gw中没有的对账码
		String msql = "select recencode, tradeno, num, price, amount from autorecon_menchorder m "
				+ "where m.settleid = ? and recencode is not null and recencode <> '' "
				+ "and not exists (select 1 from autorecon_gworder g where m.recencode = g.recencode) ";
		List<Map<String, Object>> mmaplist = jdbcTemplate.queryForList(msql, info.getSettlementId());
		for(Map<String, Object> map : mmaplist) {
			MapRow row = new MapRow(map);
			AutoreconExceptionResult result = new AutoreconExceptionResult();
			result.setSettleid(info.getSettlementId());
			result.setRecencode(row.getStringWithNull("recencode"));
			result.setMtradeno(row.getStringWithNull("tradeno"));
			result.setNum(row.getInteger("num"));
			result.setMprice(row.getDouble("price"));
			result.setMamount(row.getDouble("amount"));
			daoService.addObject(result);
		}
		//找出gw和m中都有但是结算金额不相等的
		StringBuilder sb = new StringBuilder("");
		sb.append("select g.recencode, g.tradeno as gtradeno, g.num as gnum, g.oprice as goprice, g.oamount as goamount, g.yprice as gyprice, g.yamount as gyamount, g.price as gprice, g.amount as gamount, ");
		sb.append("m.tradeno as mtradeno, m.num as mnum, m.price as mprice, m.amount as mamount ");
		sb.append("from autorecon_gworder g, autorecon_menchorder m ");
		sb.append("where g.recencode = m.recencode and g.settleid = ? and m.settleid = ? and g.amount <> m.amount ");
		List<Map<String, Object>> emaplist = jdbcTemplate.queryForList(sb.toString(), info.getSettlementId(), info.getSettlementId());
		for(Map<String, Object> map : emaplist) {
			MapRow row = new MapRow(map);
			AutoreconExceptionResult result = new AutoreconExceptionResult();
			result.setSettleid(info.getSettlementId());
			result.setRecencode(row.getStringWithNull("recencode"));
			//gw
			result.setGtradeno(row.getStringWithNull("gtradeno"));
			result.setNum(row.getInteger("gnum"));
			result.setOprice(row.getDouble("goprice"));
			result.setOamount(row.getDouble("goamount"));
			result.setYprice(row.getDouble("gyprice"));
			result.setYamount(row.getDouble("gyamount"));
			result.setGprice(row.getDouble("gprice"));
			result.setGamount(row.getDouble("gamount"));
			//m
			result.setMtradeno(row.getStringWithNull("mtradeno"));
			result.setNum(row.getInteger("mnum"));
			result.setMprice(row.getDouble("mprice"));
			result.setMamount(row.getDouble("mamount"));
			
			daoService.addObject(result);
		}
		info.setStatus(SettleConstant.AUTO_RECON_STATUS_FINISH);
		info.setRemark("compareOrder: compare order success");
		daoService.saveObject(info);
	}
	
	@Override
	public void doAutoReconcile(AutoReconciledInfo info) {
		/**
		 * 更新对账状态
		 */
		String openType = info.getOpenType();
		String reconFileURL = config.getString("fileURL") + info.getFilePath();
		Workbook wb = null;
		boolean isContinue = true;
		URL url = null; 
		InputStream inputStream = null;
		try {
			url = new URL(reconFileURL);
			inputStream = url.openStream();
			wb =  WorkbookFactory.create(inputStream);
		} catch (Exception e) {
			logger.error("自动对账失败，原因：" + e.getMessage());
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("系统获取对账文件出错，自动对账失败");
			daoService.saveObject(info);
			isContinue = false;
		} finally {
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("inputStream close failure");
				}
			}
		}
		if(!isContinue){
			return ;
		}
		try {
			if(SettleConstant.HFH.equals(openType)){
				logger.warn("HFH对账>>>billId=" + info.getSettlementId());
				//对接火凤凰   报表导出路径   “影院报表>增值业务报表>增值购票报表”
//				HSSFWorkbook hfhWorkbook = (HSSFWorkbook) wb;
				doHFHAutoReconcile(wb,info);
			}else if(SettleConstant.MTX.equals(openType)){
				logger.warn("MTX对账>>>billId=" + info.getSettlementId());
//				XSSFWorkbook mtxWorkbook = (XSSFWorkbook) wb;
				//满天星
				doMTXAutoReconcile(wb,info);
			}else if(SettleConstant.STPF.equals(openType)){
				logger.warn("STPF对账>>>billId=" + info.getSettlementId());
				//辰星
//				XSSFWorkbook stpfWorkbook = (XSSFWorkbook) wb;
				doSTPFAutoReconcile(wb,info);
			}else if(SettleConstant.XFLH2.equals(openType)){
				logger.warn("XFLH2对账>>>billId=" + info.getSettlementId());
//				XSSFWorkbook xflh2Workbook = (XSSFWorkbook) wb;
				//幸福蓝海2
				doXFLH2AutoReconcile(wb,info);
			}else if(SettleConstant.MJY.equals(openType)){
				logger.warn("MJY对账>>>billId=" + info.getSettlementId());
//				XSSFWorkbook mjyWorkbook = (XSSFWorkbook) wb;
				//MJY
				doMJYAutoReconcile(wb,info);
			}
		} catch (Exception e) {
			logger.error("自动报错程序异常，结算单号："+info.getSettlementId()+">>>>异常信息：" + e.getMessage());
			e.printStackTrace();
		} finally {
			info.setStatus(SettleConstant.AUTO_RECON_STATUS_FAILURE);
			info.setRemark("自动对账失败");
			daoService.saveObject(info);
		}
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doXFLH2AutoReconcile  
	 * @Description:
	 * @param @param wb
	 * @param @param info
	 * @return void
	 */
	private void doXFLH2AutoReconcile(Workbook wb, AutoReconciledInfo info) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		Row row = null;
		int totalRows = sheet.getLastRowNum(); 
		List<AutoAnalysObject> xflh2OrderList = new ArrayList<AutoAnalysObject>();
		AutoAnalysObject aao = null;
		String movieName = "";
		String movieHall = "";
		String useDate = "";
		String useTime = "";
		String showDate = "";
		String showTime = "";
		Integer votes = 0;
		Double totalPrice = 0.0;
		String reconCode = "";
		Integer movieNameColnum = 0;
		Integer movieHallColnum = 0;
		Integer useDateColnum = 0;
//		Integer useTimeColnum = 0;
		Integer showDateColnum = 0;
		Integer showTimeColnum = 0;
		Integer votesColnum = 0;
		Integer totalPriceColnum = 0;
		Integer reconCodeColnum = 0;
		Integer startRownum = 0;
//		Integer endRownum = 0;
		rowLable:for(int i = 0 ; i < totalRows ; i ++){
			row = sheet.getRow(i);
			if(row == null){
				continue;
			}
			for(int j = 0 ; j < row.getLastCellNum(); j ++){
				if(startRownum == 0){
					if(row.getCell(j).getStringCellValue().contains("订单编号")){
						startRownum = i;
						break rowLable;
					}					
				}
			}
		}
		row = sheet.getRow(startRownum);
		Integer columnNum = (int) row.getLastCellNum();
		for(int i = 0 ; i < columnNum; i ++){
			Cell cell = row.getCell(i);
			if(cell.getStringCellValue().contains("影片")){
				movieNameColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("影厅")){
				movieHallColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("下单时间")){
				useDateColnum = cell.getColumnIndex();
			} 
			//交易时间在下单时间中
//			else if(cell.getStringCellValue().contains("交易时间")){
//				useTimeColnum = cell.getColumnIndex();
//			}
			else if(cell.getStringCellValue().equals("放映日期")){
				showDateColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("放映时间")){
				showTimeColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("商品数量")){
				votesColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("订单合计总额(元)")){
				totalPriceColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("分销商订单号")){
				reconCodeColnum = cell.getColumnIndex();
			}
		}
		//totalRows - 1 去掉合计行
		for(int i = startRownum + 1 ; i < totalRows;i++){
			row = sheet.getRow(i);
			movieName = row.getCell(movieNameColnum) == null ? movieName : row.getCell(movieNameColnum).getStringCellValue();
			movieHall = row.getCell(movieHallColnum) == null ? movieHall : row.getCell(movieHallColnum).getStringCellValue();
			useDate = row.getCell(useDateColnum) == null ? useDate : row.getCell(useDateColnum).getStringCellValue().split(" +")[0];
			useTime = row.getCell(useDateColnum) == null ? useTime : row.getCell(useDateColnum).getStringCellValue().split(" +")[1];
			showDate = row.getCell(showDateColnum) == null ? showDate : row.getCell(showDateColnum).getStringCellValue();
			showTime = row.getCell(showTimeColnum) == null ? showTime : row.getCell(showTimeColnum).getStringCellValue();
			/**
			 * 票数需要统计
			 */
			votes = row.getCell(votesColnum) == null ? votes : Integer.valueOf(row.getCell(votesColnum).getStringCellValue());
			/**
			 * 总金额需要统计,totalPrice代表单价
			 */
			String strPrice = row.getCell(totalPriceColnum).getStringCellValue();
			totalPrice = Double.valueOf(strPrice.substring(0, strPrice.indexOf(".")));
			reconCode = row.getCell(reconCodeColnum) == null ? reconCode : row.getCell(reconCodeColnum).getStringCellValue();
			aao = new AutoAnalysObject(info.getSettlementId(),"", movieName, movieHall, useDate, useTime, showDate, showTime, votes, totalPrice, reconCode, SettleConstant.AUTO_RECON_REASON_UNKNOWN, "");
			xflh2OrderList.add(aao);
		}
		logger.warn("结算单号：" + info.getSettlementId() + " 商户导入条数：" + xflh2OrderList.size());
		//<订单号，订单对象>   按订单号统计 每个订单的票数及金额
		processCompareOrder(xflh2OrderList, info);
	}

	private void doMJYAutoReconcile(Workbook wb, AutoReconciledInfo info) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		Row row = null;
		int totalRows = sheet.getLastRowNum(); 
		List<AutoAnalysObject> mjyOrderList = new LinkedList<AutoAnalysObject>();
		AutoAnalysObject aao = null;
		String movieName = "";
		String movieHall = "";
		String useDate = "";
		String useTime = "";
		String showDate = "";
		String showTime = "";
		Integer votes = 0;
		Double totalPrice = 0.0;
		String reconCode = "";
//		Integer movieNameColnum = 0;
//		Integer movieHallColnum = 0;
		Integer useDateColnum = 0;
//		Integer useTimeColnum = 0;
//		Integer showDateColnum = 0;
//		Integer showTimeColnum = 0;
		Integer votesColnum = 0;
		Integer totalPriceColnum = 0;
		Integer reconCodeColnum = 0;
		Integer startRownum = 0;
//		Integer endRownum = 0;
		rowLable:for(int i = 0 ; i < totalRows ; i ++){
			row = sheet.getRow(i);
			if(row == null){
				continue;
			}
			for(int j = 0 ; j < row.getLastCellNum(); j ++){
				if(startRownum == 0){
					if(row.getCell(j).getStringCellValue().contains("订单编号")){
						startRownum = i;
						break rowLable;
					}					
				}
			}
		}
		row = sheet.getRow(startRownum);
		Integer columnNum = (int) row.getLastCellNum();
		for(int i = 0 ; i < columnNum; i ++){
			Cell cell = row.getCell(i);
//			if(cell.getStringCellValue().contains("影片")){
//				movieNameColnum = cell.getColumnIndex();
//			}else if(cell.getStringCellValue().contains("影厅")){
//				movieHallColnum = cell.getColumnIndex();
//			}else 
			if(cell.getStringCellValue().contains("下单时间")){
				useDateColnum = cell.getColumnIndex();
			} 
			//交易时间在下单时间中
//			else if(cell.getStringCellValue().contains("交易时间")){
//				useTimeColnum = cell.getColumnIndex();
//			}
//			else if(cell.getStringCellValue().equals("放映日期")){
//				showDateColnum = cell.getColumnIndex();
//			}else if(cell.getStringCellValue().equals("放映时间")){
//				showTimeColnum = cell.getColumnIndex();
//			}
			else if(cell.getStringCellValue().equals("商品数量")){
				votesColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("结算总计(元)")){
				totalPriceColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("第三方订单号")){
				reconCodeColnum = cell.getColumnIndex();
			}
		}
		//totalRows - 1 去掉合计行
		for(int i = startRownum + 1 ; i <= totalRows;i++){
			row = sheet.getRow(i);
//			movieName = row.getCell(movieNameColnum) == null ? movieName : row.getCell(movieNameColnum).getStringCellValue();
//			movieHall = row.getCell(movieHallColnum) == null ? movieHall : row.getCell(movieHallColnum).getStringCellValue();
			useDate = row.getCell(useDateColnum) == null ? useDate : row.getCell(useDateColnum).getStringCellValue().split(" +")[0];
			useTime = row.getCell(useDateColnum) == null ? useTime : row.getCell(useDateColnum).getStringCellValue().split(" +")[1];
//			showDate = row.getCell(showDateColnum) == null ? showDate : row.getCell(showDateColnum).getStringCellValue();
//			showTime = row.getCell(showTimeColnum) == null ? showTime : row.getCell(showTimeColnum).getStringCellValue();
			/**
			 * 票数需要统计
			 */
			votes = (int) (row.getCell(votesColnum) == null ? votes : row.getCell(votesColnum).getNumericCellValue());
			/**
			 * 总金额需要统计,totalPrice代表单价
			 */
			totalPrice = row.getCell(totalPriceColnum).getNumericCellValue();
			reconCode = row.getCell(reconCodeColnum) == null ? reconCode : row.getCell(reconCodeColnum).getStringCellValue();
			aao = new AutoAnalysObject(info.getSettlementId(),"", movieName, movieHall, useDate, useTime, showDate, showTime, votes, totalPrice, reconCode, SettleConstant.AUTO_RECON_REASON_UNKNOWN, "");
			mjyOrderList.add(aao);
		}
		logger.warn("结算单号：" + info.getSettlementId() + " 商户导入条数：" + mjyOrderList.size());
		//<订单号，订单对象>   按订单号统计 每个订单的票数及金额
		processCompareOrder(mjyOrderList, info);
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doSTPFAutoReconcile  
	 * @Description:STPF excel解析
	 * @param wb
	 * @param info
	 * @return void
	 */
	private void doSTPFAutoReconcile(Workbook wb, AutoReconciledInfo info) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		Row row = null;
		int totalRows = sheet.getLastRowNum(); 
		List<AutoAnalysObject> stpfOrderList = new ArrayList<AutoAnalysObject>();
		AutoAnalysObject aao = null;
		String movieName = "";
		String movieHall = "";
		String useDate = "";
		String useTime = "";
		String showDate = "";
		String showTime = "";
		Integer votes = 0;
		Double totalPrice = 0.0;
		String reconCode = "";
		Integer movieNameColnum = 0;
		Integer movieHallColnum = 0;
//		Integer useDateColnum = 0;
		Integer useTimeColnum = 0;
//		Integer showDateColnum = 0;
		Integer showTimeColnum = 0;
//		Integer votesColnum = 0;
		Integer totalPriceColnum = 0;
		Integer reconCodeColnum = 0;
		Integer startRownum = 0;
		Integer endRownum = 0;
		rowLable:for(int i = 0 ; i < totalRows ; i ++){
			row = sheet.getRow(i);
			if(row == null){
				continue;
			}
			for(int j = 0 ; j < row.getLastCellNum(); j ++){
				if(startRownum == 0){
					if(row.getCell(j).getStringCellValue().contains("交易订单号")){
						startRownum = i;
						break rowLable;
					}					
				}
			}
		}
		row = sheet.getRow(startRownum);
		Integer columnNum = (int) row.getLastCellNum();
		for(int i = 0 ; i < columnNum; i ++){
			Cell cell = row.getCell(i);
			if(cell.getStringCellValue().contains("影片名称")){
				movieNameColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("影厅")){
				movieHallColnum = cell.getColumnIndex();
			}
//			else if(cell.getStringCellValue().contains("交易日期")){
//				useDateColnum = cell.getColumnIndex();
//			} 
			//格式：2015-02-01 08:49:12 需要拆分
			else if(cell.getStringCellValue().contains("交易时间")){
				useTimeColnum = cell.getColumnIndex();
			}
//			else if(cell.getStringCellValue().contains("放映日期")){
//				showDateColnum = cell.getColumnIndex();
//			}
			//格式 2015-02-01 16:10:00  需要拆分
			else if(cell.getStringCellValue().contains("放映时间")){
				showTimeColnum = cell.getColumnIndex();
			/**
			 * 满天星没有票数，需要程序统计
			 */
//			}
//			else if(cell.getStringCellValue().equals("票数")){
//				votesColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("票价")){
				totalPriceColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("第三方交易单号")){
				reconCodeColnum = cell.getColumnIndex();
			}
		}
		//totalRows  去掉合计行
		endRownum = sheet.getLastRowNum();
		for(int i = startRownum + 1 ; i < endRownum;i++){
			row = sheet.getRow(i);
			if(StringUtils.isEmpty(row.getCell(1).getStringCellValue())){
				continue;
			}
			movieName = row.getCell(movieNameColnum) == null ? movieName : row.getCell(movieNameColnum).getStringCellValue();
			movieHall = row.getCell(movieHallColnum) == null ? movieHall : row.getCell(movieHallColnum).getStringCellValue();
			useDate = row.getCell(useTimeColnum) == null ? useDate : row.getCell(useTimeColnum).getStringCellValue().split(" +")[0];
			useTime = row.getCell(useTimeColnum) == null ? useTime : row.getCell(useTimeColnum).getStringCellValue().split(" +")[1];
			showDate = row.getCell(showTimeColnum) == null ? showDate : row.getCell(showTimeColnum).getStringCellValue().split(" +")[0];
			showTime = row.getCell(showTimeColnum) == null ? showTime : row.getCell(showTimeColnum).getStringCellValue().split(" +")[1];
			/**
			 * 票数需要统计
			 */
//			votes = row.getCell(votesColnum) == null ? votes : (int)row.getCell(votesColnum).getNumericCellValue();
			votes = 1;
			/**
			 * 总金额需要统计,totalPrice代表单价
			 */
			totalPrice =  row.getCell(totalPriceColnum) == null ? totalPrice : (int)row.getCell(totalPriceColnum).getNumericCellValue();
			reconCode = row.getCell(reconCodeColnum) == null ? reconCode : row.getCell(reconCodeColnum).getStringCellValue();
			aao = new AutoAnalysObject(info.getSettlementId(),"", movieName, movieHall, useDate, useTime, showDate, showTime, votes, totalPrice, reconCode, SettleConstant.AUTO_RECON_REASON_UNKNOWN, "");
			stpfOrderList.add(aao);
		}
		logger.warn("结算单号：" + info.getSettlementId() + " 商户导入条数：" + stpfOrderList.size());
		//<订单号，订单对象>   按订单号统计 每个订单的票数及金额
		Map<String,AutoAnalysObject> map = new HashMap<String, AutoAnalysObject>();
		for(AutoAnalysObject ord : stpfOrderList){
			String orderNo = ord.getReconCode();
			if(map.get(orderNo) == null){
				map.put(orderNo, ord);
			}else{
				AutoAnalysObject ord1 = map.get(orderNo);
				ord1.setVotes(ord1.getVotes() + 1);
				ord1.setTotalPrice(ord1.getTotalPrice() + ord.getTotalPrice());
				map.put(orderNo, ord1);
			}
		}
		stpfOrderList = new LinkedList<AutoAnalysObject>();
		for(Entry<String, AutoAnalysObject> entry : map.entrySet()){
			stpfOrderList.add(entry.getValue());
		}
		processCompareOrder(stpfOrderList, info);
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doMTXAutoReconcile  
	 * @Description:满天星Excel解析
	 * @param wb
	 * @param info
	 * @return void
	 */
	private void doMTXAutoReconcile(Workbook wb, AutoReconciledInfo info) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		Row row = null;
		int totalRows = sheet.getLastRowNum(); 
		List<AutoAnalysObject> mtxOrderList = new ArrayList<AutoAnalysObject>();
		AutoAnalysObject aao = null;
		String movieName = "";
		String movieHall = "";
		String useDate = "";
		String useTime = "";
		String showDate = "";
		String showTime = "";
		Integer votes = 0;
		Double totalPrice = 0.0;
		String reconCode = "";
		Integer movieNameColnum = 0;
		Integer movieHallColnum = 0;
		Integer useDateColnum = 0;
		Integer useTimeColnum = 0;
		Integer showDateColnum = 0;
		Integer showTimeColnum = 0;
//		Integer votesColnum = 0;
		Integer totalPriceColnum = 0;
		Integer reconCodeColnum = 0;
		Integer startRownum = 0;
		Integer endRownum = 0;
		rowLable:for(int i = 0 ; i < totalRows ; i ++){
			row = sheet.getRow(i);
			if(row == null){
				continue;
			}
			for(int j = 0 ; j < row.getLastCellNum(); j ++){
				if(startRownum == 0){
					if(row.getCell(j).getStringCellValue().contains("订单号")){
						startRownum = i;
					}					
				}
				if(endRownum == 0){
					if(row.getCell(j).getStringCellValue().contains("合计")){
						endRownum = i;
					}					
				}
				if(startRownum != 0 && endRownum != 0){
					break rowLable;
				}
			}
		}
		row = sheet.getRow(startRownum);
		Integer columnNum = (int) row.getLastCellNum();
		for(int i = 0 ; i < columnNum; i ++){
			Cell cell = row.getCell(i);
			if(cell.getStringCellValue().contains("影片名称")){
				movieNameColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("影厅名称")){
				movieHallColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("交易日期")){
				useDateColnum = cell.getColumnIndex();
			} else if(cell.getStringCellValue().contains("交易时间")){
				useTimeColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("放映日期")){
				showDateColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().contains("放映时间")){
				showTimeColnum = cell.getColumnIndex();
			/**
			 * 满天星没有票数，需要程序统计
			 */
//			}
//			else if(cell.getStringCellValue().equals("票数")){
//				votesColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("票价")){
				totalPriceColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("订单号")){
				reconCodeColnum = cell.getColumnIndex();
			}
		}
		//totalRows - 1 去掉合计行
		for(int i = startRownum + 1 ; i < endRownum;i++){
			row = sheet.getRow(i);
			movieName = row.getCell(movieNameColnum) == null ? movieName : row.getCell(movieNameColnum).getStringCellValue();
			movieHall = row.getCell(movieHallColnum) == null ? movieHall : row.getCell(movieHallColnum).getStringCellValue();
			useDate = row.getCell(useDateColnum) == null ? useDate : row.getCell(useDateColnum).getStringCellValue();
			useTime = row.getCell(useTimeColnum) == null ? useTime : row.getCell(useTimeColnum).getStringCellValue();
			showDate = row.getCell(showDateColnum) == null ? showDate : row.getCell(showDateColnum).getStringCellValue();
			showTime = row.getCell(showTimeColnum) == null ? showTime : row.getCell(showTimeColnum).getStringCellValue();
			/**
			 * 票数需要统计
			 */
//			votes = row.getCell(votesColnum) == null ? votes : (int)row.getCell(votesColnum).getNumericCellValue();
			votes = 1;
			/**
			 * 总金额需要统计,totalPrice代表单价
			 */
			totalPrice = row.getCell(totalPriceColnum) == null ? totalPrice : Integer.valueOf(row.getCell(totalPriceColnum).getStringCellValue());
			reconCode = row.getCell(reconCodeColnum) == null ? reconCode : row.getCell(reconCodeColnum).getStringCellValue();
			aao = new AutoAnalysObject(info.getSettlementId(),"", movieName, movieHall, useDate, useTime, showDate, showTime, votes, totalPrice, reconCode, SettleConstant.AUTO_RECON_REASON_UNKNOWN, "");
			mtxOrderList.add(aao);
		}
		logger.warn("结算单号：" + info.getSettlementId() + " 商户导入条数：" + mtxOrderList.size());
		//<订单号，订单对象>   按订单号统计 每个订单的票数及金额
		Map<String,AutoAnalysObject> map = new HashMap<String, AutoAnalysObject>();
		for(AutoAnalysObject ord : mtxOrderList){
			String orderNo = ord.getReconCode();
			//订单号去掉前面4个0即是对账码
			if(map.get(orderNo) == null){
				if(orderNo.startsWith("0000")){
					ord.setReconCode(orderNo.substring(4));
				}
				map.put(orderNo, ord);
			}else{
				AutoAnalysObject ord1 = map.get(orderNo);
				ord1.setVotes(ord1.getVotes() + 1);
				ord1.setTotalPrice(ord1.getTotalPrice() + ord.getTotalPrice());
				map.put(orderNo, ord1);
			}
		}
		mtxOrderList = new LinkedList<AutoAnalysObject>();
		for(Entry<String, AutoAnalysObject> entry : map.entrySet()){
			mtxOrderList.add(entry.getValue());
		}
		processCompareOrder(mtxOrderList, info);
	}

	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doHFHAutoReconcile  
	 * @Description:火凤凰Excel解析
	 * @param wb
	 * @param info
	 * @return void
	 */
	private void doHFHAutoReconcile(Workbook wb, AutoReconciledInfo info) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		Row row = null;
		//Excel标题行
		int totalRows = sheet.getLastRowNum(); 
		List<AutoAnalysObject> hfhOrderList = new ArrayList<AutoAnalysObject>();
		AutoAnalysObject aao = null;
		String movieName = "";
		String movieHall = "";
		String useDate = "";
		String useTime = "";
		String showDate = "";
		String showTime = "";
		Integer votes = 0;
		Double totalPrice = 0.0;
		String reconCode = "";
		Integer movieNameColnum = 0;
		Integer movieHallColnum = 0;
		Integer useDateColnum = 0;
		Integer useTimeColnum = 0;
		Integer showDateColnum = 0;
		Integer showTimeColnum = 0;
		Integer votesColnum = 0;
		Integer totalPriceColnum = 0;
		Integer reconCodeColnum = 0;
		Integer startRownum = 0;
		rowLable:for(int i = 0 ; i < totalRows ; i ++){
			row = sheet.getRow(i);
			if(row == null){
				continue;
			}
			for(int j = 0 ; j < row.getLastCellNum(); j ++){
				if(row.getCell(j).getStringCellValue().equals("订单号")){
					startRownum = i;
					break rowLable;
				}
			}
		}
		row = sheet.getRow(startRownum);
		Integer columnNum = (int) row.getLastCellNum();
		for(int i = 0 ; i < columnNum; i ++){
			Cell cell = row.getCell(i);
			if(cell.getStringCellValue().equals("影片")){
				movieNameColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("影厅")){
				movieHallColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("消费日期")){
				useDateColnum = cell.getColumnIndex();
			} else if(cell.getStringCellValue().equals("消费时间")){
				useTimeColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("放映日期")){
				showDateColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("放映时间")){
				showTimeColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("票数")){
				votesColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("票价")){
				totalPriceColnum = cell.getColumnIndex();
			}else if(cell.getStringCellValue().equals("确认号")){
				reconCodeColnum = cell.getColumnIndex();
			}
		}
		//totalRows - 1 去掉合计行
		for(int i = startRownum + 1 ; i < totalRows;i++){
			row = sheet.getRow(i);
			movieName = row.getCell(movieNameColnum) == null ? movieName : row.getCell(movieNameColnum).getStringCellValue();
			movieHall = row.getCell(movieHallColnum) == null ? movieHall : row.getCell(movieHallColnum).getStringCellValue();
			useDate = row.getCell(useDateColnum) == null ? useDate : row.getCell(useDateColnum).getStringCellValue();
			useTime = row.getCell(useTimeColnum) == null ? useTime : row.getCell(useTimeColnum).getStringCellValue();
			showDate = row.getCell(showDateColnum) == null ? showDate : row.getCell(showDateColnum).getStringCellValue();
			showTime = row.getCell(showTimeColnum) == null ? showTime : row.getCell(showTimeColnum).getStringCellValue();
			votes = row.getCell(votesColnum) == null ? votes : (int)row.getCell(votesColnum).getNumericCellValue();
			totalPrice = row.getCell(totalPriceColnum) == null ? totalPrice : (int)row.getCell(totalPriceColnum).getNumericCellValue();
			reconCode = row.getCell(reconCodeColnum) == null ? reconCode : row.getCell(reconCodeColnum).getStringCellValue();
			aao = new AutoAnalysObject(info.getSettlementId(),"", movieName, movieHall, useDate, useTime, showDate, showTime, votes, totalPrice, reconCode, SettleConstant.AUTO_RECON_REASON_UNKNOWN, "");
			hfhOrderList.add(aao);
		}
		logger.warn("结算单号：" + info.getSettlementId() + " 商户导入条数：" + hfhOrderList.size());
		processCompareOrder(hfhOrderList,info);
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: processCompareOrder  
	 * @Description:
	 * @param @param info
	 * @param @param hfhOrderList
	 * @return void
	 * @throws  
	 */
	private void processCompareOrder(List<AutoAnalysObject> merchantOrderList,AutoReconciledInfo info) {
		List<AutoAnalysObject> repeatPos = new ArrayList<AutoAnalysObject>();
		for(int i = 0 ; i < merchantOrderList.size()-1 ; i ++){ 
			for(int j = merchantOrderList.size()- 1;j > i;j -- )   { 
				if  (merchantOrderList.get(j).getReconCode().equals(merchantOrderList.get(i).getReconCode())){
					logger.warn(merchantOrderList.get(i).getReconCode());
					logger.warn(merchantOrderList.get(j).getReconCode());
					repeatPos.add(merchantOrderList.get(i));
					repeatPos.add(merchantOrderList.get(j));
				} 
			} 
		} 
//		for(AutoAnalysObject pos : repeatPos){
//			merchantOrderList.remove(pos);
//		}
		merchantOrderList.removeAll(repeatPos);
		/**
		 * 查询结算单下的订单
		 */
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, info.getSettlementId());
		List<CheckBill> checkBills = daoService.getObjectListByField(CheckBill.class, "settlementId", settlementBill.getRecordId());
		List<Long> checkBillIds = BeanUtil.getBeanPropertyList(checkBills, "recordId", true);
//		List<GewaOrder> gewaOrders = daoService.getObjectBatch(GewaOrder.class, "checkBillId", checkBillIds);
		
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		query.add(Restrictions.in("checkBillId", checkBillIds));
		query.add(Restrictions.eq("orderStatus", "paid_success"));
		List<GewaOrder> gewaOrders = daoService.findByCriteria(query);
		
		Double gewaVotes = settlementBill.getOrderTotalNumber();
		Double gewaPrice = settlementBill.getOrderTotalAmount();
		Double merchantVotes = 0.0;
		Double merchantPrice = 0.0;
		/**
		 * 开始与GewaOrder进行比较
		 */
		//正常Order
		List<AutoAnalysObject> normalMerOrderList = new ArrayList<AutoAnalysObject>();
		//异常Order
		List<AutoAnalysObject> abnormalMerOrderList = new ArrayList<AutoAnalysObject>();
		//正常gewaOrder
		List<GewaOrder> normalGewaOrderList = new ArrayList<GewaOrder>();
		//异常GewaORder
		List<GewaOrder> abnormalGewaOrderList = new ArrayList<GewaOrder>();
		Iterator<AutoAnalysObject> iterAao = merchantOrderList.iterator();
		Iterator<GewaOrder> iterGewa = gewaOrders.iterator();
		
		/*******************对账码为空的订单记录,默认为异常***************************/
		while(iterAao.hasNext()){
			AutoAnalysObject aaObj = iterAao.next();
			if(StringUtils.isEmpty(aaObj.getReconCode())){
				//添加到异常列表
				abnormalMerOrderList.add(aaObj);
			}else{
				//添加到正常列表
				normalMerOrderList.add(aaObj);
			}
		}
		//格瓦拉订单
		while(iterGewa.hasNext()){
			GewaOrder gewaOrder = iterGewa.next();
			if(StringUtils.isEmpty(gewaOrder.getOuterId())){
				//格瓦订单异常列表
				abnormalGewaOrderList.add(gewaOrder);
			}else{
				//格瓦订单正常列表
				normalGewaOrderList.add(gewaOrder);
			}
		}
		/*******************对账码为空的订单记录,默认为异常***************************/
		
		List<AutoAnalysObject> finalNormalMerOrderList = new ArrayList<AutoAnalysObject>();
		List<AutoAnalysObject> finalAbnormalMerOrderList = new ArrayList<AutoAnalysObject>();
		
		/**************先对比有对账码的订单************************/
		List<String> reonCodes = BeanUtil.getBeanPropertyList(normalMerOrderList, "reconCode", false);
		List<String> outerIds = BeanUtil.getBeanPropertyList(normalGewaOrderList, "outerId", false);
		//用来保存两者共同有的数据
		List <String> temp=new ArrayList<String>(reonCodes);
		Map<String,GewaOrder> outerIdGewaorderMap = BeanUtil.beanListToMap(normalGewaOrderList, "outerId");
		temp.retainAll(outerIds);
		//reonCodes 中的差异数据
		reonCodes.removeAll(temp);
		//outerIds中的差异数据
		outerIds.removeAll(temp);
		//将对象加入到最终正常列表
		for(int i = 0 ; i < temp.size(); i++){
			for(int j= 0 ; j < normalMerOrderList.size(); j ++){
				AutoAnalysObject autoObj = normalMerOrderList.get(j) ;
				if(autoObj.getReconCode().equals(temp.get(i))){
					autoObj.setOrderStatus(SettleConstant.AUTO_RECON_RESULT_NORMAL);
					autoObj.setTradeNo(outerIdGewaorderMap.get(temp.get(i)).getTradeno());
					if(StringUtils.isEmpty(autoObj.getMovieHall())){
						autoObj.setMovieHall(JsonUtils.readJsonToMap(outerIdGewaorderMap.get(temp.get(i)).getOtherInfo()).get("影厅").toString());
					}
					finalNormalMerOrderList.add(autoObj);
					merchantVotes += autoObj.getVotes();
					merchantPrice += autoObj.getTotalPrice();
				}
			}
		}
		
		//reonCodes异常列表
		for(int i = 0 ; i < reonCodes.size() ; i ++){
			for(int j= 0 ; j < normalMerOrderList.size(); j ++){
				AutoAnalysObject autoObj = normalMerOrderList.get(j) ;
				if(autoObj.getReconCode().equals(reonCodes.get(i))){
					autoObj.setOrderStatus(SettleConstant.AUTO_RECON_RESULT_ABNORMAL);
					finalAbnormalMerOrderList.add(autoObj);
				}
			}
		}
		
		//outerIds异常列表
		for(int i = 0 ; i < outerIds.size() ; i++){
			for(int j = 0 ; j < normalGewaOrderList.size() ; j++){
				if(normalGewaOrderList.get(j).getOuterId().equals(outerIds.get(i))){
					GewaOrder ord = normalGewaOrderList.get(j);
					AutoAnalysObject aaObj = transGewaOrderToAnalysObject(settlementBill, ord);
					aaObj.setOrderStatus(SettleConstant.AUTO_RECON_RESULT_ABNORMAL);
					finalAbnormalMerOrderList.add(aaObj);					
				}
			}
		}
		/*********************没有对账码的订单，加入到异常订单中*************
		 *********************先处理导入的订单，加入到异常订单中*****************/
		finalAbnormalMerOrderList.addAll(abnormalMerOrderList);
		
		/**********************加入格瓦拉订单**********************************/
		
		for(int i = 0 ; i< abnormalGewaOrderList.size() ; i++){
			GewaOrder ord = abnormalGewaOrderList.get(i);
			AutoAnalysObject aaObj = transGewaOrderToAnalysObject(settlementBill, ord);
			aaObj.setOrderStatus(SettleConstant.AUTO_RECON_RESULT_ABNORMAL);
			finalAbnormalMerOrderList.add(aaObj);			
		}
		
		/************************分析异常订单list中的数据**********************/
		String remark = SettleConstant.AUTO_RECON_REASON_UNKNOWN;
		for(int i = 0 ; i < finalAbnormalMerOrderList.size(); i ++){
			AutoAnalysObject autoObj = finalAbnormalMerOrderList.get(i);
			String tradeNo = autoObj.getTradeNo();
			String reconCode = autoObj.getReconCode();
			List<GewaOrder> tempGewaOrderList = null;
			//如果tradeno为空，则为导入的的订单
			if(StringUtils.isNotEmpty(reconCode) && StringUtils.isEmpty(tradeNo)){
				//用对账码查找
				tempGewaOrderList = daoService.getObjectListByField(GewaOrder.class, "outerId", reconCode);
				remark = determinWhichType(info, settlementBill,tempGewaOrderList);
			}else if(StringUtils.isNotEmpty(tradeNo) && StringUtils.isEmpty(reconCode)){
				tempGewaOrderList = daoService.getObjectListByField(GewaOrder.class, "tradeno", tradeNo);
				remark = determinWhichType(info, settlementBill,tempGewaOrderList);
			}
			if(CollectionUtils.isNotEmpty(tempGewaOrderList)){
				GewaOrder o = tempGewaOrderList.get(0);
				autoObj.setTradeNo(o.getTradeno());
				if(StringUtils.isEmpty(autoObj.getMovieHall())){
					autoObj.setMovieHall(JsonUtils.readJsonToMap(o.getOtherInfo()).get("影厅").toString());
				}
			}
			autoObj.setRemark(remark);
		}
		
//		daoService.saveObjectList(finalNormalMerOrderList);
		for(AutoAnalysObject obj : finalNormalMerOrderList){
			//daoService.saveObject(obj);			
		}
		
//		daoService.saveObjectList(finalAbnormalMerOrderList);
		for(AutoAnalysObject obj : finalAbnormalMerOrderList){
			//daoService.saveObject(obj);			
		}
		
		info.setStatus(SettleConstant.AUTO_RECON_STATUS_FINISH);
		info.setGewaOrderAmount(gewaPrice);
		info.setGewaOrderNumber(gewaVotes);
		info.setMerchantOrderNumber(merchantVotes);
		info.setMerchantOrderAmount(merchantPrice);
		
		daoService.saveObject(info);
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: determinWhichType  
	 * @Description:
	 * @param info
	 * @param settlementBill
	 * @param remark
	 * @param tempGewaOrderList
	 * @return String
	 */
	private String determinWhichType(AutoReconciledInfo info,SettlementBill settlementBill,List<GewaOrder> tempGewaOrderList) {
		String remark = SettleConstant.AUTO_RECON_REASON_UNKNOWN;
		//未找到
		if(CollectionUtils.isEmpty(tempGewaOrderList)){
			remark = SettleConstant.AUTO_RECON_REASON_NOTFOUND;
		}else{
			if(tempGewaOrderList.size() > 1){
				remark = SettleConstant.AUTO_RECON_REASON_MOREORD;
			}else{
				//已经结算
				GewaOrder ord = tempGewaOrderList.get(0);
				if(ord.getOrderStatus().equals("paid_success")){
					Timestamp start = settlementBill.getStartTime();
					Timestamp end = settlementBill.getEndTime();
					//未结算，根据结算配置 判断是否在结算周期
					SettleConfig sconfig = daoService.getObject(SettleConfig.class, info.getPlaceId());
					String settleBase = sconfig.getSettleBase();
					if(ord.getCheckBillId() != null){
						CheckBill ck = daoService.getObject(CheckBill.class, ord.getCheckBillId());
						Long settleBillId = ck.getSettlementId();
						remark = SettleConstant.AUTO_RECON_REASON_SETTLED.replace("{0}", settleBillId.toString());							
					}else if(SettleConstant.SETTLE_BASE_ORDERTIME.equals(settleBase)){
						//下单时间
						Timestamp ordTime = ord.getDealTime();
						if(ordTime.after(end) || ordTime.before(start)){
							remark = SettleConstant.AUTO_RECON_REASON_NOTRANGE;
						}
					}else if(SettleConstant.SETTLE_BASE_USETIME.equals(settleBase)){
						Timestamp useTime = ord.getUseTime();
						if(useTime.after(end) || useTime.before(start)){
							remark = SettleConstant.AUTO_RECON_REASON_NOTRANGE;
						}
					}
				}else if(ord.getOrderStatus().equals("paid_return")){
					//查询退票
					Refundment refund = daoService.getObject(Refundment.class, ord.getTradeno());
					if(refund != null){
						remark = SettleConstant.AUTO_RECON_REASON_REFUND;
					}
				}
			}
		}
		return remark;
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: transGewaOrderToAnalysObject  
	 * @Description:
	 * @param @param settlementBill
	 * @param @param ord
	 * @param @return
	 * @return AutoAnalysObject
	 * @throws  
	 */
	private AutoAnalysObject transGewaOrderToAnalysObject(
			SettlementBill settlementBill, GewaOrder ord) {
		AutoAnalysObject aaObj = null;
		Map<String, String> otherMap;
		String useDate;
		String useTime;
		String showDate;
		String showTime;
		otherMap = JsonUtils.readJsonToMap(ord.getOtherInfo());
		useDate = DateUtil.format(ord.getDealTime(), "yyyy-MM-dd HH:mm:ss").split(" +")[0];
		useTime = DateUtil.format(ord.getDealTime(), "yyyy-MM-dd HH:mm:ss").split(" +")[1];
		showDate = DateUtil.format(ord.getUseTime(), "yyyy-MM-dd HH:mm:ss").split(" +")[0];
		showTime = DateUtil.format(ord.getUseTime(), "yyyy-MM-dd HH:mm:ss").split(" +")[1];
		aaObj = new AutoAnalysObject(settlementBill.getRecordId(), ord.getTradeno(),otherMap.get("影片"), otherMap.get("影厅"), useDate, useTime, 
				showDate, showTime, ord.getQuantity(), ord.getTotalCost(), ord.getOuterId(), "", "");
		return aaObj;
	}

	@Override
	public List<AutoAnalysObject> loadReconResult(Long settlebillId,String status, Integer pageNo, Integer pageSize) {
		DetachedCriteria query = DetachedCriteria.forClass(AutoAnalysObject.class);
		query.add(Restrictions.eq("orderStatus", status));
		query.add(Restrictions.eq("settlementId", settlebillId));
		return daoService.findByCriteria(query, pageNo * pageSize, pageSize);
	}

	@Override
	public Integer countReconResult(Long settlebillId, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(AutoAnalysObject.class);
		query.add(Restrictions.eq("orderStatus", status));
		query.add(Restrictions.eq("settlementId", settlebillId));
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
	}

	@Override
	public int countReconcileResultNum(List<String> placeIds, Timestamp start,Timestamp end, Long settleid) {
		DetachedCriteria query = null;
		
		List<SettlementBill> settlebills = querySettlementbillsForAutoRecon(placeIds, start, end, settleid);
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settlebills, "recordId", true);
		if(CollectionUtils.isEmpty(settleIds)){
			return 0;
		}
		query = DetachedCriteria.forClass(AutoReconciledInfo.class);
		query.add(Restrictions.in("settlementId", settleIds));
		query.setProjection(Projections.rowCount());
		return Integer.valueOf(daoService.findByCriteria(query).get(0).toString());
	}

	@Override
	public List<AutoReconciledInfo> queryReconcileResult(List<String> placeIds,
			Integer pageNo, Integer pageSize, Timestamp start, Timestamp end, Long settleid) {
		//先查询结算单，按结算单查询对账结果
		DetachedCriteria query = null;
		List<SettlementBill> settlebills = querySettlementbillsForAutoRecon(placeIds, start, end, settleid);
		List<Long> settleIds = BeanUtil.getBeanPropertyList(settlebills, "recordId", true);
		
		query = DetachedCriteria.forClass(AutoReconciledInfo.class);
		query.add(Restrictions.in("settlementId", settleIds));
		query.addOrder(Order.desc("uploadTime"));
		return daoService.findByCriteria(query, pageNo * pageSize, pageSize);
	}

	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: querySettlementbillsForAutoRecon  
	 * @Description:
	 * @param placeIds
	 * @param start
	 * @param end
	 * @param @return
	 * @return List<SettlementBill>
	 */
	@Override
	public List<SettlementBill> querySettlementbillsForAutoRecon(List<String> placeIds, Timestamp start, Timestamp end, Long settleid) {
		DetachedCriteria query = DetachedCriteria.forClass(SettlementBill.class);
		if(CollectionUtils.isNotEmpty(placeIds)){
			query.add(Restrictions.in("configId", placeIds));
		}
		if(start != null){
			query.add(Restrictions.ge("startTime", start));
		}
		if(end != null){
			query.add(Restrictions.ge("endTime", end));
		}
		if(settleid != null) {
			query.add(Restrictions.ge("recordId", settleid));
		}
		return daoService.findByCriteria(query);
	}

	@Override
	public void deleteReconRecord(Long settlebillId) {
		String delInfo = "delete from autoreconciledinfo where settlement_id = ? ";
		String deltmp = "delete from autorecon_menchtemp where settleid = ? ";
		String delgorder = "delete from autorecon_menchorder where settleid = ? ";
		String delmorder = "delete from autorecon_gworder where settleid = ?";
		String delresult = "delete from autorecon_result where settleid = ? ";
		jdbcTemplate.update(delInfo, settlebillId);
		jdbcTemplate.update(deltmp, settlebillId);
		jdbcTemplate.update(delgorder, settlebillId);
		jdbcTemplate.update(delmorder, settlebillId);
		jdbcTemplate.update(delresult, settlebillId);
	}
}
