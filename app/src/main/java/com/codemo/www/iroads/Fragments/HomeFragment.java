package com.codemo.www.iroads.Fragments;


import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.GraphController;
import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.MobileSensors;
import com.codemo.www.iroads.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static MainActivity mainActivity;
    private static boolean obdDataAvailable = false;
    private static Location cuurentLoc;
    private static TextView obd2speed;
    private static ProgressBar speedProgressBar;
    private LineChart mChart;
    private LineChart iriChart;
    private Handler handler;
    private Runnable handlerTask;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

    public static void updateLocation(Location loc) {
        cuurentLoc = loc;
        Log.d(TAG, "--------------- Location changed --------- /// " + loc.getLatitude() + " " + loc.getLongitude());
        if (!obdDataAvailable) {
            Double speed = SpeedCalculator.getSpeed(loc.getLatitude(), loc.getLongitude());
            MobileSensors.setGpsSpeed(speed);// updates vehicle speed using GPS
            updateSpeed(speed.intValue());
        }
        SensorData.setMlat(Double.toString(loc.getLatitude()));
        SensorData.setMlon(Double.toString(loc.getLongitude()));
    }

    public static void updateSpeed(int speed) {
        obd2speed.setText(speed + " km/h");
        ObjectAnimator animSpeed = ObjectAnimator.ofInt(speedProgressBar, "progress", speedProgressBar.getProgress(), speed * 10000);
        animSpeed.setDuration(900);
        animSpeed.setInterpolator(new DecelerateInterpolator());
        animSpeed.start();
    }

    public static void updateOBD2Data(int speed, int rpm) {
        obdDataAvailable = true;
        updateSpeed(speed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        obd2speed = (TextView) view.findViewById(R.id.obd2speed);

        mChart = (LineChart) view.findViewById(R.id.chartAccelerationZ);
        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.TRANSPARENT);

        YAxis lAxis = mChart.getAxisLeft();

        YAxis rAxis = mChart.getAxisRight();
        rAxis.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        LineData data = new LineData();
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setEnabled(false);

        GraphController.setRmsChart(mChart);

        iriChart = (LineChart) view.findViewById(R.id.chartIRI);
        iriChart.getDescription().setEnabled(false);

        iriChart.setTouchEnabled(true);
        iriChart.setDragEnabled(true);
        iriChart.setScaleEnabled(false);
        iriChart.setDrawGridBackground(false);
        iriChart.setPinchZoom(false);
        iriChart.setBackgroundColor(Color.TRANSPARENT);

        YAxis lAxisIRI = iriChart.getAxisLeft();

        YAxis rAxisIRI = iriChart.getAxisRight();
        rAxisIRI.setEnabled(false);

        XAxis xAxisIRI = iriChart.getXAxis();
        xAxisIRI.setEnabled(true);
        xAxisIRI.setDrawLabels(false);
        xAxisIRI.setDrawAxisLine(false);
        xAxisIRI.setDrawGridLines(false);

        LineData dataIRI = new LineData();
        iriChart.setData(dataIRI);

        Legend lIRI = iriChart.getLegend();
        lIRI.setEnabled(false);

        GraphController.setIRIChart(iriChart);

        speedProgressBar = (ProgressBar) view.findViewById(R.id.speed_progress_bar);

        startTimer();
        return view;

    }

    public void startTimer() {
        handler = new Handler();
        handlerTask = new Runnable() {
            @Override
            public void run() {
                int min = 20;
                int max = 120;
                Random r1 = new Random();
                int i1 = r1.nextInt(max - min + 1) + min;
                Random r2 = new Random();
                int i2 = r2.nextInt(max - min + 1) + min;
                handler.postDelayed(handlerTask, 1000);
            }
        };
        handlerTask.run();
    }


}
