package com.raymond.callmonitoring.server;

import com.raymond.callmonitoring.emulator.CallSessionBuilder;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.server.actor.CallStatsHolder;
import com.raymond.callmonitoring.server.model.CallQueueStats;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

public class CallStatsHolderTest {

    @Test
    public void testForSingleThread() {
        CallSessionBuilder builder = new CallSessionBuilder();

        Date now = new Date();
        Date tenSecondsAgo = new Date(now.getTime() - (1000 * 10));
        CallSession callSessionWithTenSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(1L, tenSecondsAgo);
        CallStatsHolder.addQueueWaitingCall(callSessionWithTenSecondsAgo);
        CallStatsHolder.addQueueWaitingCall(builder.buildCallQueueWaitingSessionWithTimestamp(1L, now));
        CallStatsHolder.addQueueWaitingCall(builder.buildCallQueueWaitingSessionWithTimestamp(2L, now));
        CallStatsHolder.addQueueWaitingCall(builder.buildCallQueueWaitingSessionWithTimestamp(3L, new Date()));

        CallQueueStats callQueueStats1 = CallStatsHolder.getQueueStats(1L);
        Assert.assertEquals(callQueueStats1.getWaitingCount(), 2);
        Assert.assertEquals(callQueueStats1.getLongestWaitingSeconds(), new Date().getTime() - tenSecondsAgo.getTime());

        CallQueueStats callQueueStats2 = CallStatsHolder.getQueueStats(2L);
        Assert.assertEquals(callQueueStats2.getWaitingCount(), 1);
        Assert.assertEquals(callQueueStats2.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());


        CallQueueStats callQueueStats3 = CallStatsHolder.getQueueStats(3L);
        Assert.assertEquals(callQueueStats3.getWaitingCount(), 1);

        CallStatsHolder.removeQueueWaitingCall(callSessionWithTenSecondsAgo);
        callQueueStats1 = CallStatsHolder.getQueueStats(1L);
        Assert.assertEquals(callQueueStats1.getWaitingCount(), 1);
        Assert.assertEquals(callQueueStats1.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());

    }

    @Test
    public void testForMultipleThread() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CallSessionBuilder builder = new CallSessionBuilder();
        Date now = new Date();
        Date fiveSecondsAgo = new Date(now.getTime() - (1000 * 5));
        Date sixSecondsAgo = new Date(now.getTime() - (1000 * 6));
        Date eightSecondsAgo = new Date(now.getTime() - (1000 * 8));
        Date tenSecondsAgo = new Date(now.getTime() - (1000 * 10));
        CallSession callSessionWithFiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(1L, fiveSecondsAgo);
        CallSession callSessionWithSixSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(1L, sixSecondsAgo);
        CallSession callSessionWithEightSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(1L, eightSecondsAgo);
        CallSession callSessionWithTenSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(1L, tenSecondsAgo);
        CallSession callSessionWithNow = builder.buildCallQueueWaitingSessionWithTimestamp(1L, now);

        CountDownLatch counter = new CountDownLatch(10);
        List<AddCallSessionTask> tasks = new ArrayList<>();
        tasks.add(new AddCallSessionTask(counter, callSessionWithFiveSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, callSessionWithSixSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, callSessionWithEightSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, callSessionWithTenSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, callSessionWithNow));
        CallSession callSessionQueue2FiveSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(2L, fiveSecondsAgo);
        tasks.add(new AddCallSessionTask(counter, callSessionQueue2FiveSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, builder.buildCallQueueWaitingSessionWithTimestamp(2L, now)));
        tasks.add(new AddCallSessionTask(counter, builder.buildCallQueueWaitingSessionWithTimestamp(3L, now)));
        tasks.add(new AddCallSessionTask(counter, builder.buildCallQueueWaitingSessionWithTimestamp(4L, now)));
        CallSession callSessionQueue4SixSecondsAgo = builder.buildCallQueueWaitingSessionWithTimestamp(4L, sixSecondsAgo);
        tasks.add(new AddCallSessionTask(counter, callSessionQueue4SixSecondsAgo));
        tasks.add(new AddCallSessionTask(counter, builder.buildCallQueueWaitingSessionWithTimestamp(4L, new Date())));


