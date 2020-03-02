package com.easyim.biz.service.msg.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;
//import com.easy.springboot.redis.template.RedisTemplate;
import com.easyim.biz.Launch;
import com.easyim.biz.api.dto.message.ForwardMsgDto;
import com.easyim.biz.api.dto.message.OfflineMsgDto;
import com.easyim.biz.api.dto.message.PullOfflineMsgResultDto;
import com.easyim.biz.api.dto.message.SendMsgDto;
import com.easyim.biz.api.dto.message.SendMsgDto.MessageType;
import com.easyim.biz.api.dto.message.SendMsgResultDto;
import com.easyim.biz.api.dto.protocol.C2sProtocol;
import com.easyim.biz.api.protocol.enums.c2s.EasyImC2sType;
import com.easyim.biz.api.protocol.enums.c2s.ResourceType;
import com.easyim.biz.api.protocol.enums.c2s.Result;
import com.easyim.biz.api.protocol.c2s.MessagePush;
import com.easyim.biz.api.service.conversation.IConversationService;
import com.easyim.biz.api.service.conversation.IProxyConversationService;
import com.easyim.biz.api.service.message.IMessageSearchService;
import com.easyim.biz.api.service.message.IMessageService;
import com.easyim.biz.constant.Constant;
import com.easyim.biz.domain.ConversationDo;
import com.easyim.biz.domain.MessageDo;
import com.easyim.biz.domain.ProxyConversationDo;
import com.easyim.biz.domain.TenementDo;
import com.easyim.biz.dto.PullOfflineMsgDto;
import com.easyim.biz.listeners.ProtocolListenerFactory;
import com.easyim.biz.mapper.conversation.IConversationMapper;
import com.easyim.biz.mapper.conversation.IProxyConversationMapper;
import com.easyim.biz.mapper.message.IMessageMapper;
import com.easyim.biz.mapper.tenement.ITenementMapper;
import com.easyim.biz.task.OfflineMaxIdTask;
import com.easyim.biz.task.SynMessageTask;
import com.easyim.biz.task.SynMessageTask.SynMessageTaskDto;
import com.easyim.route.service.IProtocolRouteService;

import cn.linkedcare.springboot.redis.template.RedisTemplate;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Tuple;
import redis.clients.util.SafeEncoder;

/**
 * 消息相关业务接口
 * 
 * @author wl
 *
 */
@Slf4j
@Service(interfaceClass = IMessageService.class)
public class MessageServiceImpl implements IMessageService {
	@Value("${offline.msg.nums}")
	private long MAX_OFFLINE_NUM = 50;

	@Resource
	private RedisTemplate redisTemplate;

	@Resource
	private ITenementMapper tenementMapper;

	@Resource
	private IConversationService conversationService;

	@Resource
	private IMessageMapper messageMapper;

	@Resource
	private IProxyConversationService proxyConversationService;

	@Resource
	private IProtocolRouteService protocolRouteService;

	@Resource
	private Mapper mapper;

	@Resource
	private BeanFactory beanFactory;

	private Codec<C2sProtocol> simpleTypeCodec = ProtobufProxy.create(C2sProtocol.class);

	/**
	 * 保存离线消息
	 * 
	 * @param key
	 * @param msgId
	 * @throws IOException
	 */
	private void saveOfflineMsg(String key, long msgId, C2sProtocol c2sProtocol){
		try {
			// 序列化
			byte[] msg = simpleTypeCodec.encode(c2sProtocol);

			String offlineMsgKey = getOfflineMsgKey(msgId);
			// 保存离线消息id list
			redisTemplate.zadd(key, Double.parseDouble(String.valueOf(msgId)), JSON.toJSONString(msgId));

			//不存在数据才覆盖
			byte[] datas = redisTemplate.get(offlineMsgKey.getBytes(Constant.CHARSET));
			if(datas==null||datas.length==0) {
				redisTemplate.setex(offlineMsgKey.getBytes(Constant.CHARSET), Constant.OFFLINE_TIME, msg);
			}
			

			long count = redisTemplate.zcard(key);
			if (count > MAX_OFFLINE_NUM) {// 离线消息超过最大数
				int end = Integer.parseInt((count - MAX_OFFLINE_NUM) + "");

				Set<String> ids = redisTemplate.zrange(key, 0, end);
				for (String id : ids) {
					String outSizeMsgKey = getOfflineMsgKey(Long.parseLong(id));
					redisTemplate.del(outSizeMsgKey.getBytes(Constant.CHARSET));
				}

				redisTemplate.zremrangeByRank(key, 0, Integer.parseInt((count - MAX_OFFLINE_NUM) + ""));
			}
		}catch(IOException exception) {
			throw new RuntimeException(exception);
		}
		

	}

