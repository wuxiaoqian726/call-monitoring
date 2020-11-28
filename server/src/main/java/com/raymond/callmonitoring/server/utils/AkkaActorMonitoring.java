package com.raymond.callmonitoring.server.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AkkaActorMonitoring {
    private static Map<String, AtomicInteger> stats = new ConcurrentHashMap<>();
    private static String CONSUMING_LATENCY_KEY = "consumingLatencyKey";
    public static int CONSUMING_LATENCY_THRESHOLD_MILLISECONDS = 1000;

    static {
        stats.put(CONSUMING_LATENCY_KEY, new AtomicInteger(0));
    }

    public static void addConsumingLatencyCount(){
        stats.get(CONSUMING_LATENCY_KEY).incrementAndGet();
    }
}
