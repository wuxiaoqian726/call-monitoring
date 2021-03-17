package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.server.service.ActorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncMockCallConsumer implements CallConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMockCallConsumer.class);
    private final Executor executor = Executors.newFixedThreadPool(1);

    @Override
    public void startConsuming() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CallSession callSession = MockInMemoryQueue.getInstance().pullMessage();
                        //logger.info(JSONUtils.toJsonString(callSession));
                        ActorService actorService = new ActorService();
                        actorService.sendCallSessionToActor(callSession);
                    } catch (InterruptedException e) {
                        logger.error("error happens:", e);
                    }
                }
            }
        });
    }


}
