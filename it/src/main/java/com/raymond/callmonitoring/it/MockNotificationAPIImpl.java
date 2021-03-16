package com.raymond.callmonitoring.it;

import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.server.service.NotificationAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MockNotificationAPIImpl implements NotificationAPI{
    private static final Logger logger = LoggerFactory.getLogger(MockNotificationAPIImpl.class);
    private LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();

    @Override
    public boolean sendNotification(Object obj) {
        logger.info(JSONUtils.toJsonString(obj));
        blockingQueue.add(obj);
        return true;
    }

    public List<Object> getAllNotifications() {
        return Arrays.asList(blockingQueue.toArray());
    }
}
