package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;

public class Caller implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Caller.class);

    private boolean initial;
    private DelayQueue<DelayCallSession> delayQueue;
    private CallSession previousCallSession;
    private CallSessionGeneratorImpl callGenerator = new CallSessionGeneratorImpl();

    public Caller(DelayQueue<DelayCallSession> delayQueue, CallSession previousCallSession) {
        this.initial = false;
        this.delayQueue = delayQueue;
        this.previousCallSession = previousCallSession;
    }

    public Caller(boolean initial,DelayQueue<DelayCallSession> delayQueue) {
        this.initial = initial;
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        if (initial) {
            CallSession callSession = callGenerator.generateInitialCall();
            DelayCallSession delayCallSession = new DelayCallSession(callSession, 0);
            delayQueue.add(delayCallSession);
            return;
        }
        if (CallSessionStatus.isFinishedCall(this.previousCallSession.getStatus())) {
            return;
        }
        CallSession callSession = this.changeToNextStatus();
        long delay = RandomUtils.nextInt(0, 5000);
        //logger.info("session:{},status:{},delay:{}", callSession.getSessionId(), callSession.getStatus(), delay);
        DelayCallSession delayCallSession = new DelayCallSession(callSession, delay);
        delayQueue.add(delayCallSession);
    }

    private CallSession changeToNextStatus(){
        return callGenerator.generateNextPhaseCall(this.previousCallSession);
    }
}
