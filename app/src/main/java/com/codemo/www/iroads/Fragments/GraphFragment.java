package com.codemo.www.iroads.Fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Random;

import com.codemo.www.iroads.Database.DatabaseHandler;
import com.codemo.www.iroads.IRICalculator;
import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.MobileSensors;
import com.codemo.www.iroads.R;
import com.codemo.www.iroads.SensorDataProcessor;

/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment extends Fragment {

    private static final String TAG = "GraphFragment";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean enableFilter;
    private AppCompatCheckBox xValue, xValueFiltered, yValue, yValueFiltered, zValue, zValueAverageFiltered,
            zValueHighPassFiltered, zValueReoriented, yValueReoriented, xValueReoriented;
    private static boolean xValueChecked, xValueFilteredChecked, yValueChecked, yValueFilteredChecked, zValueChecked, zValueAverageFilteredChecked,
            zValueHighPassFilteredChecked, xValueReorientedChecked, yValueReorientedChecked, zValueReorientedChecked;
    private static LineChart mChart;
    private static LineChart rmsChart;
    private static LineChart iriChart;
    private static LineChart fuelChart;
    private Thread thread;
    private Thread fakethread;
    private static boolean plotData = false;
    private static int sleepTime = 100;
    private static int maxEntries = 200;
    private static MainActivity activity;
    private static IRICalculator calc;
    private static Runnable handlerTask;
    private static boolean started = false;

    public GraphFragment() {
        // Required empty public constructor
    }

    public static void setRmsChart(LineChart rmsChart) {
        GraphFragment.rmsChart = rmsChart;
    }
    public static void setIRIChart(LineChart iriChart) {
        GraphFragment.iriChart = iriChart;
    }

    public static void setFuelChart(LineChart fuelChart) {
        GraphFragment.fuelChart = fuelChart;
//        startTimer();
    }
    public static void startTimer(){
        Handler handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int min = 20;
                int max = 30;
                Random r1 = new Random();
                int i1 = r1.nextInt(max - min + 1) + min;
                addEntry((float)i1,"fuel", Color.BLUE, fuelChart);
                handler.postDelayed(handlerTask, 1000);
            }
        };
        handlerTask.run();
    }
    public static void setActivity(MainActivity Activity) {
        activity = Activity;
    }

    public static boolean isStarted() {
        return started;
    }

    public static void setStarted(boolean started) {
        GraphFragment.started = started;
    }

    public static int getSleepTime() {
        return sleepTime;
    }

    public static void setSleepTime(int sleepTime) {
        GraphFragment.sleepTime = sleepTime;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
//        View viewHome = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d(TAG, "Init sensor services");
        sensorManager = (SensorManager)  getActivity().getSystemService(Context.SENSOR_SERVICE);


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        if(accelerometer != null){
//            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            xValue = (AppCompatCheckBox) view.findViewById(R.id.xValue);
            xValueFiltered = (AppCompatCheckBox) view.findViewById(R.id.xValueFiltered);
            xValueReoriented = (AppCompatCheckBox) view.findViewById(R.id.xValueReorientedBox);
            // filtering process for sensor values
            yValue = (AppCompatCheckBox) view.findViewById(R.id.yValue);
            yValueFiltered = (AppCompatCheckBox) view.findViewById(R.id.yValueFiltered);
            yValueReoriented = (AppCompatCheckBox) view.findViewById(R.id.yValueReorientedBox);
            zValue = (AppCompatCheckBox) view.findViewById(R.id.zValue);
            zValueAverageFiltered = (AppCompatCheckBox) view.findViewById(R.id.zValueAverageFiltered);
            zValueHighPassFiltered = (AppCompatCheckBox) view.findViewById(R.id.zValueHighPassFiltered);
            zValueReoriented = (AppCompatCheckBox) view.findViewById(R.id.zValueReorientedBox);
        }else{
            Log.d(TAG, "Accelorometer not available");
        }

        //      initializing listners for each acceleration output
        xValue.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if(xValue.isChecked()){
                    xValueChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorX)));
                    }
