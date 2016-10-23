package com.gewara.util;

import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;

/**
 * 帮助类。 用户格式化货币
 * @author pengzhicheng
 *
 */
public class CurrencyFormatter {
	
	private  NumberFormat currencyFormatter;
	private  String currencySymbol;
	public CurrencyFormatter(){
		currencyFormatter = NumberFormat.getCurrencyInstance();
		currencySymbol = currencyFormatter.getCurrency().getSymbol();
	}
	
	/**
	 * 将以分为单位的货币转换成以元为单位， 保留两位小数, 不保留货币符号
	 * @param fen
	 * @return
	 */
	public String parseFenToYuan(String fen){
		return parseFenToYuan(fen, false);
	}
	public String format(double d){
		String r = currencyFormatter.format(d);
		r = r.replace(currencySymbol, "");
		return r;
	}
	
	public String parseFenToYuan(String fen, boolean isKeepProFix){
		if (fen == null || "".equals(fen)) return "0";
		double d = Double.valueOf(fen);
		String first="";
		if (d < 0){
			d = d * -1;
			first = "-";
		}
		
		d /= 100;
		String r = currencyFormatter.format(d);
		if (!isKeepProFix){
			r = r.replace(currencySymbol, "");
		}
		return first + r;
	}
	
	public String min(String num1, String num2){
		if (StringUtils.isBlank(num1) || StringUtils.isBlank(num2))
			return "0";
		
		return parseFenToYuan(String.valueOf(Integer.valueOf(num1) - Integer.valueOf(num2)));
	}
	
	public String parseFenToYuanWithoutComma(String fen){
		return parseFenToYuan(fen).replace(",", "");
	}
}
