package com.raymond.callmonitoring.it;

import akka.actor.Props;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.mq.CallProducer;
import com.raymond.callmonitoring.server.AkkaActorSystem;
import com.raymond.callmonitoring.server.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.server.service.ActorService;
import org.apache.rocketmq.client.exception.MQClientException;

public class MockServer {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallProducer callProducer = new MockCallProducer();
        //callProducer.produce();

        CallConsumer callConsumer = new MockCallConsumer();
        callConsumer.startConsuming();

        CallSubscription callSubscription = new CallSubscription();
        ActorService actorService = new ActorService();
        actorService.createCallSubscriptionActor(new MockNotificationAPIImpl(), callSubscription, "");

    }

}
