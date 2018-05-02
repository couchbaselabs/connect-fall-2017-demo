package com.couchbase.mobile.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.couchbase.mobile.R;
import com.couchbase.mobile.app.manual.VitalsActivity;
import com.couchbase.mobile.app.patient.PatientActivity;
import com.couchbase.mobile.app.temperature.TemperatureActivity;
import com.jasonette.seed.Core.JasonViewActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onTemperatureButtonClick(View v) {
        Intent intent = new Intent(this, TemperatureActivity.class);
        startActivity(intent);
    }

    public void onVitalsButtonClick(View v) {
        Intent intent = new Intent(this, VitalsActivity.class);
        startActivity(intent);
    }

    public void onScaleButtonClick(View v) {
        Intent intent = new Intent(this, JasonViewActivity.class);
        intent.putExtra("url", "db://hello.json");
        startActivity(intent);
    }

    public void onAppointmentButtonClick(View v) {
        Intent intent = new Intent(this, CareMessage.class);
        startActivity(intent);
    }
    public void onPatientButtonClick(View v) {
        Intent intent = new Intent(this, PatientActivity.class);
        startActivity(intent);
    }
}
