package com.raymond.callmonitoring.actor.akka;
import akka.actor.AbstractActor;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.transport.SubscriptionHolder;
import com.raymond.callmonitoring.utils.JSONUtils;

import java.io.UnsupportedEncodingException;

public class CallSessionAkkaActor extends AbstractActor {

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
            SubscriptionHolder.getInstance().pushNotification(callSession, JSONUtils.toJsonString(callSession) + "\t");
            if (CallSessionStatus.finishedCall(callSession.getStatus())) {
                //stop actor
                //System.out.println("stop actor...");
                getContext().stop(getSelf());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
