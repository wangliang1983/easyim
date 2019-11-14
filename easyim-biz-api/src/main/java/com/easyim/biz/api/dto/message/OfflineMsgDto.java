package com.easyim.biz.api.dto.message;

import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.easyim.biz.api.protocol.enums.c2s.ResourceType;

import lombok.Data;


/**
 * 查询未同步消息
 * @author wl
 *
 */
@Data
public class OfflineMsgDto implements Serializable{
	
	private static final long serialVersionUID = -7897498471392569094L;
	
	@Min(value=1)
	private long tenementId;
	@NotBlank
	private String userId;
	@Min(value=0)
	private long lastMsgId = 0;
}
