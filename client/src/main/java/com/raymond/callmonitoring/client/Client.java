package com.raymond.callmonitoring.client;

import com.raymond.callmonitoring.common.Constants;
import org.apache.rocketmq.client.exception.MQClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client {

    private static final Executor executor = Executors.newFixedThreadPool(20);

    public static void main(String[] args) throws InterruptedException, MQClientException {
        List<ClientTask> taskList = new ArrayList<>();
        int count = 100;//Constants.CONCURRENCY;
        while (count > 0) {
            taskList.add(new ClientTask());
            count--;
        }

        for (ClientTask clientTask : taskList) {
            executor.execute(clientTask);
        }
    }

    static class ClientTask implements Runnable {

        @Override
        public void run() {
            WebsocketClient websocketClient = new WebsocketClient();
            websocketClient.createConnection();
        }
    }
}
