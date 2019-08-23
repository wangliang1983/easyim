package com.easyim.biz.api.service.message;

import com.easyim.biz.api.dto.message.OfflineMsgDto;
import com.easyim.biz.api.dto.message.SendMsgDto;
import com.easyim.biz.api.dto.message.SendMsgResultDto;
import com.easyim.biz.api.dto.protocol.C2sProtocol;
import com.easyim.biz.api.protocol.c2s.MessagePush;

import java.util.List;


/**
 * 消息服务
 * @author wl
 *
 */
public interface IMessageService {
	/**
	 * 发送消息
	 * @param message
	 * @return
	 */
	public SendMsgResultDto sendMsg(SendMsgDto message);
	
	
	/**
	 * 推送消息
	 * 1.保持离线消息
	 * 2.计算未读
	 * 3.推送消息
	 * @param tenementId
	 * @param userIds
	 * @param messagePush
	 */
	public void pushMsg(MessagePush messagePush, List<String> userIds);
	
	/**
	 * 推送消息
	 * 1.保持离线
	 * 2.推送消息
	 * @param c2sProtocol
	 * @param userId
	 */
	public void pushOfflineMsg(long tenementId,String userId,C2sProtocol  c2sProtocol);
	
	
	/**
	 * 给多个用户发送消息
	 * @param message
	 * @return
	 */
	public void sendMsg(SendMsgDto message,List<String> userIds);
	
	
	/**
	 * 拉取离线消息
	 * @return
	 */
	public List<C2sProtocol> pullOfflineMsg(OfflineMsgDto offlineMsgDto);
	
	
}
