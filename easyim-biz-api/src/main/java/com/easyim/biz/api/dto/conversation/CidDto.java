package com.easyim.biz.api.dto.conversation;

import java.io.Serializable;

import lombok.Data;

@Data
public class CidDto implements Serializable{

	private static final long serialVersionUID = 5564787173580411466L;

	private long cid;
	private long proxyCid;
	
}