        executorService.invokeAll(tasks);
        counter.await();

        CallQueueStats callQueueStats1 = CallStatsHolder.getQueueStats(1L);
        Assert.assertEquals(callQueueStats1.getWaitingCount(), 5);
        Assert.assertEquals(callQueueStats1.getLongestWaitingSeconds(), new Date().getTime() - tenSecondsAgo.getTime());

        CallQueueStats callQueueStats2 = CallStatsHolder.getQueueStats(2L);
        Assert.assertEquals(callQueueStats2.getWaitingCount(), 2);
        Assert.assertEquals(callQueueStats2.getLongestWaitingSeconds(), new Date().getTime() - fiveSecondsAgo.getTime());


        CallQueueStats callQueueStats3 = CallStatsHolder.getQueueStats(3L);
        Assert.assertEquals(callQueueStats3.getWaitingCount(), 1);
        Assert.assertEquals(callQueueStats3.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());

        CallQueueStats callQueueStats4 = CallStatsHolder.getQueueStats(4L);
        Assert.assertEquals(callQueueStats4.getWaitingCount(), 3);
        Assert.assertEquals(callQueueStats4.getLongestWaitingSeconds(), new Date().getTime() - sixSecondsAgo.getTime());

        CountDownLatch countDownLatch = new CountDownLatch(5);

        List<RemoveCallSessionTask> removalTasks = new ArrayList<>();
        removalTasks.add(new RemoveCallSessionTask(countDownLatch, callSessionWithTenSecondsAgo));
        removalTasks.add(new RemoveCallSessionTask(countDownLatch, callSessionWithEightSecondsAgo));
        removalTasks.add(new RemoveCallSessionTask(countDownLatch, callSessionWithSixSecondsAgo));
        removalTasks.add(new RemoveCallSessionTask(countDownLatch, callSessionQueue2FiveSecondsAgo));
        removalTasks.add(new RemoveCallSessionTask(countDownLatch, callSessionQueue4SixSecondsAgo));
        executorService.invokeAll(removalTasks);
        countDownLatch.await();

        callQueueStats1 = CallStatsHolder.getQueueStats(1L);
        Assert.assertEquals(callQueueStats1.getWaitingCount(), 2);
        Assert.assertEquals(callQueueStats1.getLongestWaitingSeconds(), new Date().getTime() - fiveSecondsAgo.getTime());

        callQueueStats2 = CallStatsHolder.getQueueStats(2L);
        Assert.assertEquals(callQueueStats2.getWaitingCount(), 1);
        Assert.assertEquals(callQueueStats2.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());


        callQueueStats3 = CallStatsHolder.getQueueStats(3L);
        Assert.assertEquals(callQueueStats3.getWaitingCount(), 1);
        Assert.assertEquals(callQueueStats3.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());

        callQueueStats4 = CallStatsHolder.getQueueStats(4L);
        Assert.assertEquals(callQueueStats4.getWaitingCount(), 2);
        Assert.assertEquals(callQueueStats4.getLongestWaitingSeconds(), new Date().getTime() - now.getTime());

    }

    private class AddCallSessionTask implements Callable<Object> {
        private CountDownLatch counter;
        private final CallSession callSession;

        public AddCallSessionTask(CountDownLatch counter, CallSession callSession) {
            this.counter = counter;
            this.callSession = callSession;
        }

        @Override
        public Object call() throws Exception {
            CallStatsHolder.addQueueWaitingCall(callSession);
            counter.countDown();
            return null;
        }
    }

    private class RemoveCallSessionTask implements Callable<Object> {
        private CountDownLatch counter;
        private final CallSession callSession;

        public RemoveCallSessionTask(CountDownLatch counter, CallSession callSession) {
            this.counter = counter;
            this.callSession = callSession;
        }

        @Override
        public Object call() throws Exception {
            CallStatsHolder.removeQueueWaitingCall(callSession);
            counter.countDown();
            return null;
        }
    }

}
