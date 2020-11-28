package com.raymond.callmonitoring.model;

import java.util.Date;

public class CallSession {

    private String sessionId;
    private CallSessionStatus status;
    private Long toUserId;
    private Long toQueueId;
    private Long toAgentId;
    private Date timeStamp;
    private int stepIndex = 0;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public CallSessionStatus getStatus() {
        return status;
    }

    public void setStatus(CallSessionStatus status) {
        this.status = status;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getToQueueId() {
        return toQueueId;
    }

    public void setToQueueId(Long toQueueId) {
        this.toQueueId = toQueueId;
    }

    public Long getToAgentId() {
        return toAgentId;
    }

    public void setToAgentId(Long toAgentId) {
        this.toAgentId = toAgentId;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
