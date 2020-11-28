package com.raymond.callmonitoring.server.actor;

import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.server.model.CallQueueStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CallStatsHolder {
    private static final Logger logger = LoggerFactory.getLogger(CallStatsHolder.class);
    private static Map<Long,ConcurrentHashMap<String,Long>> map =new ConcurrentHashMap<>();

    public static void addQueueWaitingCall(CallSession callSession) {
        if (!map.containsKey(callSession.getToQueueId())) {
            //TODO: double check thread safe.
            ConcurrentHashMap<String, Long> queueMap = new ConcurrentHashMap<>();
            queueMap.put(callSession.getSessionId(), callSession.getTimeStamp().getTime());
            ConcurrentHashMap<String, Long> existingMap = map.putIfAbsent(callSession.getToQueueId(), queueMap);
            if (existingMap != null) {
                existingMap.put(callSession.getSessionId(), callSession.getTimeStamp().getTime());
            }
        } else {
            map.get(callSession.getToQueueId()).put(callSession.getSessionId(), callSession.getTimeStamp().getTime());
        }
    }

    public static void removeQueueWaitingCall(CallSession callSession){
        if(!map.containsKey(callSession.getToQueueId())){
            logger.warn("extensionId:{} does not exist in holder", callSession.getToQueueId());
            return;
        }
        map.get(callSession.getToQueueId()).remove(callSession.getSessionId());
    }

    public static CallQueueStats getQueueStats(Long queueId) {
        CallQueueStats callQueueStats = new CallQueueStats();
        callQueueStats.setQueueId(queueId);
        if (!map.containsKey(queueId)) {
            return callQueueStats;
        }
        Map queueStatsMap = map.get(queueId);
        callQueueStats.setWaitingCount(queueStatsMap.size());
        if (queueStatsMap.isEmpty()) {
            callQueueStats.setLongestWaitingSeconds(0);
            return callQueueStats;
        } else {
            Optional<Long> optional = queueStatsMap.values().stream().sorted().findFirst();
            if (optional.isPresent()) {
                int diff = (int) (new Date().getTime() - optional.get().longValue());
                callQueueStats.setLongestWaitingSeconds(diff);
            }
        }
        return callQueueStats;
    }

}
