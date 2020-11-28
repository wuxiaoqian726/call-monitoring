package com.raymond.callmonitoring.actor;
import akka.actor.AbstractActor;
import com.raymond.callmonitoring.actor.eventbus.CallSubscriptionEventBus;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import com.raymond.callmonitoring.model.PullQueueStat;
import com.raymond.callmonitoring.utils.AkkaActorMonitoring;
import com.raymond.callmonitoring.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallSessionActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(CallSessionActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CallSession.class, callSession -> {
                    this.handle(callSession);
                }).build();
    }

    private void handle(CallSession callSession){
        if (callSession == null)
            return;
        try {
            this.logLatencyIfNeeded(callSession);
            if (callSession.getStatus() == CallSessionStatus.Queue_Waiting) {
                CallStatsHolder.addQueueWaitingCall(callSession);
            }
            if (callSession.getStatus() == CallSessionStatus.Agent_Waiting) {
                CallStatsHolder.removeQueueWaitingCall(callSession);
            }

            CallSubscriptionEventBus.getInstance().publish(new PullQueueStat(callSession.getToQueueId()));

            if (CallSessionStatus.finishedCall(callSession.getStatus())) {
                getContext().stop(getSelf());
            }
        } catch (Exception e) {
            logger.error("error:{}", e);
            e.printStackTrace();
        }
    }

    private void logLatencyIfNeeded(CallSession callSession) {
        if (Utils.diffTimestamp(callSession.getTimeStamp()) > AkkaActorMonitoring.CONSUMING_LATENCY_THRESHOLD_MILLISECONDS) {
            AkkaActorMonitoring.addConsumingLatencyCount();
            logger.warn("call session latency warning,userId:{},sessionId:{},status:{},time:{}",callSession.getToUserId(), callSession.getSessionId(), callSession.getStatus(), callSession.getTimeStamp());
        }
    }


}
