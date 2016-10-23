package com.gewara.web.action;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.gewara.constant.SettleConstant;
import com.gewara.dao.Dao;
import com.gewara.model.ResultCode;
import com.gewara.model.gsp.AutoAnalysObject;
import com.gewara.model.gsp.AutoReconciledInfo;
import com.gewara.model.gsp.AutoreconExceptionResult;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.model.gsp.SysData;
import com.gewara.service.AutoReconcileService;
import com.gewara.service.SysDataService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.RecordIdUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebLogger;
import com.gewara.web.util.PageUtil;

/**
 * 
 * @ClassName: AutoReconcileController  
 * @Description: 自动对账Controller  
 * @author yujun.su@gewara.com
 * @date 2015-3-30 下午4:03:04
 */
@Controller
public class AutoReconcileController extends AnnotationController implements InitializingBean {
	
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;
	
	@Autowired
	@Qualifier("autoReconcileService")
	private AutoReconcileService autoReconcileService;
	
	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	private final transient GewaLogger logger = WebLogger.getLogger(getClass());
	
	private ThreadPoolExecutor executor;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		executor = new ThreadPoolExecutor(10, 10, 300, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		executor.allowCoreThreadTimeOut(false);
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: getSettlementBillByRecordId  
	 * @Description:商户点击自动对账链接，根据结算单id查询结算单
	 * @param recordId
	 * @return SettlementBill
	 */
	@RequestMapping("merchant/autoreconcile/getSettlementBillByRecordId.xhtml")
	public String getSettlementBillByRecordId(ModelMap model,Long settlementId){
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settlementId);
		if(null == settlementBill){
			model.put("canaotu", SettleConstant.COMM_N);
			model.put("msg", "结算单不存在！");
			return "settlement/merchantUploadReconciledFile.vm";
		}
		//查询该结算单是否有自动对账记录
		AutoReconciledInfo arInfo = daoService.getObjectByUkey(AutoReconciledInfo.class, "settlementId", settlementId);
		if(arInfo != null){
			return showRedirect("merchant/autoreconcile/queryReconResultByCondition.xhtml?settleid=" + settlementId, model);
		} else {
			Place place = daoService.getObject(Place.class, settlementBill.getConfigId());
			String opentype = place.getOpenType();
			SysData sys = sysDataService.getSysData(opentype, SettleConstant.SYSCATEGORY, String.class);
			if(sys != null) {
				if(!StringUtils.isEmpty(sys.getName())) {
					model.put("canaotu", SettleConstant.COMM_Y);
					model.put("settlementBill", settlementBill);
					return "settlement/merchantUploadReconciledFile.vm";
				}
			}
			model.put("canaotu", SettleConstant.COMM_N);
			model.put("msg", "结算单" + settlementId + "对应的影院系统不支持对账码，不能自动对账！");
			return "settlement/merchantUploadReconciledFile.vm";
		}
	}
	