//                    xValue.setHighlightColor(getResources().getColor(R.color.colorPrimary));
//                    xValue.setSupportButtonTintList(colorStateList);
                }else{
                    xValueChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("x", mChart);
                }
            }
        });
        yValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(yValue.isChecked()){
                    yValueChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorY)));
                    }
                }else{
                    yValueChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("y", mChart);
                }
            }
        });
        zValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(zValue.isChecked()){
                    zValueChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZ)));
                    }
                }else{
                    zValueChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValue.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("z", mChart);
                }
            }
        });
        xValueFiltered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(xValueFiltered.isChecked()){
                    xValueFilteredChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValueFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorXAvg)));
                    }
                }else{
                    xValueFilteredChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValueFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("x avg", mChart);
                }
            }
        });
        yValueFiltered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(yValueFiltered.isChecked()){
                    yValueFilteredChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValueFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorYAvg)));
                    }
                }else{
                    yValueFilteredChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValueFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("y avg", mChart);
                }
            }
        });
        zValueAverageFiltered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(zValueAverageFiltered.isChecked()){
                    zValueAverageFilteredChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueAverageFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZAvg)));
                    }
                }else{
                    zValueAverageFilteredChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueAverageFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("z avg", mChart);
                }
            }
        });
        xValueReoriented.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(xValueReoriented.isChecked()){
                    xValueReorientedChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorXReori)));
                    }
                }else{
                    xValueReorientedChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        xValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("x reori", mChart);
                }
            }
        });
        yValueReoriented.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(yValueReoriented.isChecked()){
                    yValueReorientedChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorYReori)));
                    }
                }else{
                    yValueReorientedChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        yValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("y reori", mChart);
                }
            }
        });
        zValueReoriented.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(zValueReoriented.isChecked()){
                    zValueReorientedChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZReori)));
                    }
                }else{
                    zValueReorientedChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueReoriented.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("z reori", mChart);
                }
            }
        });
        zValueHighPassFiltered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(zValueHighPassFiltered.isChecked()){
                    zValueHighPassFilteredChecked = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueHighPassFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZHighPass)));
                    }
                }else{
                    zValueHighPassFilteredChecked = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        zValueHighPassFiltered.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorGreyLight)));
                    }
                    deleteSet("z high pass", mChart);
                }
            }
        });

        mChart = (LineChart) view.findViewById(R.id.chartAcceleration);
        mChart.getDescription().setEnabled(false);
//        mChart.getDescription().setText("Accelerometer Z axis");

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.TRANSPARENT);

        YAxis lAxis = mChart.getAxisLeft();

        YAxis rAxis = mChart.getAxisRight();
        rAxis.setEnabled(false);

        XAxis xAxis  = mChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawLabels(false);
        xAxis.setEnabled(true);

        LineData data = new LineData();
//        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setEnabled(false);
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTextColor(Color.LTGRAY);

