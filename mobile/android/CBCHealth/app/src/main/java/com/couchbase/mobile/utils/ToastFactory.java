package com.couchbase.mobile.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.mobile.R;

public class ToastFactory {
    public static final int STYLE_SUCCESS = 1;
    public static final int STYLE_FAILURE = 2;
    public static final int STYLE_INFO = 3;


    public static void makeToast(Context context, String text, int style, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 0);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.utility_toast, null);
        TextView textView = (TextView) layout.findViewById(R.id.utility_toast_text);
        textView.setText(text);

        switch (style) {
            case STYLE_FAILURE:
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.pastel_red));
                break;
            case STYLE_SUCCESS:
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.pastel_green));
                break;
            case STYLE_INFO:
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.pastel_aqua));
                break;
            default:
                textView.setBackgroundColor(ContextCompat.getColor(context, R.color.pastel_mauve));
                break;

        }

        toast.setView(layout);
        toast.show();
    }
}
