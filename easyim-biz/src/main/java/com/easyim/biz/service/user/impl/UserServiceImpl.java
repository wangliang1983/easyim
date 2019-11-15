package com.easyim.biz.service.user.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.easyim.biz.api.dto.conversation.ConversationDto;
import com.easyim.biz.api.protocol.c2s.UserStatusPush;
import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;
import com.easyim.biz.api.service.conversation.IConversationService;
import com.easyim.biz.api.service.user.IUserService;
import com.easyim.route.service.IProtocolRouteService;
import com.easyim.route.service.IUserRouteService;

@Service(interfaceClass=IUserService.class)
public class UserServiceImpl implements IUserService {

	@Resource
	private IUserRouteService userRouteService;
	
	@Resource
	private IConversationService conversationService;
	
	@Resource
	private IProtocolRouteService protocolRouteService;
	
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
		List<ConversationDto>  conversationDtos  = conversationService.selectRecentlyConversation(tenementId,userId);
		
		List<String> userIds = new ArrayList<String>();
		for(ConversationDto  c:conversationDtos) {
			String fromId = c.getFromId();
			String toId   = c.getToId();
			if(userId.equals(fromId)) {
				userIds.add(toId);
			}else {
				userIds.add(fromId);
			}
		}
		
		List<String> onlineUserIds = userRouteService.getOnlineUsers(tenementId, userIds);
		
		
		UserStatusPush userStatusPush = new UserStatusPush();
		userStatusPush.setTenementId(tenementId);
		userStatusPush.setUserStatus(status);
		userStatusPush.setUserId(userId);
		
		for(String  online:onlineUserIds) {
			protocolRouteService.route(tenementId, online,JSON.toJSONString(userStatusPush),null);
		}
		
	}

}
