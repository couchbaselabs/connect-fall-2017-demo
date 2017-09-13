package com.couchbase.mobile.hardware;

import android.content.Context;

import com.couchbase.mobile.hardware.nfc.iso15693.NfcVHardwareManager;

public class HardwareManagerFactory {
    public static HardwareManager getHardwareManager(Context ctx, int type) {
        switch(type) {
            case HardwareManager.HARDWARE_TYPE_NFCV:
                return new NfcVHardwareManager(ctx);
            default:
                break;
        }

        return null;
    }
}
