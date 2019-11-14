package com.easyim.biz.api.protocol.c2s;

import lombok.Data;

@Data
public class UserStatusPush extends AbstractProtocol{

	public static enum UserStatus{
		login,//登入
		logout;//登出
	}
	
	private static final long serialVersionUID = 7634782874109390758L;

	private long tenementId;
	
	private String userId;
	
	private UserStatus userStatus;
	
}
