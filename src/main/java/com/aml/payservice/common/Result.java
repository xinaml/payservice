package com.aml.payservice.common;

import java.io.Serializable;

/**
 * @Description:返回结果
 * @author:zhangcs
 * @time:2017年9月8日 下午6:07:07
 */
public class Result implements Serializable {

	private static final long serialVersionUID = -1353816862768053854L;

	/**
	 * 返回数据
	 */
	private Object data;

	/**
	 * 是否成功
	 */
    private boolean isSuccess;

    /**
	 * 回复消息
	 */
    private String msg;
    
    /**
	 * 编码
	 */
    private String code;
    
    public static Result success() {
    	Result rs = success("200");
    	rs.msg = "操作成功";
        return rs;
    }
    
    public static Result success(String code) {
    	Result result = new Result();
        result.isSuccess = true;
        result.code = code;
        result.msg = "操作成功";
        return result;
    }
    
    public static Result success(Object data) {
    	return success(data, "200");
    }

    public static Result success(Object data, String code) {
        Result result = success();
        result.data = data;
        result.code = code;
        result.msg = "操作成功";
        return result;
    }
    
    public static Result failure() {
    	return failure("操作失败", "-1");
    }

    public static Result failure(String message) {
        return failure(message, "-1");
    }

    public static Result failure(String message, String code) {
        Result result = new Result();
        result.msg = message;
        result.code = code;
        result.isSuccess = false;
        return result;
    }

    public static Result failure(String message, Exception exception) {
        Result result = failure(message);
        result.data = exception;
        result.code = "-1";
        result.isSuccess = false;
        result.msg = "操作失败";
        return result;
    }
    
    public static Result failure(String message, Object data) {
        Result result = failure(message);
        result.data = data;
        return result;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

   
    
    public String getMsg() {
    	return msg;
    }
    
    public void setMsg(String msg) {
    	this.msg = msg;
    }

   

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
    
}