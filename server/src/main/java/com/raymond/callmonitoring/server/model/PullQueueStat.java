package com.raymond.callmonitoring.server.model;

public class PullQueueStat {
    private final Long queueId;

    public PullQueueStat(Long queueId) {
        this.queueId = queueId;
    }

    public Long getQueueId() {
        return queueId;
    }
}