package com.gewara.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DramaConst;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaBaseData;
import com.gewara.model.drama.DramaConfig;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaPriceRate;
import com.gewara.model.drama.DramaVersion;
import com.gewara.service.DramaBillService;
import com.gewara.service.DramaDoColleService;
import com.gewara.util.DateUtil;
import com.gewara.util.DramaUtil;

@Controller
public class DramaBaseDataController extends AnnotationController {

	@Autowired
	@Qualifier("dramaBillServiceImpl")
	protected DramaBillService dramaBillService;
	
	@Autowired
	@Qualifier("dramaDoCollecServiceImpl")
	protected DramaDoColleService collectionService;
	
	/**
	 * 演出查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDrama.xhtml")
	public String queryDrama(Drama drama, Long dramaversionid, Integer pageNo, Integer pageSize, ModelMap model) {
		String url = "/platform/drama/queryDrama.xhtml";
		model = dramaBillService.queryDrama(drama, dramaversionid, pageNo, pageSize, url, model);
		return "/drama/drama.vm";
	}
	
	/**
	 * 演出配置查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDramaConfig.xhtml")
	public String queryDramaConfig(DramaConfig config, Integer pageNo, Integer pageSize, ModelMap model) {
		String url = "/platform/drama/queryDramaConfig.xhtml";
		model = dramaBillService.queryDramaConfig(config, pageNo, pageSize, url, model);
		return "/drama/dramaConfig.vm";
	}
	
	/**
	 * 演出场次查询
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/drama/queryPlayItem.xhtml")
	public String queryPlayItem(Long dramaversionid, ModelMap model) {
		DramaVersion dv = daoService.getObject(DramaVersion.class, dramaversionid);
		if(dv != null) {
			DetachedCriteria query = DramaUtil.getDramaPlayItemDetaByDV(dv);
			List<DramaPlayItem> playlist = daoService.findByCriteria(query);
			model.put("playlist", playlist);
		}
		return "/drama/playitemPop.vm";
	}
	
	/**
	 * 手动同步演出
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/syncDramaByDramaid.xhtml")
	public String syncDramaByDramaid(Long dramaid, ModelMap model){
		if(dramaid == null) {
			return showJsonError(model, "参数错误！");
		}
		List<Long> dramaidlist = new ArrayList<Long>();
		dramaidlist.add(dramaid);
		String result = collectionService.syncDramaByDramaid(dramaidlist);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 手动同步场次
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/syncPlayItemByDramaid.xhtml")
	public String syncPlayItemByDramaid(Long dramaid, ModelMap model){
		if(dramaid == null) {
			return showJsonError(model, "参数错误！");
		}
		List<Long> dramaidlist = new ArrayList<Long>();
		dramaidlist.add(dramaid);
		String result = collectionService.syncPlayItemByDramaid(dramaidlist);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 查询价格扣率详情
	 * @param configid
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/drama/queryPriceRate.xhtml")
	public String queryPriceRate(Long configid, ModelMap model) {
		if(configid == null) {
			return "/drama/priceRatePop.vm";
		}
		DetachedCriteria query = DetachedCriteria.forClass(DramaPriceRate.class);
		query.add(Restrictions.eq("configid", configid));
		query.add(Restrictions.eq("status", DramaConst.AVAILABLE));
		query.addOrder(Order.asc("price"));
		List<DramaPriceRate> prlist = daoService.findByCriteria(query);
		model.put("prlist", prlist);
		return "/drama/priceRatePop.vm";
	}
	
	/**
	 * 手动刷新配置
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/updateDramConfig.xhtml")
	public String updateDramConfig(Long recordid, ModelMap model){
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, recordid);
		if(dconfig == null) {
			return showJsonError(model, "配置为空！");
		}
		String result = collectionService.updateDramConfig(dconfig, false);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 手动通过项目id同步配置
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/syncDramConfigByDramaId.xhtml")
	public String syncDramConfigByDramaId(Long dramaid, ModelMap model){
		if(dramaid == null) {
			return showJsonError(model, "参数错误！");
		}
		String result = collectionService.syncDramConfigByDramaId(dramaid);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 审批通过配置
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/passeDramConfig.xhtml")
	public String passeDramConfig(String recordids, ModelMap model){
		if(StringUtils.isEmpty(recordids)) {
			return showJsonError(model, "参数错误！");
		}
		String[] recordidList = recordids.split(",");
		List<DramaConfig> configList = new ArrayList<DramaConfig>();
		for(String recordid : recordidList) {
			DramaConfig dconfig = daoService.getObject(DramaConfig.class, Long.valueOf(recordid));
			if(dconfig != null) {
				configList.add(dconfig);
			}
		}
		String result = dramaBillService.passeDramConfig(configList);
		if("".equals(result)) {
			result = "审批通过成功！";
		}
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 审批通过配置
	 * @param recordId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/platform/drama/discardDramConfig.xhtml")
	public String discardDramConfig(Long recordid, ModelMap model){
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, recordid);
		if(dconfig == null) {
			return showJsonError(model, "配置不存在！");
		}
		dconfig.setStatus(DramaConst.DISCARD);
		dconfig.setGspupdatetime(DateUtil.getCurFullTimestamp());
		daoService.updateObject(dconfig);
		return showJsonSuccess(model, "废弃成功！");
	}
	
	/**
	 * 基础数据查询
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDramaBaseData.xhtml")
	public String queryDramaBaseData(DramaBaseData dbData, Integer pageNo, Integer pageSize, ModelMap model, HttpServletRequest request) {
		String url = "/platform/drama/queryDramaBaseData.xhtml";
		model = dramaBillService.queryDramaBaseData(dbData, pageNo, pageSize, url, model);
		if (!checkSpecialUrl(request)) {
			return "/drama/dramaBaseData.vm";
		} else {
			return "/drama/dramaAdminBaseData.vm";
		}
	}
	
	/**
	 * 加载基础数据详情
	 * @param recordid
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/loadBaseDateDetail.xhtml")
	public String loadBaseDateDetail(Long recordid, ModelMap model) {
		if(recordid == null) {
			return "/drama/baseDataPop.vm";
		}
		DramaBaseData data = daoService.getObject(DramaBaseData.class, recordid);
		model.put("data", data);
		return "/drama/baseDataPop.vm";
	}
	
	/**
	 * 保存数据
	 * @param dbData
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/saveBaseDate.xhtml")
	public String saveBaseDate(DramaBaseData dbData, ModelMap model) {
		if(StringUtils.isBlank(dbData.getCode())) {
			return showJsonError(model, "参数错误！");
		}
		if(StringUtils.isBlank(dbData.getName())) {
			return showJsonError(model, "参数错误！");
		}
		daoService.saveObject(dbData);
		return showJsonSuccess(model, "保存成功！");
	}
	
	/**
	 * 删除数据
	 * @param dbData
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/delBaseDate.xhtml")
	public String delBaseDate(Long recordid, ModelMap model) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaBaseData data = daoService.getObject(DramaBaseData.class, recordid);
		daoService.removeObject(data);
		return showJsonSuccess(model, "删除成功！");
	}
	
	/**
	 * 生成结算单据
	 * @param recordid
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/createDramaBill.xhtml")
	public String createDramaBill(Long recordid, ModelMap model) {
		if(recordid == null) {
			return showJsonError(model, "参数错误！");
		}
		DramaConfig dconfig = daoService.getObject(DramaConfig.class, recordid);
		if(dconfig == null) {
			return showJsonError(model, "配置不存在！");
		}
		if(!DramaConst.HASAPPROVAL.equals(dconfig.getStatus()) && !DramaConst.SETTLEING.equals(dconfig.getStatus())) {
			return showJsonError(model, "配置状态不正确！");
		}
		String result = collectionService.createSettleBill(dconfig);
		return showJsonSuccess(model, result);
	}
	
	/**
	 * 查询演出下拉数据
	 * @param recordid
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/drama/queryDramaData.xhtml")
	public String queryDramaData(String dramaname, Integer maxrow, ModelMap model) {
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class);
		query.add(Restrictions.like("dramaname", dramaname, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("recordid"));
		List<Drama> dramaList = daoService.findByCriteria(query, 0, maxrow);
		Map<String, Object> jsonMap  = new HashMap<String, Object>();
		jsonMap.put("rdata", dramaList);
		return showJsonSuccess(model, jsonMap);
	}
	
	/**
	 * 查询基础数据下拉数据
	 * @param recordid
	 * @param model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/platform/drama/queryBaseData.xhtml")
	public String queryBaseData(String name, String type, Integer maxrow, ModelMap model) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaBaseData.class);
		query.add(Restrictions.eq("type", type));
		query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("recordid"));
		List<DramaBaseData> dataList = daoService.findByCriteria(query, 0, maxrow);
		Map<String, Object> jsonMap  = new HashMap<String, Object>();
		jsonMap.put("rdata", dataList);
		return showJsonSuccess(model, jsonMap);
	}
	
	/**
	 * 查询订单
	 * @param recordid
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/drama/queryDramaOrder.xhtml")
	public String queryDramaOrder(String tradeno, String orderform, ModelMap model) {
		if(StringUtils.isNotEmpty(tradeno)) {
			model = dramaBillService.queryDramaOrder(tradeno, orderform, model);
		}
		return "/drama/dramaOrderDetail.vm";
	}
}
