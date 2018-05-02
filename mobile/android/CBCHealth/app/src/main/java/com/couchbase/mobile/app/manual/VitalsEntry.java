package com.couchbase.mobile.app.manual;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

public class VitalsEntry implements AdapterView.OnItemSelectedListener,
        View.OnFocusChangeListener {
    private int selection = 0;
    private String text = null;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selection = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Leave as last selection
    }

    public int getSelection() {
        return selection;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) return;

        text = ((EditText) v).getText().toString();
    }

    public String getText() {
        return text;
    }
}
