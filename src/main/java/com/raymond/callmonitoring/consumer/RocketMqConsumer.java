package com.raymond.callmonitoring.consumer;

import com.raymond.callmonitoring.actor.CallSessionActorManager;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.utils.Constants;
import com.raymond.callmonitoring.utils.JSONUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
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
                        //logger.info("consume message:{}", callSession);
                        //CallSessionActorManager.getInstance().sendCallSessionToActorSystem(JSONUtils.toObject(callSession, CallSession.class));
                        CallSessionActorManager.getInstance().sendCallSessionToAkkaActorSystem(JSONUtils.toObject(callSession, CallSession.class));
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }


}
