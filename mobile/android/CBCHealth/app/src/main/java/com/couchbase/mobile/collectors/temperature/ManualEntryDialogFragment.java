package com.couchbase.mobile.collectors.temperature;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.couchbase.mobile.R;
import com.couchbase.mobile.collectors.Collector;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManualEntryDialogFragment extends DialogFragment implements Runnable {
    private Collector.OnSampleReadyListener sampleReadyListener;
    private Map<String, Object> sample = new HashMap<>();
    private EditText text;

    HandlerThread backgroundThread;
    Handler backgroundHandler;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_temperature, null);

        backgroundThread = new HandlerThread("ManualEntryBackgroundThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        text = view.findViewById(R.id.dialog_temperature_value);

        builder.setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        this.dismiss();
        backgroundHandler.post(this);
    }

    @Override
    public void run() {
        String text = ManualEntryDialogFragment.this.text.getText().toString();

        if (text.isEmpty()) {
            sampleReadyListener.sample(null);

            return;
        }

        sample.put("value", Double.parseDouble(text));
        sample.put("unit", "degrees F");
        sample.put("issued", new Date());

        sampleReadyListener.sample(sample);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.text.setText("");
    }

    public void setSampleReadyListener(Collector.OnSampleReadyListener listener) {
        sampleReadyListener = listener;
    }
}
