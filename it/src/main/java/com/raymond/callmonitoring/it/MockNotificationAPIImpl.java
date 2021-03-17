package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.server.service.NotificationAPI;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class MockNotificationAPIImpl implements NotificationAPI {
    private CountDownLatch countDownLatch;
    private LinkedBlockingQueue<Object> notifications;

    @Override
    public boolean sendNotification(Object obj) {
        countDownLatch.countDown();
        notifications.add(obj);
        return true;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public LinkedBlockingQueue<Object> getNotifications() {
        return notifications;
    }

    public void setNotifications(LinkedBlockingQueue<Object> notifications) {
        this.notifications = notifications;
    }
}
