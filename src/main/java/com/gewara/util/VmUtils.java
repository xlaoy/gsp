package com.gewara.util;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustReasonEnums;
import com.gewara.enums.CheckBillStatusEnums;
import com.gewara.enums.DataExtSqlEmums;
import com.gewara.enums.OrderTypeEnums;
import com.gewara.enums.PlaceTypeEnums;
import com.gewara.enums.SettleBaseEnums;
import com.gewara.enums.SettleCycleEnums;
import com.gewara.model.gsp.Place;

/**
 * 每个项目都有自己独立的实现
 * @author gebiao(ge.biao@gewara.com)
 * @since Jul 10, 2012 10:37:52 PM
 */
public class VmUtils extends VmBaseUtil {
	//~~~~~~~~~~~~~~~图片路径~~~~~~~~~~~~~~~~~~~~
	private List<String> imgPathList;
	private String cssJsPath;
	private String imgPath;
	private static Map<String, Class> enumsMapping = new HashMap<String, Class>();
	
	static{
		enumsMapping.put("DataExtSqlEmums", DataExtSqlEmums.class);
		enumsMapping.put("OrderTypeEnums", OrderTypeEnums.class);
		enumsMapping.put("PlaceTypeEnums", PlaceTypeEnums.class);
		enumsMapping.put("SettleBaseEnums", SettleBaseEnums.class);
		enumsMapping.put("SettleCycleEnums", SettleCycleEnums.class);
		enumsMapping.put("CheckBillStatusEnums", CheckBillStatusEnums.class);
		enumsMapping.put("AdjustReasonEnums", AdjustReasonEnums.class);
	}
	
