package com.raymond.callmonitoring;

import com.raymond.callmonitoring.consumer.CallConsumer;
import com.raymond.callmonitoring.emulator.CallEmulator;
import com.raymond.callmonitoring.producer.CallProducer;
import com.raymond.callmonitoring.producer.MockCallProducer;
import com.raymond.callmonitoring.producer.RocketmqCallProducer;
import com.raymond.callmonitoring.transport.NotificationServer;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

public class Producer {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        CallEmulator callEmulator = new CallEmulator(getCallProducer(false));
        callEmulator.start();
        Thread.currentThread().join();
    }

    private static CallProducer getCallProducer(boolean mockMode) {
        if (mockMode)
            return new MockCallProducer();
        DefaultMQProducer producer = new DefaultMQProducer(Constants.TOPIC_NAME);
        producer.setNamesrvAddr("localhost:9876");
        try {
            producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return new RocketmqCallProducer(producer);
    }
}
