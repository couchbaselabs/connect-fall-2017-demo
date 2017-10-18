package com.couchbase.mobile.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.couchbase.mobile.R;
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onTemperatureButtonClick(View v) {
        Intent intent = new Intent(this, TemperatureActivity.class);
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
