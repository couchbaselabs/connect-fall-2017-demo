package com.couchbase.mobile.app.common;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.couchbase.mobile.app.common.Item;
import com.couchbase.mobile.R;

import java.util.List;

public class ItemsAdapter extends ArrayAdapter<Item> {
    public ItemsAdapter(@NonNull Context context, List<Item> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        Item item = getItem(position);
        TextView tvKey = convertView.findViewById(R.id.tvLabel);
        TextView tvValue = convertView.findViewById(R.id.tvValue);
        tvKey.setText(item.getLabel());
        tvValue.setText(item.getValue());
        return convertView;
    }
}
