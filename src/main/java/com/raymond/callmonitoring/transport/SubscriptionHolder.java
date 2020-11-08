package com.raymond.callmonitoring.transport;

import com.raymond.callmonitoring.actor.CallSessionActor;
import com.raymond.callmonitoring.actor.CallSessionActorExecutor;
import com.raymond.callmonitoring.actor.CallSessionActorManager;
import com.raymond.callmonitoring.model.CallSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionHolder {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);

    private static ConcurrentHashMap<String, Channel> subscriptionHolder;

    private static SubscriptionHolder subscriptionHolderInstance = new SubscriptionHolder();


    private SubscriptionHolder() {
        subscriptionHolder = new ConcurrentHashMap<>();
    }

    public static SubscriptionHolder getInstance() {
        return subscriptionHolderInstance;
    }

    public void addSubscription(String userId, Channel channel) {
        if(userId==null){
            throw new IllegalStateException("");
        }
        subscriptionHolder.putIfAbsent(userId, channel);
    }

    public void pushNotification(CallSession callSession,String rowJsonMessage) throws UnsupportedEncodingException {
        if (callSession == null || callSession.getToUserId() == null) {
            throw new IllegalStateException("");
        }
        Channel channel = subscriptionHolder.get(callSession.getToUserId().toString());
        if(channel==null){
            return;
        }
        if(!channel.isWritable()){
            throw  new IllegalStateException("");
        }
        Date now = new Date();
        StringBuilder builder=new StringBuilder();
        builder.append("sessionId:").append(callSession.getSessionId()).append(",");
        builder.append("userId:").append(callSession.getToUserId()).append(",");
        builder.append("status:").append(callSession.getStatus()).append(",");
        builder.append("delay:").append(now.getTime() - callSession.getTimeStamp().getTime()).append("\t");
        //logger.info("sessionId:{},status:{},delay:{}", callSession.getToUserId(), callSession.getStatus(), );
        ByteBuf messageBuf = Unpooled.buffer();
        messageBuf.writeBytes(builder.toString().getBytes("UTF-8"));
        channel.writeAndFlush(messageBuf);
    }

    public void removeSubscription(String userId){
        if(userId==null){
            throw new IllegalStateException("");
        }
    }

}
