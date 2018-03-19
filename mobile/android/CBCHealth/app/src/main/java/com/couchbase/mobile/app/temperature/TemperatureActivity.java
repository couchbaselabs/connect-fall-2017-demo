package com.couchbase.mobile.app.temperature;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.util.Log;
import com.couchbase.mobile.R;
import com.couchbase.mobile.app.launch.Runtime;
import com.couchbase.mobile.collectors.Collector;
import com.couchbase.mobile.collectors.CollectorService;
import com.couchbase.mobile.custom.ArcGaugeView;
import com.couchbase.mobile.database.CBLite;
import com.couchbase.mobile.utils.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TemperatureActivity extends AppCompatActivity {
    private static final String TAG = TemperatureActivity.class.getCanonicalName();

    private ArcGaugeView agv;
    private TemperatureChart chart;

    private Collector collector;
    private boolean sampling = false;

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
        sampling = true;

        final Collector.OnSampleReadyListener listener = new Collector.OnSampleReadyListener() {
            @Override
            public void sample(final Map<?, ?> sample) {
                if (null == sample) return;

                setTemperature((Double)sample.get("value"));

                Map<String, Object> properties = new HashMap<>();

                // Format reading as FHIR Observation
                properties.put("resourceType", "Observation");
                properties.put("issued",DateUtils.toJson((Date)sample.get("issued")));
                properties.put("valueQuantity", new HashMap<String, Object>() {{
                    put("value", sample.get("value"));
                    put("unit", sample.get("unit"));
                }});
                // Todo pull reference id from login info?
                properties.put("subject", new HashMap<String, Object>() {{
                    put("reference", "urn:uuid:" + Runtime.getPatientID());
                }});

                Document record = CBLite.getInstance().getDatabase().createDocument();
                try {
                    record.putProperties(properties);
                } catch (CouchbaseLiteException e) {
                    Log.e(TAG, "Failed to call putProperties(): " + properties);
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

    private void setTemperature(double temperature) {
        agv.setText(String.format ("%.1f", temperature));
        agv.setSweep((float)temperature*355.0F/108.0F);

        chart.addEntry((float)temperature);
    }
}
