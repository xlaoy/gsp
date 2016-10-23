package com.gewara.web.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class CommonTaskController extends AnnotationController {
	private static final List<String> beanNamePassList = new ArrayList<String>();
	static{
		beanNamePassList.add("dataExtractionJob");
		beanNamePassList.add("channelSettleJob");
		beanNamePassList.add("goodsSettleJob");
		beanNamePassList.add("billsGenerateJob");
		beanNamePassList.add("miscJob");
		beanNamePassList.add("wandaOrderJob");
		beanNamePassList.add("baoChangJob");
		beanNamePassList.add("rongDuanJob");
		beanNamePassList.add("WPJob");
		beanNamePassList.add("dramaJob");
	}
	
	@RequestMapping("/commonTask/exeJobTask.xhtml")
	public String exeJobTask(HttpServletRequest request,String b,String m, ModelMap model) throws Exception{
		if(!checkSpecialUrl(request)){
			return showJsonError(model, "无权限");
		}
		if(StringUtils.isEmpty(b) || StringUtils.isEmpty(m) || !beanNamePassList.contains(b)){
			return showJsonError(model, "service名称不在通过名单中！"); 
		}
		final Object bean = applicationContext.getBean(b);
		Class clazz = bean.getClass();
		final Method exeMethod = clazz.getMethod(m);
		dbLogger.warn(getLogonUser(request).getUsername() + ",clazz:" + clazz.getName() + ", Method:" + exeMethod.getName());
		new Thread() {
			@Override
			public void run() {
				 try {
					 exeMethod.invoke(bean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		return showJsonSuccess(model, "job已执行，请查看日志");
	}
	
	//@RequestMapping("/listJobs.xhtml")
	public String listJobs(ModelMap model,HttpServletRequest request){
		if(!checkSpecialUrl(request)){
			return showError(model, "无权限操作！");
		}
		return "listJobs.vm";
	}
	
}
