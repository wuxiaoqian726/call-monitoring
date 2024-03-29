package com.raymond.callmonitoring.it;

import akka.actor.Props;
import com.raymond.callmonitoring.emulator.CallSessionBuilder;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.mq.CallProducer;
import com.raymond.callmonitoring.server.AkkaActorSystem;
import com.raymond.callmonitoring.server.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.server.model.CallQueueStats;
import com.raymond.callmonitoring.server.service.ActorService;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    @org.junit.Test
    public void testInitialSubscriptionData() throws InterruptedException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallProducer callProducer = new MockCallProducer();

        List<CallSession> list = new ArrayList<>();
        list.addAll(buildQueue1CallSessions(1L));
        list.addAll(buildQueue2CallSessions(2L));
        list.addAll(buildQueue3CallSessions(3L));
        for (CallSession callSession : list) {
            callProducer.produce(callSession);
        }

        CallConsumer callConsumer = new MockCallConsumer();
        callConsumer.startConsuming();


        CallSubscription callSubscription = new CallSubscription();
        callSubscription.setUserId(1L);
        callSubscription.setQueueIdList(Arrays.asList(1L, 2L, 3L));

        ActorService actorService = new ActorService();
        int expectedNotificationCount = 3;
        CountDownLatch countDownLatch = new CountDownLatch(expectedNotificationCount);
        LinkedBlockingQueue<Object> notifications = new LinkedBlockingQueue<>();
        MockNotificationAPIImpl mockNotificationAPI = new MockNotificationAPIImpl();
        mockNotificationAPI.setCountDownLatch(countDownLatch);
        mockNotificationAPI.setNotifications(notifications);
        actorService.createCallSubscriptionActor(mockNotificationAPI, callSubscription, "integrationTesting");
        countDownLatch.await();

        CallQueueStats queue1Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue1Stats.getQueueId().longValue(), 1);
        Assert.assertEquals(queue1Stats.getWaitingCount(), 5);
        Assert.assertTrue(queue1Stats.getLongestWaitingSeconds() >= 10000 && queue1Stats.getLongestWaitingSeconds() < 11000);

        CallQueueStats queue2Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue2Stats.getQueueId().longValue(), 2);
        Assert.assertEquals(queue2Stats.getWaitingCount(), 3);
        Assert.assertTrue(queue2Stats.getLongestWaitingSeconds() >= 6000 && queue2Stats.getLongestWaitingSeconds() < 7000);

        CallQueueStats queue3Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue3Stats.getQueueId().longValue(), 3);
        Assert.assertEquals(queue3Stats.getWaitingCount(), 2);
        Assert.assertTrue(queue3Stats.getLongestWaitingSeconds() >= 5000 && queue3Stats.getLongestWaitingSeconds() < 6000);

        for (CallSession callSession : buildQueue2CallSessions(2L)) {
            callProducer.produce(callSession);
        }


    }

    @org.junit.Test
    public void testSubscriptionDataWhenNewCallComing() throws InterruptedException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallProducer callProducer = new MockCallProducer();

        List<CallSession> list = new ArrayList<>();
        list.addAll(buildQueue1CallSessions(1L));
        for (CallSession callSession : list) {
            callProducer.produce(callSession);
        }

        CallConsumer callConsumer = new MockCallConsumer();
        callConsumer.startConsuming();


        CallSubscription callSubscription = new CallSubscription();
        callSubscription.setUserId(1L);
        callSubscription.setQueueIdList(Arrays.asList(1L, 2L, 3L));

        ActorService actorService = new ActorService();
        int expectedNotificationCount = 3;
        CountDownLatch countDownLatch = new CountDownLatch(expectedNotificationCount);
        LinkedBlockingQueue<Object> notifications = new LinkedBlockingQueue<>();
        MockNotificationAPIImpl mockNotificationAPI = new MockNotificationAPIImpl();
        mockNotificationAPI.setCountDownLatch(countDownLatch);
        mockNotificationAPI.setNotifications(notifications);

        actorService.createCallSubscriptionActor(mockNotificationAPI, callSubscription, "integrationTesting");
        countDownLatch.await();

        logger.info("assert initial notifications.");
        CallQueueStats queue1Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue1Stats.getQueueId().longValue(), 1);
        Assert.assertEquals(queue1Stats.getWaitingCount(), 5);
        Assert.assertTrue(queue1Stats.getLongestWaitingSeconds() >= 10000 && queue1Stats.getLongestWaitingSeconds() < 11000);

        CallQueueStats queue2Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue2Stats.getQueueId().longValue(), 2);
        Assert.assertEquals(queue2Stats.getWaitingCount(), 0);
        Assert.assertTrue(queue2Stats.getLongestWaitingSeconds() == 0);

        logger.info("assert initial notifications successfully.");


        countDownLatch = new CountDownLatch(1);
        notifications = new LinkedBlockingQueue<>();

        mockNotificationAPI.setCountDownLatch(countDownLatch);
        mockNotificationAPI.setNotifications(notifications);

        callProducer.produce(buildQueue2CallSessions(2L).get(0));

        callConsumer.startConsuming();
        countDownLatch.await();

        logger.info("assert new coming call notifications.");

        queue2Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue2Stats.getQueueId().longValue(), 2);
        Assert.assertEquals(queue2Stats.getWaitingCount(), 1);
        Assert.assertTrue(queue2Stats.getLongestWaitingSeconds() >= 6000 && queue2Stats.getLongestWaitingSeconds() < 7000);


        countDownLatch = new CountDownLatch(1);
        notifications = new LinkedBlockingQueue<>();

        mockNotificationAPI.setCountDownLatch(countDownLatch);
        mockNotificationAPI.setNotifications(notifications);

        callProducer.produce(buildQueue2CallSessions(2L).get(1));
        callConsumer.startConsuming();
        countDownLatch.await();

        queue2Stats = (CallQueueStats) notifications.poll();
        Assert.assertEquals(queue2Stats.getQueueId().longValue(), 2);
        Assert.assertEquals(queue2Stats.getWaitingCount(), 2);
        Assert.assertTrue(queue2Stats.getLongestWaitingSeconds() >= 6000 && queue2Stats.getLongestWaitingSeconds() < 7000);


    }

    private static List<CallSession> buildQueue1CallSessions(Long queueId) {
        CallSessionBuilder builder = new CallSessionBuilder();
        Date now = new Date();
        Date fiveSecondsAgo = new Date(now.getTime() - (1000 * 5));
        Date sixSecondsAgo = new Date(now.getTime() - (1000 * 6));
        Date eightSecondsAgo = new Date(now.getTime() - (1000 * 8));
        Date tenSecondsAgo = new Date(now.getTime() - (1000 * 10));
        CallSession callSessionWithFiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, fiveSecondsAgo);
        CallSession callSessionWithSixSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, sixSecondsAgo);
        CallSession callSessionWithEightSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, eightSecondsAgo);
        CallSession callSessionWithTenSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, tenSecondsAgo);
        CallSession callSessionWithNow = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, now);

        List<CallSession> list = new ArrayList<>();
        list.add(callSessionWithTenSecondsAgo);
        list.add(callSessionWithEightSecondsAgo);
        list.add(callSessionWithSixSecondsAgo);
        list.add(callSessionWithFiveSecondsAgo);
        list.add(callSessionWithNow);

        return list;
    }

    private static List<CallSession> buildQueue2CallSessions(Long queueId) {
        CallSessionBuilder builder = new CallSessionBuilder();
        Date now = new Date();
        Date fiveSecondsAgo = new Date(now.getTime() - (1000 * 5));
        Date sixSecondsAgo = new Date(now.getTime() - (1000 * 6));
        CallSession callSessionWithFiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, fiveSecondsAgo);
        CallSession callSessionWithSixSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, sixSecondsAgo);
        CallSession callSessionWithNow = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, now);

        List<CallSession> list = new ArrayList<>();
        list.add(callSessionWithSixSecondsAgo);
        list.add(callSessionWithFiveSecondsAgo);
        list.add(callSessionWithNow);

        return list;
    }

    private static List<CallSession> buildQueue3CallSessions(Long queueId) {
        CallSessionBuilder builder = new CallSessionBuilder();
        Date now = new Date();
        Date fiveSecondsAgo = new Date(now.getTime() - (1000 * 5));
        CallSession callSessionWithFiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, fiveSecondsAgo);
        CallSession callSessionWithNow = builder.buildCallQueueWaitingSessionWithTimestamp(queueId, now);

        List<CallSession> list = new ArrayList<>();
        list.add(callSessionWithFiveSecondsAgo);
        list.add(callSessionWithNow);

        return list;
    }

}
