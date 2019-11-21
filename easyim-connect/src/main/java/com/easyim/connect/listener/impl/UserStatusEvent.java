package com.easyim.connect.listener.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.easyim.biz.api.service.conversation.IConversationService;
import com.easyim.connect.listener.SessionEventDto;
import com.easyim.connect.listener.SessionEventListener;

@Component
public class UserStatusEvent implements SessionEventListener{

	@Reference
	private IConversationService conversationService;
	
	@Override
	public void callback(SessionEventDto sessionEventDto) {
	
	}

}