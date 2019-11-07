package com.easyim.biz.api.dto.message;


import com.easyim.biz.api.protocol.c2s.MessagePush;
import com.easyim.biz.api.protocol.enums.c2s.Result;

import lombok.Data;

@Data
public class SendMsgResultDto {
	private Result result = Result.success;
	private MessagePush messagePush;
	private long msgId;
	
}
