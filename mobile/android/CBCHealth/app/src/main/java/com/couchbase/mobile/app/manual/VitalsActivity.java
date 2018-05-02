package com.couchbase.mobile.app.manual;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.couchbase.mobile.R;

public class VitalsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals);

        FloatingActionButton fab = findViewById(R.id.new_entry);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new VitalsDialogFragment();
                dialog.show(getFragmentManager(), "VitalsDialogFragment");
            }
        });
    }
}
