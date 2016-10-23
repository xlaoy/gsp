package com.gewara.web.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.SettleCycleEnums;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.ChannelSettleConfig;
import com.gewara.model.gsp.CheckBill;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettleConfig;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.StatusTracker;
import com.gewara.model.gsp.SysData;
import com.gewara.model.gsp.VendorCinemaRelation;
import com.gewara.service.AdjustmentService;
import com.gewara.service.ChannelSettleService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebLogger;
import com.gewara.web.support.GewaMultipartResolver;
import com.gewara.web.util.PageUtil;

/**
 * 
 * @ClassName: ChannelSettleController  
 * @Description: ͨ���ѽ��������  
 * @author yujun.su@gewara.com
 * @date 2014-10-11 ����3:39:49
 */

@Controller
public class ChannelSettleController extends AnnotationController {
	private final GewaLogger logger = WebLogger.getLogger(getClass());
	@Autowired
	@Qualifier("channelSettleService")
	private ChannelSettleService channelSettleService;
	@Autowired
	@Qualifier("adjustmentService")
	private AdjustmentService adjustmentService;
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private GewaMultipartResolver gewaMultipartResolver;
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadChannelConfig  
	 * @Description:ͨ�������ü���
	 * @return String
	 * @throws
	 */
	@RequestMapping("platform/channelsettle/loadChannelConfigPage.xhtml")
	public String loadChannelConfig(ModelMap model,Integer pageNo, Integer pageSize,Long vendorId, String placeId, String vendorType){
		channelSettleService.loadChannelConfig(model,pageNo,pageSize,vendorId, placeId, vendorType);
		return "channelsettle/channelConfigpage.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: addChannelSettleConfig  
	 * @Description:ͨ���ѽ��� ��������
	 * @return String
	 * @throws
	 */
	@RequestMapping("/platform/channelsettle/addChannelSettleConfig.xhtml")
	public String addChannelSettleConfig(ModelMap model){
		 model.put("channelVendorType", SettleConstant.CHANNELVENDORTYPE);
		return "/channelsettle/addChannelSettleConfig.vm";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: loadAllCinema  
	 * @Description:�������ã�����ӰԺ
	 * @param model
	 * @return String
	 * @throws
	 */
	@RequestMapping("/platform/channelsettle/loadAllCinema.xhtml")
	public String loadAllCinema(ModelMap model,String recordId){
		model = channelSettleService.loadAllCinemas(model,recordId);
		Place p = (Place) model.get("place");
		if(null != p){
			return showJsonSuccess(model, p.getRecordId() + "@" + p.getName());
		}else{
			return showJsonError(model, "��ѡ��ӰԺ��");
		}
	}
	
	/**
	 * 
	 * @throws
	 */
	@RequestMapping("/platform/channelsettle/saveChannelSettleConfig.xhtml")
	public String saveChannelSettleConfig(HttpServletRequest req, ChannelSettleConfig settleConfig, ModelMap model,  String[] cinema) {
		User user = getLogonUser(req);
		String vendorType = settleConfig.getVendorType();
		if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(vendorType)){
			if(null == cinema || cinema.length == 0){
				return goBack(model,"��ѡ���˵���ӰԺ������û�����ӰԺ��");
			}
			if(cinema.length > 1){
				return goBack(model,"��ѡ����ӰԺ��Ӧ�̣���������˶��ӰԺ��");
			}
			SettleConfig sc = daoService.getObject(SettleConfig.class, cinema[0]);
			if(sc == null) {
				return goBack(model,"��ӰԺû��ӰƱ�������ã�");
			}
			if(SettleCycleEnums.MONTHLY.getType() == sc.getSettleCycle()) {
				return goBack(model,"��ӰԺ��ӰƱ�������ò��ǰ��½��㣡");
			}
		}
		if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(vendorType)) {
			if(cinema!=null && cinema.length > 0){
				return goBack(model,"Ժ�߲����Զ���ӰԺ��");
			}
			if(null == settleConfig.getHoutaiId()) {
				return goBack(model,"��ѡ����̺�̨��Ӧ��Ժ��!");
			}
		}
		channelSettleService.saveChannelSettleConfig(model, settleConfig, cinema, user);
		return "redirect:/platform/channelsettle/loadChannelConfigPage.xhtml";
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: deleteChannelSettleConfig  
	 * @Description:ͨ���ѽ�������ɾ��
	 * @param model
	 * @param recordId
	 * @param @return
	 * @return String
	 * @throws
	 */
	@RequestMapping("/platform/channelsettle/deleteChannelSettleConfig.xhtml")
	public String deleteChannelSettleConfig(ModelMap model,Long recordId){
		if(null == recordId){
			return showJsonError(model, "�����쳣!");
		}
		ChannelSettleConfig cfg = daoService.getObject(ChannelSettleConfig.class, recordId);
		if(null == cfg){
			return showJsonError(model, "ͨ���������쳣!");
		}
		channelSettleService.deleteChannelSettleConfig(model,recordId);
		return showJsonSuccess(model, "ͨ���ѽ�������ɾ���ɹ���");
	}
	
	@RequestMapping("/platform/channelsettle/queryChannelSettlebills.xhtml")
	public String queryChannelSettlebills(ModelMap model,Integer vendorId,Timestamp start,Timestamp end,String reqMoneyStatus,Integer pageNo, Integer pageSize){
		channelSettleService.queryChannelSettlebills(model,vendorId,start,end,reqMoneyStatus,pageNo,pageSize);
		return "/channelsettle/channelSettleBills.vm";
	}
	
	@RequestMapping("/platform/channelsettle/queryOrderDetailForChannel.xhtml")
	public String queryOrderDetailForChannel(ModelMap model,Long settleBillId,Integer pageNo,Integer pageSize,String isXls,HttpServletResponse res){
		channelSettleService.queryOrderDetailForChannel(model,settleBillId,pageNo,pageSize,isXls);
		if(StringUtils.isNotEmpty(isXls)){
			this.download("xls", "ͨ���Ѷ�����ϸ", res);
			return "/downloadtemplate/channelSettleOrderDetails.vm";
		}
		return "/channelsettle/channelSettleOrderDetails.vm";
	}
	
	@RequestMapping("platform/channelsettle/queryCheckBillBySettleId.xhtml")
	public String queryChannelCheckBillBySettleId(ModelMap model , Long settleId ,Long vendorId,Integer pageNo,Integer pageSize){
		pageNo = (pageNo == null ? 0 : pageNo);
		pageSize = (pageSize == null ? 30 : pageSize);
		model = channelSettleService.queryCheckBillBySettleId(model,settleId,vendorId,pageNo,pageSize);
		int count = Integer.valueOf(model.get("count").toString());
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath()+ "/platform/channelsettle/queryCheckBillBySettleId.xhtml", true, true);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("settleId", settleId);
		params.put("vendorId", vendorId);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return "channelsettle/channelCheckBill.vm";
	}
	
