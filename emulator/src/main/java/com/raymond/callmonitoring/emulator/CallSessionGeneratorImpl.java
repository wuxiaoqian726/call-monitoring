package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.model.CallSessionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CallSessionGeneratorImpl {

    private Map<CallSessionStatus,List<NextPhaseStatus>> statusRelationMap=new HashMap();
    private Map<CallSessionStatus, CallSessionGenerator> statsGeneratorMap = new HashMap();

    public CallSessionGeneratorImpl() {
        statusRelationMap.put(CallSessionStatus.Queue_Waiting, Arrays.asList(new NextPhaseStatus(90, CallSessionStatus.Agent_Waiting), new NextPhaseStatus(10, CallSessionStatus.Abandoned)));
        statusRelationMap.put(CallSessionStatus.Agent_Waiting, Arrays.asList(new NextPhaseStatus(90, CallSessionStatus.Calling), new NextPhaseStatus(20, CallSessionStatus.Abandoned)));
        statusRelationMap.put(CallSessionStatus.Calling, Arrays.asList(new NextPhaseStatus(30, CallSessionStatus.Holding), new NextPhaseStatus(90, CallSessionStatus.Ended)));
        statusRelationMap.put(CallSessionStatus.Holding, Arrays.asList(new NextPhaseStatus(90, CallSessionStatus.Calling), new NextPhaseStatus(10, CallSessionStatus.Ended)));

        statsGeneratorMap.put(CallSessionStatus.Queue_Waiting, new InitialCallGenerator());
        statsGeneratorMap.put(CallSessionStatus.Agent_Waiting, new AgentCallingCallGenerator());
        statsGeneratorMap.put(CallSessionStatus.Calling, new CallingCallGenerator());
        statsGeneratorMap.put(CallSessionStatus.Holding, new HoldingCallGenerator());
        statsGeneratorMap.put(CallSessionStatus.Ended, new EndedCallGenerator());
        statsGeneratorMap.put(CallSessionStatus.Abandoned, new AgentCallingCallGenerator());
    }

    public CallSession generateInitialCall(){
        return statsGeneratorMap.get(CallSessionStatus.Queue_Waiting).generate(null);
    }

    public CallSession generateNextPhaseCall(CallSession previousCallSession) {
        List<NextPhaseStatus> nextPhaseStatuses = this.statusRelationMap.get(previousCallSession.getStatus());
        if (CollectionUtils.isEmpty(nextPhaseStatuses))
            throw new IllegalStateException("incorrect status...");

        NextPhaseStatus nextPhaseStatus = this.randomNextPhaseStatus(nextPhaseStatuses);
        return this.statsGeneratorMap.get(nextPhaseStatus.status).generate(previousCallSession);
    }

    private NextPhaseStatus randomNextPhaseStatus(List<NextPhaseStatus> nextPhaseStatuses){
        List<Integer> list = nextPhaseStatuses.stream().map(item -> item.probability).collect(Collectors.toList());
        int randomValue = RandomUtils.nextInt(0, 100000);
        int mod = randomValue % 100;
        int sum=0;
        int index=0;

        for (int i = 0; i < list.size() ; i++) {
            sum = list.get(i) + sum;
            if (mod < sum) {
                index = i;
                break;
            }
        }
        return nextPhaseStatuses.get(index);
    }

    private class NextPhaseStatus {

        private final int probability;
        private final CallSessionStatus status;

        public NextPhaseStatus(int probability, CallSessionStatus status) {
            this.probability = probability;
            this.status=status;
        }

        public int getProbability() {
            return probability;
        }

        public CallSessionStatus getStatus() {
            return status;
        }
    }

    private class InitialCallGenerator implements CallSessionGenerator {

        private String generateSessionId(){
            return "Session-" + Thread.currentThread().getId() + "-" + RandomStringUtils.randomAlphabetic(10);
        }

        public CallSession generate(CallSession previousSession) {
            CallSession session = new CallSession();
            session.setSessionId(generateSessionId());
            session.setToUserId(RandomUtils.nextLong(1, Constants.CONCURRENCY + 1));
            session.setToQueueId(RandomUtils.nextLong(1, Constants.CONCURRENCY + 1));
            session.setStatus(CallSessionStatus.Queue_Waiting);
            session.setTimeStamp(new Date());
            return session;
        }
    }

    private class AgentCallingCallGenerator implements CallSessionGenerator {

        public CallSession generate(CallSession previousSession) {
            CallSession session = new CallSession();
            session.setSessionId(previousSession.getSessionId());
            session.setToUserId(previousSession.getToUserId());
            session.setToQueueId(previousSession.getToQueueId());
            session.setToAgentId(RandomUtils.nextLong(1, Constants.CONCURRENCY + 1));
            session.setStatus(CallSessionStatus.Agent_Waiting);
            session.setStepIndex(session.getStepIndex()+1);
            session.setTimeStamp(new Date());
            return session;
        }
    }

    private class CallingCallGenerator implements CallSessionGenerator {

        public CallSession generate(CallSession previousSession) {
            CallSession session = new CallSession();
            session.setSessionId(previousSession.getSessionId());
            session.setToUserId(previousSession.getToUserId());
            session.setToQueueId(previousSession.getToQueueId());
            session.setToAgentId(previousSession.getToAgentId());
            session.setStatus(CallSessionStatus.Calling);
            session.setStepIndex(session.getStepIndex()+1);
            session.setTimeStamp(new Date());
            return session;
        }
    }

    private class HoldingCallGenerator implements CallSessionGenerator {

        public CallSession generate(CallSession previousSession) {
            CallSession session = new CallSession();
            session.setSessionId(previousSession.getSessionId());
            session.setToUserId(previousSession.getToUserId());
            session.setToQueueId(previousSession.getToQueueId());
            session.setToAgentId(previousSession.getToAgentId());
            session.setStatus(CallSessionStatus.Holding);
            session.setStepIndex(session.getStepIndex()+1);
            session.setTimeStamp(new Date());
            return session;
        }
    }

    public static class EndedCallGenerator implements CallSessionGenerator {
        public CallSession generate(CallSession previousSession) {
            CallSession session = new CallSession();
            session.setSessionId(previousSession.getSessionId());
            session.setToUserId(previousSession.getToUserId());
            session.setToQueueId(previousSession.getToQueueId());
            session.setToAgentId(previousSession.getToAgentId());
            session.setStatus(CallSessionStatus.Ended);
            session.setStepIndex(session.getStepIndex()+1);
            session.setTimeStamp(new Date());
            return session;
        }
    }
}
