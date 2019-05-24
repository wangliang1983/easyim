package com.wl.easyim.biz.api.protocol.protocol.c2s;

import com.wl.easyim.biz.api.protocol.enums.c2s.Result;

import lombok.Data;

@Data
public abstract class AbstractAckProtocol extends AbstractProtocol{
	private Result result = Result.success;
	private int code      = Result.success.getCode();
	private String msg = null;
	
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
		this.msg  = result.getMsg();
		this.code = result.getCode();
	}
	
	
}