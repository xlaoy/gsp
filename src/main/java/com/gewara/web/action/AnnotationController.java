package com.gewara.web.action;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;

import com.gewara.Config;
import com.gewara.commons.sign.Sign;
import com.gewara.component.CityInfoHolder;
import com.gewara.constant.UserLoginConstant;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.service.DaoService;
import com.gewara.service.StatusTrackerService;
import com.gewara.service.TokenService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.MiscUtil;
import com.gewara.util.WebLogger;
import com.gewara.util.WebUtils;
import com.gewara.vo.PlaceVO;
import com.gewara.web.support.GewaVelocityView;
import com.gewara.web.util.PageUtil;

import net.spy.memcached.MemcachedClient;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class AnnotationController implements ApplicationContextAware {
	@Autowired
	@Qualifier("memcachedClient")
	protected MemcachedClient memcachedClient;
	@Autowired
	@Qualifier("tokenService")
	private TokenService tokenService;
	@Autowired
	@Qualifier("monitorService")
	protected MonitorService monitorService;
	
	@Autowired
	@Qualifier("cityInfoHolder")
	protected CityInfoHolder cityInfoHolder; 
	@Autowired
	@Qualifier("statusTrackerService")
	protected StatusTrackerService statusTrackerService;

	public static final String SUCCESS_MESSAGES_KEY = "successMsgs";
	public static final String ERROR_MESSAGES_KEY = "errorMsgs";

	protected final transient GewaLogger dbLogger = WebLogger.getLogger(getClass());
	@Autowired
	@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	@Autowired
	@Qualifier("daoService")
	protected DaoService daoService;
	@Autowired
	protected Config config;
	

	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	public Timestamp defaultTimeForPlace(SettleConfig sc, Timestamp t) {
		return MiscUtil.defaultTimeForPlace(sc, t);

	}

	/**
	 * {"id":424,"type":"cinema","enabled":false,"username":"shangjia3",
	 * "companyid":432,"rolenames":"",
	 * "cinemaid":"32761850,6,4699299,1277473,10,9,2",
	 * "cinemaIdList":[32761850,6,4699299,1277473,10,9,2],
	 * "usertype":"merchant", "authorities":[], "member":false,
	 * "rolesString":"",
	 * "cinemaList":[{"id":32761850,"citycode":"310000","companyid"
	 * :423,"cinemaname"
	 * :"test"},{"address":"淮海中路99号（大上海时代广场6楼）","id":6,"citycode"
	 * :"310000","companyid"
	 * :9585,"cinemaname":"万裕国际影城-淮海店"},{"address":"武宁路101号我格广场4F"
	 * ,"id":4699299,"citycode"
	 * :"310000","companyid":9489,"cinemaname":"上海博纳银兴国际影城"
	 * },{"address":"海宁路330号"
	 * ,"id":1277473,"citycode":"310000","companyid":9433,"cinemaname"
	 * :"上海星美国际影城"
	 * },{"address":"虹桥路1号港汇广场六楼","id":10,"citycode":"310000","companyid"
	 * :9527,"cinemaname"
	 * :"永华电影城"},{"address":"兴业路123弄新天地南里6号5楼（新天地广场内）","id":9,"citycode"
	 * :"310000"
	 * ,"companyid":9621,"cinemaname":"UME新天地国际影城"},{"address":"西藏中路290号来福士广场"
	 * ,"id"
	 * :2,"citycode":"310000","companyid":62,"cinemaname":"和平影都"}],"company"
	 * :{"name"
	 * :"shangjia3","id":432,"cinemaids":"32761850,6,4699299,1277473,10,9,2"
	 * ,"cinemaIdList"
	 * :[32761850,6,4699299,1277473,10,9,2]},"roleList":[],"accountNonExpired"
	 * :true,"accountNonLocked":true,"credentialsNonExpired":true}
	 * 
	 * @param request
	 * @param response
	 * @param token
	 * @param dataJson
	 * @throws UnsupportedEncodingException 
	 */
	protected void userLogined(HttpServletRequest request,
			HttpServletResponse response, String token, String dataJson) throws UnsupportedEncodingException {
		if (StringUtils.isNotBlank(dataJson))
			dataJson = new String(dataJson.getBytes(), "UTF-8");
		WebUtils.addCookie(response, UserLoginConstant.USER_LOGIN, token, "/gsp/merchant/",
				UserLoginConstant.MAX_SECOND);
		String pdata = "";
		if (StringUtils.isNotBlank(dataJson)) {
			HttpSession session = request.getSession();
			Map<String, Object> dataMap = JsonUtils.readJsonToMap(dataJson);
			memcachedClient.set(UserLoginConstant.USER_LOGIN + token,
								UserLoginConstant.MAX_SECOND, dataMap); // user data.
			pdata = tokenService.getPlaceData(dataMap);
			session.setAttribute("dataMap", dataMap);
			session.setAttribute("pdata", pdata);
			String menuTreeString = new String(dataMap.get("menuTree").toString().getBytes());
			session.setAttribute("menu", JsonUtils.readJsonToMap(menuTreeString));
			dbLogger.warn(dataMap.toString());
			dbLogger.warn("user login:" + dataMap.get("username") + pdata);
			
			Map<String, String> entry = new LinkedHashMap<String, String>();
			entry.put("logtype", "gspmerchant" );
			String curtime = DateUtil.formatTimestamp(new Timestamp(System.currentTimeMillis()));
			entry.put("addtime", curtime);
			entry.put("adddate", curtime.substring(0, 10));
			entry.put("username", dataMap.get("username").toString());
			entry.put("cinemaIdList", dataMap.get("cinemaIdList").toString());
			entry.put("pdata", pdata);
			monitorService.addSysLog(SysLogType.userlogin, entry);
		}
	}

	protected String getMerchantLoginToken(HttpServletRequest request) {
		return WebUtils.getCookieValue(request, UserLoginConstant.USER_LOGIN);
	}

	protected Long getMerchantUserId(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Object> dataMap = (Map<String, Object>) session
				.getAttribute("dataMap");

		return Long.valueOf(dataMap.get("id").toString());
		//return 1111l;
	}

	protected String getMerchantUserName(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Map<String, Object> dataMap = (Map<String, Object>) session
				.getAttribute("dataMap");
		return dataMap.get("username").toString();
		//return "test";
	}

	protected boolean isPlaceAllowed(String placeId, HttpServletRequest request) {
		Object jsonData = request.getSession().getAttribute("pdata");
		if (jsonData == null)
			return false;
		List<PlaceVO> vos = JsonUtils.readJsonToObjectList(PlaceVO.class,
				jsonData.toString());
		for (PlaceVO vo : vos) {
			if (StringUtils.equals(vo.getRecordId(), placeId))
				return true;
		}
		return false;

	}

	/**
	 * @param request
	 * @return
	 */
	protected List<String> getAllowedPlaceIds(HttpServletRequest request) {
		String d = (String) request.getSession().getAttribute("pdata");
		List<PlaceVO> vos = JsonUtils.readJsonToObjectList(PlaceVO.class, d);
		return BeanUtil.getBeanPropertyList(vos, "recordId", true);
		/*List<String> placeIds = new ArrayList<String>();
		placeIds.add("1,TICKET");
		return placeIds;*/
	}
	
	/**
	 * @param request
	 * @return
	 */
	protected String defaultPlaceId(HttpServletRequest request) {
		List<String> placeIds = getAllowedPlaceIds(request);
		if (CollectionUtils.isNotEmpty(placeIds) && placeIds.size() == 1)
			return placeIds.get(0);
		return null;
	}

	protected WebApplicationContext applicationContext;

	protected final PageUtil getScriptPageUtil(int count, int rowsPerpage,
			int pageNo, Map<String, Object> params) {
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, null);
		pageUtil.initPageInfo(params);
		return pageUtil;
	}

	protected final User getLogonUser(HttpServletRequest request) {
		dbLogger.warn(request.getHeader("x-forwarded-for"));
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		if (auth == null)
			return null;
		if (auth.isAuthenticated() && !auth.getName().equals("anonymous")) {// 登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			// refresh(user);
			if (user instanceof User)
				return (User) user;
		}
		return null;
	}

	protected final String showError(ModelMap model, String errMsg) {
		model.put(ERROR_MESSAGES_KEY, errMsg);
		return "redirect:/showResult.xhtml";
	}

	protected final String showError_NOT_LOGIN(ModelMap model) {
		model.put(ERROR_MESSAGES_KEY, "请先登录!");
		return "redirect:/showResult.xhtml";
	}

	protected final String showMessage(ModelMap model, String msg) {
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "redirect:/showResult.xhtml";
	}

	protected final String showMessage(ModelMap model, List<String> msgList) {
		model.put(SUCCESS_MESSAGES_KEY,
				StringUtils.join(msgList.toArray(), "@@"));
		return "redirect:/showResult.xhtml";
	}

	protected final String showMessageAndReturn(ModelMap model,
			HttpServletRequest request, String msg) {
		if (request != null) {
			model.put("returnUrl", request.getHeader("Referer"));
		}
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "redirect:/showResult.xhtml";
	}

	protected final String forwardMessage(ModelMap model, String msg) {
		model.put(SUCCESS_MESSAGES_KEY, msg);
		return "showResult.vm";
	}

	protected final String forwardMessage(ModelMap model, List<String> msgList) {
		model.put("msgList", msgList);
		return "showResult.vm";
	}

	/**
	 * @param model
	 * @param retval
	 * @return
	 */
	protected final String showJsonSuccess(ModelMap model) {
		return showJsonSuccess(model, "");
	}

	protected final String showJsonSuccess(ModelMap model, String retval) {
		return showJsonSuccess(model, retval, "data");
	}

	protected final String showJsonSuccess(ModelMap model, Map jsonMap) {
		return showJsonSuccess(model, jsonMap, "data");
	}

	protected final String showJsonSuccess(ModelMap model, Map jsonMap,
			String jsname) {
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}

	protected final String showJsonSuccess(ModelMap model, String retval,
			String jsname) {
		Map jsonMap = new HashMap();
		jsonMap.put("success", true);
		jsonMap.put("retval", retval);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}

	protected final String showDirectJson(ModelMap model, String jsonStr) {
		model.put("jsonStr", jsonStr);
		return "common/directJson.vm";
	}

	protected final String showJsonError(ModelMap model, String msg) {
		return showJsonError(model, msg, "data");
	}

	protected final String showJsonError(ModelMap model, Map jsonMap) {
		return showJsonError(model, jsonMap, "data");
	}

	protected final String showJsonError(ModelMap model, Map jsonMap,
			String jsname) {
		if (jsonMap == null)
			jsonMap = new HashMap();
		jsonMap.put("success", false);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}

	protected final String showJsonError(ModelMap model, String msg,
			String jsname) {
		Map jsonMap = new HashMap();
		jsonMap.put("success", false);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		model.put("jsname", jsname);
		return "common/json.vm";
	}

	protected final String showJsonError_CAPTCHA_ERROR(ModelMap model) {
		return showJsonError(model, "验证码错误！", "data");
	}

	protected final String showJsonError_NOT_LOGIN(ModelMap model) {
		return showJsonError(model, "您还没有登录，请先登录！", "data");
	}

	protected final String showJsonError_NORIGHTS(ModelMap model) {
		return showJsonError(model, "您没有权限！", "data");
	}

	protected final String showJsonError_REPEATED(ModelMap model) {
		return showJsonError(model, "不能重复操作！", "data");
	}

	protected final String showJsonError_NOT_FOUND(ModelMap model) {
		return showJsonError(model, "未找到相关数据！", "data");
	}

	protected final String showJsonError_DATAERROR(ModelMap model) {
		return showJsonError(model, "数据有错误！请刷新重试！", "data");
	}

	protected final String showJsonError_PARAMSERROR(ModelMap model) {
		return showJsonError(model, "参数错误！", "data");
	}

	protected final String showJsonError_BLACK_LIST(ModelMap model) {
		return showJsonError(model, "你在黑名单中！如有疑问请联系格瓦拉客服！", "data");
	}

	protected final String showJsonError_SOFAST(ModelMap model) {
		return showJsonError(model, "提交内容频率不能太快！", "data");
	}

	protected final String showJsonError_KEYWORD(ModelMap model) {
		return showJsonError(model, "你发表的帖子包含“敏感关键词”，通过管理员审核后显示!", "data");
	}

	protected final String showJsonInfo(ModelMap model, String keys,
			Object... values) {
		Map jsonMap = new HashMap();
		String[] keyList = keys.split(",");
		for (int i = 0, len = Math.min(keyList.length, values.length); i < len; i++) {
			jsonMap.put(keyList[i], values[i]);
		}
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		model.put("jsname", "data");
		return "common/json.vm";
	}

	/**
	 * @param model
	 * @param msg
	 * @param returnUrl
	 *            /home/xxx.xhtm
	 * @return
	 */
	protected final String alertMessage(ModelMap model, String msg) {
		return alertMessageError(model, msg);
	}

	protected final String alertMessageSuccess(ModelMap model, String msg) {
		Map jsonMap = new HashMap();
		jsonMap.put("success", true);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}

	protected final String alertMessageError(ModelMap model, String msg) {
		Map jsonMap = new HashMap();
		jsonMap.put("success", false);
		jsonMap.put("msg", msg);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}

	protected final String alertMessage(ModelMap model, String msg,
			String returnUrl) {
		Map jsonMap = new HashMap();
		jsonMap.put("msg", msg);
		jsonMap.put("returnUrl", returnUrl);
		model.put("jsonMap", jsonMap);
		return "showMessage.vm";
	}

	protected final String goBack(ModelMap model, String msg) {
		Map jsonMap = new HashMap();
		jsonMap.put("msg", msg);
		jsonMap.put("goback", true);
		model.put("jsonMap", jsonMap);
		return "showMessage1.vm";
	}

	protected final String show404(ModelMap model, String msg) {
		model.put("msg", msg);
		return "404.vm";
	}

	protected final String getRealPath(String filename) {
		return applicationContext.getServletContext().getRealPath(filename);
	}

	protected final String getRealPath(String filename, boolean addSplash) {
		String result = applicationContext.getServletContext().getRealPath(
				filename);
		if (addSplash) {
			if (!result.endsWith("/") && !result.endsWith("\\"))
				result += "/";
		}
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = (WebApplicationContext) applicationContext;
	}

	private final List<String> VALID_REPORT_FORMAT = Arrays.asList("csv",
			"html", "pdf", "xls");

	protected final String showReportView(ModelMap model, String viewname) {
		return showReportView(model, "pdf", viewname);
	}

	protected final String REPORT_DATA_KEY = "REPORT_DATA";// 报表循环数据

	protected final String showReportView(ModelMap model, String format,
			String viewname) {
		if (format == null || !VALID_REPORT_FORMAT.contains(format)) {
			dbLogger.warn("没有指定报表格式或格式不正确[" + format + "]：" + viewname
					+ ", 使用默认的pdf格式！");
			format = "pdf";
		}
		model.put("format", format);
		return viewname;
	}

	private final String supportOtherCharset = "gbk,gb2312";

	protected void useCharset(ModelMap model, String charset) {
		if (StringUtils.containsIgnoreCase(supportOtherCharset, charset)) {
			model.put(GewaVelocityView.USE_OTHER_CHARSET, charset);
		}
	}

	protected final String showRedirect(String path, ModelMap model) {
		if (StringUtils.startsWith(path, "/"))
			path = path.substring(1);
		StringBuilder targetUrl = new StringBuilder(path);
		appendQueryProperties(targetUrl, model, "utf-8");
		model.put("redirectUrl", targetUrl.toString());
		return "tempRedirect.vm";
	}

	private void appendQueryProperties(StringBuilder targetUrl, ModelMap model,
			String encoding) {
		boolean first = (targetUrl.indexOf("?") < 0);
		for (Map.Entry<String, Object> entry : queryProperties(model)
				.entrySet()) {
			Object rawValue = entry.getValue();
			Iterator valueIter = null;
			if (rawValue != null && rawValue.getClass().isArray()) {
				valueIter = Arrays.asList(ObjectUtils.toObjectArray(rawValue))
						.iterator();
			} else if (rawValue instanceof Collection) {
				valueIter = ((Collection) rawValue).iterator();
			} else {
				valueIter = Collections.singleton(rawValue).iterator();
			}
			while (valueIter.hasNext()) {
				Object value = valueIter.next();
				if (first) {
					targetUrl.append('?');
					first = false;
				} else {
					targetUrl.append('&');
				}
				String encodedKey = urlEncode(entry.getKey(), encoding);
				String encodedValue = (value != null ? urlEncode(
						value.toString(), encoding) : "");
				targetUrl.append(encodedKey).append('=').append(encodedValue);
			}
		}
	}

	private Map<String, Object> queryProperties(Map<String, Object> model) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (isEligibleProperty(entry.getValue())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private boolean isEligibleProperty(Object value) {
		if (value == null)
			return false;
		if (isEligibleValue(value))
			return true;

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (length == 0) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		if (value instanceof Collection) {
			Collection coll = (Collection) value;
			if (coll.isEmpty()) {
				return false;
			}
			for (Object element : coll) {
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	private String urlEncode(String input, String charsetName) {
		try {
			return (input != null ? URLEncoder.encode(input, charsetName)
					: null);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private boolean isEligibleValue(Object value) {
		return (value != null && BeanUtils.isSimpleValueType(value.getClass()));
	}

	protected void download(String downloadType, String fileName,
			HttpServletResponse response) {
		if (StringUtils.equals(downloadType, "xls")) {
			response.setContentType("application/xls");
		} else if (StringUtils.equals(downloadType, "jpg")) {
			response.setContentType("image/jpeg");
		} else {
			response.setContentType("application/x-download");
		}
		fileName = StringUtils.isBlank(fileName) ? "gewara"
				+ DateUtil.format(new Date(), "yyMMdd_hhmmss") : fileName;
		try {
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String(fileName.getBytes("gb2312"), "ISO8859-1")
					+ "." + downloadType);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void download(String downloadType, HttpServletResponse response) {
		download(downloadType, "", response);
	}

	/**
	 * 只接受Get提交的参数跳转
	 * 
	 * @param targetUrl
	 * @param request
	 * @param model
	 * @return
	 */
	protected String gotoLogin(String targetUrl, HttpServletRequest request,
			ModelMap model) {
		try {
			if (StringUtils.isNotBlank(targetUrl)) {
				String queryStr = request.getQueryString();// 只接受Get方法
				String paramStr = "";
				if (StringUtils.isNotBlank(queryStr)
						&& StringUtils.length(queryStr) < 300) {// 300以下的转发
					paramStr = URLDecoder.decode(queryStr, "utf-8");
				}
				targetUrl += targetUrl.indexOf('?') > 0 ? "&" + paramStr : "?"
						+ paramStr;

				return showRedirect(
						"login.xhtml?TARGETURL="
								+ URLEncoder.encode(targetUrl, "utf-8"), model);
			}
		} catch (UnsupportedEncodingException e) {// ignore
		}
		return showRedirect("login.xhtml", model);
	}

	/**
	 * @param allObjects
	 * @return
	 */
	protected List<PlaceVO> transforToVo(List<Place> allObjects) {
		return tokenService.transforToVo(allObjects);
	}
	protected boolean checkSpecialUrl(HttpServletRequest request){
		User logonUser = getLogonUser(request);
		if (logonUser == null){
			dbLogger.warn(WebUtils.getHeaderStr(request));
			return false;
		}
		dbLogger.warn(logonUser.getUsername() + "::" + WebUtils.getHeaderStr(request));
		String userName = logonUser.getUsername();
		if("yijun.zhang".equals(userName) 
			|| "matrix".equals(userName)
			|| "wenhui.fang".equals(userName)
			|| "mengyu.yin".equals(userName)){
			return true;
		}
		return false;
	}
	protected Map<String, String> getRemoteMerchantInfo(String operator) {
//		com.gewara.cinemapro.cinemauser.otherinfo.get 	获取商户财务数据
//		com.gewara.cinemapro.cinemauser.otherinfo.save 	保存商户财务数据
		String method = "com.gewara.cinemapro.cinemauser.otherinfo.get";
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("method", method);
		params.put("appkey", "gewara_pro");
		params.put("timestamp", DateUtil.getCurFullTimestampStr());
		params.put("format", "json");
		params.put("v", "1.0");
		params.put("username", operator);
		params.put("signmethod", "MD5");
		String sign = Sign.signMD5(params, config.getString("openApiPrikey"));
		params.put("sign", sign);
		String url = config.getString("openApiUrl");
		HttpResult result = HttpUtils.getUrlAsString(url, params);
		if (result.isSuccess()){
			Map resultMap = JsonUtils.readJsonToMap(result.getResponse());
			if (resultMap.get("success") != null){
				return JsonUtils.readJsonToMap((String) resultMap.get("retval"));
			}
		}
		dbLogger.error(method + ":" + result.getResponse());
		return null;
	}
	
	protected StatusTracker getMerchantConfirmST(Long settleId){
		StatusTracker lastMerchant = statusTrackerService.getlastStatusTrackerByStatus(settleId, 
														CheckBillStatusEnums.GEWACONFIRMED.getStatus(), 
														CheckBillStatusEnums.WAITINGPAY.getStatus());
		
		return lastMerchant;
	}
	public StatusTracker getGewaPayST(Long settleId){
		StatusTracker lastGewa = statusTrackerService.getlastStatusTrackerByStatus(settleId, 
														CheckBillStatusEnums.WAITINGPAY.getStatus(), 
														CheckBillStatusEnums.SETTLED.getStatus());
		
		return lastGewa;
	}
	
}
