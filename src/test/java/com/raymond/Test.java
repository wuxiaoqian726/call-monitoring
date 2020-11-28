package com.raymond;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSubscription;
import com.raymond.callmonitoring.utils.Constants;
import com.raymond.callmonitoring.utils.JSONUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.ThreadFactoryImpl;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {
    private static final BlockingQueue<Runnable> actorQueue = new LinkedBlockingQueue<Runnable>();

    private static final ThreadPoolExecutor consumeExecutor  = new ThreadPoolExecutor(
                5,
                        5,
                        1000 * 60,
                TimeUnit.MILLISECONDS,
                actorQueue,
                new ThreadFactoryImpl("CallSessionActorExecutor_"));

    public static void main(String[] args) throws InterruptedException, MQClientException {
        int count = Constants.CONCURRENCY;
        while (count > 0) {
            consumeExecutor.submit(new Task(count, new LinkedBlockingQueue<>()));
            count--;
        }

//        CallSubscription callSubscription = new CallSubscription();
//        callSubscription.setUserId(1L);
//        callSubscription.setQueueIdList(Arrays.asList(1L, 2L, 3L));
//        System.out.println(JSONUtils.toJsonString(callSubscription));
    }


    private static class Task implements Runnable{

        private final int count;
        private final LinkedBlockingQueue<CallSession> callSessions;

        public Task(int count, LinkedBlockingQueue<CallSession> callSessions) {
            this.count = count;
            this.callSessions = callSessions;
        }

        @Override
        public void run() {
             while (true){
                 System.out.println("Thread:" + Thread.currentThread().getId() + " count:" + count);
                 try {
                     this.callSessions.poll(100,TimeUnit.MILLISECONDS);
                     //Thread.currentThread().sleep(100);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
        }
    }

}
