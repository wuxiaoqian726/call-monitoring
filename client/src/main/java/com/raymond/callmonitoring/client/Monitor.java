package com.raymond.callmonitoring.client;

import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    private static ScheduledExecutorService executor = MoreExecutors.getExitingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(1), 10, TimeUnit.SECONDS);
    private static final AtomicInteger connectedClientCount = new AtomicInteger(0);
    private static final AtomicInteger errorClientCount = new AtomicInteger(0);
    private static final AtomicInteger receivedMsgCount = new AtomicInteger(0);

    public static void incConnectedClientCount() {
        connectedClientCount.incrementAndGet();
    }

    public static void decConnectedClientCount() {
        connectedClientCount.decrementAndGet();
    }

    public static void incReceivedMsgCount() {
        receivedMsgCount.incrementAndGet();
    }

    public static void incErrorClientCount() {
        errorClientCount.incrementAndGet();
    }

    public static void startReport() {
        executor.scheduleAtFixedRate(() -> {
            logger.info("connectedClientCount:{}, receivedMsgCount:{}, errorClientCount:{}",
                    connectedClientCount, receivedMsgCount, errorClientCount);
        }, 0, 20, TimeUnit.SECONDS);
    }
}
