package com.raymond.callmonitoring.utils;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.SimpleCallSession;

public class Converter {

    public static SimpleCallSession convertToSimpleCallSession(CallSession callSession) {
        SimpleCallSession simpleCallSession = new SimpleCallSession();
        simpleCallSession.setSessionId(callSession.getSessionId());
        simpleCallSession.setTimestamp(callSession.getTimeStamp().getTime());
        return simpleCallSession;
    }
}