	@RequestMapping("platform/channelsettle/verifyChannelConfig.xhtml")
	public String verifyChannelConfig(ModelMap model,Long recordId,HttpServletRequest req){
		if(null == recordId){
			return showJsonError(model, "��������");
		}
		ChannelSettleConfig channelSettleConfig = daoService.getObject(ChannelSettleConfig.class, recordId);
		if(null == channelSettleConfig){
			return showJsonError(model, "���ò����ڣ�");
		}
		if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(channelSettleConfig.getVendorType())) {
			List<VendorCinemaRelation> relations = daoService.getObjectListByField(VendorCinemaRelation.class, "vendorRecordId", recordId);
			if(CollectionUtils.isEmpty(relations)){
				return showJsonError(model, "�ý���������û������ӰԺ���������ͨ����");
			}
		}
		User user = getLogonUser(req);
		channelSettleService.verifyChannelConfig(recordId, SettleConstant.STATUS_VALID, user);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/channelsettle/loadUnVerifiedConfig.xhtml")
	public String loadUnVerifiedConfig(ModelMap model,String vendorName,Integer pageSize,Integer pageNo){
		pageSize = (pageSize == null ? 50 : pageSize);
		pageNo = (pageNo == null ? 0 : pageNo);
		Map<String,Object> result = channelSettleService.loadUnVerifiedConfig(vendorName,pageSize,pageNo); 
		model.put("configs", result.get("configs"));
		Integer count = Integer.valueOf(result.get("count").toString());
		PageUtil pageUtil = new PageUtil(count,pageSize,pageNo, config.getBasePath()+ "/platform/channelsettle/loadUnVerifiedConfig.xhtml", true, true);
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		model.put("vendorName", vendorName);
		return "channelsettle/unVerifiedChannelSettleConfig.vm";
	}
	
