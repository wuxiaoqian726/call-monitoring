package com.raymond.callmonitoring.mq;

import com.raymond.callmonitoring.common.Constants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMqConsumer implements CallConsumer {
    private final DefaultMQPushConsumer consumer;
    private final MessageListenerOrderly messageListenerOrderly;
    private static final Logger logger = LoggerFactory.getLogger(RocketMqConsumer.class);

    public RocketMqConsumer(DefaultMQPushConsumer consumer,MessageListenerOrderly messageListenerOrderly) {
        this.consumer = consumer;
        this.messageListenerOrderly = messageListenerOrderly;
    }

    @Override
    public void startConsuming() {
        try {
            consumer.subscribe(Constants.TOPIC_NAME, "*");
            consumer.registerMessageListener(messageListenerOrderly);
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }




}
