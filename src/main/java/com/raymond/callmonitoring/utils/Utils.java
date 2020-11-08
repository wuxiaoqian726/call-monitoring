package com.raymond.callmonitoring.utils;

import com.raymond.callmonitoring.model.CallSession;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Utils {

    public static long diffTimestamp(Date time){
        if(time==null){
            return new Date().getTime();
        }
        return new Date().getTime() - time.getTime();
    }

    public static String getActorName(CallSession callSession){
        if (callSession.getToUserId() == null || StringUtils.isEmpty(callSession.getSessionId())) {
            throw new IllegalStateException("Both UserId and session id cannot be null");
        }
        return callSession.getToUserId() + "-" + callSession.getSessionId();
    }

    public static String getActorPath(CallSession callSession){
        return Constants.ACTOR_PATH_PREFIX + getActorName(callSession);
    }
}
