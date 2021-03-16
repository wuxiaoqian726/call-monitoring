package com.raymond.callmonitoring.it;

import akka.actor.Props;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.emulator.CallSessionBuilder;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.mq.CallProducer;
import com.raymond.callmonitoring.server.AkkaActorSystem;
import com.raymond.callmonitoring.server.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.server.service.ActorService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MockServer {

    private static final Logger logger = LoggerFactory.getLogger(MockServer.class);


    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallProducer callProducer = new MockCallProducer();

        List<CallSession> list = new ArrayList<>();
        list.addAll(buildCallSessions(1L));
        list.addAll(buildCallSessions(2L));
        list.addAll(buildCallSessions(3L));
        for (CallSession callSession : list) {
            callProducer.produce(callSession);
        }

        CallSubscription callSubscription = new CallSubscription();
        callSubscription.setUserId(1L);
        callSubscription.setQueueIdList(Arrays.asList(1L, 2L, 3L));

        ActorService actorService = new ActorService();
        MockNotificationAPIImpl notificationAPI = new MockNotificationAPIImpl();

        actorService.createCallSubscriptionActor(notificationAPI, callSubscription, "integrationTesting");

        CallConsumer callConsumer = new MockCallConsumer();
        callConsumer.startConsuming();

        List<Object> notifications = notificationAPI.getAllNotifications();
//        for (Object notification : notifications) {
//            logger.info(JSONUtils.toJsonString(notification));
//        }
    }

    private static List<CallSession> buildCallSessions(Long queueId){
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

}
