package com.raymond.callmonitoring.model;

public enum CallSessionStatus {
    Queue_Waiting,
    Agent_Waiting,
    Calling,
    Holding,
    Abandoned,
    Ended;

    public static boolean isFinishedCall(CallSessionStatus status) {
        return status == Ended || status == Abandoned;
    }

}
