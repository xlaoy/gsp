package com.gewara.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.HtmlUtils;

public class PageUtil {
	public static final int pageSize = 10;
	//request 中定义的参数名称
	public static final String PARAM_PAGE_NUM = "pageNo";
	public static final String PARAM_ROWS_COUNT = "rowsCount";
	public static final String PARAM_ROWS_PER_PAGE ="rowsPerPage";
	private int currentPage;//当前页
	private int rowsCount;//记录数
	private int startNum;//显示的第一页码
	private int endNum;//显示的最后
	private int pageCount;//分页数量
	private int rowsPerPage;//每页显示数量
	private boolean isShowFirst;
	private boolean isShowLast;
	private String scriptParams;
	private String commonParams;
	private String firsturl;
	private String preurl;
	private String nexturl;
	private String lasturl;
	private String encode = "UTF-8";
	private List<PageInfo> pageInfoList;
	private String baseUrl;
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}
	public List<PageInfo> getPageInfoList() {
		return pageInfoList;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public int getRowsCount() {
		return rowsCount;
	}
	public int getRowsPerPage() {
		return rowsPerPage;
	}
	public int getPageCount() {
		return pageCount;
	}
	public PageUtil(int rowsCount, int rowsPerPage, int currentPage, String url){
		this.rowsCount = rowsCount;
		this.rowsPerPage = rowsPerPage;
		this.currentPage = currentPage;
		this.pageCount = (rowsCount-1)/rowsPerPage + 1;
		this.startNum = Math.max(0, currentPage - 3);
		this.endNum = Math.min(startNum + 6 , pageCount);
		if(StringUtils.isBlank(url)) this.baseUrl="";
		else this.baseUrl = url;
	}
	public PageUtil(int rowsCount, int rowsPerPage, int currentPage, String url, boolean isShowFirst, boolean isShowLast){
		this.rowsCount = rowsCount;
		this.rowsPerPage = rowsPerPage;
		this.currentPage = currentPage;
		this.pageCount = (rowsCount-1)/rowsPerPage + 1;
		this.startNum = Math.max(0, currentPage - 3);
		this.endNum = Math.min(startNum + 6, pageCount);
		if(StringUtils.isBlank(url)) this.baseUrl="";
		else this.baseUrl = url;
		this.isShowFirst = isShowFirst;
		this.isShowLast = isShowLast;
	}
	/**
	 * 获取不同分页的URI query查询串，如<br>
	 * param1=xxxx&param2=YYYYY&pageNo=0<br>
	 * param1=TTTT&param2=SSSSS&pageNo=1<br>
	 * .......
	 **/
	public void initPageInfo(Map params, List paramNames){
		pageInfoList = new ArrayList<PageInfo>();
		//1、获取查询串
		String commonParam = "";
		String scriptParam = "{";
		if(params != null){
			if(paramNames==null) paramNames = new ArrayList(params.keySet());
			for(Object paramName: paramNames){
				if(paramName.equals("pageNo")) continue;
				Object values = params.get(paramName);
				if(values==null) continue;
				String[] tmp = null;
				if(values instanceof String[]) tmp = (String[])values;
				else tmp = new String[]{"" + values};
				for(String value:tmp){
					if(StringUtils.isNotBlank(value)){
						try {
							commonParam += "&" + paramName + "=" + URLEncoder.encode(value,encode);
							scriptParam += "'" + paramName + "':'" + HtmlUtils.htmlEscape(value) + "',";
						} catch (UnsupportedEncodingException e) {
						}
					}
				}
			}
		}
		//2、整理出页码链接
		if(scriptParam.length() > 1) this.scriptParams = scriptParam.substring(0, scriptParam.length()-1) + "}";
		this.commonParams = commonParam;
		if(isPrePage()){//有上一页
			String tmpUrl = baseUrl;
			int pn = currentPage-1;
			if(pn == 0){
				if(StringUtils.isNotBlank(commonParam))
					tmpUrl += "?" + StringUtils.substring(commonParam,1);
			}else tmpUrl += "?pageNo=" + pn + commonParam;
			this.preurl = tmpUrl;
		}
		for(int pn=startNum; pn<endNum; pn++){
			PageInfo pageInfo = new PageInfo();
			String tmpUrl = baseUrl;
			if(pn == 0){
				if(StringUtils.isNotBlank(commonParam))
					tmpUrl += "?" + StringUtils.substring(commonParam,1);
			}else tmpUrl += "?pageNo=" + pn + commonParam;
			pageInfo.setUrl(tmpUrl);
			pageInfo.setPageNo(""+(pn+1));//显示的页码
			pageInfo.setRealPageNo(pn);
			if(pn == currentPage) pageInfo.setCurrentPage(true);
			if(isLastPage()) {
				if((pn+1)!=pageCount)pageInfoList.add(pageInfo);
			}else pageInfoList.add(pageInfo);
		}
		if(isNextPage()){//有下一页
			this.nexturl = baseUrl + "?pageNo=" + (currentPage + 1) + commonParam;
		}
		if(isFirstPage()){
			if(StringUtils.isNotBlank(commonParam))	this.firsturl = baseUrl +"?" + StringUtils.substring(commonParam, 1);
			else this.firsturl = baseUrl;
		}
		if(isLastPage()){
			this.lasturl = baseUrl + "?pageNo=" + (pageCount-1) + commonParam;
		}
	}
	public void initPageInfo(){
		initPageInfo(null);
	}
	public void initPageInfo(Map<String,?> params){
		initPageInfo(params, null);
	}
	public String getCommonParams() {
		return commonParams;
	}
	public void setCommonParams(String commonParams) {
		this.commonParams = commonParams;
	}
	public String getScriptParams() {
		return scriptParams;
	}
	public void setScriptParams(String scriptParams) {
		this.scriptParams = scriptParams;
	}
	public boolean getIsShowFirst() {
		return isShowFirst;
	}
	public void setIsShowFirst(boolean isShowFirst) {
		this.isShowFirst = isShowFirst;
	}
	public boolean getIsShowLast() {
		return isShowLast;
	}
	public void setIsShowLast(boolean isShowLast) {
		this.isShowLast = isShowLast;
	}
	public String getFirsturl() {
		return firsturl;
	}
	public void setFirsturl(String firsturl) {
		this.firsturl = firsturl;
	}
	public String getPreurl() {
		return preurl;
	}
	public void setPreurl(String preurl) {
		this.preurl = preurl;
	}
	public String getNexturl() {
		return nexturl;
	}
	public void setNexturl(String nexturl) {
		this.nexturl = nexturl;
	}
	public String getLasturl() {
		return lasturl;
	}
	public void setLasturl(String lasturl) {
		this.lasturl = lasturl;
	}
	
	public boolean isFirstPage(){
		if(this.isShowFirst && currentPage>5) return true;
		return false;
	}
	
	public boolean isPrePage(){
		if(this.currentPage>0) return true;
		return false;
	}
	
	public boolean isNextPage(){
		if(this.currentPage+1 < this.pageCount)	return true;
		return false;
	}
	
	public boolean isLastPage(){
		if(this.isShowLast && pageCount>5) return true;
		return false;
	}
	public boolean isOnLast(String pageNo ){
		if(StringUtils.isBlank(pageNo)) pageNo="0";
		if(isLastPage() && StringUtils.equals((Integer.valueOf(pageNo)+1)+"", pageCount+"")) return true;
		return false;
	}
}
