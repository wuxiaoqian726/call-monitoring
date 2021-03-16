package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;

import java.util.Date;

public class CallSessionBuilder {

    public CallSession buildCallQueueWaitingSessionWithTimestamp(Long queueId, Date timestamp) {
        return this.buildCallSessionWithTimestamp(queueId, timestamp, CallSessionStatus.Queue_Waiting);
    }

    public CallSession buildCallSessionWithTimestamp(Long queueId, Date timestamp,CallSessionStatus status) {
        CallSession callSession = new CallSession();
        callSession.setToUserId(1L);
        callSession.setToQueueId(queueId);
        callSession.setSessionId(EmulatorUtils.generateSessionId());
        callSession.setStatus(status);
        callSession.setTimeStamp(timestamp);

        return callSession;
    }
}
