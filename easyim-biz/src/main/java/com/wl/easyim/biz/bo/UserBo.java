package com.wl.easyim.biz.bo;

import com.wl.easyim.biz.api.protocol.c2s.enums.ResourceType;

import lombok.Data;

@Data
public class UserBo {
	private long tenementId;
	private String userId;
	private ResourceType resourceType;
}