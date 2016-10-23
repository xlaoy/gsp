package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.model.gsp.SyncMark;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.WebUtils;
import com.gewara.web.support.ResourceStatsUtil;

@Controller
public class SysManageController extends AnnotationController{
	@RequestMapping("/stopCamel.xhtml")
	@ResponseBody
	public String stopCame(HttpServletRequest request) throws Exception{
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		String msg = "stop CamelContext:" + new Date();
		dbLogger.warn(msg);
		String result = "";
		CamelContext context = applicationContext.getBean("camel", CamelContext.class);
		if(context!=null){
			context.stop();
			result += "stop camel success!";
		}else{
			result += "camel not exists!";
		}
		try{
			Scheduler scheduler =applicationContext.getBean(Scheduler.class);
			if(scheduler != null){
				scheduler.shutdown();
				for (int i = 0;i < 10;i++){
					if (ResourceStatsUtil.getCallStats().getTotalProcessing() == 0){
						break;
					}else {
						Thread.sleep(1000 * 5);
					}
				}
				result += "job scheduler shutdown success!";
			}else{
				result += "no job scheduler!";
			}
		}catch(Exception e){
		}
		return result;
	}
	@RequestMapping("/startCamel.xhtml")
	public String startCame(ModelMap model) throws Exception{
		String msg = "start camel CamelContext:" + new Date();
		dbLogger.warn(msg);
		CamelContext context = applicationContext.getBean("camel", CamelContext.class);
		context.start();
		return forwardMessage(model, msg);
	}
	
	@RequestMapping("/check.xhtml")
	@ResponseBody
	public String checkMySelf(HttpServletRequest request){
		GewaIpConfig.filterOfficeIp(WebUtils.getRemoteIp(request));
		SyncMark syncMark = daoService.getObject(SyncMark.class, "validateNumber");
		Timestamp lastTime = syncMark.getLastExecuteTime();
		Timestamp lastBegin = DateUtil.getBeginningTimeOfDay(lastTime);
		Timestamp todayBegin = DateUtil.getBeginningTimeOfDay(DateUtil.getCurFullTimestamp());
		if (lastBegin.equals(todayBegin)){
			return "ok";
		}
		return "订单同步异常，请检查>>最后校验日期:" + lastBegin + " ，当前日期：" + todayBegin;
	}
	
	public void checkIp(String ip){
		GewaIpConfig.filterIp(ip);
	}
}
