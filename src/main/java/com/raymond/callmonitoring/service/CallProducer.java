package com.raymond.callmonitoring.service;

import com.raymond.callmonitoring.model.CallSession;

public interface CallProducer {
    void produce(CallSession callSession);
}
