package com.easyim.biz.api.protocol.c2s;

import com.easyim.biz.api.dto.message.SendMsgDto.MessageType;

import lombok.Data;

@Data
public class Message extends AbstractProtocol{

	public static enum UserType{
		system,//系统
		customerService,//客服
		customer,//顾客
		other//自定义
		;
	}
	
	private static final long serialVersionUID = 1L;
	private long cid;
	private long proxyCid;
	private long tenementId;
	
	
	private String fromId;
	private UserType fromType;
	private String fromProxyId;
	
	private String toId;
	private UserType toType;
	private String toProxyId;
	
	private MessageType type;
	private int subType;
	private String content;
	
}
