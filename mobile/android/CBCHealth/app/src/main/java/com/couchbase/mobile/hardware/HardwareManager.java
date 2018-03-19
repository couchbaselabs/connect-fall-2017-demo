package com.couchbase.mobile.hardware;

import android.content.Context;
import android.content.Intent;

public interface HardwareManager {
    int HARDWARE_TYPE_NONE = 0;
    int HARDWARE_TYPE_BLE = 1;
    int HARDWARE_TYPE_NFCA = 2;
    int HARDWARE_TYPE_NFCB = 3;
    int HARDWARE_TYPE_NFCF = 4;
    int HARDWARE_TYPE_NFCV = 5;
    int HARDWARE_TYPE_NFCISODEP = 6;
    int HARDWARE_TYPE_NDEF = 7;
    int HARDWARE_TYPE_NDEFFORMATABLE = 8;

    void enable(Context ctx, Intent parameters);
    void disable(Context ctx);
}
