package com.raymond.callmonitoring.service;

import com.raymond.callmonitoring.model.CallSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MockCallConsumer implements CallConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MockCallConsumer.class);
    private final Executor executor = Executors.newFixedThreadPool(1);

    @Override
    public void startConsuming() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CallSession callSession = MockImMemoryQueue.getInstance().pullMessage();
                        //logger.info("consume message:{}", JSONUtils.toJsonString(callSession));
                        //CallSessionActorManager.getInstance().sendCallSessionToActorSystem(callSession);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


}
