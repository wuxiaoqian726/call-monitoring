package com.raymond.callmonitoring.actor;

import akka.actor.*;
import com.raymond.callmonitoring.AkkaActorSystem;
import com.raymond.callmonitoring.actor.akka.CallSessionAkkaActor;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.model.MockImMemoryQueue;
import com.raymond.callmonitoring.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import scala.collection.Iterable;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CallSessionActorManager {
    private final ConcurrentHashMap<String, CallSessionActor> map;
    private final CallSessionActorExecutor executor;

    private static CallSessionActorManager callSessionActorManager = new CallSessionActorManager();

    private CallSessionActorManager() {
        map = new ConcurrentHashMap<String, CallSessionActor>();
        executor = new CallSessionActorExecutor();
    }

    public static CallSessionActorManager getInstance() {
        return callSessionActorManager;
    }

    public void sendCallSessionToActorSystem(CallSession callSession) {
        CallSessionActor callSessionActor = map.get(callSession.getSessionId());
        if (callSessionActor == null) {
            callSessionActor = new CallSessionActor(callSession);
            map.putIfAbsent(callSession.getSessionId(), callSessionActor);
            executor.addCallSessionActor(callSessionActor);
        } else {
            callSessionActor.addCallSession(callSession);
        }
    }

    public void sendCallSessionToAkkaActorSystem(CallSession callSession) {
        if (callSession.getToUserId() == null || StringUtils.isEmpty(callSession.getSessionId())) {
            return;
        }
        ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
        if (callSession.getStatus() == CallSessionStatus.Queue_Waiting) {
            ActorRef actorRef = actorSystem.actorOf(Props.create(CallSessionAkkaActor.class), Utils.getActorName(callSession));
            actorRef.tell(callSession, ActorRef.noSender());
        } else {
            actorSystem.actorSelection(Utils.getActorPath(callSession)).tell(callSession, ActorRef.noSender());
        }
    }

    public void removeActor(String sessionId) {
        this.map.remove(sessionId);
    }


}
