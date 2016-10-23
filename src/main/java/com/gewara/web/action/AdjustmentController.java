/** 
 */
package com.gewara.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustTypeEnums;
import com.gewara.enums.AdjustmentStatusEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.model.ResultCode;
import com.gewara.model.acl.User;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.Place;
import com.gewara.model.gsp.SettlementBill;
import com.gewara.service.AdjustmentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.support.GewaMultipartResolver;
import com.gewara.web.util.PageUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Aug 14, 2013  6:52:25 PM
 */
@Controller
public class AdjustmentController extends AnnotationController implements InitializingBean{
	@Autowired
	@Qualifier("adjustmentService")
	private AdjustmentService adjustmentService;
	
	@Autowired
	@Qualifier("gewaMultipartResolver")
	private GewaMultipartResolver gewaMultipartResolver;
	
	
	@RequestMapping("/platform/adjust/loadAdjust.xhtml")
	public String loadAdjust(Long recordId, ModelMap model){
		Adjustment ad = daoService.getObject(Adjustment.class, recordId);
		model.put("ad", ad);
		return "/adjust/adjustDetails.vm";
	}
	@RequestMapping("/platform/adjust/loadAdjustList.xhtml")
	public String loadAdjustList(Long settleId, ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("settleBillId", settleId));
		query.add(Restrictions.eq("status", AdjustmentStatusEnums.APPROVED.getStatus()));
		List<Adjustment> ads = daoService.findByCriteria(query);
		model.put("ads",ads);
		return "/adjust/adjustList.vm";
	}
	
	@RequestMapping("/merchant/adjust/loadAdjustList.xhtml")
	public String merchantLoadAdjustList(Long settleId, String configId, ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(Adjustment.class);
		query.add(Restrictions.eq("settleBillId", settleId));
		query.add(Restrictions.eq("status", AdjustmentStatusEnums.APPROVED.getStatus()));
		query.add(Restrictions.eq("configId", configId));
		List<Adjustment> ads = daoService.findByCriteria(query);
		model.put("ads",ads);
		return "/adjust/merchantAdjustList.vm";
	}
	
	@RequestMapping("/platform/adjust/updateAdjust.xhtml")
	public String updateAdjustment(Long recordId, Double adjustNumber,String filePath ,Double amount, String reason, String comments , ModelMap model, HttpServletRequest request){
		if (recordId == null)
			return showJsonError(model, "参数异常");
		
		Adjustment ad = daoService.getObject(Adjustment.class, recordId);
		
		if (ad == null)
			return showJsonError(model, "参数异常"); 
		
		if (!AdjustmentStatusEnums.NEW.getStatus().equals(ad.getStatus()))
			return showJsonError(model, "只允许修改状态为：" + AdjustmentStatusEnums.NEW.getDisplay() + "的调整, 当前调整状态为：" + ad.getStatus() );
		
		User user = getLogonUser(request);
		if (!user.getUsername().equals(ad.getOperator()))
			return showJsonError(model, "只允许修改自己的调整,当前调整申请者为：" + ad.getOperator());
		
		ChangeEntry ce = new ChangeEntry(ad);
		ad.setAdjustNumber(adjustNumber);
		ad.setAmount(amount);
		ad.setReason(reason);
		ad.setComments(comments);
		if(StringUtils.isNotEmpty(filePath)){
			if(!filePath.startsWith("http")){
				ResultCode result = transFileFromTempToPersist(request, filePath);
				filePath = config.getPageMap().get("fileURL") + result.getRetval().toString();
			}
			ad.setAttachFilePath(filePath);
		}
		daoService.saveObject(ad);							
		monitorService.saveChangeLog(user.getId(), Adjustment.class, recordId, ce.getChangeMap(ad));
		return showJsonSuccess(model, "已修改");
	}
	
	@RequestMapping("/platform/adjust/saveAdjustment.xhtml")
	public String saveAdjustment(String tag, String relateId, String adjustNumber,String filePath,
								String amount, String reason, String comments , ModelMap model, HttpServletRequest request){
		if (StringUtils.isBlank(relateId))
			return showJsonError(model, "参数异常");
		
		User user = getLogonUser(request);
		
		if(StringUtils.isEmpty(amount)){
			return showJsonError(model, "请输入调整金额");
		}
		
		if(StringUtils.isNotEmpty(adjustNumber)){
			if(!adjustNumber.matches("^-?\\d+$")){
				return showJsonError(model, "调整数量只能是整数！");
			}			
		}
		
		if(!amount.matches("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+") && !amount.matches("^-?\\d+$")){
			return showJsonError(model, "调整金额只能是数字！");
		}
		ResultCode result = null;
		String attachFilePath = "";
		if(StringUtils.isNotEmpty(filePath)){
			result = transFileFromTempToPersist(request, filePath);
			attachFilePath = config.getPageMap().get("fileURL") + result.getRetval().toString();
		}
		result = adjustmentService.saveAdjust(tag, relateId, Double.valueOf(adjustNumber), Double.valueOf(amount), reason, comments, user.getUsername(),attachFilePath);
		
		if (result.isSuccess())
			return showJsonSuccess(model, "成功");
		
		return showJsonError(model, result.getMsg());
	}
	@RequestMapping("/platform/adjust/rejectAdjust.xhtml")
	public String rejectAdjust(Long recordId, ModelMap model, HttpServletRequest request){
		if (recordId == null)
			return showJsonError(model, "数据不正确.");
		
		Adjustment ad = daoService.getObject(Adjustment.class, recordId);
		if (ad == null || !AdjustmentStatusEnums.NEW.getStatus().equals(ad.getStatus()))
			return showJsonError(model, "没有找到关联数据或者该数据已审核或已废弃.");
		
		ChangeEntry ce = new ChangeEntry(ad);
		ad.setStatus(AdjustmentStatusEnums.REJECTED.getStatus());
		daoService.saveObject(ad);
		Long uid = getLogonUser(request).getId();
		monitorService.saveChangeLog(uid, Adjustment.class, recordId, ce.getChangeMap(ad));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/adjust/approveAdjust.xhtml")
	public String approveAdjust(Long recordId, ModelMap model, HttpServletRequest request){
		if (recordId == null)
			return showJsonError(model, "数据不正确.");
		
		Adjustment ad = daoService.getObject(Adjustment.class, recordId);
		if (ad == null || !AdjustmentStatusEnums.NEW.getStatus().equals(ad.getStatus()))
			return showJsonError(model, "没有找到关联数据或者该数据已经通过审核.");
		
		String adjustTag = ad.getTag();
		Long uid = getLogonUser(request).getId();
		
		if (AdjustTypeEnums.PLACE.getType().equals(adjustTag)){
			ad.setStatus(AdjustmentStatusEnums.APPROVED.getStatus());
			ad.setUpdateTime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(ad);
		}else{
			SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(ad.getRelateId()));
			
			if (bill == null)
				return showJsonError(model, "数据异常：" + recordId);
			
			if (!CheckBillStatusEnums.READJUST.getStatus().equals(bill.getStatus())){
				return showJsonError(model, "当前结算单状态：" + bill.getStatus() + 
						", 只允许通过:" + CheckBillStatusEnums.READJUST.getDisplay() + "的结算单");
			}
			ChangeEntry ce1 = new ChangeEntry(bill);
			adjustmentService.approvedAdjust(bill, ad);
			monitorService.saveChangeLog(uid, SettlementBill.class, bill.getRecordId(), ce1.getChangeMap(bill));
			/**
			 * 结算单调整审核通过，自动提交到商户
			 */
//			dbLogger.warn("结算单调整审核通过,settlebillId="+bill.getRecordId());
//			ResultCode result = settlementBillService.toMerchant(bill, getLogonUser(request));
//			if(!result.isSuccess()){
//				dbLogger.warn("结算单自动提交失败，需要手动调整,settlebillId="+bill.getRecordId()+",reason>>" + result.getMsg());
//			}
		}
		
		ChangeEntry ce = new ChangeEntry(ad);
		monitorService.saveChangeLog(uid, Adjustment.class, recordId, ce.getChangeMap(ad));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/platform/adjust/queryAllAdjustment.xhtml")
	public String queryAllAdjustment(String placeId, String queryreason, String status, Integer pageNo, Integer pageSize, ModelMap model, HttpServletRequest request){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		
		User gewaUser = getLogonUser(request);
		
		int totalNumber = adjustmentService.countingAdjustment(placeId, queryreason, status, gewaUser);
		if (totalNumber <= 0)
			return "/adjust/allAdjustment.vm";
		
		List<Adjustment> ads = adjustmentService.queryAdjustment(placeId, queryreason, status, gewaUser, pageNo, pageSize);
		Map<String, List> groupBeanList = BeanUtil.groupBeanList(ads, "tag");
		List list = groupBeanList.get(AdjustTypeEnums.SETTLEBILL.getType());
		if (CollectionUtils.isNotEmpty(list)){
			List<String> billStrIds = BeanUtil.getBeanPropertyList(list, "relateId", true);
			List<Long> billIds = new ArrayList<Long>();
			for (String str : billStrIds) billIds.add(Long.valueOf(str));
			Map<Long, SettlementBill> sbmap = daoService.getObjectMap(SettlementBill.class, billIds);
			model.put("sbm", sbmap);
		}
		
		List configIds = BeanUtil.getBeanPropertyList(ads, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, configIds);
		model.put("ads", ads);
		model.put("pm", placeMap);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo,
											config.getBasePath() + "platform/adjust/queryAllAdjustment.xhtml",
											true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("queryreason", queryreason);
		m.put("placeId", placeId);
		m.put("status", status);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		return "/adjust/allAdjustment.vm";
		
	}
	
	@RequestMapping("/platform/adjust/queryUnApprovedAdjustment.xhtml")
	public String queryUnApprovedAdjustment(String placeId, String operator, String reason, Integer pageNo, Integer pageSize, ModelMap model){
		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? 30 : pageSize;
		
		int totalNumber = adjustmentService.countingUnApprovedAdjustment(placeId, reason, operator);
		if (totalNumber <= 0)
			return "/adjust/unApprovedAdjustment.vm";
		
		List<Adjustment> ads = adjustmentService.queryUnApprovedAdjustment(placeId, reason, operator, pageNo, pageSize);
		Map<String, List> groupBeanList = BeanUtil.groupBeanList(ads, "tag");
		List list = groupBeanList.get(AdjustTypeEnums.SETTLEBILL.getType());
		
		if (!CollectionUtils.isEmpty(list)){
			List<String> billStrIds = BeanUtil.getBeanPropertyList(list, "relateId", true);
			List<Long> billIds = new ArrayList<Long>();
			
			for (String str : billStrIds) billIds.add(Long.valueOf(str));
			
			Map<Long, SettlementBill> sbmap = daoService.getObjectMap(SettlementBill.class, billIds);
			model.put("sbm", sbmap);
		}
		List configIds = BeanUtil.getBeanPropertyList(ads, "configId", true);
		Map<String, Place> placeMap = daoService.getObjectMap(Place.class, configIds);
										
		model.put("ads", ads);
		model.put("pm", placeMap);
		PageUtil pageUtil = new PageUtil(totalNumber, pageSize, pageNo,
											config.getBasePath() + "platform/adjust/queryUnApprovedAdjustment.xhtml",
											true, true);
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("reason", reason);
		m.put("placeId", placeId);
		pageUtil.initPageInfo(m);
		model.put("pageUtil", pageUtil);
		model.put("billTypeMap", SettleConstant.BILLTYPEMAP);
		return "/adjust/unApprovedAdjustment.vm";
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}
	
//	private long getFileFromRemote(ResponseStreamWriter pw, Path path) throws IOException {
//		InputStream in = null;
//		try {
//			if(fs.exists(path) && fs.isFile(path)){
//				in = fs.open(path);
//				if(in.available()< 50){//少于50byte，不能保存
//					fs.delete(path, false);
//					dbLogger.warn("file is empty:" + path.toString());
//					return 0L;
//				}else{
//					FileStatus status = fs.getFileStatus(path);
//					if(pw!=null) pw.write(in, status.getModificationTime(), 5184000, DateUtil.addDay(new Date(), 60).getTime());
//					return status.getModificationTime();
//				}
//			}
//		} catch (MalformedURLException e) {
//			dbLogger.error("", e);
//		} finally {
//			if(in!=null) IOUtils.closeStream(in);
//		}
//		dbLogger.warn("file is empty:" + path.toString());
//		return 0L;
//	}
//	/* (non-Javadoc)
//	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
//	 */
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		Configuration conf = new Configuration();
//		try {
//			fs = FileSystem.get(URI.create(config.getString("hdfsUrl")), conf);
//		} catch (IOException e) {
//			dbLogger.error("", e);
//		}
//		
//	}
	
	@RequestMapping("platform/adjust/uploadAdjustAccordingFile.xhtml")
	@ResponseBody
	public String uploadAdjustAccordingFile(HttpServletRequest request){
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
 		MultipartFile file = multipartRequest.getFile("file");
		String oriName = file.getOriginalFilename();
		ResultCode<String> result = ResultCode.SUCCESS;
		try {
			result = uploadFileToTemp(file.getBytes(), oriName);
			if(!result.isSuccess()){
				return "<script>window.parent.uploadSucced('" + result.getMsg() + "');</script>";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<script>window.parent.uploadSucced('" + result.getRetval() + "');</script>";
	}
	
	private ResultCode uploadFileToTemp(byte[] fileBuff, String fileName){
		String tempUrl = config.getString("uploadTempURL");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("callbackUrl", "no call back");
		
		HttpResult result = HttpUtils.uploadFile(tempUrl, params, fileBuff, "noinput", fileName);
		Map tempResult = JsonUtils.readJsonToMap(result.getResponse());
		String successFile = (String) tempResult.get("successFile");
		if (StringUtils.isBlank(successFile)){
			dbLogger.error("upload file failed:" + result.getResponse());
			return ResultCode.getFailure("上传到临时文件时发生错误");
		}
		return ResultCode.getSuccessReturn(successFile);
	}
	/** 
	 * @auth yujun.su@gewara.com 
	 * @Title: transFileFromTempToPersist  
	 * @Description:
	 * @param @param request
	 * @param @param remoteUrl
	 * @param @param result
	 * @param @param successFile
	 * @param @return
	 * @return ResultCode
	 * @throws  
	 */
	private ResultCode transFileFromTempToPersist(HttpServletRequest request,String successFile) {
		String remoteUrl = config.getString("uploadComit");
		Map<String, String> commitParams = new HashMap<String, String>();
		Long mid = getLogonUser(request).getId();
		commitParams.put("userid", mid.toString());
		commitParams.put("systemid", "GSP");
		String dateStr = DateUtil.format(new Date(), "yyyyMM");
		String remoteFold = "/upload/gsp/" + dateStr + "/";
		commitParams.put("path", remoteFold);
		commitParams.put("files", successFile);
		commitParams.put("check", StringUtil.md5(successFile + remoteFold + "GSP" + mid + "GewaUploadFile"));
		HttpResult r = HttpUtils.postUrlAsString(remoteUrl, commitParams);
		if (!"SUCCESS".equals(r.getResponse())){
			dbLogger.error("upload file failed:");
			return ResultCode.getFailure(r.getResponse());
		}
			
		return ResultCode.getSuccessReturn(remoteFold + successFile);
	}
	
	@RequestMapping("platform/adjust/batchConfirmPass.xhtml")
	public String batchConfirmPass(ModelMap model,String recordIds,HttpServletRequest request){
		if(StringUtils.isEmpty(recordIds)){
			return showJsonError(model, "参数异常");
		}
		String[] adjustIds = recordIds.split(",");
		for(String adjustId : adjustIds){
			Long recordId = Long.valueOf(adjustId);
			Adjustment ad = daoService.getObject(Adjustment.class, recordId);
			if (ad == null || !AdjustmentStatusEnums.NEW.getStatus().equals(ad.getStatus())){
				continue;
			}
			
			String adjustTag = ad.getTag();
			Long uid = getLogonUser(request).getId();
			
			if (AdjustTypeEnums.PLACE.getType().equals(adjustTag)){
				ad.setStatus(AdjustmentStatusEnums.APPROVED.getStatus());
				ad.setUpdateTime(DateUtil.getCurFullTimestamp());
				daoService.saveObject(ad);
			}else{
				SettlementBill bill = daoService.getObject(SettlementBill.class, Long.valueOf(ad.getRelateId()));
				
				if (bill == null){
					continue;
				}
				if (!CheckBillStatusEnums.READJUST.getStatus().equals(bill.getStatus())){
					continue;
				}
				ChangeEntry ce1 = new ChangeEntry(bill);
				adjustmentService.approvedAdjust(bill, ad);
				monitorService.saveChangeLog(uid, SettlementBill.class, bill.getRecordId(), ce1.getChangeMap(bill));
				dbLogger.warn("结算单调整审核通过,settlebillId="+bill.getRecordId());
			}
			
			ChangeEntry ce = new ChangeEntry(ad);
			monitorService.saveChangeLog(uid, Adjustment.class, recordId, ce.getChangeMap(ad));
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 演出结算调整审核
	 * @return
	 */
	@RequestMapping("platform/adjust/queryDramaUnapprovedAdjust.xhtml")
	public String queryDramaUnapprovedAdjust(Long settleid, Long dramaid, String optuser, String reason) {
		
		return "adjust/dramaUnapprovedAdjust.vm";
	}
}
