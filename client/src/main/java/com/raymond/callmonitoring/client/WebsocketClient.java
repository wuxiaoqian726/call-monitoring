package com.raymond.callmonitoring.client;

import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.model.CallSubscription;
import org.apache.commons.lang3.RandomUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.asynchttpclient.ws.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebsocketClient {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketClient.class);
    private static final AsyncHttpClient asyncClient = Dsl.asyncHttpClient();
    private static final String[] ipList = new String[]{"192.168.77.10", "192.168.77.11", "192.168.77.12", "192.168.77.13", "192.168.77.14", "192.168.77.15", "192.168.77.16", "192.168.77.17", "192.168.77.18", "192.168.77.19", "192.168.77.20"};

    public void createConnection(String host,InetAddress localAddr) {
        try {
            BoundRequestBuilder builder = localAddr != null ? asyncClient.prepareGet(host).setLocalAddress(localAddr) : asyncClient.prepareGet(host);
            builder.execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket websocket) {
                            Monitor.incConnectedClientCount();
                            CallSubscription callSubscription = createCallSubscription();
                            websocket.sendTextFrame(JSONUtils.toJsonString(callSubscription));
                        }

                        @Override
                        public void onClose(WebSocket websocket, int code, String reason) {
                            Monitor.decConnectedClientCount();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Monitor.incErrorClientCount();
                        }

                        @Override
                        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                            Monitor.incReceivedMsgCount();
                            logger.debug("client receive message:{}", payload);
                        }

                    }).build()).get();
        } catch (Exception e) {
            logger.error("websocket exception:{}", e);
        }
    }

    private CallSubscription createCallSubscription() {
        CallSubscription callSubscription = new CallSubscription();
        callSubscription.setUserId(RandomUtils.nextLong(1, Constants.CONCURRENCY + 1));
        List<Long> queueIdList = new ArrayList<>();
        int size = RandomUtils.nextInt(1, 20);
        while (size > 0) {
            queueIdList.add(RandomUtils.nextLong(1, Constants.MAX_QUEUE_CONCURRENCY + 1));
            size--;
        }
        callSubscription.setQueueIdList(queueIdList);
        return callSubscription;
    }
}

