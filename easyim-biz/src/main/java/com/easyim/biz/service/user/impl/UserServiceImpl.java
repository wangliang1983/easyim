package com.easyim.biz.service.user.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.easyim.biz.api.dto.conversation.ConversationDto;
import com.easyim.biz.api.dto.protocol.C2sProtocol;
import com.easyim.biz.api.protocol.c2s.UserStatusPush;
import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;
import com.easyim.biz.api.protocol.enums.c2s.EasyImC2sType;
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
		Map<String, Long>  map = conversationService.selectRecentlyConversationMap(tenementId,userId);
		
		List<String> list = new ArrayList<String>();
		list.addAll(map.keySet());
		
		List<String> onlineUserIds = userRouteService.getOnlineUsers(tenementId,list);
		
		
		
		
		for(String  online:onlineUserIds) {
			
			C2sProtocol c2sProtocol = new C2sProtocol();
			
			UserStatusPush userStatusPush = new UserStatusPush();
			
			userStatusPush.setUserStatus(status);
			userStatusPush.setToId(userId);
			userStatusPush.setCid(map.get(userId));
			
			c2sProtocol.setType(EasyImC2sType.userStatusPush.name());
			c2sProtocol.setBody(JSON.toJSONString(userStatusPush));
			
			protocolRouteService.route(tenementId, online,JSON.toJSONString(c2sProtocol),null);
		}
		
	}



}