	@RequestMapping("/platform/channelsettle/checkVendorNameIsRepeat.xhtml")
	public String checkVendorNameIsRepeat(ModelMap model,String vendorCode,Long recordId){
		if(StringUtils.isNotEmpty(vendorCode)){
			Integer i = channelSettleService.checkVendorNameIsRepeat(vendorCode,recordId);
			if(i > 0){
				return showJsonError(model, "�Ѿ����ڵĹ�Ӧ�̱��룬���ʵ��");
			}
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/channelsettle/ajaxLoadVendor.xhtml")
	public String ajaxLoadVendor(ModelMap model,String inputValue){
		List<ChannelSettleConfig> configs = channelSettleService.queryVendorByName(inputValue);
		Map<String, Object> jsonMap  = new HashMap<String, Object>();
		jsonMap.put("rdata", configs);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/platform/channelsettle/loadChannelConfigById.xhtml")
	public String loadChannelConfigById(ModelMap model,Long recordId){
		Map<String,Object> map = channelSettleService.loadConfigAndCinemaById(recordId);
		model.put("config", map.get("config"));
		model.put("places", map.get("places"));
		return "channelsettle/modifyChannelConfig.vm";
	}
	
	@RequestMapping("/platform/settleconfig/modifyChannelSettleConfig.xhtml")
	public String modifyChannelSettleConfig(ModelMap model,ChannelSettleConfig newconfig, String[] cinema, HttpServletRequest req){
		if(null == newconfig.getRecordId()){
			return goBack(model,"�����쳣");
		}
		ChannelSettleConfig oldconfig = daoService.getObject(ChannelSettleConfig.class, newconfig.getRecordId());
		if(oldconfig == null){
			return goBack(model,"oldconfig������");
		}
		String username = getLogonUser(req).getUsername();
		channelSettleService.modifyChannelSettleConfig(newconfig, oldconfig, cinema, username);
		return showRedirect("platform/channelsettle/loadChannelConfigPage.xhtml?vendorId=" + newconfig.getRecordId(), model);
	}
	
	
	
	@RequestMapping("platform/channelsettle/viewCinemaDetails.xhtml")
	public String viewCinemaDetails(ModelMap model,Long recordId){
		List<Place> results = new ArrayList<Place>();
		if(null != recordId){
			results = channelSettleService.loadPlacesByVendorId(recordId);
		}
		model.put("results", results);
		return "channelsettle/viewCinemaDetails.vm";
	}
	
	@RequestMapping("platform/channelsettle/exportCinemaDetails.xhtml")
	public String exportCinemaDetails(ModelMap model,HttpServletResponse response,Long recordId){
	    ChannelSettleConfig channelConfig = daoService.getObject(ChannelSettleConfig.class, recordId);
	    List<Place> places = channelSettleService.loadPlacesByVendorId(recordId);
	    model.put("places", places);
		this.download("xls", channelConfig.getVendorName(), response);
		return "channelsettle/downloadCinemaDetails.vm";
	}
	
	@RequestMapping("platform/channelsettle/abandonChannelConfig.xhtml")
	public String abandonChannelConfig(ModelMap model,HttpServletRequest request,Long recordId){
		ChannelSettleConfig channelConfig = daoService.getObject(ChannelSettleConfig.class, recordId);
		if(null == channelConfig){
			return showJsonError(model, "�����쳣��");
		}
		User user = getLogonUser(request);
		channelSettleService.abandonChannelConfig(channelConfig,user);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("platform/channelsettle/loadVendorFromEC.xhtml")
	@ResponseBody
	public String loadVendorFromEC(String vendorType){
		logger.warn("��houtai.gewara.com��ѯ����Ϊ��" + vendorType +"�Ĺ�Ӧ��");
		String url = config.getString("channelUrl");
		Map<String, String> params = new HashMap<String,String>();
		params.put("channelType", vendorType);
		HttpResult result = HttpUtils.postUrlAsString(url, params);
		if(result.isSuccess()){
			logger.warn(result.getResponse());
			return result.getResponse();
		}
		logger.error("��ѯ��̨����" + result.getStatus());
		return "";
	}
	
	@RequestMapping("platform/channelsettle/loadSysCode.xhtml")
	public String loadSysCode(String vendorType, ModelMap model){
		List<SysData> datalist = daoService.getObjectListByField(SysData.class, "type", SettleConstant.SYSCATEGORY);
		Map<String, Object> jsonMap  = new HashMap<String, Object>();
		jsonMap.put("rdata", datalist);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("platform/channelsettle/channelDetails.xhtml")
	public String channelDetails(ModelMap model,Long recordId,String isPrint){
		if(null == recordId){
			return showMessage(model, "�����쳣��");
		}
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, recordId);
		//zyj
		model.put("settlementBill", settlementBill);
		Long vendorId = settlementBill.getRelateId();
		ChannelSettleConfig channelCfg = daoService.getObject(ChannelSettleConfig.class, vendorId);
		
		Timestamp timecut = DateUtil.parseTimestamp("2015-09-02", "yyyy-MM-dd");
		
		if(settlementBill.getEndTime().before(timecut)) {
			List<CheckBill>	cks = daoService.getObjectListByField(CheckBill.class, "settlementId", recordId);
			if (CollectionUtils.isNotEmpty(cks)){
				Collections.sort(cks);
			}
			model.put("cks", cks);
			if (StringUtils.isNotEmpty(isPrint)){
				StatusTracker lastMerchant = getMerchantConfirmST(recordId);
				StatusTracker lastGewa = getGewaPayST(recordId);
				if (lastMerchant != null){
					Map<String, String> merchantInfo = getRemoteMerchantInfo(lastMerchant.getOperator());
					if (merchantInfo != null)
						model.put("merinfo", JsonUtils.readJsonToMap(merchantInfo.get("otherInfo")));
				}
				model.put("mst", lastMerchant);
				model.put("gst", lastGewa);
				model.put("settleId", recordId);
				model.put("vendorName", channelCfg.getVendorName());
				model.put("settle", settlementBill);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("settleId", settlementBill.getRecordId());
				List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(daoService.getObject(SettlementBill.class, recordId));
				model.put("ads",ads);
			}
			if(StringUtils.isNotBlank(isPrint)){
				model.put("tag", settlementBill.getTag());
				return "/printtemplate/checkbills.vm";
			}
			return "/checkbill/checkbills.vm";
		} else {
			/********************zyj*******************/
			String vendorType = channelCfg.getVendorType();
			List<CheckBill>	cks = daoService.getObjectListByField(CheckBill.class, "settlementId", recordId);
			
			if (CollectionUtils.isNotEmpty(cks)){
				Collections.sort(cks);
			}
			model.put("cks", cks);
			List<Place> places = daoService.getAllObjects(Place.class);
			Map<Long,Place> placeMap = com.gewara.util.BeanUtil.beanListToMap(places, "relateId");
			model.put("placeMap", placeMap);
			if (StringUtils.isNotEmpty(isPrint)){
				StatusTracker lastMerchant = getMerchantConfirmST(recordId);
				StatusTracker lastGewa = getGewaPayST(recordId);
				if (lastMerchant != null){
					Map<String, String> merchantInfo = getRemoteMerchantInfo(lastMerchant.getOperator());
					if (merchantInfo != null)
						model.put("merinfo", JsonUtils.readJsonToMap(merchantInfo.get("otherInfo")));
				}
				model.put("mst", lastMerchant);
				model.put("gst", lastGewa);
				model.put("settleId", recordId);
				model.put("vendorName", channelCfg.getVendorName());
				model.put("settle", settlementBill);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("settleId", settlementBill.getRecordId());
				List<Adjustment> ads = adjustmentService.queryAdsBySettleBill(daoService.getObject(SettlementBill.class, recordId));
				model.put("ads",ads);
			}
			if(StringUtils.isNotBlank(isPrint)){
				model.put("tag", settlementBill.getTag());
				return "/printtemplate/checkbills.vm";
			}
			
			if(SettleConstant.CHANNEL_VENDOR_CINEMA.equals(vendorType)) {
				return "/checkbill/checkbills.vm";
			} else if(SettleConstant.CHANNEL_VENDOR_THEATRES.equals(vendorType) || SettleConstant.CHANNEL_VENDOR_SYS.equals(vendorType)) {
				return "/checkbill/thtAndSysCheckbills.vm";
			}
			return null;
			/********************zyj*******************/
		}
	}
}
