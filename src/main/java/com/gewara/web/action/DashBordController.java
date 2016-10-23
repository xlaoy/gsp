/** 
 */
package com.gewara.web.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Oct 10, 2013  2:42:37 PM
 */
@Controller
public class DashBordController {
	@RequestMapping("/platform/dashBord/dashBord.xhtml")
	public String dashBord(){
		return "/dashboard/dashbord.vm";
	}
}
