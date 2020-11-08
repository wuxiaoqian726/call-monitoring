package com.raymond.callmonitoring.emulator;

import com.raymond.callmonitoring.model.CallSession;

public interface CallSessionGenerator {

    //Generate next call session based on previous session.
    CallSession generate(CallSession previousSession);
}
