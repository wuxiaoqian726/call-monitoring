package com.raymond.netty;

import com.alibaba.fastjson.JSON;
import com.raymond.callmonitoring.emulator.CallEmulator;
import com.raymond.callmonitoring.emulator.CallSessionGeneratorImpl;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.producer.CallProducer;
import com.raymond.callmonitoring.producer.MockCallProducer;
import com.raymond.callmonitoring.producer.RocketmqCallProducer;
import com.raymond.callmonitoring.consumer.*;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.util.ArrayList;
import java.util.List;

public class MainTest {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        CallEmulator callEmulator = new CallEmulator(getCallProducer(true));
        callEmulator.start();

        CallConsumer callConsumer = getCallConsumer(true);

        callConsumer.startConsuming();
    }

    private static CallProducer getCallProducer(boolean mockMode){
        if(mockMode)
            return new MockCallProducer();
        DefaultMQProducer producer = new DefaultMQProducer("firstTopicGroup");
        producer.setNamesrvAddr("localhost:9876");
        return new RocketmqCallProducer(producer);
    }

    private static CallConsumer getCallConsumer(boolean mockMode) throws MQClientException {
        if(mockMode)
            return new MockCallConsumer();
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");

        return new RocketMqConsumer(consumer);
    }

    public static void test1(String[] args) throws InterruptedException {
        CallSessionGeneratorImpl callGenerator = new CallSessionGeneratorImpl();
        int count = 10;
        while (count > 0) {
            count--;

            List<CallSession> sessions = new ArrayList<>();
            CallSession callSession = callGenerator.generateInitialCall();
            sessions.add(callSession);
            while (!CallSessionStatus.finishedCall(callSession.getStatus())) {
                callSession  = callGenerator.generateNextPhaseCall(callSession);
                sessions.add(callSession);
            }

            sessions.forEach(item -> {
                System.out.println(JSON.toJSON(item));
            });
            System.out.println("-------------");
        }

    }
}
