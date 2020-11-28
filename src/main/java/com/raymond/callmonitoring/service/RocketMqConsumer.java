package com.raymond.callmonitoring.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.raymond.callmonitoring.AkkaActorSystem;
import com.raymond.callmonitoring.actor.CallSessionActor;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.utils.Constants;
import com.raymond.callmonitoring.utils.JSONUtils;
import com.raymond.callmonitoring.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

public class RocketMqConsumer implements CallConsumer {
    private final DefaultMQPushConsumer consumer;
    private static final Logger logger = LoggerFactory.getLogger(RocketMqConsumer.class);

    public RocketMqConsumer(DefaultMQPushConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void startConsuming() {
        try {
            consumer.subscribe(Constants.TOPIC_NAME, "*");
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    for (MessageExt msg : msgs) {
                        String callSession = new String(msg.getBody(), Charset.forName("UTF-8"));
                        logger.info("consume message:{}", callSession);
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
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }




}
