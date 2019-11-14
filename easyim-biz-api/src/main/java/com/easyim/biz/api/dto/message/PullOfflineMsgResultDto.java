package com.easyim.biz.api.dto.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.easyim.biz.api.dto.protocol.C2sProtocol;

import lombok.Data;

/**
 * 未同步消息返回
 * @author wl
 *
 */
@Data
public class PullOfflineMsgResultDto implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8029370791408841800L;
	private List<C2sProtocol> list = new ArrayList<C2sProtocol>();//消息数
	private boolean more = false;//是否有更多的未同步消息
}
