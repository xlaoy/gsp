/** 
 */
package com.gewara.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Nov 21, 2012  3:56:45 PM
 */
public class ResultCode<T> implements Serializable{
	private static final long serialVersionUID = 4418416282894231647L;
	
	public static final String CODE_SUCCESS = "0000";	
	public static final String CODE_NOTLOGIN = "5001";
	public static final String CODE_USER_NORIGHTS = "5002";
	public static final String CODE_REPEAT_OPERATION = "5003";
	public static final String CODE_NOT_EXISTS = "5004";
	public static final String CODE_DATA_ERROR = "4005";
	public static final String CODE_CHECK_ERROR = "4003";
	public static final String CODE_UNKNOWN_ERROR = "9999";
	
	
	private String errcode;
	private String msg;
	private T retval;
	private boolean success = false;
	private ResultCode(String code, String msg, T retval){
		this.errcode = code;
		this.msg = msg;
		this.retval = retval;
		this.success = StringUtils.equals(code, CODE_SUCCESS);
	}

	public static final ResultCode SUCCESS = new ResultCode(CODE_SUCCESS, "操作成功！", null);
	public static final ResultCode NOT_LOGIN = new ResultCode(CODE_NOTLOGIN, "您还没有登录，请先登录！", null);
	public static final ResultCode NORIGHTS = new ResultCode(CODE_USER_NORIGHTS, "您没有权限！", null);
	public static final ResultCode REPEATED = new ResultCode(CODE_REPEAT_OPERATION, "不能重复操作！", null);
	public static final ResultCode NOT_FOUND = new ResultCode(CODE_NOT_EXISTS, "未找到相关数据！", null);
	public static final ResultCode DATEERROR = new ResultCode(CODE_DATA_ERROR, "数据有错误！", null);
	public static final ResultCode BLACK_LIST = new ResultCode(CODE_CHECK_ERROR, "黑名单中！", null);
	@Override
	public boolean equals(Object another){
		if(another == null || !(another instanceof ResultCode)) return false;
		return this.errcode== ((ResultCode)another).errcode;
	}
	@Override
	public int hashCode() {
		return (this.success + this.errcode + this.msg).hashCode();
	}

	public boolean isSuccess(){
		return success;
	}
	public static ResultCode getFailure(String msg){
		return new ResultCode(CODE_UNKNOWN_ERROR, msg, null);
	}
	public static ResultCode getFailure(String code, String msg){
		return new ResultCode(code, msg, null);
	}
	public static ResultCode getSuccess(String msg){
		return new ResultCode(CODE_SUCCESS, msg, null);
	}
	public static <T> ResultCode<T> getSuccessReturn(T retval){
		return new ResultCode(CODE_SUCCESS, null, retval);
	}
	public static ResultCode getSuccessMap(){
		return new ResultCode(CODE_SUCCESS, null, new HashMap());
	}
	public static <T> ResultCode getFailureReturn(T retval){
		return new ResultCode(CODE_UNKNOWN_ERROR, null, retval);
	}
	public static ResultCode getFullResultCode(String code, String msg, Object retval){
		return new ResultCode(code, msg, retval);
	}
	public T getRetval() {
		return retval;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public void setRetval(T retval) {
		this.retval = retval;
	}
	public String getMsg() {
		return msg;
	}
	public void put(Object key, Object value){
		((Map)retval).put(key, value);
	}
	public String getErrcode() {
		return errcode;
	}

}
