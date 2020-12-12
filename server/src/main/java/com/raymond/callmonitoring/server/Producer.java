package com.raymond.callmonitoring.server;

import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.emulator.CallEmulator;

import com.raymond.callmonitoring.mq.CallProducer;
import com.raymond.callmonitoring.mq.RocketmqCallProducer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

public class Producer {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        CallEmulator callEmulator = new CallEmulator(getCallProducer());
        callEmulator.start();
        Thread.currentThread().join();
    }

    private static CallProducer getCallProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(Constants.TOPIC_NAME);
        producer.setNamesrvAddr("localhost:9876");
        producer.setProducerGroup(Constants.PRODUCER_GROUP_NAME);
        try {
            producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return new RocketmqCallProducer(producer);
    }
}
