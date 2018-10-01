package com.codemo.www.iroads.Fragments;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.codemo.www.iroads.Database.DatabaseHandler;
import com.codemo.www.iroads.Database.SensorData;
import com.codemo.www.iroads.GraphController;
import com.codemo.www.iroads.MainActivity;
import com.codemo.www.iroads.MobileSensors;
import com.codemo.www.iroads.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{

    private DatabaseHandler dbHandler;

    private static final String TAG = "HomeFragment";

    private static MainActivity mainActivity;
    private static boolean obdDataAvailable = false;
    private boolean enableFilter;
    private static Location cuurentLoc;
    private LineChart mChart;
    private LineChart iriChart;
    private LineChart fuelChart;
    private static TextView  obd2speed;
    private static ProgressBar speedProgressBar;
    private Handler handler;
    private Runnable handlerTask;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        obd2speed = (TextView) view.findViewById(R.id.obd2speed);

//        startBtn = (ImageButton) view.findViewById(R.id.startBtn);
//        startBtn.setOnClickListener(new ImageButton.OnClickListener(){
//            @Override
//            public void onClick(View view) {
////                spinnerReori.setVisibility(View.VISIBLE);
//                if(!GraphFragment.isStarted()){
//                    // check whether the permission granted to retrieve IMEI number
//                    TelephonyManager telephonyManager = (TelephonyManager) mainActivity.getSystemService(Context.TELEPHONY_SERVICE);
//                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                        showAlertPhPermission();
//                        return;
//                    }
//                    String deviceId = telephonyManager.getDeviceId();
//                    SensorData.setDeviceId(deviceId);
//                    Log.d(TAG,"--------------- DeviceId --------- /// "+ deviceId);
//                    askJourneyName();
//                }else{
//                    // change the btn icon back to idle state
//                    startBtn.setImageResource(R.drawable.ic_play_blue_outline);
//                    GraphFragment.setStarted(false);
//                    Toast.makeText( getContext(),"Journey Stopped", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

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

        XAxis xAxis  = mChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        LineData data = new LineData();
//        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setEnabled(false);
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTextColor(Color.LTGRAY);

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

        XAxis xAxisIRI  = iriChart.getXAxis();
        xAxisIRI.setEnabled(true);
        xAxisIRI.setDrawLabels(false);
        xAxisIRI.setDrawAxisLine(false);
        xAxisIRI.setDrawGridLines(false);

        LineData dataIRI = new LineData();
//        data.setValueTextColor(Color.WHITE);
        iriChart.setData(dataIRI);

        Legend lIRI = iriChart.getLegend();
        lIRI.setEnabled(false);

        GraphController.setIRIChart(iriChart);

        fuelChart = (LineChart) view.findViewById(R.id.chartFuel);
        fuelChart.getDescription().setEnabled(false);

        fuelChart.setTouchEnabled(true);
        fuelChart.setDragEnabled(true);
        fuelChart.setScaleEnabled(false);
        fuelChart.setDrawGridBackground(false);
        fuelChart.setPinchZoom(false);
        fuelChart.setBackgroundColor(Color.TRANSPARENT);

        YAxis lAxisFuel = fuelChart.getAxisLeft();

        YAxis rAxisFuel = fuelChart.getAxisRight();
        rAxisFuel.setEnabled(false);

        XAxis xAxisFuel  = fuelChart.getXAxis();
        xAxisFuel.setEnabled(true);
        xAxisFuel.setDrawLabels(false);
        xAxisFuel.setDrawAxisLine(false);
        xAxisFuel.setDrawGridLines(false);

        LineData dataFuel = new LineData();
//        data.setValueTextColor(Color.WHITE);
        fuelChart.setData(dataFuel);

        Legend lFuel = fuelChart.getLegend();
        lFuel.setEnabled(false);
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTextColor(Color.LTGRAY);

        GraphController.setFuelChart(fuelChart);

        speedProgressBar = (ProgressBar) view.findViewById(R.id.speed_progress_bar);
//        rpmProgressBar = (ProgressBar) view.findViewById(R.id.rpm_progress_bar);


//        startFakeProgress();
        startTimer();
        return view;

    }

    public static void setMainActivity(MainActivity activity){
        mainActivity=activity;
    }

    public static  void updateLocation(Location loc){
        cuurentLoc = loc;
        Log.d(TAG,"--------------- Location changed --------- /// "+ loc.getLatitude()+ " "+loc.getLongitude());
        if(!obdDataAvailable){
            Double speed = SpeedCalculator.getSpeed(loc.getLatitude(), loc.getLongitude());
            MobileSensors.setGpsSpeed(speed);// updates vehicle speed using GPS
            updateSpeed(speed.intValue());
        }
        SensorData.setMlat(Double.toString(loc.getLatitude()));
        SensorData.setMlon(Double.toString(loc.getLongitude()));
    }

    public static void updateSpeed(int speed){
        obd2speed.setText(speed+" km/h");
//        obd2rpm.setText("RPM: "+ rpm);
        ObjectAnimator animSpeed = ObjectAnimator.ofInt(speedProgressBar, "progress", speedProgressBar.getProgress(), speed*10000);
        animSpeed.setDuration(900);
        animSpeed.setInterpolator(new DecelerateInterpolator());
        animSpeed.start();
    }

    public void startTimer(){
        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                int min = 20;
                int max = 120;
                Random r1 = new Random();
                int i1 = r1.nextInt(max - min + 1) + min;
                Random r2 = new Random();
                int i2 = r2.nextInt(max - min + 1) + min;
//                updateOBD2Data(i1,i2);
                handler.postDelayed(handlerTask, 1000);
            }
        };
        handlerTask.run();
    }

    public static  void updateOBD2Data(int speed,int rpm){
        obdDataAvailable = true;
        updateSpeed(speed);
//        ObjectAnimator animRpm = ObjectAnimator.ofInt(rpmProgressBar, "progress", rpmProgressBar.getProgress(), rpm*10000);
//        animRpm.setDuration(900);
//        animRpm.setInterpolator(new DecelerateInterpolator());
//        animRpm.start();
    }






}