	/**
	 * 离线消息的的key
	 * 
	 * @param tenementId
	 * @param toId
	 * @return
	 */
	private String getOfflineMsgKey(long msgId) {
		String key = Constant.OFFLINE_MSG_KEY + msgId;
		return key;
	}

	/**
	 * 离线消息的id列表的key
	 * 
	 * @param tenementId
	 * @param toId
	 * @return
	 */
	private String getOfflineSetKey(long tenementId, String toId) {
		String key = Constant.OFFLINE_MSG_SET_KEY + tenementId + "_" + toId;
		return key;
	}

	/**
	 * 保存离线消息
	 * 
	 * @param tenementId
	 * @param toId
	 * @param msgId
	 * @param isMultiDevice
	 */
	private C2sProtocol saveOfflineMsg(MessagePush messagePush,String toId,int type,String product) {
		C2sProtocol c2sProtocol = new C2sProtocol();

		c2sProtocol.setType(EasyImC2sType.messagePush.getValue());
		c2sProtocol.setBody(JSON.toJSONString(messagePush));
		c2sProtocol.setProduct(product);
		
		// 不保存离线消息
		if (!MessageType.isSaveOffline(type)) {
			return c2sProtocol;
		}

		//离线和未读消息数 to方
		this.conversationService.increaseUnread(messagePush.getType(), messagePush.getToId(), messagePush.getCid());

		String toKey = getOfflineSetKey(messagePush.getTenementId(), toId);
		saveOfflineMsg(toKey, messagePush.getId(), c2sProtocol);
		
		
		//离线和未读消息数from方
		String fromKey = getOfflineSetKey(messagePush.getTenementId(), messagePush.getFromId());
		saveOfflineMsg(fromKey, messagePush.getId(), c2sProtocol);


		return c2sProtocol;
	}

	/**
	 * 得到未读消息的key
	 * 
	 * @param tenementId
	 * @param userId
	 * @return
	 */
	public String getUnreadKey(long tenementId, long cid) {
		String key = Constant.UNREAD_MSG_KEY + tenementId + "_" + cid;
		return key;
	}

