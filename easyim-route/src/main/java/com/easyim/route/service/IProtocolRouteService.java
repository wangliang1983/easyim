package com.easyim.route.service;

import com.easyim.biz.api.dto.protocol.C2sProtocol;

/**
 * 协议路由
 * @author wl
 *
 */
public interface IProtocolRouteService {
	/**
	 * 同步路由协议
	 * @param tenementId
	 * @param userId
	 * @param protocol
	 * @param String excludeSessionId
	 * @return
	 */
	public boolean route(long tenementId,String userId,String product,C2sProtocol body,String excludeSessionId);
	
	/**
	 * 异步路由协议
	 * @param tenementId
	 * @param userId
	 * @param protocol
	 * @param String excludeSessionId
	 * @return
	 */
	public boolean routeAsyn(long tenementId,String userId,String product,C2sProtocol body,String excludeSessionId);
}
