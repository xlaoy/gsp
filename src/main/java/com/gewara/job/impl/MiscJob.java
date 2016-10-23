/** 
 */
package com.gewara.job.impl;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.dao.DataExtractionDao;
import com.gewara.job.JobService;
import com.gewara.model.gsp.SyncMark;
import com.gewara.service.DaoService;
import com.gewara.untrans.GSPMaill;
import com.gewara.untrans.impl.GSPSendMaill;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 13, 2013  2:02:44 PM
 */
public class MiscJob extends JobService{
	private final transient GewaLogger dbLogger = WebLogger.getLogger(getClass());
	
	@Autowired
	@Qualifier("dataExtractionDao")
	private DataExtractionDao dataExtractionDao;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Autowired
	@Qualifier("GSPSendMaill")
	private GSPMaill GSPMaill;
	
	public void validateNumber(){
		Timestamp start = DateUtil.getCurFullTimestamp();
		start = DateUtil.getMonthFirstDay(start);
		start = MiscUtil.addMonth(start, -3);
		
		SyncMark sm = daoService.getObject(SyncMark.class, "syncOrder");
		SyncMark sm1 = daoService.getObject(SyncMark.class, "syncGoods");
		Timestamp orderEnd = sm.getLastExecuteTime();
		Timestamp goodsEnd = sm1.getLastExecuteTime();
		
		Long shNumber = dataExtractionDao.getSHOrderCount(start, orderEnd, goodsEnd);
		long gspNumber = dataExtractionDao.getGSPOrderCount(start, orderEnd, goodsEnd);
		
		String content = "上海：" + shNumber + ",GSP:" + gspNumber + "<br>";
		content = content + "差值：" + (shNumber - gspNumber);
		content = content + "<br>统计起始时间：" + start;
		content = content + "<br>orderEnd：" + orderEnd + " | goodsEnd:" + goodsEnd;
		dbLogger.warn(content);
		
		if (Math.abs(shNumber - gspNumber) < 30){
			SyncMark mark = daoService.getObject(SyncMark.class, "validateNumber");
			if (mark == null){
				mark = new SyncMark("validateNumber", DateUtil.getCurTruncTimestamp());
			}
			mark.setLastExecuteTime(DateUtil.getCurTruncTimestamp());
			mark.setModifyTime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(mark);
		} else {
			GSPMaill.sendMaill(content, GSPSendMaill.SYSERRORMAIL);
		}
		
	}
}
