package com.easyim.biz.service.protocol.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.easyim.biz.api.dto.user.UserSessionDto;
import com.easyim.biz.api.protocol.c2s.Message;
import com.easyim.biz.api.protocol.c2s.MessageAck;
import com.easyim.biz.api.protocol.enums.c2s.C2sCommandType;
import com.easyim.biz.service.protocol.IC2SProtocolService;

@Service("messagePService")
public class MessagePushServiceImpl implements IC2SProtocolService<Message,MessageAck>{

	@Override
	public C2sCommandType getC2sCommandType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageAck handleProtocolBody(UserSessionDto userSessionDto, Message body, Map<String, String> extendsMap) {
		// TODO Auto-generated method stub
		return null;
	}






}
