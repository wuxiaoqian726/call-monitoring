package com.raymond.callmonitoring.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(50);
    private static String DEFAULT_HOST = "ws://47.118.50.228:8080/websocket"; //"ws://127.0.0.1:8080/websocket";

    public static void main(String[] args) throws InterruptedException, MQClientException {
        List<ClientTask> taskList = new ArrayList<>();
        int count = args.length > 0 ? NumberUtils.toInt(args[0]) : 2;
        String host = args.length > 1 ? args[1] : DEFAULT_HOST;
        LOGGER.info("start {} client for host {}.", count, host);
        while (count > 0) {
            taskList.add(new ClientTask(host));
            count--;
        }

        Monitor.startReport();

        for (ClientTask clientTask : taskList) {
            EXECUTOR.execute(clientTask);
        }
    }

    static class ClientTask implements Runnable {
        private final String host;

        ClientTask(String host) {
            this.host = host;
        }


        @Override
        public void run() {
            WebsocketClient websocketClient = new WebsocketClient();
            websocketClient.createConnection(host,null);
        }
    }
}
