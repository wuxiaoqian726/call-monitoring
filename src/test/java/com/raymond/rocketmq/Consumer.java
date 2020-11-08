package com.raymond.rocketmq;

import java.util.List;

import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

public class Consumer {

    public static void main(String[] args) throws InterruptedException, MQClientException {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");
        consumer.subscribe(Constants.TOPIC_NAME, "*");
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }
}