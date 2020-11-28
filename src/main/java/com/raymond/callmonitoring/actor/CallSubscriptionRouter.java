package com.raymond.callmonitoring.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.raymond.callmonitoring.actor.eventbus.CallSubscriptionEventBus;
import com.raymond.callmonitoring.actor.eventbus.SubscribeOperation;
import com.raymond.callmonitoring.actor.eventbus.SubscribeOperationType;
import com.raymond.callmonitoring.model.PullQueueStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallSubscriptionRouter extends AbstractActor {

    public static final String ACTOR_NAME = "RouterOfEventBus";

    private static final Logger logger = LoggerFactory.getLogger(CallSubscriptionRouter.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscribeOperation.class, subscribeOperation -> {
                    if (subscribeOperation.getOperationType() == SubscribeOperationType.Subscribe) {
                        logger.info("subscribe...");
                        subscribeOperation.getQueueId().forEach(item->{
                            CallSubscriptionEventBus.getInstance().subscribe(sender(), item);
                            sender().tell(new PullQueueStat(item), ActorRef.noSender());
                        });
                    } else {
                        logger.info("unsubscribe...");
                        subscribeOperation.getQueueId().forEach(item->{
                            CallSubscriptionEventBus.getInstance().unsubscribe(sender(), item);
                        });
                    }

                }).build();

    }
}
