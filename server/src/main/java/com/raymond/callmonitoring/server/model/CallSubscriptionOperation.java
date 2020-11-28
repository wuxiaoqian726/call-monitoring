package com.raymond.callmonitoring.server.model;

import java.util.List;

public class CallSubscriptionOperation {
    private List<Long> queueId;
    private CallSubscriptionOperationType operationType;

    public List<Long> getQueueId() {
        return queueId;
    }

    public void setQueueId(List<Long> queueId) {
        this.queueId = queueId;
    }

    public CallSubscriptionOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(CallSubscriptionOperationType operationType) {
        this.operationType = operationType;
    }
}
