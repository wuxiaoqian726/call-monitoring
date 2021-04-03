package com.raymond.callmonitoring.server.transport;

import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.server.Monitor;
import com.raymond.callmonitoring.server.service.NotificationAPI;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketNotificationAPIImpl implements NotificationAPI {

    private Channel channel;

    public WebSocketNotificationAPIImpl(Channel channel) {
        this.channel = channel;
    }

    @Override
    public boolean sendNotification(Object obj) {
        if (!this.channel.isWritable()) {
            return false;
        }
        this.channel.writeAndFlush(new TextWebSocketFrame(JSONUtils.toJsonString(obj)));
        Monitor.incPushedNotificationCount();
        return true;
    }
}
