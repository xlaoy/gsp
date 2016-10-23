package com.gewara.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gewara.Config;
import com.gewara.constant.SettleConstant;
import com.gewara.untrans.monitor.ZookeeperService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.GewaLogger;
import com.gewara.util.ObjectId;
import com.gewara.util.WebLogger;

/**
 * <p>
 * StartupListener class used to initialize and database settings and populate
 * any application-wide drop-downs.
 * 
 * <p>
 * Keep in mind that this listener is executed outside of
 * OpenSessionInViewFilter, so if you're using Hibernate you'll have to
 * explicitly initialize all loaded data at the Dao or service level to avoid
 * LazyInitializationException. Hibernate.initialize() works well for doing
 * this.
 * 
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class StartupListener extends ContextLoaderListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		//根据环境加载配置文件
		String SPRING_CONFIG_KEY = "contextConfigLocation";
		String hostName = Config.getHostname();
		String configPath = "classpath:spring/" + (SettleConstant.hostname.equals(hostName) ? "config.local37.xml" : "config.remote.xml");
		String taskPath = "classpath:spring/" + (SettleConstant.hostname.equals(hostName) ? "task.local37.xml" : "task.remote.xml");
		String[] remoteConfig = new String[]{
				configPath,
				"classpath:spring/jms.remote.xml",
				"classpath:spring/hibernate.remote.xml",
				"classpath:spring/camel.remote.xml",
				"classpath:spring/appContext-*.xml",
				"classpath:spring/dubbo.remote.xml",
				//"classpath:spring/task.remote.xml"
				taskPath
		};
		String[] localConfig = new String[]{
				"classpath:spring/property.dev.xml",
				"classpath:spring/config.local.xml",
				"classpath:spring/jms.local.xml",
				"classpath:spring/hibernate.local.xml",
				"classpath:spring/camel.local.xml",
				"classpath:spring/appContext-*.xml",
				"classpath:spring/task.local.xml",
				"classpath:spring/dubbo.local.xml"
		};
		String SPRING_CONFIG_VALUE = "";
		
		String ip = Config.getServerIp();
		
		//String alipay = "";
		GewaLogger logger = WebLogger.getLogger(StartupListener.class);
		if(GewaIpConfig.isGewaInnerIp(ip)){//remote
			System.setProperty("GEWACONFIG", "REMOTE");
			SPRING_CONFIG_VALUE = StringUtils.join(remoteConfig, ",");
			//ConfigProperties.init("config.remote.properties");
			logger.warn("Config Using REMOTE:" + SPRING_CONFIG_VALUE);
		}else{
			System.setProperty("GEWACONFIG", "LOCAL");
			SPRING_CONFIG_VALUE = StringUtils.join(localConfig, ",");
			//ConfigProperties.init("config.local.properties");
			logger.warn("Config Using LOCAL:" + SPRING_CONFIG_VALUE);
		}
		//context.setInitParams(SPRING_CONFIG_KEY, SPRING_CONFIG_VALUE);
		ServletContext context = event.getServletContext();
		context.setInitParameter(SPRING_CONFIG_KEY, SPRING_CONFIG_VALUE);
		super.contextInitialized(event);
		
		//初始化其他信息
		WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
		//数据库里加入一条id为0的user，作为匿名用户
		logger.warn("INIT GLOCAL PAGE-TOOLS: " + Config.getPageTools());
		logger.warn("INIT OBJECTID:" + ObjectId.get().toString());
		ZookeeperService keeperService = ctx.getBean(ZookeeperService.class);
		String serverPath = "/server/" + Config.SYSTEMID + "/s";
		try {
			keeperService.registerNode(serverPath, Config.getServerIp() + "|" + Config.getHostname() + "|" + DateUtil.formatTimestamp(System.currentTimeMillis()));
			logger.warn("register server on node:" + Config.getHostname()+ "---->" + Config.getServerIp());
		} catch (Exception e) {
			logger.warn("", e);
		}
	}
}
