package com.couchbase.mobile.app.temperature;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class TemperatureChart extends LineChart {
    private Description description;

    public TemperatureChart(Context ctx) {
        super(ctx);
        init(ctx, null, 0);
    }

    public TemperatureChart(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx, attrs, 0);
    }

    public TemperatureChart(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx, attrs, defStyleAttr);
    }

    private void init(Context ctx, AttributeSet attrs, int defStyleAttr) {
        description = new Description();
        description.setText("Temperature (°F)");
        setDescription(description);
        description.setEnabled(true);

        setDrawGridBackground(false);
        setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        setData(data);

        // get the legend (only possible after setting data)
        Legend l = getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = getXAxis();
        //xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(120f);
        leftAxis.setAxisMinimum(20f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void addEntry(float temperature) {
        LineData data = getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), temperature), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            notifyDataSetChanged();

            // limit the number of visible entries
            setVisibleXRangeMaximum(10);
            // setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
}

