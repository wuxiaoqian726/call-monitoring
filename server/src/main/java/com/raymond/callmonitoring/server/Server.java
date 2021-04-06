package com.raymond.callmonitoring.server;

import akka.actor.Props;

import com.google.common.base.Charsets;
import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.common.Utils;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.mq.RocketMqConsumer;
import com.raymond.callmonitoring.server.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.server.service.ActorService;
import com.raymond.callmonitoring.server.transport.WebSocketNotificationServer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);


    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);
        boolean rocketMQMode = args.length > 0 ? BooleanUtils.toBoolean(args[0]) : false;
        CallConsumer callConsumer = rocketMQMode ? getRocketMqCallConsumer() : getMockConsumer();
        callConsumer.startConsuming();

        Monitor.startReport();

        WebSocketNotificationServer webSocketNotificationServer = new WebSocketNotificationServer();
        webSocketNotificationServer.start();

    }

    private static CallConsumer getMockConsumer(){
        CallConsumer callConsumer = new CallConsumer() {
            @Override
            public void startConsuming() {

            }
        };
        return callConsumer;
    }

    private static CallConsumer getRocketMqCallConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");

        return new RocketMqConsumer(consumer, new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {
                    Monitor.incConsumedMsgCount();
                    CallSession callSession = JSONUtils.toObject(new String(msg.getBody(), Charsets.UTF_8),CallSession.class);
                    if (Utils.diffTimestamp(callSession.getTimeStamp()) > Monitor.CONSUMING_LATENCY_THRESHOLD_MILLISECONDS) {
                        Monitor.incConsumedMsgDelayCount();
                        logger.warn("consuming latency warning,userId:{},sessionId:{},status:{},time:{}",callSession.getToUserId(), callSession.getSessionId(), callSession.getStatus(), callSession.getTimeStamp());
                    }
                    ActorService actorService = new ActorService();
                    actorService.sendCallSessionToActor(callSession);
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
    }

}