	/**
	  * 过滤UTF-8 4个字节
	 * @param str
	 * @return
	 */
	private static String emojiFilter(String str) {
		String patternString = "([\\x{10000}-\\x{10ffff}\ud800-\udfff])";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);

		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, "[[EMOJI:" + URLEncoder.encode(matcher.group(1), "UTF-8") + "]]");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	  * 还原UTF-8 4个字节
	 * @param str
	 * @return
	 */
	private static String emojiRecovery(String str) {
		String patternString = "\\[\\[EMOJI:(.*?)\\]\\]";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(str);

		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				matcher.appendReplacement(sb, URLDecoder.decode(matcher.group(1), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		matcher.appendTail(sb);

		return sb.toString();
	}
	
	
	/**
	 * 保存消息
	 * 
	 * @param cid
	 * @param toId
	 */
	private MessageDo saveMsg(MessagePush messagePush, String proxyFromId, String proxyToId,String product) {
		MessageDo message = mapper.map(messagePush, MessageDo.class);
		message.setProxyFromId(proxyFromId);
		message.setProxyToId(proxyToId);
		message.setGmtCreate(new Date());
		message.setProduct(product);
		
		
		if (MessageType.isSaveDb(messagePush.getType())) {
			
			message.setContent(emojiFilter(message.getContent()));
			
			this.messageMapper.insertMessage(message);
		}

		return message;
	}

	/**
	 * 保存消息
	 * 
	 * @param msgId
	 * @param messageDto
	 */
	@Override
	public C2sProtocol saveMsg(SendMsgDto messageDto) {
		long id = this.getId();

		return saveMsg(id, messageDto);
	}

	/**
	 * 保存消息
	 * 
	 * @param msgId
	 * @param messageDto
	 */
	@Override
	public C2sProtocol saveMsg(long msgId,SendMsgDto messageDto) {
		// 得到代理会话
		long tenementId = messageDto.getTenementId();
		long proxyCid = messageDto.getProxyCid();
		String fromId = messageDto.getFromId();
		String toId = messageDto.getToId();
		String proxyFromId = messageDto.getProxyFromId();
		String proxyToId = messageDto.getProxyToId();

		if (proxyCid == 0) {
			if (StringUtils.isEmpty(proxyFromId)) {
				proxyFromId = fromId;
			}
			if (StringUtils.isEmpty(proxyToId)) {
				proxyToId = toId;
			}
			proxyCid = proxyConversationService.getProxyCid(tenementId, proxyFromId, proxyToId);
		}

		long cid = messageDto.getCid();
		if (cid == 0) {
			cid = conversationService.getAndCreateCid(tenementId, fromId, toId, proxyCid);
		}

		// build msg push
		MessagePush messagePush = new MessagePush();
		messagePush.setId(msgId);
		messagePush.setBizUuid(messageDto.getBizUuid());
		messagePush.setCid(cid);
		messagePush.setContent(messageDto.getContent());
		messagePush.setFromId(fromId);
		messagePush.setProxyCid(proxyCid);
		messagePush.setSubType(messageDto.getSubType());
		messagePush.setTenementId(tenementId);
		messagePush.setToId(toId);
		messagePush.setType(messageDto.getType().getValue());
		
		messagePush.setFromType(messageDto.getFromType());
		messagePush.setToType(messageDto.getToType());
		
		// 保存db消息
		MessageDo messageDo = saveMsg(messagePush, proxyFromId, proxyToId,messageDto.getProduct());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		messagePush.setTime(sdf.format(messageDo.getGmtCreate()));

		// 保存离线消息
		C2sProtocol c2sProtocol = saveOfflineMsg(messagePush, messagePush.getToId(),messagePush.getType(),messageDto.getProduct());

		// 增加最近聊天的会话
		this.conversationService.addRecentlyConversation(messagePush,messageDto.isSaveFromConversation(),messageDto.isSaveToConversation());
		
		return c2sProtocol;
	}

	@Override
	public SendMsgResultDto sendMsg(SendMsgDto messageDto, String excludeSessionId) {
		// 生产msgId
		long msgId = getId();

		SendMsgResultDto dto = new SendMsgResultDto();

		TenementDo tenement = tenementMapper.getTenementById(messageDto.getTenementId());

		boolean result = Launch.doValidator(messageDto);
		if (tenement == null || !result) {
			log.warn("sendMsg doValidator error:{},{}", messageDto.getToId(), JSON.toJSONString(messageDto));
			dto.setResult(Result.inputError);
			return dto;
		}

		C2sProtocol c2sProtocol = saveMsg(msgId, messageDto);
		// 路由协议
		this.protocolRouteService.route(messageDto.getTenementId(), messageDto.getToId(),messageDto.getProduct(),
				c2sProtocol, excludeSessionId);

		log.info("sendMsg msg:{},{} route succ", msgId, messageDto.getToId());
		dto.setMessagePush(JSON.parseObject(c2sProtocol.getBody(), MessagePush.class));
		dto.setMsgId(msgId);
		return dto;
	}

	private long getId() {
		return redisTemplate.incr(Constant.ID_KEY);
	}

	private byte[][] getOfflineMsgKeys(String key, long lastMsgId) {
		Set<Tuple> sets = null;
		if (lastMsgId <= 0) {
			// 查询最近的
			sets = redisTemplate.zrangeWithScores(key, 0, Constant.MAX_GET_OFFLINE_NUM);
		} else {// 最小id，为lastMsgId+1
			sets = redisTemplate.zrangeByScoreWithScores(key, Double.parseDouble(String.valueOf(lastMsgId + 1)),
					Double.MAX_VALUE, 0, Constant.MAX_GET_OFFLINE_NUM);
		}

		if (sets.size() <= 0) {
			return null;
		}

		byte[][] bytes = new byte[sets.size()][];
		int i = 0;
		for (Tuple set : sets) {
			String offlineKey = this.getOfflineMsgKey(Long.parseLong(set.getElement()));
			try {
				bytes[i] = (offlineKey.getBytes(Constant.CHARSET));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			i++;
		}

		return bytes;
	}

	private PullOfflineMsgResultDto pullOfflineMsg(String key, long lastMsgId) {

		PullOfflineMsgResultDto dto = new PullOfflineMsgResultDto();
		byte[][] offlineKeys = getOfflineMsgKeys(key, lastMsgId);
		if (offlineKeys == null) {
			return dto;
		}

		// 还需要拉取更多的消息，防止消息过期，还有部分消息没有同步
		dto.setMore(true);

		List<byte[]> c2sBytes = this.redisTemplate.mget(offlineKeys);
		if (c2sBytes == null) {
			return dto;
		}

		List<C2sProtocol> list = new ArrayList<C2sProtocol>();

		for (byte[] b : c2sBytes) {
			if(b==null||b.length<=0) {
				continue;
			}
			C2sProtocol newStt = null;
			try {
				newStt = simpleTypeCodec.decode(b);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			list.add(newStt);
		}

		dto.setList(list);

		return dto;
	}

	@Override
	public void batchSendMsg(SendMsgDto message, String excludeSessionId, List<String> userIds) {
		for (String userId : userIds) {
			SendMsgDto dto = mapper.map(message, SendMsgDto.class);
			dto.setToId(userId);

			SynMessageTaskDto taskDto = new SynMessageTaskDto();
			taskDto.setSendMsgDto(dto);
			taskDto.setExcludeSessionId(excludeSessionId);

			SynMessageTask.addTask(taskDto);
		}
	}

	@Override
	public void pushMsg(MessagePush messagePush, String pushId,SendMsgDto sendMsgDto) {
		
		long tenementId = messagePush.getTenementId();
		long proxyCid = messagePush.getProxyCid();

		long cid = conversationService.getAndCreateCid(tenementId,messagePush.getFromId(),messagePush.getToId(),proxyCid);
		messagePush.setCid(cid);
		
		log.info("pushMsg:{},{},{}",pushId,cid,JSON.toJSONString(messagePush));
		
		
		C2sProtocol c2sProtocol = saveOfflineMsg(messagePush, pushId,
				sendMsgDto.isSaveOfflineMsg()?messagePush.getType():MessageType.onlyPushOnline.getValue(),sendMsgDto.getProduct());

		this.conversationService.addRecentlyConversation(messagePush,sendMsgDto.isSaveFromConversation(),sendMsgDto.isSaveToConversation());

		c2sProtocol.setProduct(sendMsgDto.getProduct());
		// 路由协议
		this.protocolRouteService.route(tenementId,pushId,sendMsgDto.getProduct(),c2sProtocol,null);
	}



	@Override
	public List<C2sProtocol> pullOfflineMsg(OfflineMsgDto offlineMsgDto) {
		
		List<C2sProtocol> list =  pullOfflineMsgByOvertime(offlineMsgDto).getList();
		//更新用户已同步的最后一条消息id
		OfflineMaxIdTask.addTask(offlineMsgDto);
		return list;
	}

	@Override
	public PullOfflineMsgResultDto pullOfflineMsgByOvertime(OfflineMsgDto offlineMsgDto) {

		PullOfflineMsgResultDto dto = new PullOfflineMsgResultDto();
		// 验证参数
		boolean result = Launch.doValidator(offlineMsgDto);
		if (!result) {
			return dto;
		}

		long tenementId = offlineMsgDto.getTenementId();
		String userId = offlineMsgDto.getUserId();
		TenementDo tenement = this.tenementMapper.getTenementById(offlineMsgDto.getTenementId());
		if (tenement == null) {
			return dto;
		}

		String key = getOfflineSetKey(tenementId, userId);
		log.info("pullOfflineMsgByOvertime key:{}",key);
		
		long lastMsgId = offlineMsgDto.getLastMsgId();

		return pullOfflineMsg(key, lastMsgId);
	}
	
	private String getLastOfflineMsgIdKey(long tenementId, String userId) {
		String key = Constant.OFFLINE_MSG_MAX_ID_KEY+tenementId+"_"+userId;
		return key;
	}

	@Override
	public void updateLastOfflineMsgId(OfflineMsgDto offlineMsgDto,long lastMsgId) {
		String key = getLastOfflineMsgIdKey(offlineMsgDto.getTenementId(),offlineMsgDto.getUserId());
		String value = this.redisTemplate.get(key);
		
		if(value==null||lastMsgId>Long.parseLong(value)) {
			this.redisTemplate.set(key,value);
		}
	}

	@Override
	public long getLastOfflineMsgId(long tenementId, String userId) {
		String key   = getLastOfflineMsgIdKey(tenementId,userId);
		String value = this.redisTemplate.get(key);		
		if(value==null) {
			return 0l;
		}
		return Long.parseLong(value);
	}
}