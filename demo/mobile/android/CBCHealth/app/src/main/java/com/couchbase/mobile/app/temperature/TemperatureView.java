package com.couchbase.mobile.app.temperature;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.couchbase.mobile.R;
import com.couchbase.mobile.custom.ArcGaugeView;

import java.lang.ref.WeakReference;

public class TemperatureView {
    private static WeakReference<TemperatureView> temperatureView = null;

    private View root;
    private ArcGaugeView agv;
    private TemperatureChart chart;

    TemperatureView(AppCompatActivity ctx, Bundle savedInstanceState) {
        root = ctx.getLayoutInflater().inflate(R.layout.view_temperature, null, false);
        agv = root.findViewById(R.id.agv);
        chart = root.findViewById(R.id.chart);
    }

    public static TemperatureView getInstance(AppCompatActivity ctx, Bundle savedInstanceState) {
        if (null == temperatureView || null == temperatureView.get()) {
            temperatureView = new WeakReference<>(new TemperatureView(ctx, savedInstanceState));
        }

        return temperatureView.get();
    }

    public View getRoot() {
        return root;
    }

    public void setTemperature(double temperature) {
        agv.setText(String.format ("%.1f", temperature));
        agv.setSweep((float)temperature*355.0F/108.0F);

        chart.addEntry((float)temperature);
    }
}
