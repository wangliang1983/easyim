package com.easyim.biz.api.dto.message;


import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.easyim.biz.api.protocol.c2s.Message.UserType;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SendMsgDto implements Serializable{
	
	private static final long serialVersionUID = 5892262306396991909L;
	@Min(value = 0)
	private long tenementId;
	@NotNull
	private String fromId;
	
	private UserType fromType;
	
	private String proxyFromId;
	
	@NotNull
	private String toId;

	private UserType toType;
	
	private String proxyToId; 
	@Min(value = 0)
	private long cid;
	@Min(value = 0)
	private long proxyCid;
	@NotNull
	private MessageType type;
	@NotNull
	private String subType;
	@NotNull
	private String content;

	private String bizUid;//业务方唯一id
	
	private boolean saveOfflineMsg = false;//是否保存离线消息
	
	private boolean saveFromConversation = false;//是否保存发送方最近会话列表
	
	private boolean saveToConversation = false;//是否保存离线消息
	
	
	/**
	 * 客户端根据messageType解析推送的内容
	 * @author wl
	 *
	 */
	public static enum MessageType{
		text(0),//文本
		pic(1),//图片
		voice(2),//声音
		notify(3),//系统通知,不落库,只走离线消息
		userDefined(4),//自定义
		onlyPushOnline(5),//系统通知,不落库,不走离线消息,只推在线
		notifyOnline(5),//系统通知,不落库,不走离线消息,只推在线
		file(6),//文件
		video(7),//视频
		notifyIndb(100);//系统通知落库
		
		MessageType(int value){
			this.value = value;
		}
		private int value;
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}
		
		public static boolean isIncrementUnread(int type){
			if(type==text.value
					||type==pic.value
					||type==voice.value
					||type==file.value
					){
				return true;
			}else{
				return false;
			}
		}
		
		public static boolean isSaveDb(int type){
			if(type==text.value
					||type==pic.value
					||type==voice.value
					||type==file.value
					){
				return true;
			}else{
				return false;
			}
		}
		
		public static boolean isSaveOffline(int type){
			if(type==text.value
					||type==pic.value
					||type==voice.value
					||type==file.value
					||type==notify.value
					){
				return true;
			}else{
				return false;
			}
		}
		
		public static MessageType getMessageType(int type){
			MessageType[] types = MessageType.values();
			for(MessageType t:types){
				if(t.getValue()==type){
					return t;
				}
			}
			throw new IllegalArgumentException("value:"+type);
		}
	}
}
