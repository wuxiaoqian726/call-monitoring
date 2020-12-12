package com.raymond.callmonitoring.emulator;

import org.apache.commons.lang3.RandomStringUtils;

public class EmulatorUtils {
    public static String generateSessionId() {
        return "Session-" + Thread.currentThread().getId() + "-" + RandomStringUtils.randomAlphabetic(10);
    }
}
