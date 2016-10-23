package com.gewara.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.gewara.constant.SettleConstant;
import com.gewara.enums.AdjustReasonEnums;
import com.gewara.model.gsp.Adjustment;
import com.gewara.model.gsp.SettleConfig;

/**
 * @author Zhicheng.Peng Johnny.Resurgam@Gmail.com
 * 
 *         Aug 16, 2013 4:54:09 PM
 */
public class MiscUtil {
	
	private static final String UNIT = "万仟佰拾亿仟佰拾万仟佰拾元角分";
	private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
	private static final double MAX_VALUE = 9999999999999.99D;
	
	
	/**
	 * 增加条件：不参加结算的订单category
	 * @param query
	 */
	public static void appendCategoryQueryCondition(DetachedCriteria query){
		for (String str : SettleConstant.UNSETTLECATE){
			query.add(Restrictions.ne("category", str));
		}
	}
	
	
	public static String groupAdjustReasons(List<Adjustment> adjustments) {
		Set<String> r = BeanUtil.groupBeanList(adjustments, "reason").keySet();
		List<String> converted = new ArrayList<String>();
		for (String str : r) {
			converted.add(AdjustReasonEnums.valueOf(str).getDisplay());
		}

		return StringUtils.join(converted, ",");
	}

	public static Timestamp defaultTimeForPlace(SettleConfig sc, Timestamp t) {
		if (sc == null || t == null)
			return null;
		String time = sc.getSettleTime();
		String rtStr = DateUtil.formatDate(t) + " " + time;
		return DateUtil.parseTimestamp(rtStr, "yyyy-MM-dd HH:mm");
	}

	public static byte[] AESencrypt(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**将二进制转换成16进制 
	 * @param buf 
	 * @return 
	 */  
	public static String parseByte2HexStr(byte buf[]) {  
	        StringBuffer sb = new StringBuffer();  
	        for (int i = 0; i < buf.length; i++) {  
	                String hex = Integer.toHexString(buf[i] & 0xFF);  
	                if (hex.length() == 1) {  
	                        hex = '0' + hex;  
	                }  
	                sb.append(hex.toUpperCase());  
	        }  
	        return sb.toString();  
	}
	
	public static Timestamp getValidateStart(){
		//String str = "2015-02-01 00:00:00";  //v2.1上线第二天
		String str = "2016-01-01 00:00:00";  //v2.1上线第二天
		return DateUtil.parseTimestamp(str); 
	}
	
	
	
	public static final String getCheckBillConfigId(String tag,String configId){
		String placeId = configId.split(",")[0];
		return RecordIdUtils.contactRecordId(tag, Long.valueOf(placeId));
	}
	
	public static final String convertMoneyChineseFmt(Double amount){

		  if (amount < 0 || amount > MAX_VALUE){
			  return "参数非法!";
		  }
		  long l = Math.round(amount * 100);
		  if (l == 0){
			  return "零元整";
		  }
		  String strValue = l + "";
		  // i用来控制数
		  int i = 0;
		  // j用来控制单位
		  int j = UNIT.length() - strValue.length();
		  String rs = "";
		  boolean isZero = false;
		  for (; i < strValue.length(); i++, j++) {
		   char ch = strValue.charAt(i);
		   if (ch == '0') {
		    isZero = true;
		    if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
		     rs = rs + UNIT.charAt(j);
		     isZero = false;
		    }
		   } else {
		    if (isZero) {
		     rs = rs + "零";
		     isZero = false;
		    }
		    rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);
		   }
		  }
		  if (!rs.endsWith("分")) {
		   rs = rs + "整";
		  }
		  rs = rs.replaceAll("亿万", "亿");
		  return rs;
	}
	
	
	/**
	 * 区分万达活动场和非活动场
	 * @return
	 */
	public static String getSpecial(Map<String, Object> row) {
		if(row == null) {
			return null;
		}
		String orderType = row.get("ORDER_TYPE") == null ? null : StringUtils.upperCase((String)row.get("ORDER_TYPE"));
		if(!SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(orderType)) {
			return null;
		}
		String category = row.get("CATEGORY") == null ? null : (String)row.get("CATEGORY");
		if(SettleConstant.WDCATE.contains(category)) { //万达订单
			String otherinfo = row.get("OTHERINFO") == null ? null : (String)row.get("OTHERINFO");
			if(otherinfo == null || "".equals(otherinfo)) {
				return null;
			}
			Map otherMap = JsonUtils.readJsonToMap(otherinfo);
			String ticketId = otherMap.get("ticketId") == null ? null : (String)otherMap.get("ticketId");
			if(ticketId == null || "".equals(ticketId)) {
				return null;
			}
			if(ticketId.indexOf("非网购票") > 0) {
				return SettleConstant.WANDA_ACTIVITY;
			}
			return null;
		}
		return null;
	}
	
	/**
	 * 区分和平影都，和平影都placeid为2
	 * @return
	 */
	public static Long getPlaceId(Map<String, Object> row, Long placeid) {
		if(row == null) {
			return placeid;
		}
		if(placeid == null) {
			return placeid;
		}
		if(placeid != 2) {
			return placeid;
		}
		String desc = row.get("DESCRIPTION2") == null ? null : (String)row.get("DESCRIPTION2");
		if(desc == null || "".equals(desc)) {
			return placeid;
		}
		Map otherMap = JsonUtils.readJsonToMap(desc);
		String room = otherMap.get("影厅") == null ? null : (String)otherMap.get("影厅");
		if(room == null || "".equals(room)) {
			return placeid;
		}
		//非和平影厅
		if(room.indexOf(SettleConstant.HEPING_ROMNAME) > 0) {
			return SettleConstant.HEPING_OUTER_PLACEID;
		}
		return placeid;
	}
	
	/**
	 * 四舍五入保留小数
	 * @param money
	 * @return
	 */
	public static Double moneyFormat(Double money) {
		BigDecimal bd = new BigDecimal(money);
		return bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	/**
	 * 用户包场字段
	 * @param row
	 * @return
	 */
	public static String getUserBaoChang(Map<String, Object> row) {
		if(row == null) {
			return null;
		}
		String orderType = row.get("ORDER_TYPE") == null ? null : StringUtils.upperCase((String)row.get("ORDER_TYPE"));
		if(!SettleConstant.TAG_SETTLEMENTBILL_TICKET.equals(orderType)) {
			return null;
		}
		String otherinfo = row.get("OTHERINFO") == null ? null : (String)row.get("OTHERINFO");
		if(otherinfo == null || "".equals(otherinfo)) {
			return null;
		}
		Map otherMap = JsonUtils.readJsonToMap(otherinfo);
		String baoChang = otherMap.get("baoChangType") == null ? null : (String)otherMap.get("baoChangType");
		return baoChang;
	}
	
	public static Timestamp addMonth(Timestamp tt, int n) {
		Calendar cd = Calendar.getInstance();
		cd.setTimeInMillis(tt.getTime());
		cd.add(Calendar.MONTH, n);
		return new Timestamp(cd.getTimeInMillis());
	}
	
	public static String sortSettleid(String settleids) {
		String[] strs = settleids.split(",");
		Long[] lids = new Long[strs.length];
		for(int i = 0; i < strs.length; i++) {
			lids[i] = Long.valueOf(strs[i]);
		}
		Arrays.sort(lids);
		String result = "";
		for(int i = 0; i < lids.length; i++) {
			if(i == (lids.length - 1)) {
				result += lids[i] + "";
			} else {
				result += lids[i] + ",";
			}
		}
		return result;
	}
	
}
