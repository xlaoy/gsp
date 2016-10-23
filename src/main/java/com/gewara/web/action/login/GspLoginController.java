/** 
 */
package com.gewara.web.action.login;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.UserLoginConstant;
import com.gewara.service.TokenService;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 13, 2013  11:21:00 AM
 */
@Controller
public class GspLoginController extends AnnotationController{
	@Autowired
	@Qualifier("tokenService")
	private TokenService tokenService;
	
	@RequestMapping("/api/login.xhtml")
	public String login(String token, String s,  ModelMap model, HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException{
		if (StringUtils.isBlank(token))
			return showJsonError(model, "ÇëµÇÂ½!");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", token);
		String url = config.getString("merchantAbs") + config.getString(s);
		HttpResult result = HttpUtils.postUrlAsString(url, params);
		if (!result.isSuccess())
			return showJsonError(model, "ÍøÂç´íÎó¡£");
		String dataJson = result.getResponse();
		if (StringUtils.contains(dataJson, "ÑéÖ¤Ê§°Ü"))
			return showJsonError(model, "ÑéÖ¤tokenÊ§°Ü");
		userLogined(request, response, token, dataJson);  
		return showRedirect("/merchant/settlement/settlementbills.xhtml", model);
	}
	@RequestMapping("/api/getToken.xhtml")
	@ResponseBody
	public String getToken(String userFlag, String ip, HttpServletRequest request){
		 GewaIpConfig.filterIp(WebUtils.getRemoteIp(request));
		 String token = tokenService.getToken(userFlag, ip);
		 memcachedClient.set(token, UserLoginConstant.expTime, ip);         //user token.
		 return token;
	}
}
