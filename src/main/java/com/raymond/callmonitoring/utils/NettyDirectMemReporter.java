package com.raymond.callmonitoring.utils;

import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NettyDirectMemReporter {
    private static final Logger logger = LoggerFactory.getLogger(NettyDirectMemReporter.class);

    private AtomicLong directMem = new AtomicLong();
    private ScheduledExecutorService executor = MoreExecutors.getExitingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(1), 10, TimeUnit.SECONDS);

    public NettyDirectMemReporter() {
        Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER",AtomicLong.class);
        field.setAccessible(true);
        try {
            directMem = (AtomicLong) field.get(PlatformDependent.class);
        } catch (IllegalAccessException e) {
        }
    }

    public int getDirectMem(){
        return directMem.intValue();
    }


    public void startReport() {
        executor.scheduleAtFixedRate(() -> {
            logger.info("netty direct memory size:{}b, max:{}", directMem.get(), PlatformDependent.maxDirectMemory());
        }, 0, 1, TimeUnit.SECONDS);
    }
}