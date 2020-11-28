package com.raymond.callmonitoring;

import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkkaActorSystem {

    private static final Logger logger = LoggerFactory.getLogger(AkkaActorSystem.class);
    private static AkkaActorSystem akkaActorSystem = new AkkaActorSystem();
    private final ActorSystem system;

    private AkkaActorSystem() {
        this.system = ActorSystem.create("callMonitoring");
    }

    public static AkkaActorSystem getInstance() {
        return akkaActorSystem;
    }

    public ActorSystem getActorSystem() {
        return this.system;
    }

}
