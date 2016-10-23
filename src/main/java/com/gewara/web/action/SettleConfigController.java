/** 
 */
package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettleConfigUpd;
import com.gewara.model.gsp.SysData;
import com.gewara.service.SettleConfigService;
import com.gewara.service.SysDataService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.web.util.PageUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 2, 2013  11:11:54 AM
 */
@Controller
public class SettleConfigController extends AnnotationController{
	
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("settleConfigService")
	private SettleConfigService settleConfigService;
	
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	@RequestMapping("/platform/settleconfig/settleconfigs.xhtml")
	public String querySettleConfigs(String placeId, Long relateId, String payVenderNo, String venderName, 
			String owner, Long belong_to, String xls, Integer pageNo, Integer pageSize, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response){
		model.put("megermap", sysDataService.getSysDataMap(SettleConstant.MEGERPAYVENDER));
		
		pageNo = pageNo == null ? 0 : pageNo;
		if(StringUtils.isNotBlank(xls)) {
			pageSize = pageSize == null ? 5000 : pageSize;
		} else {
			pageSize = pageSize == null ? 10 : pageSize;
		}
		
		List<Map<String, Object>> maplist = new ArrayList<>();
		
		List<Object> params = new ArrayList<Object>();
		
		String countsql = "select count(1) as mcount ";
		String mapsql = "select  p.record_id, p.place_id, p.open_type, p.belong_to, p.name, p.pcid, p.owner, sc.settle_cycle, sc.offset_value, sc.settle_base, sc.settle_time, sc.first_settle, "
				+ "sc.zhangtao_no, sc.vender_no, sc.vender_name, sc.pay_vender_no, sc.pay_vender_name, sc.is_goods_settle, sc.status, sc.play_type ";
		
		StringBuilder sb = new StringBuilder("");
		sb.append("from place p left outer join settle_config sc on p.record_id = sc.record_id where 1 = 1 ");
		if(StringUtils.isNotBlank(placeId)) {
			sb.append("and p.record_id = ? ");
			params.add(placeId);
		}
		if(relateId != null) {
			sb.append("and p.place_id = ? ");
			params.add(relateId);
		}
		if(StringUtils.isNotBlank(owner)) {
			sb.append("and p.owner = ? ");
			params.add(owner);
		}
		if(belong_to != null) {
			sb.append("and p.belong_to = ? ");
			params.add(belong_to);
		}
		if(StringUtils.isNotBlank(venderName)) {
			sb.append("and sc.vender_name like '%' || ? || '%' ");
			params.add(venderName);
		}
		if(StringUtils.isNotBlank(payVenderNo)) {
			sb.append("and sc.pay_vender_no = ? ");
			params.add(payVenderNo);
		}
		if(StringUtils.isBlank(placeId) && relateId == null) {
			sb.append("and p.open_type <> 'WP' ");
		}
		String ordersql = "order by p.is_config asc limit ? offset ? ";
		
		Map<String, Object> countmap = jdbcTemplate.queryForMap(countsql + sb.toString(), params.toArray());
		Integer count = Integer.valueOf(countmap.get("mcount").toString());
		if(count > 0) {
			params.add(pageSize);
			params.add(pageNo * pageSize);
			maplist = jdbcTemplate.queryForList(mapsql + sb.toString() + ordersql, params.toArray());
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo,config.getBasePath() + "platform/settleconfig/settleconfigs.xhtml", true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("placeId", placeId);
			m.put("relateId", relateId);
			m.put("owner", owner);
			m.put("belong_to", belong_to);
			m.put("payVenderNo", payVenderNo);
			m.put("venderName", venderName);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		
		model.put("maplist", maplist);
		if(StringUtils.isNotBlank(xls)) {
			download("xls", "placesettle-config", response);
			return "/settleconfig/configs_xls.vm";
		} else {
			//修改管理人
			String gewauser = getLogonUser(request).getUsername();
			SysData admin = sysDataService.getSysData(gewauser, SettleConstant.RESETADJUST, String.class);
			if(admin != null) {
				model.put("resetadjust", SettleConstant.COMM_Y);
			} else {
				model.put("resetadjust", SettleConstant.COMM_N);
			}
			return "/settleconfig/configs.vm";
		}
	}
	
	@RequestMapping("/platform/settleconfig/removeConfig.xhtml")
	public String removeConfig(String recordId, ModelMap model, HttpServletRequest request){
		if (recordId == null)
			return showJsonError(model, "数据不正确");
		
		SettleConfig sc = daoService.getObject(SettleConfig.class, recordId);
		if (sc == null)
			return showJsonError(model, "没有找到关联的结算配置");
		
		ChangeEntry ce = new ChangeEntry(sc);
		long uid = getLogonUser(request).getId();
		ResultCode result =  settleConfigService.removeSettleConfig(sc, uid);
		
		if (!result.isSuccess())
			return showJsonError(model, result.getMsg());
		
		monitorService.saveChangeLog(uid, SettleConfig.class, recordId, ce.getChangeMap(result.getRetval()));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/settleconfig/approveConfig.xhtml")
	public String approveConfig(String recordId, ModelMap model, HttpServletRequest request){
		if (recordId == null){
			return showJsonError(model, "数据不正确");
		}
		
		final SettleConfigUpd scu = daoService.getObject(SettleConfigUpd.class, recordId);
		if (scu == null){
			return showJsonError(model, "没有找到关联的结算配置或该配置已经通过审核.");
		}
		Place place = daoService.getObject(Place.class, recordId);
		if(place == null) {
			return showJsonError(model, "没有找到关联的影院.");
		}
		doApproveConfig(scu, request);
		return showJsonSuccess(model);
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: doApproveConfig  
	 * @Description: 审核结算配置操作
	 * @param scu
	 * @return void
	 * @throws
	 */
	private void doApproveConfig(SettleConfigUpd scu,HttpServletRequest request) {
		String recordId = scu.getRecordId();
		final SettleConfig oldSettleConfig = daoService.getObject(SettleConfig.class, recordId);
		final SettleConfig newSettleConfig = new SettleConfig();
		User logonUser = getLogonUser(request);
		BeanUtil.copyProperties(newSettleConfig, scu);
		newSettleConfig.setStatus("Y");
		newSettleConfig.setApproveUser(logonUser.getUsername());
		
		Place place = daoService.getObject(Place.class, recordId);
		if(StringUtils.isEmpty(place.getOwner())) {
			if(oldSettleConfig == null) {
				place.setOwner(getLogonUser(request).getUsername());
			} else if("system".equals(oldSettleConfig.getApplyUser())) {
				place.setOwner(getLogonUser(request).getUsername());
			} else if(StringUtils.isEmpty(oldSettleConfig.getApplyUser())) {
				place.setOwner(getLogonUser(request).getUsername());
			}
			daoService.saveObject(place);
		}
		
		newSettleConfig.setOpenType(place.getOpenType());
		daoService.saveObject(newSettleConfig);
		//移除SCU
		daoService.removeObject(scu);
		new Thread(new Runnable() {
			@Override
			public void run() {
				settleConfigService.processBillAfterModifySettleConfig(newSettleConfig,oldSettleConfig);
			}
		}).start();
	}

	@RequestMapping("/platform/settleconfig/queryUnApprovedConfig.xhtml")
	public String queryUnApprovedConfig(String openType,String placeId, Integer pageNo, Integer pageSize, ModelMap model){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		
		int totalNumber = settleConfigService.countingUnApprovedConfig(openType,placeId);
		if (totalNumber <= 0)
			return "/settleconfig/unApprovedConfig.vm";
		
		List<SettleConfigUpd> scs = settleConfigService.queryUnApprovedConfig(openType,placeId, pageNo, pageSize);
		List<String> placeIds = BeanUtil.getBeanPropertyList(scs, "recordId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, placeIds);
		model.put("scs", scs);
		model.put("pm", placeMap);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo,
											config.getBasePath() + "platform/settleconfig/queryUnApprovedConfig.xhtml",
											true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("openType", openType);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		return "/settleconfig/unApprovedConfig.vm";
	}
	
	@RequestMapping("/platform/settleconfig/loadConfigDetails.xhtml")
	public String loadConfigDetails(String recordId, ModelMap model){
		if (StringUtils.isNotBlank(recordId)){
			SettleConfig settleconfig = daoService.getObject(SettleConfig.class, recordId);
			Place place = daoService.getObject(Place.class, recordId);
			model.put("config", settleconfig);
			model.put("place", place);
		}
		//return "/settleconfig/configDetails.vm";
		return "/settleconfig/configDetailsExtend.vm";
	}
	
	@RequestMapping("/platform/settleconfig/loadConfigUpdDetails.xhtml")
	public String loadConfigUpdDetails(String recordId, ModelMap model){
		if (StringUtils.isNotBlank(recordId)){
			SettleConfigUpd settleconfig = daoService.getObject(SettleConfigUpd.class, recordId);
			Place place = daoService.getObject(Place.class, recordId);
			model.put("config", settleconfig);
			model.put("place", place);
		}
		//return "/settleconfig/configDetails.vm";
		return "/settleconfig/configDetailsExtend.vm";
	}
	
	@RequestMapping("/platform/settleconfig/modifyConfigDetails.xhtml")
	public String modifyConfigDetails(String recordId, String settleCycle, Integer settleValue, Integer offsetValue, 
										String settleBase, String settleTime, Timestamp firstSettle, String venderNo,String isGoodsSettle, 
										String playType, String payVenderNo, String payVenderName,
										String venderName, String zhangtaoNo, ModelMap model, HttpServletRequest request){
		Place place = daoService.getObject(Place.class, recordId);
		if (place == null)
			return showJsonError(model, "没有找到关联数据");
		
		if(StringUtils.isBlank(playType)) {
			playType = SettleConstant.COMM_N;
		}
		
		User logonUser = getLogonUser(request);
		SettleConfig settleConfig = daoService.getObject(SettleConfig.class, recordId);
		SettleConfigUpd settleConfigUpd = null;
		ChangeEntry ce = new ChangeEntry(settleConfig);
		if(null != settleConfig && "Y".equals(settleConfig.getStatus())){
			settleConfigUpd = new SettleConfigUpd();
			BeanUtil.copyProperties(settleConfigUpd, settleConfig);
			/*****************结算配置修改start******************/
			settleConfigUpd.setSettleCycle(settleCycle);
			settleConfigUpd.setSettleValue(settleValue);
			settleConfigUpd.setOffsetValue(offsetValue);
			settleConfigUpd.setSettleBase(settleBase);
			settleConfigUpd.setSettleTime(settleTime);
			settleConfigUpd.setFirstSettle(firstSettle);
			/*****************结算配置修改end******************/
			settleConfigUpd.setIsGoodsSettle(isGoodsSettle);
			settleConfigUpd.setVenderName(venderName);
			settleConfigUpd.setVenderNo(venderNo);
			settleConfigUpd.setZhangtaoNo(zhangtaoNo);
			settleConfigUpd.setOpenType(place.getOpenType());
			settleConfigUpd.setApplyUser(logonUser.getUsername());
			settleConfigUpd.setPlayType(playType);
			settleConfigUpd.setPayVenderNo(payVenderNo);
			settleConfigUpd.setPayVenderName(payVenderName);
			settleConfigUpd.setStatus("N");
			settleConfigUpd.setOperateType(SettleConstant.OPERATE_TYPE_MODIFY);
		}else{
			settleConfigUpd = new SettleConfigUpd();
			if (settleConfig == null){
				settleConfigUpd.setStatus("N");
				settleConfigUpd.setOpenType(place.getOpenType());
				settleConfigUpd.setApplyUser(logonUser.getUsername());
				settleConfigUpd.setIsGoodsSettle("N");
				settleConfigUpd.setOperateType(SettleConstant.OPERATE_TYPE_ADD);
			}else{
				BeanUtil.copyProperties(settleConfigUpd, settleConfig);
				settleConfigUpd.setOperateType(SettleConstant.OPERATE_TYPE_MODIFY);
			}
			settleConfigUpd.setRecordId(place.getRecordId());
			settleConfigUpd.setTag(place.getTag());
			settleConfigUpd.setRelateId(place.getRelateId());
			settleConfigUpd.setSettleCycle(settleCycle);
			settleConfigUpd.setSettleValue(settleValue);
			settleConfigUpd.setOffsetValue(offsetValue);
			settleConfigUpd.setSettleBase(settleBase);
			settleConfigUpd.setSettleTime(settleTime);
			settleConfigUpd.setFirstSettle(firstSettle);
			settleConfigUpd.setIsGoodsSettle(isGoodsSettle);
			settleConfigUpd.setVenderNo(venderNo);
			settleConfigUpd.setVenderName(venderName);
			settleConfigUpd.setZhangtaoNo(zhangtaoNo); 	
			settleConfigUpd.setPlayType(playType);
			settleConfigUpd.setPayVenderNo(payVenderNo);
			settleConfigUpd.setPayVenderName(payVenderName);
		}
		if(settleConfig != null){
			settleConfig.setStatus("N");
			daoService.saveObject(settleConfig);
		}
		place.setIsConfig("Y");
		daoService.saveObject(settleConfigUpd);
		daoService.saveObject(place);
		
		monitorService.saveChangeLog(logonUser.getId(), SettleConfig.class, recordId, ce.getChangeMap(settleConfigUpd));
		return showJsonSuccess(model);
	}
	private List<Place> queryPlace(Integer pageNo, Integer pageSize){
		DetachedCriteria query = DetachedCriteria.forClass(Place.class);
		query.addOrder(Order.asc("isConfig"));
		query.addOrder(Order.desc("name"));
		return daoService.findByCriteria(query, pageSize * pageNo, pageSize);
	}
	
	@RequestMapping("/platform/settleconfig/modifyOwner.xhtml")
	public String modifyOwner(String placeid, String newowner, ModelMap model) {
		if(StringUtils.isEmpty(placeid)) {
			return showJsonError(model, "影院id不能为空！");
		}
		if(StringUtils.isEmpty(newowner)) {
			return showJsonError(model, "新管理人不能为空！");
		}
		Place place = daoService.getObject(Place.class, placeid);
		if(place == null) {
			return showJsonError(model, "影院为空！");
		}
		place.setOwner(newowner);
		daoService.updateObject(place);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/settleconfig/replaceOwner.xhtml")
	public String replaceOwner(String oldowner, String newowner, ModelMap model) {
		if(StringUtils.isEmpty(oldowner)) {
			return showJsonError(model, "旧管理人不能为空！");
		}
		if(StringUtils.isEmpty(newowner)) {
			return showJsonError(model, "新管理人不能为空！");
		}
		String sql = "update place set owner = ? where owner = ? ";
		int n = jdbcTemplate.update(sql, newowner, oldowner);
		String result = "";
		if(n == 0) {
			result = "更新0条记录，请检查旧的管理人是否拼写正确！";
		} else {
			result = "成功更新" + n + "条记录！";
		}
		return showJsonSuccess(model, result);
	}
}
