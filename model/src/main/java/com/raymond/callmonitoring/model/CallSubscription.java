package com.raymond.callmonitoring.model;

import io.netty.channel.Channel;

import java.util.List;

public class CallSubscription {
    private Long userId;
    private List<Long> queueIdList;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Long> getQueueIdList() {
        return queueIdList;
    }

    public void setQueueIdList(List<Long> queueIdList) {
        this.queueIdList = queueIdList;
    }
}
