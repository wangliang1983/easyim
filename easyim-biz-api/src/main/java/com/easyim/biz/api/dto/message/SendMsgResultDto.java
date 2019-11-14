package com.easyim.biz.api.dto.message;


import java.io.Serializable;

import com.easyim.biz.api.protocol.c2s.MessagePush;
import com.easyim.biz.api.protocol.enums.c2s.Result;

import lombok.Data;

@Data
public class SendMsgResultDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -159787023071317414L;
	private Result result = Result.success;
	private MessagePush messagePush;
	private long msgId;
	
}
