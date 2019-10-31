package com.easyim.biz.api.service.conversation;

import java.util.List;

import com.easyim.biz.api.dto.conversation.ConversationDto;
import com.easyim.biz.api.dto.conversation.UnreadDto;
import com.easyim.biz.api.protocol.c2s.MessagePush;

/**
 * 会话服务
 * @author wl
 *
 */
public interface IConversationService {
	
	/**
	 * 查询会话
	 * @param tenementId
	 * @param fromId
	 * @param toId
	 * @return
	 */
	public long getAndCreateCid(long tenementId,String fromId,String toId,long proxyCid);
	
	/**
	  * 查询会话
	 * @param tenementId
	 * @param fromId
	 * @param toId
	 * @return
	 */
	public long getCid(long tenementId,String fromId,String toId);
	
	/**
	 * 增加未读消息数
	 * @param tenementId
	 * @param cid
	 */
	public void increaseUnread(int msgType,String fromId,long cid);
	
	/**
	 * 清空未读消息数
	 * @param tenementId
	 * @param cid
	 */
	public void cleanUnread(String fromId,long cid);
	
	/**
	 * 
	 * @param tenementId
	 * @param fromId
	 * @param toId
	 */
	public void cleanUnread(long tenementId,String fromId,String toId);
	
	/**
	  *  查询会话的未读消息
	 * @param fromId
	 * @param toId
	 * @return
	 */
	public int getUnread(long tenementId,String fromId,String toId);
	
	/**
	 *  设置未读消息数
	 * @param fromId
	 * @param toId
	 * @param unreads
	 */
	public int setUnread(long cid,String fromId,int unreads);
	
	/**
	 * 得到会话的未读消息数
	 * @param userId
	 * @param cids
	 */
	public List<UnreadDto> selectUnread(String fromId,List<Long> cids);
	
	
	
	/**
	 * 添加最近的聊天会话
	 * @param messagePush
	 */
	public void addRecentlyConversation(MessagePush messagePush);
	
	/**
	 * 查询一个用户的最近的会话列表
	 * @param tenementId
	 * @param userId
	 * @return
	 */
	public List<ConversationDto> selectRecentlyConversation(long tenementId,String userId);
}