	/**
	 * 
	 * @auth yujun.su@gewara.com 
	 * @Title: uploadReconciledFile  
	 * @Description:商户点击上传按钮触发
	 * 				1、保存文件到remote
	 * 				2、生成待自动对账对象
	 * @param model
	 * @param request
	 * @param settlementId 结算单id
	 * @return String
	 */
	@RequestMapping("/merchant/autoreconcile/uploadReconciledFile.xhtml")
	public String uploadReconciledFile(ModelMap model,HttpServletRequest request,final Long settlementId){
		//查询该结算单是否有自动对账记录
		AutoReconciledInfo arInfo = daoService.getObjectByUkey(AutoReconciledInfo.class, "settlementId", settlementId);
		if(arInfo != null){
			return goBack(model, "该结算单已有自动对账记录,不能再次进行自动对账!");
		}
		String user = this.getMerchantUserName(request);
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
 		MultipartFile file = multipartRequest.getFile("file");
		String oriName = file.getOriginalFilename();
		if(StringUtils.isEmpty(oriName) || (!oriName.toLowerCase().endsWith(".xls") && !oriName.toLowerCase().endsWith(".xlsx"))){
			return goBack(model, "上传文件格式有误，请检查文件名称及类型，需要的文件类型为.xls或.xlsx!");
		}
		if(file.getSize() == 0 ){
			return goBack(model, "请选择文件！");
		}
		SettlementBill settlementBill = daoService.getObject(SettlementBill.class, settlementId);
		Place place = daoService.getObject(Place.class, settlementBill.getConfigId());
		String opentype = place.getOpenType();
		SysData sys = sysDataService.getSysData(opentype, SettleConstant.SYSCATEGORY, String.class);
		if(sys == null) {
			return goBack(model, "当前影院不支持自动对账！");
		}
		if(StringUtils.isEmpty(sys.getName())) {
			return goBack(model, "当前影院不支持自动对账！");
		}
		
		ResultCode<String> result = ResultCode.SUCCESS;
		try {
			logger.warn("自动对账文件上传，结算单号：" + settlementId + " 文件名：" + oriName + " user:" + user);
			result = uploadFile(file.getBytes(), request, oriName);
		} catch (IOException e) {
			logger.error("文件上传失败 reason:" + e.getMessage());
			return goBack(model, result.getMsg());
		}
		if(!result.isSuccess()){
			return goBack(model, result.getMsg());
		}
		final AutoReconciledInfo info = new AutoReconciledInfo();
		info.setSettlementId(settlementId);
		info.setFileName(oriName);
		info.setFilePath(result.getRetval());
		info.setOpenType(opentype);
		info.setStatus(SettleConstant.AUTO_RECON_STATUS_WAIT_PROCESS);
		info.setPlaceId(settlementBill.getConfigId());
		info.setUploadTime(DateUtil.getCurFullTimestamp());
		info.setUploadUser(user);
		info.setUpdateTime(DateUtil.getCurFullTimestamp());
		info.setUpdateUser(user);
		info.setRemark("");
		info.setGewaOrderNumber(settlementBill.getSuccTicketNumber());
		info.setGewaOrderAmount(settlementBill.getSuccTicketAmount());
		info.setMerchantOrderNumber(0.0);
		info.setMerchantOrderAmount(0.0);
		daoService.saveObject(info);
		
		//进入自动对账操作
		executor.execute(new Runnable() {
			@Override
			public void run() {
				info.setStatus(SettleConstant.AUTO_RECON_STATUS_PROCESSING);
				info.setUpdateTime(DateUtil.getCurFullTimestamp());
				info.setUpdateUser(SettleConstant.SYSTEM);
				daoService.saveObject(info);
				boolean flag = false;
				flag = autoReconcileService.loadTemplateFile(info);
				if(flag) {
					flag = autoReconcileService.doMenchTempData(info);
				}
				if(flag) {
					flag = autoReconcileService.doGWOrder(info);
				}
				if(flag) {
					autoReconcileService.compareOrder(info);
				}
			}
		});
		return showRedirect("merchant/autoreconcile/queryReconResultByCondition.xhtml?settleid=" + settlementId, model);
	}
	
	
	private ResultCode<String> uploadFile(byte[] fileBuff,HttpServletRequest request,String fileName){
		String tempUrl = config.getString("uploadTempURL");
		String remoteUrl = config.getString("uploadComit");
		Map<String, String> params = new HashMap<String, String>();
		params.put("callbackUrl", "no call back");
		
		HttpResult result = HttpUtils.uploadFile(tempUrl, params, fileBuff, "noinput", fileName);
		Map tempResult = JsonUtils.readJsonToMap(result.getResponse());
		String successFile = (String) tempResult.get("successFile");
		if (StringUtils.isBlank(successFile)){
			dbLogger.error("upload file failed:" + result.getResponse());
			return ResultCode.getFailure("上传到临时文件时发生错误");
		}
			
		
		Map<String, String> commitParams = new HashMap<String, String>();
		Long mid = getMerchantUserId(request);
		commitParams.put("userid", mid.toString());
		commitParams.put("systemid", "GSP");
		String dateStr = DateUtil.format(new Date(), "yyyyMM");
		String remoteFold = "/upload/gsp/" + dateStr + "/";
		commitParams.put("path", remoteFold);
		commitParams.put("files", successFile);
		commitParams.put("check", StringUtil.md5(successFile + remoteFold + "GSP" + mid + "GewaUploadFile"));
		HttpResult r = HttpUtils.postUrlAsString(remoteUrl, commitParams);
		if (!"SUCCESS".equals(r.getResponse())){
			dbLogger.error("upload file failed:" + result.getResponse());
			return ResultCode.getFailure(r.getResponse());
		}
			
		return ResultCode.getSuccessReturn(remoteFold + successFile);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("merchant/autoreconcile/queryExceptionResult.xhtml")
	public String queryExceptionResult(ModelMap model,Long settlebillId, Integer pageNo, Integer pageSize, String isXls,HttpServletResponse response) {
		if(settlebillId == null) {
			return "settlement/merchReconciledExceptionResult.vm";
		}
		
		if(StringUtils.isNotBlank(isXls)) {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 50000 : pageSize;
		} else {
			pageNo = pageNo == null ? 0 : pageNo;
			pageSize = pageSize == null ? 30 : pageSize;
		}
		
		List<AutoreconExceptionResult> resultlist = new ArrayList<AutoreconExceptionResult>();
		
		DetachedCriteria querycount = DetachedCriteria.forClass(AutoreconExceptionResult.class);
		DetachedCriteria querylist = DetachedCriteria.forClass(AutoreconExceptionResult.class);
		querycount.add(Restrictions.eq("settleid", settlebillId));
		querylist.add(Restrictions.eq("settleid", settlebillId));
		querycount.setProjection(Projections.rowCount());
		querylist.addOrder(Order.desc("recordid"));
		
		int count = Integer.valueOf(daoService.findByCriteria(querycount).get(0).toString());
		
		if(count > 0) {
			resultlist = daoService.findByCriteria(querylist, pageSize * pageNo, pageSize);
			PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "merchant/autoreconcile/queryExceptionResult.xhtml", true, true);
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("settlebillId", settlebillId);
			pageUtil.initPageInfo(m);
			model.put("pageUtil", pageUtil);
		}
		model.put("settlebillId", settlebillId);
		model.put("resultlist", resultlist);
		if(StringUtils.isNotBlank(isXls)) {
			this.download("xls", settlebillId + "对账异常订单结果", response);
			return "settlement/merchReconciledExceptionResult_xls.vm";
		}
		return "settlement/merchReconciledExceptionResult.vm";
	}
	
