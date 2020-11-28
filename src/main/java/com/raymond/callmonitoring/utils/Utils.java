package com.raymond.callmonitoring.utils;

import com.raymond.callmonitoring.model.CallSession;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Date;

public class Utils {

    public static long diffTimestamp(Date time){
        if(time==null){
            return new Date().getTime();
        }
        return new Date().getTime() - time.getTime();
    }

    public static String getActorPath(String name){
        if (name == null ) {
            throw new IllegalStateException("name cannot be null");
        }
        return Constants.ACTOR_PATH_PREFIX + name;
    }

    public static String getCallSessionActorName(CallSession callSession){
        if (callSession.getToUserId() == null || StringUtils.isEmpty(callSession.getSessionId())) {
            throw new IllegalStateException("Both UserId and session id cannot be null");
        }
        return callSession.getToUserId() + "-" + callSession.getSessionId();
    }

    public static String getCallSessionActorPath(CallSession callSession){
        return Constants.ACTOR_PATH_PREFIX + getCallSessionActorName(callSession);
    }

    public static String getUniqueChannelId(Channel channel) {
        if (channel == null || channel.remoteAddress() == null || channel.localAddress() == null) {
            throw new IllegalStateException("Invalid channel");
        }
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();

        StringBuilder builder=new StringBuilder();
        builder.append(remoteAddress.getHostName()).append('-').append(remoteAddress.getPort());
        builder.append("-");
        builder.append(localAddress.getHostName()).append("-").append(localAddress.getPort());
        return builder.toString();
    }
}
