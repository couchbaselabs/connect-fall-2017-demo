package com.couchbase.mobile.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.couchbase.mobile.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CareMessage extends AppCompatActivity {
    SimpleDateFormat dateFormat;
    SimpleDateFormat timeFormat;
    Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Schedule a Visit");

        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        timeFormat = new SimpleDateFormat("hh:mm aaa");

        cal = Calendar.getInstance();
        //Date now = new Date();

        // date picker
        final TextView dateView = findViewById(R.id.textViewDate);
        dateView.setText(dateFormat.format(cal.getTime()));
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(
                        CareMessage.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //Calendar cal = Calendar.getInstance();
                                cal.set(year, month, dayOfMonth);
                                dateView.setText(dateFormat.format(cal.getTime()));
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show();
            }
        });

        final TextView timeView = findViewById(R.id.textViewTime);
        timeView.setText(timeFormat.format(cal.getTime()));
        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = new TimePickerDialog(
                        CareMessage.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                timeView.setText(timeFormat.format(cal.getTime()));
                            }
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );
                tpd.show();
            }
        });

        // schedule button
        Button btn = findViewById(R.id.btnSchedule);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(Locale.ENGLISH, "Thank you for setting up your appointment. We will see you on %s at %s.",
                        dateFormat.format(cal.getTime()), timeFormat.format(cal.getTime()));
                AlertDialog.Builder builder  = new AlertDialog.Builder(CareMessage.this);
                builder.setMessage(message);
                builder.setTitle("Your Appointment");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CareMessage.this.finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                //dlgAlert.setCancelable(true);
                builder.create().show();
            }
        });
    }

}