	@RequestMapping("merchant/autoreconcile/loadReconResult.xhtml")
	public String loadReconResult(ModelMap model,Long settlebillId,String status,Integer pageNo, Integer pageSize,String isXls,HttpServletResponse response){
		pageNo = StringUtils.isNotEmpty(isXls) ? 0 : pageNo == null ? 0 : pageNo;
		pageSize = StringUtils.isNotEmpty(isXls) ? 10000 :pageSize == null ? 20 : pageSize;
		List<AutoAnalysObject> list = autoReconcileService.loadReconResult(settlebillId,status,pageNo,pageSize);
		Integer count = autoReconcileService.countReconResult(settlebillId,status);
		
		PageUtil pageUtil = new PageUtil(count, pageSize, pageNo, config.getBasePath() + "merchant/autoreconcile/loadReconResult.xhtml", true, true);
		Map<String,Object> params = new HashMap<>();
		params.put("settlebillId", settlebillId);
		params.put("status", status);
		pageUtil.initPageInfo(params);
		
		model.put("resultList", list);
		model.put("pageUtil", pageUtil);
		
		SettlementBill bill = daoService.getObject(SettlementBill.class, settlebillId);
		Place place = daoService.getObject(Place.class, bill.getConfigId());
		model.put("place", place);
		if(StringUtils.isNotBlank(isXls)){
			if(SettleConstant.AUTO_RECON_RESULT_NORMAL.equals(status)){
				this.download("xls", "正常列表" , response);
				return "settlement/autoReconciledNormal.vm";				
			}else{
				this.download("xls", "异常列表" , response);
				return "settlement/autoReconciledAbnormal.vm";	
			}
		}
		if(SettleConstant.AUTO_RECON_RESULT_NORMAL.equals(status)){
			return "settlement/autoReconciledNormal.vm";			
		}
		return "settlement/autoReconciledAbnormal.vm"; 
	}
	
	@RequestMapping("merchant/autoreconcile/queryReconResultByCondition.xhtml")
	public String queryReconResultByCondition(ModelMap model,HttpServletRequest request,Timestamp start, Timestamp end ,
			Long settleid, String placeId,String proviceCode,Integer pageNo,Integer pageSize){
		placeId = defaultPlaceId(request) == null ? placeId : defaultPlaceId(request);
		List<String> placeIds = new ArrayList<String>();
		if (StringUtils.isNotBlank(placeId))
			placeIds.add(placeId);
		else
			placeIds = getAllowedPlaceIds(request);
		
		if (StringUtils.isNotBlank(proviceCode))
			placeIds = cityInfoHolder.filteredPlaceIdForProvice(placeIds, proviceCode);
		
		if (CollectionUtils.isEmpty(placeIds))
			return "/settlement/merchantAutoReconcileResult.vm";
		
		List<String> allowPlaceIds = getAllowedPlaceIds(request);
		Map map = cityInfoHolder.wrarpPlaceId(allowPlaceIds);
		model.put("optionData", map);
		model.put("proviceMap", cityInfoHolder.getProviceByPlaceId(allowPlaceIds));
		
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 50 : pageSize;
		model.put("placeId", placeId);
		model.put("place", daoService.getObject(Place.class, placeId));
		int totalNumber = autoReconcileService.countReconcileResultNum(placeIds,start,end, settleid);
		if (totalNumber <= 0){
			return "/settlement/merchantAutoReconcileResult.vm";
		}
		List<SettlementBill> bills = autoReconcileService.querySettlementbillsForAutoRecon(allowPlaceIds, start, end, settleid);
		Map<Long,SettlementBill> billsMap = BeanUtil.beanListToMap(bills, "recordId");
		model.put("billsMap", billsMap);
		
		List<AutoReconciledInfo>  autoReconciledInfos = autoReconcileService.queryReconcileResult(placeIds, pageNo, pageSize,start,end, settleid);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo,config.getBasePath() + "merchant/autoreconcile/queryReconResultByCondition.xhtml",true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("placeId", placeId);
		m.put("proviceCode", proviceCode);
		m.put("start", start);
		m.put("end", end);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("autoReconciledInfos", autoReconciledInfos);
		model.put("autoReconStatusMap", SettleConstant.AUTORECONSTATUSMAP);
		return "/settlement/merchantAutoReconcileResult.vm";
	}
	
	@RequestMapping("merchant/autoreconcile/deleteReconRecord.xhtml")
	public String deleteReconRecord(ModelMap model , Long settlebillId){
		if(settlebillId == null){
			return showJsonError(model, "参数错误");
		}
		autoReconcileService.deleteReconRecord(settlebillId);
		return showJsonSuccess(model);
	}
}
