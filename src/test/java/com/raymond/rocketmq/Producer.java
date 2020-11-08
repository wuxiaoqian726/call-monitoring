package com.raymond.rocketmq;

import com.raymond.callmonitoring.emulator.CallEmulator;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * This class demonstrates how to send messages to brokers using provided {@link DefaultMQProducer}.
 */
public class Producer {
    public static void main(String[] args) throws MQClientException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer(Constants.PRODUCER_GROUP_NAME);
        producer.setNamesrvAddr("localhost:9876");
        producer.start();



//        CallEmulator callEmulator = null;
//        try {
//            callEmulator = new CallEmulator(producer);
//            callEmulator.start();
//        } catch (Exception e) {
//            if (callEmulator != null)
//                callEmulator.stop();
//            producer.shutdown();
//        }

    }
}
