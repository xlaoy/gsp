package com.gewara.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28����02:05:17
 */
public abstract class PinYinUtils {
	private static HanyuPinyinOutputFormat fmt = new HanyuPinyinOutputFormat();
	static{
		fmt.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	}
	// �õ�һ���ֵ�ƴ��.
	private static String getPinyin(char tt) {
		try {
			return PinyinHelper.toHanyuPinyinStringArray(tt, fmt)[0];
		} catch (Exception e) {
			return ""+tt; //ԭ������
		}
	}
	public static String getPinyin(String target) {
		if(StringUtils.isBlank(target)) return "";
		int len = target.length();
		StringBuilder bf = new StringBuilder();
		for (int j = 0; j < len; j++) {
			bf.append(getPinyin(target.charAt(j)));
		}
		return bf.toString();
	}
	public static String getFirstSpell(String target){
		if(target==null){target="";}
		int len = target.length();
		StringBuilder bf = new StringBuilder();
		String py=null;
		for (int j = 0; j < len; j++) {
				py = getPinyin(target.charAt(j));
				if(StringUtils.isNotBlank(py)){
					if(StringUtils.isAlpha(py)) bf.append(py.charAt(0));
				}
		}
		return bf.toString();
	}
	/**
	 * 
	 * @param chineses
	 * @return �������=jqwd
	 */
	public static Map<String, String> getPinyinMap(String[] chineses){
		Map<String, String> pinyinMap = new HashMap<String, String>();
		for (String str : chineses){
			pinyinMap.put(str, getFirstSpell(str));
		}
		return pinyinMap;
	}
}
