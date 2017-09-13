package com.couchbase.mobile.utils;

import java.util.Formatter;

public class Conversion {
    private static final String TAG = Conversion.class.getCanonicalName();

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return "0x" + sb.toString().toUpperCase();
    }
}
