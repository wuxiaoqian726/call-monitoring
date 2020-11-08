package com.raymond.callmonitoring.producer;

import com.raymond.callmonitoring.model.CallSession;

public interface CallProducer {
    void produce(CallSession callSession);
}
