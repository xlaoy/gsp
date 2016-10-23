package com.gewara.untrans.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.api.gmail.request.SendMailByOutboxRequest;
import com.gewara.api.gmail.service.GmailService;
import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.SysData;
import com.gewara.service.DaoService;
import com.gewara.untrans.GSPMaill;

@Service("GSPSendMaill")
public class GSPSendMaill implements GSPMaill {
	
	@Autowired
	@Qualifier("gmailService")
	private GmailService gmailService;
	
	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	
	public static final String DRAMAMAIL = "DRAMAMAIL";
	public static final String RONGDUANMAIL = "RONGDUANMAIL";
	public static final String SYSERRORMAIL = "SYSERRORMAIL";
	public static final String SYSADMINMAIL = "SYSADMINMAIL";
	public static final String TESTMAIL = "TESTMAIL";
	

	@SuppressWarnings("unchecked")
	@Override
	public void sendMaill(String content, String tag) {
		String title = "结算平台通知";
		String hostName = Config.getHostname();
		if (StringUtils.containsIgnoreCase(hostName, SettleConstant.hostname)) {
			title = title + ":预发环境-" + Config.getHostname();
		} else {
			title = title + ":正式环境-" + Config.getHostname();
		}
		
		DetachedCriteria qry = DetachedCriteria.forClass(SysData.class);
		qry.add(Restrictions.eq("type", tag));
		List<SysData> dataList = daoService.findByCriteria(qry);
		List<String> emails = new ArrayList<String>();
		for(SysData data : dataList) {
			emails.add(data.getName());
		}
		
		for (String sendTo : emails){
			SendMailByOutboxRequest request = new SendMailByOutboxRequest(SendMailByOutboxRequest.OUTBOX_OPERATION021,"GewaraGSP",
					sendTo,title,content);
			gmailService.sendMailByOutbox2(request);
		}
	}
	
}
