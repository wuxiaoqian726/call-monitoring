package com.raymond.callmonitoring.actor;


import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.transport.SubscriptionHolder;
import com.raymond.callmonitoring.utils.Constants;
import com.raymond.callmonitoring.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CallSessionActor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CallSessionActor.class);

    private final LinkedBlockingQueue<CallSession> callSessions;
    private final String sessionId;
    private List<String> result = new ArrayList<>();

    public CallSessionActor(CallSession initialCallSession) {
        callSessions = new LinkedBlockingQueue<CallSession>();
        this.sessionId = initialCallSession.getSessionId();
        this.addCallSession(initialCallSession);
    }

    //case1: actor 内部队列如果满了要怎么处理
    //case2: actor 内部如果某条消息失败了要怎么处理
    //case3: 自己怎么封装一个actor model
    //case4: 这样的实现方式有问题，当此actor 没有结束时会一直占用线程资源，其他的actor没法被执行
    public void run() {
        try {
            while (true) {
                CallSession callSession = callSessions.poll(100, TimeUnit.MILLISECONDS);
                if (callSession == null)
                    continue;
                this.calculate(callSession);
                SubscriptionHolder.getInstance().pushNotification(callSession, JSONUtils.toJsonString(callSession) + "\t");
                if (CallSessionStatus.finishedCall(callSession.getStatus())) {
                    this.printResult();
                    this.finish();
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addCallSession(CallSession callSession) {
        try {
            this.callSessions.put(callSession);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculate(CallSession callSession){
        result.add(JSONUtils.toJsonString(callSession));
    }

    private void printResult(){
       synchronized (Constants.globalLock){
           //logger.info("actor:{},result:{}",this.sessionId,JSONUtils.toJsonString(result));
       }
    }

    private void finish(){
        CallSessionActorManager.getInstance().removeActor(this.sessionId);
    }

    public String getCallSessionId() {
        return this.sessionId;
    }
}
