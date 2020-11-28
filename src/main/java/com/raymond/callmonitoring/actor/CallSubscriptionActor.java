package com.raymond.callmonitoring.actor;

import akka.actor.AbstractActor;
import com.raymond.callmonitoring.AkkaActorSystem;
import com.raymond.callmonitoring.actor.eventbus.CallSubscriptionEventBus;
import com.raymond.callmonitoring.actor.eventbus.SubscribeOperation;
import com.raymond.callmonitoring.actor.eventbus.SubscribeOperationType;
import com.raymond.callmonitoring.model.CallQueueStats;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.model.PullQueueStat;
import com.raymond.callmonitoring.utils.Utils;
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
        this.subscribeOrUnSubscribe(SubscribeOperationType.Subscribe);
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
        System.out.println("stop subscription actor....");
        this.subscribeOrUnSubscribe(SubscribeOperationType.UnSubscribe);
        super.postStop();
    }

    private void subscribeOrUnSubscribe(SubscribeOperationType type){
        SubscribeOperation subscribeOperation = new SubscribeOperation();
        subscribeOperation.setOperationType(type);
        subscribeOperation.setQueueId(callSubscription.getQueueIdList());
        AkkaActorSystem.getInstance().getActorSystem().actorSelection(Utils.getActorPath(CallSubscriptionRouter.ACTOR_NAME)).tell(subscribeOperation, this.self());

    }

    private void handlePullQueueStat(PullQueueStat pullQueueStat) {
        CallQueueStats callQueueStats = CallStatsHolder.getQueueStats(pullQueueStat.getQueueId());
        if (this.channel.isWritable()) {
            logger.info("push notification....");
            StringBuilder builder = new StringBuilder();
            builder.append("WaitingCount:").append(callQueueStats.getWaitingCount()).append(",");
            builder.append("LongestWaitingSeconds:").append(callQueueStats.getLongestWaitingSeconds()).append(",");
            builder.append("\t");
            this.channel.writeAndFlush(new TextWebSocketFrame(builder.toString()));
        }
    }
}
