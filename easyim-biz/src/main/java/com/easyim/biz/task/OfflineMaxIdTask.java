package com.easyim.biz.task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.easyim.biz.api.dto.message.OfflineMsgDto;
import com.easyim.biz.api.dto.message.SendMsgDto;
import com.easyim.biz.api.service.message.IMessageService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OfflineMaxIdTask {
	private static final Map<Integer,LinkedBlockingQueue<OfflineMsgDto>> OFFLINE_MAP = new HashMap<Integer,LinkedBlockingQueue<OfflineMsgDto>>();

	public static final int SHARDING =Runtime.getRuntime().availableProcessors()*2;
	
	
	static {
		for(int i=0;i<SHARDING;i++){
			OFFLINE_MAP.put(i, new LinkedBlockingQueue<OfflineMsgDto>());
		}
	}
	
	
	@Data
	public static class SynMessageTaskDto{
		private SendMsgDto sendMsgDto;
		private String excludeSessionId;
	}
	
	@Resource
	private IMessageService service;
	
	

	
	public static void addTask(OfflineMsgDto dto){
		String hash  = dto.getTenementId()+dto.getUserId();
		
		int key = Math.abs(hash.hashCode())%SHARDING;
		OFFLINE_MAP.get(key).add(dto);
	}
	
	
	public static final class OfflineThead implements Runnable{
		private int key;
		
		private IMessageService service;
		
		public OfflineThead(IMessageService service,int key) {
			this.key = key;
			this.service = service;
		}

		@Override
		public void run() {
			OfflineMsgDto offlineMsgDto;
			try {
				offlineMsgDto = OFFLINE_MAP.get(key).poll(1, TimeUnit.HOURS);

				if(offlineMsgDto!=null) {
					this.service.updateLastOfflineMsgId(offlineMsgDto, offlineMsgDto.getLastMsgId());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.info("exception:",e);
			}
			
		}
	}
	
	@PostConstruct
	public void init(){
		for(int i=0;i<SHARDING;i++){
			new Thread(new OfflineThead(service,i)).start();
		}
	}
}
