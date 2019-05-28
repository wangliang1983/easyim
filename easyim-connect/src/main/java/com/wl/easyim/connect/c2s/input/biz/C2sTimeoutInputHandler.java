package com.wl.easyim.connect.c2s.input.biz;

import com.wl.easyim.connect.session.SessionManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class C2sTimeoutInputHandler extends ReadTimeoutHandler{
	
    private boolean closed;

	
	public C2sTimeoutInputHandler(int timeoutSeconds) {
		super(timeoutSeconds);
	}
	
	/**
     * Is called when a read timeout was detected.
     */
    protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
            SessionManager.removeSession(ctx,SessionManager.TIMEOUT);
            closed = true;
        }
    }

}
