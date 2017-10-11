package com.couchbase.mobile.app.launch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.couchbase.mobile.app.MainActivity;
import com.couchbase.mobile.database.CBLite;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(new Runnable() {
            @Override
            public void run() {
                CBLite.getInstance().startReplication();
            }
        }).start();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent, savedInstanceState);
        finish();
    }
}
