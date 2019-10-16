package com.easyim.connect.listener;

import com.easyim.biz.api.protocol.enums.c2s.ResourceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionEventDto {
	public static enum SessionEvent{
		login,//登录
		logout;//登出
	}
	
	private SessionEvent sessionEvent;
	private long tenementId;
	private String userId;
	private ResourceType resource;//设备
	private String bizExtends;//业务扩展字段
	
}