//        setRmsChart((LineChart) viewHome.findViewById(R.id.chartAccelerationZ));
//        LineData rmsData = new LineData();
//        mChart.setData(rmsData);

        startPlot();

        return view;
    }

    private void startPlot() {
        if(thread != null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG,"--------------- thrad started --------- /// ");
                while(true){
                    plotData=true;
//                    Log.d(TAG,"--------------- inside while loop--------- /// ");
                    try {
                        Thread.sleep(getSleepTime());

                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    public static void drawGraph(SensorEvent sensorEvent) {
            if(plotData){
                if(isStarted()){
                    DatabaseHandler.saveToDatabase();
//                    Log.d(TAG, "saving....");
                }

                /**
                 * raw x y z values
                 */

                if(xValueChecked){
                    addEntry(MobileSensors.getCurrentAccelerationX(),
//                            sensorEvent.values[0],
                            "x", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorX), mChart);
                }
                if(yValueChecked){
                    addEntry(MobileSensors.getCurrentAccelerationY(),
//                            sensorEvent.values[1],
                            "y", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorY), mChart);
                }
                if(zValueChecked){
                    addEntry(MobileSensors.getCurrentAccelerationZ(),
//                            sensorEvent.values[2],
                            "z", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZ), mChart);
                }


                /**
                 * filter added x y z
                  */

                //avg filter
                if(xValueFilteredChecked){
                    addEntry((float) SensorDataProcessor.getAvgFilteredAx(),
                            "x avg", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorXAvg), mChart);
                }

                if(yValueFilteredChecked){
                    addEntry((float) SensorDataProcessor.getAvgFilteredAy(),
                            "y avg", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorYAvg), mChart);
                }

                if(zValueAverageFilteredChecked){
                    addEntry((float) SensorDataProcessor.getAvgFilteredAz(),
                            "z avg", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZAvg), mChart);
                }

                //high pass filter
                if(zValueHighPassFilteredChecked){
                    addEntry((float)SensorDataProcessor.getHighPassFilteredAz(),
                            "z high pass", ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZHighPass), mChart);
                }



                /**
                 * reoriented x y z
                 */

                if(xValueReorientedChecked){
                    addEntry((float) SensorDataProcessor.getReorientedAx(),
                            "x reori",
                            ContextCompat.getColor(activity.getApplicationContext(), R.color.colorXReori), mChart);
                }
                if(yValueReorientedChecked){
                    addEntry((float)SensorDataProcessor.getReorientedAy(),
                            "y reori",
                            ContextCompat.getColor(activity.getApplicationContext(), R.color.colorYReori), mChart);
                }
                if(zValueReorientedChecked){
                    addEntry((float)SensorDataProcessor.getReorientedAz(),
                            "z reori",
                            ContextCompat.getColor(activity.getApplicationContext(), R.color.colorZReori), mChart);
                }


                /**
                 * RMS
                 */
                addEntry((float) SensorDataProcessor.getRms(),
                        "rms", Color.RED, rmsChart);

                /**
                 * IRI
                 */
                addEntry((float)SensorDataProcessor.getIri(),
                        "iri", Color.BLACK, iriChart);


//                Log.d(TAG,"--------------- IRI is  --------- /// "+
//                calc.processIRI_usingSlope(zValueSignalProcessor.averageFilter(sensorEvent.values[2]));
//                addEntry((float)5.0,"iri", Color.RED, iriChart);
                plotData = false;
//            }
        }
    }

    public boolean isEnableFilter() {
        return enableFilter;
    }

    public void setEnableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
    }

    private static void addEntry(float value, String type, int color, LineChart chart) {
        if(Float.isNaN(value)){
            return;
        }
        LineData data = chart.getLineData();
        if(data != null){
            ILineDataSet set = data.getDataSetByLabel(type, true);
            if(set == null){
                set = createSet(type, color);
                data.addDataSet(set);
            }
//            Log.d(TAG,"--------------- data set is  --------- /// "+set.getLabel());
//            (float) Math.random()*75+75f
            data.addEntry(new Entry(set.getEntryCount(), value),data.getIndexOfDataSet(set));
//            Log.d(TAG,"--------------- z data --------- /// "+value+"...."+set.getEntryCount());
            if(set.getEntryCount() > maxEntries){
                set.removeFirst();
                for (int i=0; i<set.getEntryCount(); i++) {
                    Entry entryToChange = set.getEntryForIndex(i);
                    entryToChange.setX(entryToChange.getX() - 1);
                }
            }
//            Log.d(TAG,"--------------- entry count is --------- /// "+set.getEntryCount());
            chart.notifyDataSetChanged();
//            mChart.setMaxVisibleValueCount(150);
//            mChart.setFocusable(true);
//            mChart.setVisibleXRangeMaximum(100);
            chart.setVisibleXRange(200f,200f);
            chart.moveViewToX(data.getEntryCount());
//            mChart.invalidate();
        }
    }

    private static LineDataSet createSet(String type, int color) {
        LineDataSet set = new LineDataSet(null, type);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setLineWidth(1f);
        set.setColor(color);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        for(int i=0; i<maxEntries; i++){
            set.addEntry(new Entry(i,0));
        }
        Log.d(TAG,"---------------  data set created --------- /// ");
        return  set;
    }

    public void deleteSet(String type, LineChart chart){
        Log.d(TAG,"---------------  data set deleted --------- /// ");
        LineData data = chart.getLineData();
//        ILineDataSet set = data.getDataSetByLabel(type, true);
        data.removeDataSet(data.getDataSetByLabel(type, true));
//        set.clear();
        chart.invalidate();
    }

}
