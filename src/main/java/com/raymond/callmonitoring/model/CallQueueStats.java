package com.raymond.callmonitoring.model;

public class CallQueueStats {
    private Long queueId;
    private int waitingCount = 0;
    private int longestWaitingSeconds = 0;

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public void setWaitingCount(int waitingCount) {
        this.waitingCount = waitingCount;
    }

    public int getWaitingCount() {
        return waitingCount;
    }

    public int getLongestWaitingSeconds() {
        return longestWaitingSeconds;
    }

    public void setLongestWaitingSeconds(int longestWaitingSeconds) {
        this.longestWaitingSeconds = longestWaitingSeconds;
    }
}
