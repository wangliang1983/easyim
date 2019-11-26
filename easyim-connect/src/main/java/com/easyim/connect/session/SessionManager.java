package com.easyim.connect.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.easy.springboot.c2s.server.AbstractServerRegister;
import com.easyim.biz.api.dto.protocol.C2sProtocol;
import com.easyim.biz.api.dto.user.UserSessionDto;
import com.easyim.biz.api.protocol.c2s.AuthAck;
import com.easyim.biz.api.protocol.c2s.CloseSession;
import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;
import com.easyim.biz.api.protocol.enums.c2s.EasyImC2sType;
import com.easyim.biz.api.protocol.enums.c2s.Result;
import com.easyim.connect.listener.SessionEventDto;
import com.easyim.connect.listener.SessionEventListenerManager;
import com.easyim.connect.session.Session.SessionStatus;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * session的会话管理
 * @author wl
 *
 */
@Slf4j
@Component
public class SessionManager {
	
	private static Map<String,ConcurrentHashMap<String,Session>> uidMap 
		=new ConcurrentHashMap<String,ConcurrentHashMap<String,Session>>();
	
	
	private static Map<String,Session> sessionIdMap	
		=new ConcurrentHashMap<String,Session>();

	
	public static final String SPLIT="_";
	
	public static final C2sProtocol TIMEOUT = new C2sProtocol();

	static{
		TIMEOUT.setType(EasyImC2sType.closeSession.getValue());
		
		CloseSession cs = new CloseSession();
		cs.setResult(Result.timeOut);
		
		TIMEOUT.setBody(JSON.toJSONString(cs));
	}
	
	public static Session getSession(ChannelHandlerContext chc){
		return sessionIdMap.get(Session.getSessionId(chc));
	}

	/**
	 * 得到会话列表
	 * @param uid
	 * @return
	 */
	public static List<Session> getSession(String uid){
		ConcurrentHashMap<String,Session> map =  uidMap.get(uid);
		log.info("getSession:{},{}",uid,uidMap.size());
		
		
		List<Session> list = new ArrayList<Session>();
		
		if(map!=null){
			list.addAll(map.values());
		}
		
		return list;
	}
	
	public static String getUid(long tenementId,String userId){
		return tenementId+SPLIT+userId;
	}
	
	/**
	 * 删除会话
	 * @param session
	 */
	public static void removeSession(ChannelHandlerContext chc,C2sProtocol c2sProtocol){
		
		Session session = sessionIdMap.get(Session.getSessionId(chc));
		if(session!=null){
			//移除会话
			
			String uid =getUid(session.getTenementId(),session.getUserId());
			
			ConcurrentHashMap<String,Session> sessionMap = uidMap.get(uid);
			if(sessionMap!=null){
				sessionMap.remove(session.getSessionId());
			}
			
			if(sessionMap.size()==0){
				synchronized(uidMap){
					if(sessionMap.size()==0){
						uidMap.remove(uid);
					}
				}
			}
			
			log.info("uid map:{},{}",uid,sessionMap.size());
		}
		
		//doserver logout
		
		sessionIdMap.remove(Session.getSessionId(chc));
		
		if(c2sProtocol!=null){
			String json  = JSON.toJSONString(c2sProtocol);
			chc.channel().writeAndFlush(json);
		}
		
		chc.close();
		
		sessionCallback(session,UserStatus.offline);
	}
	
	/**
	 * session事件回调
	 * @param session
	 * @param sessionEvent
	 */
	private static void sessionCallback(Session session,UserStatus userStatus){
		if(session==null){
			return;
		}
		
		//钩子事件回调
		SessionEventDto sessionEventDto = SessionEventDto.builder()
				.resource(session.getResource())
				.userStatus(userStatus)
				.userType(session.getUserType())
				.tenementId(session.getTenementId())
				.userId(session.getUserId()).build();
		
		SessionEventListenerManager.addSessionEventDto(sessionEventDto);
	
	}

	/**
	 * 更新会话状态为已登录
	 * @param session
	 */
	public static boolean addSession(ChannelHandlerContext chc,AuthAck authAck,int timeOutCycle){
		
		Session session = Session
				.builder()
				.chc(chc).sessionStatus(SessionStatus.auth)
				.tenementId(authAck.getTenementId())
				.userId(authAck.getUserId())
				.resource(authAck.getResource())
				.timeOutCycle(timeOutCycle)
				.userType(authAck.getUserType())
				.merchantId(authAck.getMerchantId())
				.build();
				
		
		String uid =getUid(session.getTenementId(),session.getUserId());
		
		ConcurrentHashMap<String,Session> map = uidMap.get(uid);
		if(map==null){
			synchronized(uidMap){
				map = uidMap.get(uid);
				if(map==null){//double check
					map =  new ConcurrentHashMap<String,Session>();
				}
				uidMap.put(uid,map);
			}
		}
		
		map.put(session.getSessionId(),session);
	

		log.info("uid map:{},{}",uid,map.size());
		
		sessionIdMap.put(Session.getSessionId(chc),session);
		
		sessionCallback(session,UserStatus.online);

		return true;
	}
	

	
	
	public static UserSessionDto getUserDto(ChannelHandlerContext chc){
		
		String connectServer = AbstractServerRegister.getConnectServer();
		
		Session session      = sessionIdMap.get(Session.getSessionId(chc));
		
		
		
		UserSessionDto userSessionDto = new UserSessionDto();
		userSessionDto.setConnectServer(connectServer);
		userSessionDto.setSessionId(Session.getSessionId(chc));
		
		if(session!=null){
			userSessionDto.setTenementId(session.getTenementId());
			userSessionDto.setUserId(session.getUserId());
			userSessionDto.setResourceType(session.getResource());
			userSessionDto.setSessionTimeOut(session.getTimeOutCycle()*60);
			userSessionDto.setMerchantId(session.getMerchantId());
		}else{
			userSessionDto.setSessionTimeOut(60);
		}
		
		return userSessionDto;
	}
}
