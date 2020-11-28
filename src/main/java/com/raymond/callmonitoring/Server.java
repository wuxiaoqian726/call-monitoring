package com.raymond.callmonitoring;

import akka.actor.Props;
import com.raymond.callmonitoring.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.service.CallConsumer;
import com.raymond.callmonitoring.service.MockCallConsumer;
import com.raymond.callmonitoring.service.RocketMqConsumer;
import com.raymond.callmonitoring.transport.WebSocketNotificationServer;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;

public class Server {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallConsumer callConsumer = getCallConsumer(false);
        callConsumer.startConsuming();

        WebSocketNotificationServer webSocketNotificationServer = new WebSocketNotificationServer();
        webSocketNotificationServer.start();

    }

    private static CallConsumer getCallConsumer(boolean mockMode) throws MQClientException {
        if (mockMode)
            return new MockCallConsumer();
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");

        return new RocketMqConsumer(consumer);
    }

}
