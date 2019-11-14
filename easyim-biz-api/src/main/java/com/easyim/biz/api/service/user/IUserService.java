package com.easyim.biz.api.service.user;

import java.util.List;

import com.easyim.biz.api.protocol.c2s.UserStatusPush.UserStatus;


/**
 * 用户在线状态查询
 * @author wl
 *
 */
public interface IUserService {
	/**
	 * 查询用户在线状态
	 * @param tenementId
	 * @param userIds
	 * @return
	 */
	List<String> selectUserOnline(long tenementId,List<String> userIds);
	
	/**
	 * 查询用户最后一次登录时间
	 * @param tenementId
	 * @param userIds
	 * @return
	 */
	List<String> selectUserLoginTime(long tenementId,List<String> userIds);
	
	/**
	 * 推送用户登录状态
	 * @param tenementId
	 * @param userId
	 * @param status
	 */
	void pushUserStatus(long tenementId,String userId,UserStatus status);
}
