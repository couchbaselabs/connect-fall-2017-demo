package com.couchbase.mobile.app.manual;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.couchbase.mobile.R;
import com.couchbase.mobile.fhir.Temperature;

import java.util.Date;

public class VitalsDialogFragment extends DialogFragment {
    private VitalsEntry entry = new VitalsEntry();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_vitals, null);
        Spinner spinner = view.findViewById(R.id.vitals_spinner);

        spinner.setOnItemSelectedListener(entry);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.vital_signs, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        EditText text = view.findViewById(R.id.vitals_value);

        text.setOnFocusChangeListener(entry);

        builder.setView(view)
                .setTitle(R.string.title_activity_vitals)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch(entry.getSelection()) {
                            case 0:
                                new Temperature(entry.getText(), "degrees F", new Date());
                                break;

                            case 1:
                                break;

                            case 2:
                                break;

                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }
}
