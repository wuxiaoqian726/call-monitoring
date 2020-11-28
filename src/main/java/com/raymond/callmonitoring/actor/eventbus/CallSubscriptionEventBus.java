package com.raymond.callmonitoring.actor.eventbus;

import akka.actor.ActorRef;
import akka.event.japi.LookupEventBus;
import com.raymond.callmonitoring.model.PullQueueStat;

public class CallSubscriptionEventBus extends LookupEventBus<PullQueueStat, ActorRef, Long> {

    private static CallSubscriptionEventBus instance = new CallSubscriptionEventBus();

    private CallSubscriptionEventBus() {
    }

    @Override
    public int mapSize() {
        return 300;
    }

    @Override
    public int compareSubscribers(ActorRef a, ActorRef b) {
        return a.compareTo(b);
    }

    @Override
    public Long classify(PullQueueStat pullQueueStat) {
        return pullQueueStat.getQueueId();
    }

    @Override
    public void publish(PullQueueStat pullQueueStat, ActorRef subscriber) {
        subscriber.tell(pullQueueStat, ActorRef.noSender());
    }

    public static CallSubscriptionEventBus getInstance() {
        return instance;
    }
}
