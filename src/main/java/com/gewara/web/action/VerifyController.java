/** 
 */
package com.gewara.web.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Oct 9, 2013  3:06:21 PM
 */
@Controller
public class VerifyController {
	
	@RequestMapping("/platform/verify/verify.xhtml")
	public String verify(){
		return "/verify/verify.vm";
	}
}
