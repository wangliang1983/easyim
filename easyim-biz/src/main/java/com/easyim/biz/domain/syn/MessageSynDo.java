package com.easyim.biz.domain.syn;

import java.util.Date;

import lombok.Data;

@Data
public class MessageSynDo {
	private long id;
	private long tenementId;
	private String fromId;
	private String proxyFromId;
	private String toId;
	private String proxyToId;
	private int type;
	private int subType;
	private String content;
	private String ticket;
	private String bizCode;
	private Date gmtCreate;
}
