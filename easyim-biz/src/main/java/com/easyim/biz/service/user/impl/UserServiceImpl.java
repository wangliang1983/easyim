package com.easyim.biz.service.user.impl;

import java.util.List;

import javax.annotation.Resource;

import com.alibaba.dubbo.config.annotation.Service;
import com.easyim.biz.api.dto.conversation.ConversationDto;
import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;
import com.easyim.biz.api.service.conversation.IConversationService;
import com.easyim.biz.api.service.user.IUserService;
import com.easyim.route.service.IUserRouteService;

@Service(interfaceClass=IUserService.class)
public class UserServiceImpl implements IUserService {

	@Resource
	private IUserRouteService userRouteService;
	
	@Resource
	private IConversationService conversationService;
	
	@Override
	public List<String> selectUserOnline(long tenementId, List<String> userIds) {

		return userRouteService.getOnlineUsers(tenementId, userIds);
	}

	@Override
	public List<String> selectUserLoginTime(long tenementId, List<String> userIds) {
		return userRouteService.getOnlineUsers(tenementId, userIds);
	}

	@Override
	public void pushUserStatus(long tenementId, String userId, UserStatus status) {
		List<ConversationDto>  conversationDto  = conversationService.selectRecentlyConversation(tenementId,userId);
		
		
	}

}
