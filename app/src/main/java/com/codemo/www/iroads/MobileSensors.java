package com.codemo.www.iroads;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.codemo.www.iroads.Database.SensorData;


/**
 * A simple {@link Fragment} subclass.
 */
public class MobileSensors implements SensorEventListener {

    private static final String TAG = "MobileSensors";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private static float currentMagneticX;
    private static float currentMagneticY;
    private static float currentMagneticZ;

    public static float getCurrentMagneticX() {
        return currentMagneticX;
    }
    public static float getCurrentMagneticY() {
        return currentMagneticY;
    }
    public static float getCurrentMagneticZ() {
        return currentMagneticZ;
    }

    private void setCurrentMagneticValues(float currentMagneticX,float currentMagneticY,float currentMagneticZ) {
        this.currentMagneticX = currentMagneticX;
        this.currentMagneticY = currentMagneticY;
        this.currentMagneticZ = currentMagneticZ;
    }


    private static float currentAccelerationX;
    private static float currentAccelerationY;
    private static float currentAccelerationZ;

    public static float getCurrentAccelerationX() {
        return currentAccelerationX;
    }

    public static float getCurrentAccelerationY() {
        return currentAccelerationY;
    }

    public static float getCurrentAccelerationZ() {
        return currentAccelerationZ;
    }

    private void setCurrentAccelerationValues(float currentAccelerationX,float currentAccelerationY,float currentAccelerationZ) {
        this.currentAccelerationX = currentAccelerationX;
        this.currentAccelerationY = currentAccelerationY;
        this.currentAccelerationZ = currentAccelerationZ;
    }

    private static double lon; // keeps longitude of the vehicle
    private static double lat; // keeps latitude of the vehicle
    private static double alt; // keeps altitude of the vehicle
    private static double bearing; // keeps bearing of the vehicle
    private static Location previousLocation; // Previous location of the vehicle

    public static double getLon() {
        return lon;
    }

    public static double getLat() {
        return lat;
    }

    public static double getAlt() {
        return alt;
    }

    public static double getBearing() {
        return bearing;
    }

   public static void updateLocation(Location location){
        if(previousLocation == null){
            previousLocation = location;
        }
        lon = location.getLongitude();
        lat = location.getLatitude();
        alt = location.getAltitude();
        bearing = previousLocation.bearingTo(location);
        if(previousLocation.getLongitude() != lon || previousLocation.getLatitude() != lat ||
                previousLocation.getAltitude() != alt){
            previousLocation = location;
        }
//        Log.d("UpdateLoc",lon+","+lat+","+NumberFormat.getInstance().format(lon)+","+NumberFormat.getInstance().format(lat));
//        SensorData.setMlon(NumberFormat.getInstance().format(lon));
//        SensorData.setMlat(NumberFormat.getInstance().format(lat));
   }

    public static double gpsSpeed; // keeps GPS speed of the vehicle

    public static void setGpsSpeed(double gpsSpeed) {
        MobileSensors.gpsSpeed = gpsSpeed;
    }

    public static double getGpsSpeed() {
        return gpsSpeed;
    }

    public MobileSensors(MainActivity mainActivity) {
        // Required empty public constructor
        sensorManager = (SensorManager)  mainActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer != null){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        }else{
            Log.d(TAG, "Accelerometer not available");
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magnetometer != null){
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        }else{
            Log.d(TAG, "Magnetometer not available");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        int sensorType = sensor.getType();

        if(sensorType == Sensor.TYPE_ACCELEROMETER){

            GraphController.drawGraph(sensorEvent);
//            Log.d("DATA=======",SensorData.getMacceX());
            SensorData.setMacceX(Float.toString(sensorEvent.values[0]));
            SensorData.setMacceY(Float.toString(sensorEvent.values[1]));
            SensorData.setMacceZ(Float.toString(sensorEvent.values[2]));

            setCurrentAccelerationValues(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);


        }else if(sensorType == Sensor.TYPE_MAGNETIC_FIELD){
            setCurrentMagneticValues(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2]);
        }


        /**
         *  all the data processing on sensor values are done in here
         */
        SensorDataProcessor.updateSensorDataProcessingValues();

        /**
         * drawing
         */
        GraphController.drawGraph(sensorEvent);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
