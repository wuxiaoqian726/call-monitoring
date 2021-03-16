package emulator;


import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.emulator.CallEmulator;
import com.raymond.callmonitoring.model.CallSession;
import com.raymond.callmonitoring.mq.CallProducer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CallEmulatorTest {

    public static void main(String[] args) throws InterruptedException {
        Map<String, List<CallSession>> map = new ConcurrentHashMap<>();
        CallEmulator callEmulator = new CallEmulator(new CallProducer() {
            @Override
            public void produce(CallSession callSession) {
                List<CallSession> callSessions = map.putIfAbsent(callSession.getSessionId(), Stream.of(callSession)
                        .collect(Collectors.toList()));
                if (callSessions != null) {
                    callSessions.add(callSession);
                }

            }
        }, 10);
        callEmulator.start();
        Thread.currentThread().join(100000);
        callEmulator.stop();
        map.forEach((key,value)->{
            for (CallSession callSession : value) {
                System.out.println(JSONUtils.toJsonString(callSession));
            }
        });
    }

    private void assertCallSession(List<CallSession> callSessions){
        callSessions.forEach(item->{
           //TODO: do assertion
        });
    }
}
