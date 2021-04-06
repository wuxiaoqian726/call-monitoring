package com.raymond.callmonitoring.client;

import com.raymond.callmonitoring.common.JSONUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BenchmarkClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkClient.class);
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(50);
    private static String DEFAULT_HOST = "ws://47.118.50.228:8080/websocket";

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        int count = args.length > 0 ? NumberUtils.toInt(args[0]) : 2;
        String host = args.length > 1 ? args[1] : DEFAULT_HOST;
        List<InetAddress> inetAddressList = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            byte[] ipBuf = new byte[4];
            for(int j = 0; j < 4; j++){
                String[] ipStr = args[i].split("\\.");
                ipBuf[j] = (byte) (Integer.parseInt(ipStr[j]) & 0xff);
            }
            inetAddressList.add(InetAddress.getByAddress(ipBuf));
        }
        if(!inetAddressList.isEmpty()){
            LOGGER.info("all interfaces:{}", JSONUtils.toJsonString(inetAddressList));
        }
        LOGGER.info("start {} client for host {}.", count, host);
        int interfaceCount = inetAddressList.size();
        while (count > 0) {
            count--;
            InetAddress inetAddress = interfaceCount == 0 ? null : inetAddressList.get(count % interfaceCount);
            EXECUTOR.execute(new BenchmarkClientTask(host,inetAddress));
        }

        Monitor.startReport();
    }

    static class BenchmarkClientTask implements Runnable {
        private final String host;
        private InetAddress inetAddress;

        BenchmarkClientTask(String host, InetAddress inetAddress) {
            this.host = host;
            this.inetAddress = inetAddress;
        }


        @Override
        public void run() {
            WebsocketClient websocketClient = new WebsocketClient();
            websocketClient.createConnection(host, inetAddress);
        }
    }
}
