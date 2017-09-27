package com.couchbase.mobile.app.temperature;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.collectors.Collector;
import com.couchbase.mobile.collectors.CollectorService;
import com.couchbase.mobile.database.CBLite;
import com.couchbase.mobile.fhir.FHIRResource;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TemperatureActivity extends AppCompatActivity {
    private static final String TAG = TemperatureActivity.class.getCanonicalName();

    private TemperatureView temperatureView;
    private Collector collector;
    private boolean sampling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        temperatureView = TemperatureView.getInstance(this, savedInstanceState);

        collector = CollectorService.getCollectorByType(Collector.COLLECTOR_TYPE_TEMPERATURE);
        collector.initialize(this, getIntent());
        CBLite.getInstance().startReplication();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ViewGroup parent = (ViewGroup)temperatureView.getRoot().getParent();

        if (null != parent) {
            parent.removeView(temperatureView.getRoot());
        }

        setContentView(temperatureView.getRoot());
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

    @Override
    protected void onStop() {
        super.onStop();

        ((ViewGroup)temperatureView.getRoot().getParent()).removeView(temperatureView.getRoot());
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private void startSampling() {
        sampling = true;

        final Collector.OnSampleReadyListener listener = new Collector.OnSampleReadyListener() {
            @Override
            public void sample(final Map<?, ?> sample) {
                if (null == sample) return;

                temperatureView.setTemperature((Double)sample.get("value"));

                Map<String, Object> properties = new HashMap<>();

                // Format reading as FHIR Observation
                properties.put("resourceType", "Observation");
                properties.put("issued", sample.get("issued"));
                properties.put("valueQuantity", new HashMap<String, Object>() {{
                    put("value", sample.get("value"));
                    put("unit", sample.get("unit"));
                }});
                // Todo pull reference id from login info?
                properties.put("subject", new HashMap<String, Object>() {{
                    put("reference", "patient::perrykrug");
                }});

                Document record = new Document(properties);

                try {
                    CBLite.getInstance().getDatabase().save(record);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }

                SystemClock.sleep(1000);

                if (!sampling) return;

                collector.requestSample(this);
            }
        };

        collector.requestSample(listener);
    }

    private void stopSampling() {
        sampling = false;
    }
}
