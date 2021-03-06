package com.easyim.connect.s2s.server;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.easyim.connect.s2s.input.S2sInputHandle;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order
public class TcpS2sServer {


	@Value("${c2s.server.port}")
	private int s2sTcpPort;
	
	@Resource
	private S2sInputHandle s2sInputHandle;
	
	@PostConstruct
	public void initTcpServer() {
		final EventLoopGroup workerGroup = new NioEventLoopGroup();
		final EventLoopGroup bossGroup = new NioEventLoopGroup();
		
	    new Thread(()->{
    		ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workerGroup)
            	.option(ChannelOption.SO_REUSEADDR,true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addFirst(s2sInputHandle);
                        pipeline.addFirst(new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addFirst(new JsonObjectDecoder());
                       
                    }

                });

            try {
                Channel ch = boot.bind(s2sTcpPort).sync().channel();

                log.info("===================================");
                log.info("s2s server start at port:"+s2sTcpPort);
                log.info("===================================");

                
                ch.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally{
               bossGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
            }
    	}).start();
	}


}
