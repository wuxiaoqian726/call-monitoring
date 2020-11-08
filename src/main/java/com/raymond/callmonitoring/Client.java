package com.raymond.callmonitoring;

import com.raymond.callmonitoring.transport.NotificationClient;
import com.raymond.callmonitoring.utils.Constants;
import org.apache.rocketmq.client.exception.MQClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client {

    private static final Executor executor = Executors.newFixedThreadPool(Constants.CONCURRENCY);

    public static void main(String[] args) throws InterruptedException, MQClientException {
        List<ClientTask> taskList = new ArrayList<>();
        int count = Constants.CONCURRENCY;
        while (count > 0) {
            taskList.add(new ClientTask(String.valueOf(count)));
            count--;
        }

        for (ClientTask clientTask : taskList) {
            executor.execute(clientTask);
        }
    }

    static class ClientTask implements Runnable {
        private final String userId;

        public ClientTask(String userId) {
            this.userId = userId;
        }

        @Override
        public void run() {
            NotificationClient client = new NotificationClient(userId);
            client.start();
        }
    }
}
