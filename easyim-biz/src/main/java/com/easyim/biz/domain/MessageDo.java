package com.wl.easyim.biz.domain;

import java.util.Date;

import lombok.Data;

@Data
public class MessageDo {
	private long id;
	private long tenementId;//租户id
	private String fromId;//消息from
	private String toId;//消息接收方
	private String proxyFromId;//发送方路由的代理
	private String proxyToId;//消息接受路由的代理
	private long proxyCid;
	private long cid;
	private int type;//1 文本  
	private int subType;
	private String content;
	private Date gmtCreate;
	private String ticket;//消息唯一业务码

	
	

}