package com.raymond.callmonitoring.client;

import com.raymond.callmonitoring.common.Constants;
import com.raymond.callmonitoring.common.JSONUtils;
import com.raymond.callmonitoring.model.CallSubscription;
import org.apache.commons.lang3.RandomUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.asynchttpclient.ws.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WebsocketClient {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketClient.class);
    private static final AsyncHttpClient asyncClient = Dsl.asyncHttpClient();

    public void createConnection(String host) {
        try {
            WebSocket webSocket = asyncClient.prepareGet(host)
                    .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket websocket) {
                            CallSubscription callSubscription = createCallSubscription();
                            websocket.sendTextFrame(JSONUtils.toJsonString(callSubscription));
                        }

                        @Override
                        public void onClose(WebSocket websocket, int code, String reason) {
                        }

                        @Override
                        public void onError(Throwable t) {
                        }

                        @Override
                        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                            logger.info("client receive message:" + payload);
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

