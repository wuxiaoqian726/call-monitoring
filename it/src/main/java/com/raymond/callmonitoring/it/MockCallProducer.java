package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockCallProducer implements CallProducer {

    private static final Logger logger = LoggerFactory.getLogger(MockCallProducer.class);


    @Override
    public void produce(CallSession callSession) {
        try {
            MockImMemoryQueue.getInstance().addMessage(callSession);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
