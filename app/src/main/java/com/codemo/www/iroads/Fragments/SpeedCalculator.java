package com.codemo.www.iroads.Fragments;

import android.util.Log;

/**
 * Created by Pivithuru on 4/2/2018.
 */

public class SpeedCalculator {

    private static final String TAG = "SpeedCalculator";

    private static boolean start; // to identify new journeys
    private static double lon1;
    private static double lat1;
    private static double time1;


    public static double getRadiant(double degree){ // calculates radiant values.
        double radiant = degree*(2*Math.PI/360);
        return radiant;
    }

    public void setStart(boolean starts) {
        this.start = starts;
    }

    public static double getDistance(double lon1, double lat1, double lon2, double lat2){
        double r = 6371000; // radius of the earth.
        double phi1 = getRadiant(lat1);
        double phi2 = getRadiant(lat2);
        double deltaPhi = getRadiant(lat2-lat1);
        double deltaLamda = getRadiant(lon2 - lon1);

        double a = Math.sin(deltaPhi/2.0)*Math.sin(deltaPhi/2.0) +
                Math.cos(phi1)*Math.cos(phi2)*Math.sin(deltaLamda/2.0)*Math.sin(deltaLamda/2.0);
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double meters = r * c;
        double km = meters/1000.0;
        return km;
    }

    public static double getSpeed (double lon, double lat){
        double lon2 = lon;
        double lat2 = lat;
        double time2 = System.currentTimeMillis();
        double speed;
        if(!start){ //set initial values.
            lon1 = lon;
            lat1 = lat;
            time1 = time2;
            speed = 0;
            start = true;
        } else {
            double distance = getDistance(lon1, lat1, lon2, lat2);
            double timeDiff = (time2 - time1)/3600000; // converts time in to hours
            speed = distance/timeDiff;
            lon1 = lon2;
            lat1 = lat2;
            time1 = time2;
        }
        Log.d(TAG,"--------------- speed ---------"+speed);
        return speed;
    }
}
