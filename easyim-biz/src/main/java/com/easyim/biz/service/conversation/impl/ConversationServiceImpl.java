package com.easyim.biz.service.conversation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.dozer.Mapper;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.easyim.biz.api.dto.conversation.ConversationDto;
import com.easyim.biz.api.dto.conversation.UnreadDto;
import com.easyim.biz.api.dto.message.SendMsgDto.MessageType;
import com.easyim.biz.api.protocol.c2s.MessagePush;
import com.easyim.biz.api.service.conversation.IConversationService;
import com.easyim.biz.constant.Constant;
import com.easyim.biz.domain.ConversationDo;
import com.easyim.biz.domain.ProxyConversationDo;
import com.easyim.biz.mapper.conversation.IConversationMapper;
import com.easyim.route.service.IProtocolRouteService;
import com.easyim.route.service.IUserRouteService;

import cn.linkedcare.springboot.redis.template.RedisTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service(interfaceClass = IConversationService.class)
public class ConversationServiceImpl implements IConversationService {

	@Resource
	private IConversationMapper conversationMapper;

	@Resource
	private IUserRouteService userRouteService;

	@Resource
	private IProtocolRouteService protocolRouteService;

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private Mapper mapper;

	@Override
	public long getAndCreateCid(long tenementId, String fromId, String toId, long proxyCid) {
		long cid = getCid(tenementId, fromId, toId);
		if (cid > 0) {
			return cid;
		}

		String smallId;
		String bigId;
		if (fromId.compareTo(toId) > 0) {
			bigId = fromId;
			smallId = toId;
		} else {
			bigId = toId;
			smallId = fromId;
		}

		ConversationDo conversation = new ConversationDo();
		conversation.setTenementId(tenementId);
		conversation.setProxyCid(proxyCid);
		conversation.setSmallId(smallId);
		conversation.setBigId(bigId);
		conversationMapper.insertConversationDo(conversation);

		return conversation.getId();
	}

	/**
	 * 得到未读消息的key
	 * 
	 * @param tenementId
	 * @param userId
	 * @return
	 */
	public String getUnreadKey(String userId, long cid) {
		String key = Constant.UNREAD_MSG_KEY + userId + "_" + cid;
		return key;
	}

	@Override
	public void increaseUnread(int msgType, String fromId, long cid) {
		if (!MessageType.isIncrementUnread(msgType)) {
			return;
		}

		String key = getUnreadKey(fromId,cid);

		this.redisTemplate.incr(key);
	}

	@Override
	public void cleanUnread(String fromId,long cid) {
		String key = getUnreadKey(fromId, cid);

		this.redisTemplate.del(key);

	}
	
	@Override
	public void cleanUnread(long tenementId, String fromId, String toId) {
		long cid = this.getCid(tenementId, fromId, toId);
		
		this.cleanUnread(fromId, cid);
	}

	/**
	 * 最近消息列表的key
	 * 
	 * @param tenementId
	 * @param toId
	 * @return
	 */
	private String getRecentlyKey(long tenementId, String userId) {
		String key = Constant.RECENTLY_KEY + tenementId + "_" + userId;
		return key;
	}

	@Override
	public void addRecentlyConversation(MessagePush messagePush) {

		long tenementId = messagePush.getTenementId();
		String toId = messagePush.getFromId();
		String fromId = messagePush.getToId();

		String fromRecentlyCids = getRecentlyKey(tenementId, fromId);
		String toRecentlyCids = getRecentlyKey(tenementId, toId);

		double score = (double) System.currentTimeMillis();

		redisTemplate.zadd(fromRecentlyCids, score, String.valueOf(messagePush.getCid()));
		redisTemplate.zremrangeByRank(fromRecentlyCids, 100, Integer.MAX_VALUE);

		redisTemplate.zadd(toRecentlyCids, score, String.valueOf(messagePush.getCid()));
	}

	@Override
	public List<ConversationDto> selectRecentlyConversation(long tenementId, String userId) {
		String key = getRecentlyKey(tenementId, userId);
		Set<String> set = this.redisTemplate.zrevrange(key, 0, Constant.MAX_RECENTLY_NUM);
		List<Long> ids = new ArrayList<Long>();

		for (String s : set) {
			ids.add(Long.parseLong(s));
		}

		List<ConversationDto> dtos = new ArrayList<ConversationDto>();

		List<ConversationDo> cs = this.conversationMapper.selectConversationByIds(tenementId, ids);
		for (ConversationDo c : cs) {
			ConversationDto dto = mapper.map(c, ConversationDto.class);
			dto.setCid(c.getId());
			
			if(userId.equals(c.getSmallId())) {
				dto.setFromId(c.getSmallId());
				dto.setToId(c.getBigId());
			}else {
				dto.setFromId(c.getBigId());
				dto.setToId(c.getSmallId());
			}
			
			dtos.add(dto);
		}

		log.info("selectRecentlyConversation:{},{},{}",tenementId,userId,JSON.toJSONString(dtos));
		return dtos;
	}

	@Override
	public List<UnreadDto> selectUnread(String userId, List<Long> cids) {

		List<String> keys = new ArrayList<String>();
		for (long cid : cids) {
			String key = getUnreadKey(userId, cid);
			keys.add(key);
		}

		List<String> results = this.redisTemplate.mget(keys.toArray(new String[] {}));

		List<UnreadDto> dtos = new ArrayList<UnreadDto>();
		for (int i = 0; i < cids.size(); i++) {
			UnreadDto dto = new UnreadDto();
			dto.setCid(cids.get(i));

			if (results.get(i) != null) {
				dto.setUnreads(Integer.parseInt(results.get(i)));
			}

			dtos.add(dto);
		}

		return dtos;
	}

	@Override
	public int getUnread(long tenementId,String fromId, String toId) {
		long cid = this.getCid(tenementId, fromId, toId);
		String key = getUnreadKey(toId, cid);
		String nums = this.redisTemplate.get(key);
		
		if(nums==null){
			return  0;
		}
		
		return Integer.parseInt(nums);
	}

	@Override
	public int setUnread(long cid, String toId, int unreads) {
		String key = getUnreadKey(toId, cid);
		
		String nums = this.redisTemplate.get(key);
		if(nums==null||Integer.parseInt(nums)<unreads) {
			this.redisTemplate.set(key,String.valueOf(unreads));
			return unreads;
		}
			return Integer.parseInt(nums);
	}

	@Override
	public long getCid(long tenementId, String fromId, String toId) {
		String smallId;
		String bigId;
		if (fromId.compareTo(toId) > 0) {
			bigId = fromId;
			smallId = toId;
		} else {
			bigId = toId;
			smallId = fromId;
		}

		ConversationDo conversation = conversationMapper.getConversation(tenementId, smallId, bigId);
		if (conversation == null) {
			return 0l;
		}
		return conversation.getId();
	}

	@Override
	public List<ConversationDto> selectRecentlyConversationByDb(long tenementId, String userId) {
		return null;
	}

	@Override
	public Map<String, Long> selectRecentlyConversationMap(long tenementId, String userId) {
		List<ConversationDto> dtos = selectRecentlyConversationByDb(tenementId,userId);
		
		Map<String,Long> maps = new HashMap<String,Long>();
		
		for(ConversationDto dto:dtos) {
			if(userId.equals(dto.getFromId())) {
				maps.put(dto.getToId(),dto.getCid());
			}else {
				maps.put(dto.getFromId(),dto.getCid());
			}
		}
		
		return maps;
	}


}
