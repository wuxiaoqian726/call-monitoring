package com.raymond.callmonitoring.actor.eventbus;

import java.util.List;

public class SubscribeOperation {
    private List<Long> queueId;
    private SubscribeOperationType operationType;

    public List<Long> getQueueId() {
        return queueId;
    }

    public void setQueueId(List<Long> queueId) {
        this.queueId = queueId;
    }

    public SubscribeOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(SubscribeOperationType operationType) {
        this.operationType = operationType;
    }
}
