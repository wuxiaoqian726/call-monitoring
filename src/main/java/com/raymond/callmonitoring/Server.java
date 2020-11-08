package com.raymond.callmonitoring;

import com.raymond.callmonitoring.emulator.CallEmulator;
import com.raymond.callmonitoring.producer.CallProducer;
import com.raymond.callmonitoring.producer.MockCallProducer;
import com.raymond.callmonitoring.producer.RocketmqCallProducer;
import com.raymond.callmonitoring.consumer.*;
import com.raymond.callmonitoring.transport.NotificationServer;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

public class Server {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        CallConsumer callConsumer = getCallConsumer(false);
        callConsumer.startConsuming();

        NotificationServer notificationServer = new NotificationServer();
        notificationServer.start();

    }

    private static CallConsumer getCallConsumer(boolean mockMode) throws MQClientException {
        if (mockMode)
            return new MockCallConsumer();
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");

        return new RocketMqConsumer(consumer);
    }

}
