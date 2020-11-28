package com.raymond.callmonitoring.mq;

import com.raymond.callmonitoring.model.CallSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MockImMemoryQueue {

    private static MockImMemoryQueue mockImMemoryQueue = new MockImMemoryQueue();
    private final BlockingQueue<CallSession> queue = new LinkedBlockingQueue<CallSession>(2000);

    private MockImMemoryQueue(){
    }

    public static MockImMemoryQueue getInstance() {
        return mockImMemoryQueue;
    }

    public void addMessage(CallSession callSession) throws InterruptedException {
        this.queue.put(callSession);
    }

    public CallSession pullMessage() throws InterruptedException {
        return this.queue.take();
    }
}

