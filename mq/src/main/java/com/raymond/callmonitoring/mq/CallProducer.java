package com.raymond.callmonitoring.mq;

import com.raymond.callmonitoring.model.CallSession;

public interface CallProducer {
    void produce(CallSession callSession);
}
