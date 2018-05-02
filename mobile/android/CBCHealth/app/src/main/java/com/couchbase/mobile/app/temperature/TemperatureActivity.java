package com.couchbase.mobile.app.temperature;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.util.Log;
import com.couchbase.mobile.R;
import com.couchbase.mobile.collectors.Collector;
import com.couchbase.mobile.collectors.CollectorService;
import com.couchbase.mobile.custom.ArcGaugeView;
import com.couchbase.mobile.database.CBLite;
import com.couchbase.mobile.fhir.Temperature;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TemperatureActivity extends AppCompatActivity implements Collector.OnSampleReadyListener {
    private static final String TAG = TemperatureActivity.class.getCanonicalName();

    private ArcGaugeView agv;
    private TemperatureChart chart;

    private Collector collector;
    private AtomicBoolean sampling = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_temperature);
        agv = findViewById(R.id.agv);
        chart = findViewById(R.id.chart);

        collector = CollectorService.getCollectorByType(Collector.COLLECTOR_TYPE_TEMPERATURE);
        collector.initialize(this, getIntent());
    }

    @Override
    public void onResume() {
        super.onResume();

        collector.enable(this, getIntent());
        startSampling();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopSampling();
        collector.disable(this);
    }

    /**
     * Obtaining information from intents
     * https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#obtain-info
     */

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private void startSampling() {
        sampling = new AtomicBoolean(true);

        collector.requestSample(this);
    }

    private void stopSampling() {
        sampling = new AtomicBoolean(false);
    }

    @Override
    public void sample(final Map<String, ? extends Object> sample) {
        if (null == sample) return;

        setTemperature((Double) sample.get("value"));

        Temperature fhir = new Temperature(sample.get("value"), sample.get("unit"), (Date) sample.get("issued"));

        Document record = CBLite.getInstance().getDatabase().createDocument();

        try {
            record.putProperties(fhir);
        } catch (CouchbaseLiteException ex) {
            Log.e(TAG, "Failed to call putProperties(): " + fhir);
        }

        if (!sampling.get()) return;

        SystemClock.sleep(1000);

        collector.requestSample(this);
    }

    private void setTemperature(final double temperature) {
        agv.setText(String.format("%.1f", temperature));
        agv.setSweep((float) temperature * 355.0F / 108.0F);

        chart.addEntry((float) temperature);
    }
}
