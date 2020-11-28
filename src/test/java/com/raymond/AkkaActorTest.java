package com.raymond;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.raymond.callmonitoring.actor.CallSessionActor;
import com.raymond.callmonitoring.emulator.CallSessionGeneratorImpl;
import com.raymond.callmonitoring.model.CallSession;
import org.apache.rocketmq.client.exception.MQClientException;

public class AkkaActorTest {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        CallSessionGeneratorImpl generator = new CallSessionGeneratorImpl();
        CallSession callSession = generator.generateInitialCall();
        String actorName = callSession.getToUserId() + callSession.getSessionId();
        ActorSystem system = ActorSystem.create("callMonitoring");
        ActorRef actorRef = system.actorOf(Props.create(CallSessionActor.class), actorName);
        actorRef.path();
        actorRef.tell(callSession,null);
        ActorSelection actorSelection = system.actorSelection("user/" + actorName);

        //actorSelection.resolveOne(FiniteDuration.apply(10, TimeUnit.MILLISECONDS));
        actorSelection.tell(new CallSessionGeneratorImpl.EndedCallGenerator().generate(callSession),null);
        //actorRef.tell(new CallSessionGeneratorImpl.EndedCallGenerator().generate(callSession),null);
    }

}
