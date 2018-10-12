package com.codemo.www.iroads.Reorientation;

import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.util.Log;

import com.codemo.www.iroads.Entity.Vector3D;
import com.codemo.www.iroads.MobileSensors;

/**
 * Created by dushan on 3/18/18.
 */

public class WolverineMechanism implements Reorientation {
    private static final String TAG = "Wolverine";

    @Override
    public Vector3D reorient(double xValueA, double yValueA, double zValueA, float xValueM, float yValueM, float zValueM) {
        Log.d(TAG, "********************** Wolverine run by pivi *************");
        float[] rotation = new float[9];
        float[] inclination = new float[9];
        float[] gravity = {(float) xValueA, (float) yValueA, (float) zValueA};
        float[] geomagnetic = {xValueM, yValueM, zValueM};
        boolean rotationMatrixOk = SensorManager.getRotationMatrix(rotation, inclination, gravity,
                geomagnetic);
        Log.d(TAG, "********************** rotation matrix sucessful:" + rotationMatrixOk);

        float geometryAx = rotation[0] * gravity[0] + rotation[1] * gravity[1] + rotation[2] * gravity[2];
        float geometryAy = rotation[3] * gravity[0] + rotation[4] * gravity[1] + rotation[5] * gravity[2];
        float geometryAz = rotation[6] * gravity[0] + rotation[7] * gravity[1] + rotation[8] * gravity[2];

        float latitude = (float) MobileSensors.getLat();
        float longitude = (float) MobileSensors.getLon();
        float altitude = (float) MobileSensors.getAlt();
        long timeMilis = System.currentTimeMillis();

        GeomagneticField geomagneticField = new GeomagneticField(latitude, longitude, altitude, timeMilis);
        float magneticDeclination = geomagneticField.getDeclination();
        float bearing = (float) MobileSensors.getBearing();

        float teta = bearing - magneticDeclination;
        double ay = geometryAy * Math.cos(teta) - geometryAx * Math.sin(teta);
        double ax = geometryAy * Math.sin(teta) + geometryAx * Math.cos(teta);
        double az = geometryAz;

        Vector3D accelerationVector = new Vector3D();
        accelerationVector.setX(ax);// doing proper assignment of values to axises
        accelerationVector.setY(az);
        accelerationVector.setZ(-ay);

        return accelerationVector;

    }

}
