package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.mq.CallProducer;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CallEmulator {

    private static final Logger logger = LoggerFactory.getLogger(CallEmulator.class);

    private final ThreadPoolExecutor generateNewCallExecutor;
    private final ThreadPoolExecutor generateFollowingCallExecutor;
    private final ThreadPoolExecutor mainExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(11);
    private final BlockingQueue<Runnable> newCallQueue = new LinkedBlockingQueue<Runnable>(2000);
    private final BlockingQueue<Runnable> followingCallQueue = new LinkedBlockingQueue<Runnable>(2000);
    private final DelayQueue<DelayCallSession> delayQueue = new DelayQueue<>();
    private final int concurrency;
    private final CallProducer producer;
    private volatile boolean start = false;
    private AtomicInteger liveCallerCount = new AtomicInteger(0);
    private ScheduledExecutorService newCallerCheckExecutor = Executors.newSingleThreadScheduledExecutor();
    private final int mainThreadCount=10;
    private CountDownLatch countDownLatch = new CountDownLatch(mainThreadCount);

    public CallEmulator(CallProducer producer, int concurrency) {
        this.producer = producer;
        this.concurrency = concurrency;
        this.generateNewCallExecutor = new ThreadPoolExecutor(
                10,
                10,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.newCallQueue,
                new ThreadFactoryImpl("GenerateNewCallExecutor"));
        this.generateFollowingCallExecutor = new ThreadPoolExecutor(
                10,
                10,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.followingCallQueue,
                new ThreadFactoryImpl("GenerateFollowingCallExecutor"));
    }

    public void start() throws InterruptedException {
        start = true;
        newCallerCheckExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!start) {
                    return;
                }
                int callerCount = liveCallerCount.get();
                int count = concurrency - callerCount;
                //logger.debug("Check whether need to create new callers, expected concurrency:{},live callers:{}", concurrency, callerCount);
                for (int i = 0; i < count; i++) {
                    Caller caller = new Caller(true, delayQueue);
                    liveCallerCount.incrementAndGet();
                    generateNewCallExecutor.submit(caller);
                }
                //logger.debug("Finished to check whether need to create new callers.");
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);

        int count = mainThreadCount;

        while (count > 0) {
            mainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (start || delayQueue.size() > 0) {
                        DelayCallSession delayCallSession = null;
                        try {
                            delayCallSession = delayQueue.poll(1000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            logger.error("error happens when take element from delay queue.", e);
                            continue;
                        }
                        if (delayCallSession == null) {
                            continue;
                        }
                        CallSession callSession = delayCallSession.getCallSession();
                        //logger.info("session:{},status:{},poll from delay queue.", callSession.getSessionId(), callSession.getStatus());
                        if (CallSessionStatus.Queue_Waiting != callSession.getStatus()) {
                            callSession.setTimeStamp(new Date());
                        }
                        producer.produce(callSession);
                        if (CallSessionStatus.isFinishedCall(callSession.getStatus())) {
                            liveCallerCount.decrementAndGet();
                            continue;
                        }
                        generateFollowingCallExecutor.submit(new Caller(delayQueue, callSession));
                    }
                    countDownLatch.countDown();
                }
            });
            count--;
        }
    }

    public void stop() throws InterruptedException {
        start = false;
        while (liveCallerCount.get() > 0) {
            logger.info("Still has {} non-finished callers.", this.liveCallerCount.get());
            Thread.currentThread().join(2000);
        }
        countDownLatch.await();
        this.newCallerCheckExecutor.shutdownNow();
        this.generateFollowingCallExecutor.shutdownNow();
        this.generateNewCallExecutor.shutdownNow();
        this.mainExecutor.shutdownNow();
        logger.info("Finished to shutdown Emulator.");
    }


}
