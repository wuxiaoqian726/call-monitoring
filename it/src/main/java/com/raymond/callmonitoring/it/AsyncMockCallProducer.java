package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.emulator.CallSessionBuilder;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncMockCallProducer implements CallProducer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncMockCallProducer.class);

    private final Executor executor = Executors.newFixedThreadPool(1);
    private DelayQueue<CallSession> delayQueue = new DelayQueue<>();

    @Override
    public void produce(CallSession callSession) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CallSession callSession1 = delayQueue.take();
                        MockInMemoryQueue.getInstance().addMessage(callSession);
                    } catch (InterruptedException e) {
                        logger.error("error happens:", e);
                    }
                }
            }
        });
    }

    private void doProduce(Long queueId){
        CallSessionBuilder builder = new CallSessionBuilder();
        Date now = new Date();
        Date fiveSecondsAgo = new Date(now.getTime() - (1000 * 5));
        Date sixSecondsAgo = new Date(now.getTime() - (1000 * 6));
        Date eightSecondsAgo = new Date(now.getTime() - (1000 * 8));
        Date tenSecondsAgo = new Date(now.getTime() - (1000 * 10));
        CallSession callSessionWithFiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, fiveSecondsAgo);
        CallSession callSessionWithSixSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, sixSecondsAgo);
        CallSession callSessionWithEightSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, eightSecondsAgo);
        CallSession callSessionWithTenSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, tenSecondsAgo);
        CallSession callSessionWithNow = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, now);
        delayQueue.add(callSessionWithTenSecondsAgo);
        delayQueue.add(callSessionWithEightSecondsAgo);
        delayQueue.add(callSessionWithSixSecondsAgo);
        delayQueue.add(callSessionWithFiveSecondsAgo);
        delayQueue.add(callSessionWithNow);
    }


}
