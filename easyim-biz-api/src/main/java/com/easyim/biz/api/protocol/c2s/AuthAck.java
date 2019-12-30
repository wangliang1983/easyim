package com.easyim.biz.api.protocol.c2s;

import com.easyim.biz.api.protocol.c2s.Message.UserType;
import com.easyim.biz.api.protocol.enums.c2s.ResourceType;
import com.easyim.biz.api.protocol.enums.c2s.Result;
import com.easyim.biz.api.utils.MD5Util;

import lombok.Builder;
import lombok.Data;

/**
 * 验证结果返回
 * @author wl
 *
 */
@Data
public class AuthAck extends AbstractResultProtocol{
	private static final long serialVersionUID = -26783567650417475L;
	private ResourceType resource;//多设备登录相关
	private String userId;//用户id
	private String userDeviceId;//用户设备id
	private long tenementId;//用户租户
	private long merchantId;
	private UserType userType;
	
	public AuthAck() {
	}
	
	public AuthAck(Result result) {
		super.setResult(result);
	}

	

	
	
}
