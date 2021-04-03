package com.raymond.callmonitoring.server.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.raymond.callmonitoring.common.Utils;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.server.AkkaActorSystem;
import com.raymond.callmonitoring.server.Monitor;
import com.raymond.callmonitoring.server.actor.CallSessionActor;
import com.raymond.callmonitoring.server.actor.CallSubscriptionActor;
import org.apache.commons.lang3.StringUtils;

public class ActorService {

    public ActorRef createCallSubscriptionActor(NotificationAPI notificationAPI, CallSubscription subscription, String uniqueId) {
        ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
        return actorSystem.actorOf(Props.create(CallSubscriptionActor.class, notificationAPI, subscription), uniqueId);
    }

    public void sendCallSessionToActor(CallSession callSession) {
        if (callSession.getToUserId() == null || StringUtils.isEmpty(callSession.getSessionId())) {
            return;
        }
        ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
        if (callSession.getStatus() == CallSessionStatus.Queue_Waiting) {
            ActorRef actorRef = actorSystem.actorOf(Props.create(CallSessionActor.class), Utils.getCallSessionActorName(callSession));
            actorRef.tell(callSession, ActorRef.noSender());
        } else {
            actorSystem.actorSelection(Utils.getCallSessionActorPath(callSession)).tell(callSession, ActorRef.noSender());
        }
    }
}
