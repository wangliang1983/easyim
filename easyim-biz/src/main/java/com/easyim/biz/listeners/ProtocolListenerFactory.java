package com.easyim.biz.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.easyim.biz.api.listeners.IProtocolListeners;
import com.easyim.biz.api.protocol.c2s.AbstractProtocol;
import com.easyim.biz.api.protocol.enums.c2s.C2sType;
import com.easyim.biz.api.protocol.enums.c2s.EasyImC2sType;
import com.easyim.biz.listeners.dto.ProtocolListenerDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;

@Slf4j
@Component
public class ProtocolListenerFactory
		implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent>{
	private static Map<String, List<IProtocolListeners>> map = new ConcurrentHashMap<String, List<IProtocolListeners>>();

	private static LinkedBlockingQueue<ProtocolListenerDto> queue = new LinkedBlockingQueue<ProtocolListenerDto>();

	private List<IProtocolListeners> getProtocolListener(C2sType c2sCommandType) {
		return map.get(c2sCommandType);
	}

	public static void addProtocolCallback(ProtocolListenerDto dto) {
		String type = dto.getC2sType();
		if (map.get(type) == null || map.get(type).size() <= 0) {
			return;
		}
		queue.add(dto);
		log.info("addProtocolCallback:{}", dto);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof IProtocolListeners) {
			IProtocolListeners l = (IProtocolListeners) bean;

			List<IProtocolListeners> list = map.get(l.type());
			if (list == null) {
				list = new ArrayList<IProtocolListeners>();
				map.put(l.type().getValue(), list);
			}
			list.add(l);
		}

		return bean;
	}


	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		new Thread(() -> {
			while (true) {
				ProtocolListenerDto dto = null;
				try {
					dto = queue.poll(1, TimeUnit.HOURS);
				} catch (InterruptedException e1) {
					e1.printStackTrace();// do noting
				}
				if (dto == null) {
					continue;
				}

				List<IProtocolListeners> list = map.get(dto.getC2sType());
				if (list == null) {
					continue;
				}

				try {
					for (IProtocolListeners l : list) {
						l.callback(dto.getUserSessionDto(), dto.getC2sType(), dto.getInput(), dto.getOutput());
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.error("exception:{}", e);
				}

			}
		}).start();
	}

}
