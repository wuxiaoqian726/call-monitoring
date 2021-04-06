package com.raymond.callmonitoring.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);

    public static List<InetAddress> getAllInterfaces()  {

        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
            return Collections.list(nets).stream().filter(item -> item.getDisplayName().contains("eth0")).map(item -> item.getInetAddresses().nextElement()).collect(Collectors.toList());
        } catch (SocketException e) {
            LOGGER.error("error happens when try to get all network interface", e);
        }
        return Collections.emptyList();
    }
}  