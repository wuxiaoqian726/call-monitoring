package com.raymond.callmonitoring.server.actor;

import akka.actor.AbstractActor;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.common.Utils;
import com.raymond.callmonitoring.server.AkkaActorSystem;
import com.raymond.callmonitoring.server.model.CallSubscriptionOperation;
import com.raymond.callmonitoring.server.model.CallSubscriptionOperationType;

import com.raymond.callmonitoring.server.model.CallQueueStats;
import com.raymond.callmonitoring.server.model.CallSubscription;
import com.raymond.callmonitoring.server.model.PullQueueStat;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallSubscriptionActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(CallSubscriptionActor.class);

    private final Channel channel;
    private final CallSubscription callSubscription;

    public CallSubscriptionActor(Channel channel, CallSubscription callSubscription) {
        this.channel = channel;
        this.callSubscription = callSubscription;
    }

    @Override
    public void preStart() throws Exception {
        logger.info("CallSubscriptionActor start...");
        super.preStart();
        this.subscribeOrUnSubscribe(CallSubscriptionOperationType.Subscribe);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PullQueueStat.class, pullStat -> {
                    this.handlePullQueueStat(pullStat);
                }).build();
    }

    @Override
    public void postStop() throws Exception {
        logger.info("stop subscription actor....");
        this.subscribeOrUnSubscribe(CallSubscriptionOperationType.UnSubscribe);
        super.postStop();
    }

    private void subscribeOrUnSubscribe(CallSubscriptionOperationType type){
        CallSubscriptionOperation subscribeOperation = new CallSubscriptionOperation();
        subscribeOperation.setOperationType(type);
        subscribeOperation.setQueueId(callSubscription.getQueueIdList());
        AkkaActorSystem.getInstance().getActorSystem().actorSelection(Utils.getActorPath(CallSubscriptionRouter.ACTOR_NAME)).tell(subscribeOperation, this.self());

    }

    private void handlePullQueueStat(PullQueueStat pullQueueStat) {
        CallQueueStats callQueueStats = CallStatsHolder.getQueueStats(pullQueueStat.getQueueId());
        if (this.channel.isWritable()) {
            this.channel.writeAndFlush(new TextWebSocketFrame(JSONUtils.toJsonString(callQueueStats)));
        }
    }
}
