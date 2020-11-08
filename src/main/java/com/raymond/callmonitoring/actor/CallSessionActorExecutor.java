package com.raymond.callmonitoring.actor;

import org.apache.rocketmq.common.ThreadFactoryImpl;

import java.util.concurrent.*;

public class CallSessionActorExecutor {

    private final ThreadPoolExecutor consumeExecutor;
    private final BlockingQueue<Runnable> actorQueue;

    public CallSessionActorExecutor(){
        this.actorQueue = new LinkedBlockingQueue<Runnable>();

        this.consumeExecutor = new ThreadPoolExecutor(
                5,
                5,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.actorQueue,
                new ThreadFactoryImpl("CallSessionActorExecutor_"));

    }

    public void addCallSessionActor(CallSessionActor callSessionActor){
        this.consumeExecutor.submit(callSessionActor);
    }

}
