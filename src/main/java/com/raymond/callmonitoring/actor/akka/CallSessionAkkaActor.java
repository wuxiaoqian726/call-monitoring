package com.raymond.callmonitoring.actor.akka;
import akka.actor.AbstractActor;
import com.raymond.callmonitoring.actor.CallSessionActor;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.transport.SubscriptionHolder;
import com.raymond.callmonitoring.utils.JSONUtils;
import com.raymond.callmonitoring.utils.Utils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class CallSessionAkkaActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(CallSessionActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CallSession.class, callSession -> {
                    this.handle(callSession);
                }).build();
    }

    private void handle(CallSession callSession){
        if (callSession == null)
            return;
        try {
            this.logLatencyIfNeeded(callSession);
            SubscriptionHolder.getInstance().pushNotification(callSession, JSONUtils.toJsonString(callSession) + "\t");
            if (CallSessionStatus.finishedCall(callSession.getStatus())) {
                getContext().stop(getSelf());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void logLatencyIfNeeded(CallSession callSession){
         if(Utils.diffTimestamp(callSession.getTimeStamp())>1000){
             logger.warn("call session latency warning,userId:{},sessionId:{},status:{},time:{}",
                     callSession.getToUserId(), callSession.getSessionId(), callSession.getStatus(), callSession.getTimeStamp());
         }
    }


    //    @Override
//    public void preStart() throws Exception {
//        System.out.println("pre start");
//        super.preStart();
//    }
//
//    @Override
//    public void postStop() throws Exception {
//        System.out.println("post stop");
//        super.postStop();
//    }

}
