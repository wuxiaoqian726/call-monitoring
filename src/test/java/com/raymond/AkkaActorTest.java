package com.raymond;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.raymond.callmonitoring.actor.akka.CallSessionAkkaActor;
import com.raymond.callmonitoring.emulator.CallSessionGeneratorImpl;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.exception.MQClientException;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AkkaActorTest {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        CallSessionGeneratorImpl generator = new CallSessionGeneratorImpl();
        CallSession callSession = generator.generateInitialCall();
        String actorName = callSession.getToUserId() + callSession.getSessionId();
        ActorSystem system = ActorSystem.create("callMonitoring");
        ActorRef actorRef = system.actorOf(Props.create(CallSessionAkkaActor.class), actorName);
        actorRef.path();
        actorRef.tell(callSession,null);
        ActorSelection actorSelection = system.actorSelection("user/" + actorName);

        //actorSelection.resolveOne(FiniteDuration.apply(10, TimeUnit.MILLISECONDS));
        actorSelection.tell(new CallSessionGeneratorImpl.EndedCallGenerator().generate(callSession),null);
        //actorRef.tell(new CallSessionGeneratorImpl.EndedCallGenerator().generate(callSession),null);
    }

}
