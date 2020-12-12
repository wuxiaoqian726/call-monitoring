package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;

import java.util.Date;

public class CallSessionBuilder {

    public CallSession buildCallQueueWaitingSession(Long queueId) {
        return this.buildCallQueueWaitingSessionWithTimestamp(queueId,new Date());
    }

    public CallSession buildCallQueueWaitingSessionWithTimestamp(Long queueId, Date timestamp) {
        CallSession callSession = new CallSession();
        callSession.setToQueueId(queueId);
        callSession.setSessionId(EmulatorUtils.generateSessionId());
        callSession.setStatus(CallSessionStatus.Queue_Waiting);
        callSession.setTimeStamp(timestamp);

        return callSession;
    }
}
