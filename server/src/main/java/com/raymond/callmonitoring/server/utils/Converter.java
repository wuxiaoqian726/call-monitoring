package com.raymond.callmonitoring.server.utils;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.server.model.SimpleCallSession;

public class Converter {

    public static SimpleCallSession convertToSimpleCallSession(CallSession callSession) {
        SimpleCallSession simpleCallSession = new SimpleCallSession();
        simpleCallSession.setSessionId(callSession.getSessionId());
        simpleCallSession.setTimestamp(callSession.getTimeStamp().getTime());
        return simpleCallSession;
    }
}
