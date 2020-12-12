package com.raymond.callmonitoring.server;

public class ServerConfig {
    private boolean mockMode;

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }
}
