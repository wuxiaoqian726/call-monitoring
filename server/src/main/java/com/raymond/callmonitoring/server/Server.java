package com.raymond.callmonitoring.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.common.Utils;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.mq.CallConsumer;
import com.raymond.callmonitoring.mq.MockCallConsumer;
import com.raymond.callmonitoring.mq.RocketMqConsumer;
import com.raymond.callmonitoring.server.actor.CallSessionActor;
import com.raymond.callmonitoring.server.actor.CallSubscriptionRouter;
import com.raymond.callmonitoring.server.transport.WebSocketNotificationServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.Charset;
import java.util.List;

public class Server {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        AkkaActorSystem akkaActorSystem = AkkaActorSystem.getInstance();
        akkaActorSystem.getActorSystem().actorOf(Props.create(CallSubscriptionRouter.class), CallSubscriptionRouter.ACTOR_NAME);

        CallConsumer callConsumer = getCallConsumer(false);
        callConsumer.startConsuming();

        WebSocketNotificationServer webSocketNotificationServer = new WebSocketNotificationServer();
        webSocketNotificationServer.start();
    }

    private static CallConsumer getCallConsumer(boolean mockMode) throws MQClientException {
        if (mockMode)
            return new MockCallConsumer();
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Constants.CONSUMER_GROUP_NAME);
        consumer.setNamesrvAddr("localhost:9876");

        return new RocketMqConsumer(consumer, new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                for (MessageExt msg : msgs) {
                    String callSession = new String(msg.getBody(), Charset.forName("UTF-8"));
                    //logger.info("consume message:{}", callSession);
                    this.sendCallSessionActor(JSONUtils.toObject(callSession, CallSession.class));
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }

            public void sendCallSessionActor(CallSession callSession) {
                if (callSession.getToUserId() == null || StringUtils.isEmpty(callSession.getSessionId())) {
                    return;
                }
                ActorSystem actorSystem = AkkaActorSystem.getInstance().getActorSystem();
                if (callSession.getStatus() == CallSessionStatus.Queue_Waiting) {
                    ActorRef actorRef = actorSystem.actorOf(Props.create(CallSessionActor.class), Utils.getCallSessionActorName(callSession));
                    actorRef.tell(callSession, ActorRef.noSender());
                } else {
                    actorSystem.actorSelection(Utils.getCallSessionActorPath(callSession)).tell(callSession, ActorRef.noSender());
                }
            }
        });
    }

}
