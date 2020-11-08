package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.producer.CallProducer;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CallEmulator {

    private static final Logger logger = LoggerFactory.getLogger(CallEmulator.class);

    private final ThreadPoolExecutor producerExectuor;
    private final Executor executor = Executors.newFixedThreadPool(1);
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(2000);
    private final CallProducer producer;

    public CallEmulator(CallProducer producer) {
        this.producerExectuor = new ThreadPoolExecutor(
                Constants.CONCURRENCY,
                Constants.CONCURRENCY,
                1000 * 60,
                TimeUnit.MILLISECONDS,
                this.queue,
                new ThreadFactoryImpl("CallSessionActorExecutor_"));
        this.producer = producer;
    }

    public void start() {

        List<Runnable> runnables = new ArrayList<>();
        int count = Constants.CONCURRENCY;
        while (count > 0) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            CallSessionGenerationTask task = new CallSessionGenerationTask(producer);
                            CallEmulator.this.producerExectuor.submit(task);
                        } catch (RejectedExecutionException exception) {
                            //logger.warn("Cannot submit new Produce call session task now, sleep 5 seconds.");
                            try {
                                Thread.currentThread().join(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            count--;
        }
        for (Runnable runnable : runnables) {
            executor.execute(runnable);
        }

    }

    public void stop(){
        this.producerExectuor.shutdown();
    }

}
