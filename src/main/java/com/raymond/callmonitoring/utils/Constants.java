package com.raymond.callmonitoring.utils;

public class Constants {
    public static String PRODUCER_GROUP_NAME = "callMonitoringProducer";
    public static String CONSUMER_GROUP_NAME = "consumerMonitoringProducer";
    public static String TOPIC_NAME = "CallMonitoring";
    public static Integer CONCURRENCY = 100;
    public static String ACTOR_PATH_PREFIX = "user/";

    public static Object globalLock = new Object();

}
