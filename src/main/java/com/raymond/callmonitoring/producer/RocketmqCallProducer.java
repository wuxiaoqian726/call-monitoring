package com.raymond.callmonitoring.producer;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.producer.CallProducer;
import com.raymond.callmonitoring.utils.Constants;
import com.raymond.callmonitoring.utils.JSONUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.Arrays;
import java.util.List;

public class RocketmqCallProducer implements CallProducer {

    private final DefaultMQProducer producer;

    public RocketmqCallProducer(DefaultMQProducer producer){
        this.producer = producer;
    }


    @Override
    public void produce(CallSession callSession) {
        Message message = new Message();
        message.setTopic(Constants.TOPIC_NAME);
        message.setKeys(Arrays.asList(callSession.getToUserId().toString(), callSession.getSessionId()));
        message.setBody(JSONUtils.toJsonByte(callSession));
        try {
            producer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    CallSession session = (CallSession) arg;
                    int index = (int) (session.getToUserId() % mqs.size());
                    return mqs.get(index);
                }
            }, callSession);
            //TODO:handle message send failure exception
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
