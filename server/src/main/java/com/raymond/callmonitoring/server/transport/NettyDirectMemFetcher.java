package com.raymond.callmonitoring.server.transport;

import com.raymond.callmonitoring.common.ReflectionUtils;
import io.netty.util.internal.PlatformDependent;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

public class NettyDirectMemFetcher {
    private AtomicLong directMem = new AtomicLong();

    public NettyDirectMemFetcher() {
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

}