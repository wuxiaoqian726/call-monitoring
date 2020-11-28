package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.mq.CallProducer;
import org.apache.commons.lang3.RandomUtils;

public class CallSessionGenerationTask implements Runnable {
    private final CallProducer producer;

    public CallSessionGenerationTask(CallProducer producer) {
        this.producer = producer;
    }

    @Override
    public void run() {
        CallSessionGeneratorImpl callGenerator = new CallSessionGeneratorImpl();
        CallSession callSession = callGenerator.generateInitialCall();
        while (!CallSessionStatus.finishedCall(callSession.getStatus())) {
            try {
                this.sendCallSessionMessage(callSession);
                Thread.currentThread().join(RandomUtils.nextInt(0,5000));
            } catch (InterruptedException e) {
                //TODO:handle exception
                e.printStackTrace();
            }
            CallSession nextPhaseCall = callGenerator.generateNextPhaseCall(callSession);
            callSession = nextPhaseCall;
            this.sendCallSessionMessage(nextPhaseCall);
        }
    }

    private void sendCallSessionMessage(CallSession callSession) {
        //System.out.println(JSONUtils.toJsonString(callSession));
        producer.produce(callSession);
    }
}
