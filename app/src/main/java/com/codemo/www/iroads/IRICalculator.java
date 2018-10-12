package com.codemo.www.iroads;

import java.util.ArrayList;

/**
 * Created by aminda on 3/31/2018.
 */

public class IRICalculator {

    private static final String TAG = "IRICalculator";

    private static int pulseCountUsingSlope = 0;
    private static double slopeMethodSensivity = -0.2;
    private static double upperBoundary = 11;
    private static double lowerBoundary = 9.8;
    private static double candidateValue = 0;
    private static boolean candidate = false;
    private static boolean notSelected = true;

    private static ArrayList<Double> dataQueue = new ArrayList<Double>();
    private static int pulseCountUsing_aWindow = 0;
    private static double windowMethodSensivity = -0.01;


    public static double mean(ArrayList<Double> data) {
        double temp = 0.0;
        for (int i = 0; i < data.size(); i++) {
            temp += data.get(i);
        }
        double mean = (double) temp / (double) data.size();
        return mean;
    }


    public static double processIRI_using_aWindow(double z) {
        if (dataQueue.size() < 3) {
            if (dataQueue.isEmpty()) {
                dataQueue.add(z);
            } else {
                if (dataQueue.get(dataQueue.size() - 1) != z) {
                    dataQueue.add(z);
                }
            }

        }

        if (dataQueue.size() == 3) {
            if (dataQueue.get(0) < dataQueue.get(1) + windowMethodSensivity && dataQueue.get(1) +
                    windowMethodSensivity > dataQueue.get(2)) {
                pulseCountUsing_aWindow++;
                dataQueue.clear();
            } else {
                dataQueue.remove(0);
            }
        }
        return pulseCountUsing_aWindow;
    }

    public static double processIRI_usingSlope(double z) {

        if (lowerBoundary < z && z < upperBoundary) {
            if (notSelected) {
                if (!candidate) {
                    candidate = true;
                    candidateValue = z;
                } else {
                    if (candidateValue - z < slopeMethodSensivity) {// finds a peak
                        pulseCountUsingSlope++;
                        notSelected = false;
                    }
                    candidate = false;
                }

            } else {
                if (candidateValue > z) { // looks for the next peak
                    notSelected = true;
                    candidateValue = 0;
                }
            }
        }

        return pulseCountUsingSlope;
    }
}
