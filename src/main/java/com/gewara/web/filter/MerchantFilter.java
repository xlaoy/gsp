/** 
 */
package com.gewara.web.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.constant.UserLoginConstant;
import com.gewara.service.TokenService;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.vo.PlaceVO;

import net.spy.memcached.MemcachedClient;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Sep 16, 2013  2:02:56 PM
 */
public class MerchantFilter implements Filter{
	@Autowired
	@Qualifier("memcachedClient")
	private MemcachedClient memcachedClient;
	
	@Autowired
	@Qualifier("tokenService")
	private TokenService tokenService;
	@Autowired
	private Config config;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		String uri = req.getRequestURI();
		boolean doFilter = true;
		boolean allowedPlaceId = true;
		if (StringUtils.contains(uri, "/merchant/")){
			String token = WebUtils.getCookieValue(req, UserLoginConstant.USER_LOGIN);
			if (StringUtils.isBlank(token))
				doFilter = false;
			else{
				Object jsonData = req.getSession().getAttribute("pdata");
				if (jsonData == null){
					Object jsonDataMem = memcachedClient.get(UserLoginConstant.USER_LOGIN + token);
					if (jsonDataMem == null)
						doFilter = false;
					else{
						Map<String, Object> dataMap = (Map<String, Object>) jsonDataMem;
						String pdata = tokenService.getPlaceData(dataMap);
						HttpSession session = req.getSession();
						session.setAttribute("pdata", pdata);
						session.setAttribute("dataMap", dataMap);
						session.setAttribute("menu", dataMap.get("menuTree"));
					}
				}
			}
			
			boolean multiPart = ServletFileUpload.isMultipartContent(req);
			//check permission
			if (doFilter && !multiPart){
				String placeId = "";
				placeId = req.getParameter("placeId");
				if (StringUtils.isNotBlank(placeId)){
					Object jsonData = req.getSession().getAttribute("pdata");
					if (!containPlace(jsonData.toString(), placeId))
						allowedPlaceId = false;
				}
			}
		}
		if (doFilter && allowedPlaceId)
			chain.doFilter(request, response);
		else
			res.sendRedirect(config.getString("merchantAbs") + "login.xhtml");
		
	}
	private boolean containPlace(String data, String placeId){
		List<PlaceVO> vos  = JsonUtils.readJsonToObjectList(PlaceVO.class, data);
		for (PlaceVO vo : vos){
			if (StringUtils.equals(vo.getRecordId(), placeId))
				return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		
	}
}
