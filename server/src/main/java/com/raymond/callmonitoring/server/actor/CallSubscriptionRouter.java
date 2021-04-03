package com.raymond.callmonitoring.server.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.raymond.callmonitoring.server.model.CallSubscriptionOperation;
import com.raymond.callmonitoring.server.model.CallSubscriptionOperationType;
import com.raymond.callmonitoring.server.model.PullQueueStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallSubscriptionRouter extends AbstractActor {

    public static final String ACTOR_NAME = "EventBusRouter";

    private static final Logger logger = LoggerFactory.getLogger(CallSubscriptionRouter.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CallSubscriptionOperation.class, subscribeOperation -> {
                    if (subscribeOperation.getOperationType() == CallSubscriptionOperationType.Subscribe) {
                        logger.debug("subscribe...");
                        subscribeOperation.getQueueId().forEach(item->{
                            CallSubscriptionEventBus.getInstance().subscribe(sender(), item);
                            sender().tell(new PullQueueStat(item), ActorRef.noSender());
                        });
                    } else {
                        logger.debug("unsubscribe...");
                        subscribeOperation.getQueueId().forEach(item->{
                            CallSubscriptionEventBus.getInstance().unsubscribe(sender(), item);
                        });
                    }

                }).build();

    }
}
