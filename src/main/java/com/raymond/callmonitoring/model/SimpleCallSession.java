package com.raymond.callmonitoring.model;

public class SimpleCallSession implements Comparable<SimpleCallSession> {
    private String sessionId;
    private Long timestamp;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(SimpleCallSession o) {
        if (this.timestamp < o.getTimestamp()) {
            return -1;
        } else if (this.timestamp > o.getTimestamp()) {
            return 1;
        } else {
            return 0;
        }
    }
}
