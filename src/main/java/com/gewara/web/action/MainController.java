/** 
 */
package com.gewara.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gewara.model.acl.User;
import com.gewara.model.gsp.Place;
import com.gewara.sso.model.WebModule;
import com.gewara.util.JsonUtils;
import com.gewara.vo.PlaceVO;
import com.gewara.web.menu.GBMenuDataBuilder;
import com.gewara.web.menu.MenuRepository;
import com.gewara.web.support.AclService;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 2, 2013  11:00:58 AM
 */
@Controller
public class MainController extends AnnotationController{
	
	@Autowired
	private AclService aclService;
	
	@RequestMapping("/platform/main.xhtml")
	public String mainpage(String reload, ModelMap model, HttpServletRequest request){
		User user = getLogonUser(request);
		String[] roles = StringUtils.split(user.getRolenames(), ",");
		MenuRepository repository = (MenuRepository) applicationContext.getServletContext().getAttribute(
				MenuRepository.GEWA_MENU_REPOSITORY_KEY);
		if (repository == null || "true".equals(reload)) {
			repository = new MenuRepository(aclService.getSecurityModuleList());
			applicationContext.getServletContext().setAttribute(MenuRepository.GEWA_MENU_REPOSITORY_KEY, repository);
		}
		GBMenuDataBuilder mdb = new GBMenuDataBuilder(config.getBasePath(), roles, repository);

		String menuData = mdb.getMenuData().toString();
		List<WebModule> subModules = mdb.getAllSubModule();

		model.put("menuData", menuData);
		model.put("menuTop", mdb.getMenuTop());
		model.put("user", user);
		HttpSession session = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes())).getRequest()
				.getSession();
		session.setAttribute("subModules", subModules);
		List<PlaceVO> pvos = transforToVo(daoService.getAllObjects(Place.class));
		String placeData = JsonUtils.writeObjectToJson(pvos);
		model.put("pdata", placeData);
		return "main.vm";
	}
}
