package com.wl.easyim.biz.api.protocol.c2s.protocol;

import lombok.Data;

@Data
public class ReadMessagePush extends AbstractProtocol {
	private static final long serialVersionUID = 1133349500935881112L;
	private String toId;
	private long cid;
}
