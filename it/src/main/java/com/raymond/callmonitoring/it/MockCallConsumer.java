package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.server.Monitor;
import com.raymond.callmonitoring.server.service.ActorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockCallConsumer implements CallConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MockCallConsumer.class);

    @Override
    public void startConsuming() {
        CallSession callSession = null;
        try {
            while (MockInMemoryQueue.getInstance().getQueueSize() > 0) {
                callSession = MockInMemoryQueue.getInstance().pullMessage();
                System.out.println(JSONUtils.toJsonString(callSession));
                ActorService actorService = new ActorService();
                actorService.sendCallSessionToActor(callSession);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
