package com.raymond.callmonitoring.common;

public class Constants {
    public static String PRODUCER_GROUP_NAME = "callMonitoringProducer";
    public static String CONSUMER_GROUP_NAME = "consumerMonitoringProducer";
    public static String TOPIC_NAME = "CallMonitoring";
    public static Integer CONCURRENCY = 50000;
    public static Integer MAX_QUEUE_CONCURRENCY = 100000;
    public static Integer MAX_AGENT_CONCURRENCY = 2000000;
    public static String ACTOR_PATH_PREFIX = "user/";

    //public static Object globalLock = new Object();

}
