package com.raymond.callmonitoring.server;

import com.google.common.util.concurrent.MoreExecutors;
import com.raymond.callmonitoring.server.transport.NettyDirectMemFetcher;
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
    private static final AtomicInteger consumedMsgCount = new AtomicInteger(0);
    private static final AtomicInteger pushedNotificationCount = new AtomicInteger(0);
    private static final AtomicInteger consumedMsgDelayCount = new AtomicInteger(0);
    private static final AtomicInteger akkaMsgDelayCount = new AtomicInteger(0);
    public static int CONSUMING_LATENCY_THRESHOLD_MILLISECONDS = 1000;
    private static final NettyDirectMemFetcher nettyDirectMemFetcher = new NettyDirectMemFetcher();



    public static void incConnectedClientCount() {
        connectedClientCount.incrementAndGet();
    }

    public static void decConnectedClientCount() {
        connectedClientCount.decrementAndGet();
    }

    public static void incConsumedMsgCount() {
        consumedMsgCount.incrementAndGet();
    }

    public static void incPushedNotificationCount() {
        pushedNotificationCount.incrementAndGet();
    }

    public static void incConsumedMsgDelayCount() {
        consumedMsgDelayCount.incrementAndGet();
    }

    public static void incAkkaConsumedMsgDelayCount() {
        akkaMsgDelayCount.incrementAndGet();
    }

    public static void startReport() {
        executor.scheduleAtFixedRate(() -> {
            logger.info("connectedClientCount:{}, consumedMsgCount:{},pushedNotificationCount:{},consumedMsgDelayCount:{},akkaMsgDelayCount:{}",
                    connectedClientCount, consumedMsgCount, pushedNotificationCount, consumedMsgDelayCount, akkaMsgDelayCount);
            logger.info("netty direct memory usage:{}", nettyDirectMemFetcher.getDirectMem());
        }, 0, 10, TimeUnit.SECONDS);
    }
}
