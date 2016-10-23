package com.gewara.web.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SettleConstant;
import com.gewara.model.gsp.SysData;
import com.gewara.service.SysDataService;
import com.gewara.util.DateUtil;

/**
 * ����������ݹ���
 * @author user
 *
 */

@Controller
public class SysDataController extends AnnotationController {

	@Autowired
	@Qualifier("sysDataService")
	private SysDataService sysDataService;
	
	/**
	 * ��ѯ��������
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/sysdata/querySysData.xhtml")
	public String querySysData(SysData data, Integer pageNo, Integer pageSize, ModelMap model, HttpServletRequest request) {
		if (!checkSpecialUrl(request)) {
			return showJsonError(model, "��Ȩ�ޣ�");
		}
		String url = "/platform/sysdata/querySysData.xhtml";
		if(StringUtils.isEmpty(data.getType())) {
			data.setType(SettleConstant.SYSCATEGORY);
		}
		model = sysDataService.querySysData(data, pageNo, pageSize, url, model);
		return "/settleconfig/settleSysData.vm";
	}
	
	/**
	 * ���ػ�����������
	 * @param recordid
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/sysdata/loadSysDateDetail.xhtml")
	public String loadSysDateDetail(Long recordid, ModelMap model, HttpServletRequest request) {
		if (!checkSpecialUrl(request)) {
			return showJsonError(model, "��Ȩ�ޣ�");
		}
		if(recordid == null) {
			return "/settleconfig/sysDataPop.vm";
		}
		SysData data = daoService.getObject(SysData.class, recordid);
		model.put("data", data);
		return "/settleconfig/sysDataPop.vm";
	}
	
	/**
	 * ��������
	 * @param dbData
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/sysdata/saveSysDate.xhtml")
	public String saveSysDate(SysData data, ModelMap model, HttpServletRequest request) {
		if (!checkSpecialUrl(request)) {
			return showJsonError(model, "��Ȩ�ޣ�");
		}
		if(StringUtils.isBlank(data.getType())) {
			return showJsonError(model, "type����Ϊ�գ�");
		}
		if(StringUtils.isBlank(data.getCode())) {
			return showJsonError(model, "code����Ϊ�գ�");
		}
		data.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(data);
		return showJsonSuccess(model, "����ɹ���");
	}
	
	/**
	 * ɾ������
	 * @param dbData
	 * @param model
	 * @return
	 */
	@RequestMapping("/platform/sysdata/delSysDate.xhtml")
	public String delSysDate(Long recordid, ModelMap model, HttpServletRequest request) {
		if (!checkSpecialUrl(request)) {
			return showJsonError(model, "��Ȩ�ޣ�");
		}
		if(recordid == null) {
			return showJsonError(model, "��������");
		}
		SysData data = daoService.getObject(SysData.class, recordid);
		daoService.removeObject(data);
		return showJsonSuccess(model, "ɾ���ɹ���");
	}
	
}
