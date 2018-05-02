package com.couchbase.mobile.collectors.temperature;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.couchbase.mobile.collectors.Collector;

public class ManualEntry implements Collector {
    private static final String TAG = ManualEntry.class.getCanonicalName();

    private AppCompatActivity activity;
    private ManualEntryDialogFragment dialogFragment;

    @Override
    public int getType() {
        return COLLECTOR_TYPE_TEMPERATURE;
    }

    @Override
    public void initialize(Context ctx, Intent parameters) throws IllegalArgumentException {
        try {
            activity = (AppCompatActivity) ctx;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(TAG + " requires an AppCompatActivity Context");
        }

        dialogFragment = new ManualEntryDialogFragment();
    }

    @Override
    public void enable(Context ctx, Intent parameters) {
    }

    @Override
    public void disable(Context ctx) {
        dialogFragment.dismiss();
    }

    @Override
    public void requestSample(OnSampleReadyListener listener) {
        dialogFragment.setSampleReadyListener(listener);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogFragment.show(activity.getSupportFragmentManager(), "Temperature Manual Entry");
            }
        });
    }
}
