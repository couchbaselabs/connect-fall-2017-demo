package com.couchbase.mobile.collectors;

import android.content.Context;
import android.content.Intent;

import java.util.Map;

public interface Collector {
    int COLLECTOR_TYPE_TEMPERATURE = 1;
    int COLLECTOR_TYPE_WEIGHT = 2;
    int COLLECTOR_TYPE_BLOOD_PRESSURE = 3;
    int COLLECTOR_TYPE_MANUAL = 4;

    int getType();
    void initialize(Context context, Intent parameters);

    void enable(Context ctx, Intent parameters);
    void disable(Context ctx);

    void requestSample(OnSampleReadyListener listener);

    interface OnSampleReadyListener {
        void sample(Map<String, ? extends Object> sample);
    }
}
