package com.easyim.biz.api.protocol.c2s;

import lombok.Data;

/**
 * 消息发送回复
 * @author wl
 *
 */
@Data
public class MessageAck extends AbstractResultProtocol{
	private static final long serialVersionUID = 3750475092007778892L;

	private long msgId;
	
	private long cid;
	
}
