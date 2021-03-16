package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayCallSession implements Delayed {
    private final CallSession callSession;
    private final long timestamp;

    public DelayCallSession(CallSession callSession, long delay) {
        this.callSession = callSession;
        this.timestamp = System.currentTimeMillis() + delay;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return this.timestamp - System.currentTimeMillis();
    }

    public CallSession getCallSession() {
        return callSession;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Delayed o) {
        return (int) (this.timestamp - ((DelayCallSession) o).getTimestamp());
    }
}
