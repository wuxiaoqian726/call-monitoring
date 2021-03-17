package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.model.CallSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MockInMemoryQueue {

    private static MockInMemoryQueue mockInMemoryQueue = new MockInMemoryQueue();
    private final BlockingQueue<CallSession> queue = new LinkedBlockingQueue<CallSession>(2000);

    private MockInMemoryQueue(){
    }

    public static MockInMemoryQueue getInstance() {
        return mockInMemoryQueue;
    }

    public void addMessage(CallSession callSession) throws InterruptedException {
        this.queue.put(callSession);
    }

    public CallSession pullMessage() throws InterruptedException {
        return this.queue.poll(1000, TimeUnit.MILLISECONDS);
    }

    public int getQueueSize(){
       return this.queue.size();
    }
}