	public String getImgPath() {
		return imgPath;
	}
	public String getCssJsPath() {
		return cssJsPath;
	}
	private String defPath;
	public  String randomPic(String wh, String imgName){
		if(StringUtils.startsWith(imgName,"http")) return imgName;
		if(imgPathList==null || imgPathList.size()==0) return defPath + wh + "/" + imgName;
		if(StringUtils.isBlank(imgName)) imgName = "img/default_head.png";
		int hashcode = Math.abs(imgName.hashCode());
		int i = (hashcode)%(imgPathList.size());
		if(StringUtils.isBlank(wh)) return imgPathList.get(i) + imgName;
		return imgPathList.get(i) + wh + "/" + imgName;
	}
	public void initStatic(String staticPath){
		this.cssJsPath = staticPath;
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getScriptObject(Object modelObject){
		return JsonUtils.writeObjectToJson(modelObject);
	}
	public String getScriptString(String str){
		str = JsonUtils.writeObjectToJson(str);
		return str;
	}
	public static String getUniqueString(String str, String splitter){
		if(StringUtils.isBlank(str)) return "";
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(Arrays.asList(str.split(splitter)));
		return join(set.iterator(), splitter);
	}
	public static Map<String, String> readJsonToMap(String json){
		if(StringUtils.isBlank(json)) return new HashMap();
		Map<String,String> result = JsonUtils.readJsonToMap(json);
		if(result == null) result = new HashMap<String, String>();
		return result;
	}
	
	public static boolean isValidCaptchaId(String captchaId) {
		if(StringUtils.length(captchaId) != 24) return false;
		return StringUtil.md5(StringUtils.substring(captchaId, 0, 16) + "sk#8Kr", 8).equals(StringUtils.substring(captchaId, 16));
	}
	public static String getRandomCaptchaId() {
		String s = StringUtil.getRandomString(16) ;
		s += StringUtil.md5(s + "sk#8Kr", 8);
		return s;
	}
	public static String replaceAll(String text, String seachText, String replaceTo){
		if (StringUtils.isBlank(text))
			return "";
		
		String r = text.replaceAll(seachText, replaceTo);
		return r;
	}
	
	public static String displayEnums(String enumName, String tag){
		if (StringUtils.isBlank(enumName) || StringUtils.isBlank(tag))
			return "";
		Class clazz = enumsMapping.get(enumName);
		if (clazz == null)
			return "";
		try {
			Method m = clazz.getDeclaredMethod("getDisplay");
			Enum o = Enum.valueOf(clazz, tag);
			return m.invoke(o, new Object[]{}).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String getPlaceName(Map<String, Place> pm, String tag, Long relateId){
		String key = "" + relateId + "," + tag;
		if (pm.get(key) == null) return "";
		return pm.get(key).getName();
	}
	public static double calculate(Object fnum, Object snum, String operator){
		if (fnum == null || snum == null || StringUtils.isBlank(operator))
			return Integer.MIN_VALUE;
		double num1 = Double.valueOf(fnum.toString());
		double num2 = Double.valueOf(snum.toString());
		if("+".equals(operator))
			return num1 + num2;
		if("-".equals(operator))
			return num1 - num2;
		if("*".equals(operator))
			return num1 * num2;
		if("/".equals(operator))
			return num1 / num2;
		
		return Integer.MIN_VALUE;
	}
	public String generateCheckBillAllOptions(String checkedValue){
		return checkBillOption(checkedValue, true);
	}
	public boolean isOneDayDiff(Timestamp t1, Timestamp t2){
		int diffDay = DateUtil.getDiffDay(t1, t2);
		return diffDay == 1;
	}
	
	private String checkBillOption(String checkedValue, boolean isAll){
		CheckBillStatusEnums[] results = CheckBillStatusEnums.values();
		StringBuffer options = new StringBuffer("");
		options.append("<option value=''>未选择</option>");
		for (CheckBillStatusEnums result : results){
			if (result == CheckBillStatusEnums.NEW ||
				result == CheckBillStatusEnums.MERCHANTCONFIRMED ||
				result == CheckBillStatusEnums.GEWAREADJUST )
				continue;
			
			options.append("<option value=").append(result.getStatus());
			if (result.getStatus().equals(checkedValue))
				options.append(" selected ");
			options.append(">").append(result.getDisplay()).append("</option>");
		}
		return options.toString();
	}
	
	public String adjustReasionOptions(String checkedValue){
		AdjustReasonEnums[] results = AdjustReasonEnums.values();
		StringBuffer options = new StringBuffer("");
		options.append("<option value=''>未选择</option>");
		for (AdjustReasonEnums result : results){
//			if(result.getType().equals(AdjustReasonEnums.SERVICEFEE.getType()) || result.getType().equals(AdjustReasonEnums.GOODS.getType())){
//				continue;
//			}
			options.append("<option value=").append(result.getType());
			if (result.getType().equals(checkedValue))
				options.append(" selected ");
			options.append(">").append(result.getDisplay()).append("</option>");
		}
		return options.toString();
	}
	
	
	public Long stringToLong(Object value){
		try {
			return Long.valueOf(value.toString());
		} catch (Exception e) {
			return null;
		}
	}
	
	public String mapToSelect(Map<String, String> map, String checkedValue){
		StringBuffer options = new StringBuffer("<option value=''>全部</option>");
		if (map == null || map.size() == 0)
			return "";
		
		for(Entry<String, String> entry : map.entrySet()){
			options.append("<option value=").append(entry.getKey());
			if (entry.getKey().equals(checkedValue))
				options.append(" selected ");
			options.append(">").append(entry.getValue()).append("</option>");
		}
//		for (String key : map.keySet()){
//			options.append("<option value=").append(key);
//			if (key.equals(checkedValue))
//				options.append(" selected ");
//			options.append(">").append(map.get(key)).append("</option>");
//		}
		return options.toString();
	}
	
	public String generateCheckBillOptions(String checkedValue){
		return checkBillOption(checkedValue, false);
	}
	
	public Map ck2Map(){
		CheckBillStatusEnums[] values = CheckBillStatusEnums.values();
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (CheckBillStatusEnums cks : values){
			if (cks == CheckBillStatusEnums.MERCHANTCONFIRMED || cks == CheckBillStatusEnums.GEWAREADJUST)
				continue;
			
			result.put(cks.getStatus(), cks.getDisplay());
		}
		return result;
	}
	
	public static String getTotal(Object value){
		if(value == null) return "-";
		if(value instanceof Number){
			return NumberFormat.getInstance().format(value);
		}
		return ""+value;
	}
	
	public static boolean canPay(String special, String payVenderNo, String playType) {
		if(!SettleConstant.ZL.equals(playType)) {
			return true;
		} else if(SettleConstant.WANDA_ACTIVITY.equals(special)) {
			return true;
		} else if(!SettleConstant.MEGERPAYVENDER.contains(payVenderNo)) {
			return true;
		}
		return false;
	}
	
	public static String format(Timestamp time) {
		if(time == null) {
			return "";
		}
		return DateUtil.format(time, "yyyy-MM-dd");
	}
}