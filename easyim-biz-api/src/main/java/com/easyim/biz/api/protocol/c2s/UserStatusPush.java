package com.easyim.biz.api.protocol.c2s;

import lombok.Data;

@Data
public class UserStatusPush extends AbstractProtocol{

	public static enum UserStatus{
		online,//在线
		offline;//离线
	}
	
	private static final long serialVersionUID = 7634782874109390758L;
	
	private long cid;
	
	private String fromId;
	
	private String toId;
	
	private UserStatus userStatus;
	
}
