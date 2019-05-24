package com.wl.easyim.connect.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wl.easyim.biz.api.protocol.enums.s2s.S2sCommandType;
import com.wl.easyim.biz.api.protocol.protocol.s2s.S2sMessagePush;
import com.wl.easyim.biz.api.protocol.protocol.s2s.S2sMessagePushAck;
import com.wl.easyim.connect.service.IS2sProtocolService;
import com.wl.easyim.connect.session.Session;
import com.wl.easyim.connect.session.SessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务端协议推送
 * @author wl
 *
 */

@Slf4j
@Service
public class MessagePushServiceImpl implements IS2sProtocolService<S2sMessagePush,S2sMessagePushAck>{

	@Override
	public S2sMessagePushAck handleProtocolBody(S2sMessagePush messagePush) {
		
		String uid = SessionManager.getUid(messagePush.getTenementId(),messagePush.getToId());
		
		List<Session> sessions =  SessionManager.getSession(uid);
		
		for(Session s:sessions){
			log.info("uid : {} flush ",uid);
			try{
				s.getChc().writeAndFlush(messagePush.getBody());
			}catch(Exception e){
				log.error("exception:{}",e);
			}
		}
		
		return new S2sMessagePushAck();
	}



	@Override
	public S2sCommandType getType() {
		
		return S2sCommandType.s2sMessagePush;
	}

}
