package com.xxb.base;

public class MessResult {

	private Boolean success = Boolean.TRUE;
	
	private String msg;
	
	private String errorCode;
	
	private Long totalNumber;
	
	private Integer totalPage;
	
	private Object data;
	
	public MessResult() {}
	
	public MessResult(String msg) {
		this(true, msg);
	}
	public MessResult(String msg, Object data) {
		this(true, msg, data);
	}
	/**
	 * <p>Description: 构造方法</p>
	 * @param success 操作结果
	 * @param msg 返回提示信息
	 */
	public MessResult(boolean success, String msg) {
		this(success, msg , null);
	}
	/**
	 * <p>Description: 构造方法</p>
	 * @param success 操作结果
	 * @param msg 返回提示信息
	 * @param other 返回其他对象
	 */
	public MessResult(boolean success, String msg, Object data) {
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

	public Boolean isSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Long getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(Long totalNumber) {
		this.totalNumber = totalNumber;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
