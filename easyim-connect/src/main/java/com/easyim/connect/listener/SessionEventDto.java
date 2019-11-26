package com.easyim.connect.listener;

import com.easyim.biz.api.protocol.c2s.Message.UserType;
import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;
import com.easyim.biz.api.protocol.enums.c2s.ResourceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionEventDto {
	private UserStatus userStatus;
	private long tenementId;
	private long merchantId;
	private String userId;
	private ResourceType resource;//设备
	private UserType userType;//业务扩展字段
}
