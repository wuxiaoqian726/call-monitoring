package com.raymond.callmonitoring.transport;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import com.raymond.callmonitoring.AkkaActorSystem;
import com.raymond.callmonitoring.actor.CallSubscriptionActor;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.utils.JSONUtils;
import com.raymond.callmonitoring.utils.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Echoes uppercase content of text frames.
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();

            try {
                CallSubscription callSubscription = JSONUtils.toObject(request, CallSubscription.class);
                if (callSubscription.getUserId() == null || callSubscription.getQueueIdList() == null || callSubscription.getQueueIdList().isEmpty()){
                    throw new IllegalStateException("invalid subscription...");
                }
                logger.info("received subscription:{}", callSubscription.getQueueIdList());
                ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
                String uniqueChannelId = Utils.getUniqueChannelId(ctx.channel());
                actorSystem.actorOf(Props.create(CallSubscriptionActor.class, ctx.channel(), callSubscription), uniqueChannelId);
            }catch (Exception e){
                //TODO: handle json parse and other exceptions;
            }
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
        actorSystem.actorSelection(Utils.getActorPath(Utils.getUniqueChannelId(ctx.channel()))).tell(PoisonPill.getInstance(), ActorRef.noSender());
        super.channelInactive(ctx);
    }
}