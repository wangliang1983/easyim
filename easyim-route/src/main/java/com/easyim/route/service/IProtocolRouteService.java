package com.easyim.route.service;

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
	public boolean route(long tenementId,String userId,String body,String excludeSessionId);
	
	/**
	 * 异步路由协议
	 * @param tenementId
	 * @param userId
	 * @param protocol
	 * @param String excludeSessionId
	 * @return
	 */
	public boolean routeAsyn(long tenementId,String userId,String body,String excludeSessionId);
}
