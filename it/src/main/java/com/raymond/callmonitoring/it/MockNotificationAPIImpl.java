package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.server.service.NotificationAPI;

import java.util.concurrent.LinkedBlockingQueue;

public class MockNotificationAPIImpl implements NotificationAPI{
    private LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();

    @Override
    public boolean sendNotification(Object obj) {
        blockingQueue.add(obj);
        return true;
    }
}
